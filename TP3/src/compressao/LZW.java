package compressao;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Scanner;

public class LZW {

    public static final int BITS_POR_INDICE = 12;
    public static final String LZW_COMPRESSION_PATH = "TP3/data/";
    public static final String LZW_DECOMPRESSION_PATH = "TP3/data/";
    public static final String BASE_COMPRESSION_PATH = "TP3/data/dataLZWCompressao";
    public static int countArqCompac = 0;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        RandomAccessFile arq;
        ByteArrayOutputStream baos;
        DataOutputStream dos;
        try {
            arq = new RandomAccessFile("TP3/data/data.db", "rw");
            baos = new ByteArrayOutputStream();
            dos = new DataOutputStream(baos);
            long tam = arq.length();
            for (int i = 0; i < tam; i++) {
                dos.writeByte(arq.readByte());
            }

            System.out.println("Menu:");
            System.out.println("1. Compactar arquivo");
            System.out.println("2. Descompactar arquivo");
            System.out.println("3. Sair");
            int choice;

            do {
                System.out.print("Digite sua opção: ");
                choice = sc.nextInt();
                switch (choice) {
                    case 1:
                        byte[] txt = baos.toByteArray();
                        byte[] compactado;
                        try {
                            compactado = compactacao(txt);
                            System.out.println("mensagem original tem " + txt.length + " bytes");
                            System.out.println("codificado em " + compactado.length + " bytes");
                            float taxaCompressao = calculaTaxa(txt.length, compactado.length);
                            System.out.printf("Taxa de compressão: %.2f%n", taxaCompressao);
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 2:
                        descompactarArquivo();
                        break;
                    case 3:
                        System.out.println("Saindo...");
                        break;
                    default:
                        System.out.println("Opção inválida, tente novamente.");
                }
            } while (choice != 3);
        } catch (Exception e) {
            e.printStackTrace();
        }

        sc.close();
    }

    private static void descompactarArquivo() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Digite o número da compactação que deseja descompactar:");
        int numComp = sc.nextInt();

        String caminhoArqCompCerto = BASE_COMPRESSION_PATH + numComp + ".db";
        try {
            byte[] compactado = leArquivoComp(caminhoArqCompCerto);
            byte[] descompactado = descompactacao(compactado);
            System.out.println("Arquivo descompactado com sucesso. Conteúdo:\n" + new String(descompactado));
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo.");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Erro durante a descompactação.");
            e.printStackTrace();
        }
    }

    public static byte[] compactacao(byte[] txt) throws Exception {

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
        System.out.println("Número de elementos no dicionário: " + dicionario.size());

        BitSequence bs = new BitSequence(BITS_POR_INDICE);
        for (i = 0; i < saida.size(); i++) {
            bs.add(saida.get(i));
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(bs.size());
        dos.write(bs.getBytes());

        RandomAccessFile arqComp;
        try {
            arqComp = new RandomAccessFile(LZW_COMPRESSION_PATH + "dataLZWCompressao" + countArqCompac + ".db", "rw");
            arqComp.write(baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();

    }

    // @SuppressWarnings("unchecked")
    public static byte[] descompactacao(byte[] txt) throws Exception {

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
            if (i < entrada.size()) {
                proxAuxDic = dicionario.get(entrada.get(i));
                auxDic.add(proxAuxDic.get(0));

                // adiciona o vetor de bytes (+1 byte do próximo vetor) ao fim do dicionário
                if (dicionario.size() < Math.pow(2, BITS_POR_INDICE))
                    dicionario.add(auxDic);
            }

        }

        byte[] msgDecodificadaBytes = new byte[msgDecodificada.size()];
        for (i = 0; i < msgDecodificada.size(); i++)
            msgDecodificadaBytes[i] = msgDecodificada.get(i);
        return msgDecodificadaBytes;

    }

    public static float calculaTaxa(int tamOriginal, int tamComprimido) {
        float tamOriginalFloat = tamOriginal;
        float tamComprimidoFloat = tamComprimido;

        return tamComprimidoFloat / tamOriginalFloat * 100;

    }

    // le o arquivo compactado escolhido e o retorna como array de bytes para ser
    // descompactado
    private static byte[] leArquivoComp(String caminhoArqCompCerto) throws IOException {
        java.io.RandomAccessFile file = new java.io.RandomAccessFile(caminhoArqCompCerto, "r");
        byte[] bytes = new byte[(int) file.length()];
        file.readFully(bytes);
        file.close();
        return bytes;
    }
}
