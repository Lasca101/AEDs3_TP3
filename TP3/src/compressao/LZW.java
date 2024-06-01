package compressao;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class LZW {

    public static final int BITS_POR_INDICE = 12;
    public static final String LZW_COMPRESSION_PATH = "TP3/data/";
    public static final String LZW_DECOMPRESSION_PATH = "TP3/data/";
    public static final String BASE_COMPRESSION_PATH = "TP3/data/dataLZWCompressao";
    public static int countArqCompac = 0;

    

    public static void compactacao(String camArqSaida) throws Exception {
        long inicio = System.currentTimeMillis();

        RandomAccessFile arq;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        DataOutputStream dos;
        try {
            arq = new RandomAccessFile("TP3/data/data.db", "rw");
            dos = new DataOutputStream(baos);
            long tam = arq.length();
            for (int i = 0; i < tam; i++) {
                dos.writeByte(arq.readByte());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] txt = baos.toByteArray();

        ArrayList<ArrayList<Byte>> dicionario = new ArrayList<>();
        ArrayList<Byte> auxDic;
        ArrayList<Integer> saida = new ArrayList<>();

        // inicialiando dicionário
        byte b;
        for (int i = -128; i < 128; i++) {
            b = (byte) i;
            auxDic = new ArrayList<>();
            auxDic.add(b);
            dicionario.add(auxDic);
        }

        int i = 0;
        int idx = -1;
        int ultimoIdx;
        while (idx == -1 && i < txt.length) {
            auxDic = new ArrayList<>();
            b = txt[i];
            auxDic.add(b);
            idx = dicionario.indexOf(auxDic);
            ultimoIdx = idx;

            while (idx != -1 && i < txt.length - 1) {
                i++;
                b = txt[i];
                auxDic.add(b);
                ultimoIdx = idx;
                idx = dicionario.indexOf(auxDic);
            }

            saida.add(ultimoIdx);

            if (dicionario.size() < (Math.pow(2, BITS_POR_INDICE))) {
                dicionario.add(auxDic);
            }

        }
        // System.out.println("Indices: ");
        // System.out.println(saida);
        //System.out.println("Número de elementos no dicionário: " + dicionario.size());

        BitSequence bs = new BitSequence(BITS_POR_INDICE);
        for (i = 0; i < saida.size(); i++) {
            bs.add(saida.get(i));
        }

        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        DataOutputStream dos2 = new DataOutputStream(baos2);
        dos2.writeInt(bs.size());
        dos2.write(bs.getBytes());

        RandomAccessFile arqComp;
        try {
            arqComp = new RandomAccessFile(camArqSaida, "rw");
            arqComp.write(baos2.toByteArray());

            arqComp.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] compactado = baos2.toByteArray();

        System.out.println("\nArquivo original data.db tem " + txt.length + " bytes\n");
        System.out.println("Arquivo codificado LZW tem " + compactado.length + " bytes");
        float taxaCompressao = calculaTaxa(txt.length, compactado.length);
        System.out.printf("Taxa de compressão LZW: %.2f%n", taxaCompressao);

        long fim = System.currentTimeMillis();
        System.out.println("Compactação LZW levou " + (fim-inicio) + " milisegundos");

    }

    // @SuppressWarnings("unchecked")
    public static void descompactacao(String camCompactado) throws Exception {
        long inicio = System.currentTimeMillis();

        RandomAccessFile arq;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        DataOutputStream dos;
        try {
            arq = new RandomAccessFile(camCompactado, "rw");
            dos = new DataOutputStream(baos);
            long tam = arq.length();
            for (int i = 0; i < tam; i++) {
                dos.writeByte(arq.readByte());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] txt = baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(txt);
        DataInputStream dis = new DataInputStream(bais);
        int n = dis.readInt();
        byte[] bytes = new byte[txt.length - 4];
        dis.read(bytes);
        BitSequence bs = new BitSequence(BITS_POR_INDICE);
        bs.setBytes(n, bytes);

        // Recupera os números do bitset
        ArrayList<Integer> entrada = new ArrayList<>();
        int i, j;
        for (i = 0; i < bs.size(); i++) {
            j = bs.get(i);
            entrada.add(j);
        }

        // inicializa o dicionário
        ArrayList<ArrayList<Byte>> dicionario = new ArrayList<>(); // dicionario
        ArrayList<Byte> auxDic; // auxiliar para cada elemento do dicionario
        byte b;
        for (j = -128; j < 128; j++) {
            b = (byte) j;
            auxDic = new ArrayList<>();
            auxDic.add(b);
            dicionario.add(auxDic);
        }

        // Decodifica os números
        ArrayList<Byte> proxAuxDic;
        ArrayList<Byte> msgDecodificada = new ArrayList<>();
        i = 0;
        while (i < entrada.size()) {

            // decodifica o número
            auxDic = (ArrayList<Byte>) (dicionario.get(entrada.get(i)).clone());
            msgDecodificada.addAll(auxDic);

            // decodifica o próximo número
            i++;
            if (i + 1< entrada.size()) {
                proxAuxDic = dicionario.get(entrada.get(i + 1));
                auxDic.add(proxAuxDic.get(0));

                // adiciona o vetor de bytes (+1 byte do próximo vetor) ao fim do dicionário
                if (dicionario.size() < Math.pow(2, BITS_POR_INDICE))
                    dicionario.add(auxDic);
            }

        }

        byte[] msgDecodificadaBytes = new byte[msgDecodificada.size()];
        for (i = 0; i < msgDecodificada.size(); i++)
            msgDecodificadaBytes[i] = msgDecodificada.get(i);

        long fim = System.currentTimeMillis();
        System.out.println("\nDescompactação LZW levou " + (fim-inicio) + " milisegundos");

    }

    public static float calculaTaxa(int tamOriginal, int tamComprimido) {
        float tamOriginalFloat = tamOriginal;
        float tamComprimidoFloat = tamComprimido;

        return tamComprimidoFloat / tamOriginalFloat * 100;

    }

    

    public static void excluiArquivos(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
