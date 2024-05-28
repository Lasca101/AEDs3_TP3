package compressao;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;

public class LZW {

    public static final int BITS_POR_INDICE = 16;
    public static final String CSV_PATH = "TP3/src/resources/netflix.csv";
    public static final String LZW_COMPRESSION_PATH = "TP3/data/compressao/LZW";
    public static final String LZW_DECOMPRESSION_PATH = "TP3/data/descompressao/LZW";


    public static void main(String[] args) {

        //converte csv em string, para a compactação
        CsvReader reader = new CsvReader();  
        String csvContent = reader.readCsvToString(CSV_PATH);
        //System.out.println(csvContent);

        byte[] csv = csvContent.getBytes();
        try {
            byte[] csvCodificado = compactacao(csv);
            System.out.println("mensagem original tem "+csv.length+" bytes");
            System.out.println("codificado em "+csvCodificado.length+" bytes");

            byte[] csvDecodificado = descompactacao(csvCodificado);
            System.out.println(new String(csvDecodificado));
        } catch (Exception e) {
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
        System.out.println("Indices: ");
        System.out.println(saida);
        System.out.println("Número de elementos no dicionário: " + dicionario.size());

        BitSequence bs = new BitSequence(BITS_POR_INDICE);
        for (i = 0; i < saida.size(); i++) {
            bs.add(saida.get(i));
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(bs.size());
        dos.write(bs.getBytes());
        return baos.toByteArray();


    }

    //@SuppressWarnings("unchecked")
    public static byte[] descompactacao(byte[] txt) throws Exception {

        ByteArrayInputStream bais = new ByteArrayInputStream(txt);
        DataInputStream dis = new DataInputStream(bais);
        int n = dis.readInt();
        byte[] bytes = new byte[txt.length-4];
        dis.read(bytes);
        BitSequence bs = new BitSequence(BITS_POR_INDICE);
        bs.setBytes(n, bytes);

        // Recupera os números do bitset
        ArrayList<Integer> entrada = new ArrayList<>();
        int i, j;
        for(i=0; i<bs.size(); i++) {
            j = bs.get(i);
            entrada.add(j);
        }

        // inicializa o dicionário
        ArrayList<ArrayList<Byte>> dicionario = new ArrayList<>(); // dicionario
        ArrayList<Byte> auxDic;  // auxiliar para cada elemento do dicionario
        byte b;
        for(j=-128; j<128; j++) {
            b = (byte)j;
            auxDic = new ArrayList<>();
            auxDic.add(b);
            dicionario.add(auxDic);
        }

        // Decodifica os números
        ArrayList<Byte> proxAuxDic;
        ArrayList<Byte> msgDecodificada = new ArrayList<>();
        i = 0;
        while( i< entrada.size() ) {

            // decodifica o número
            auxDic = (ArrayList<Byte>)(dicionario.get(entrada.get(i)).clone());
            msgDecodificada.addAll(auxDic);

            // decodifica o próximo número
            i++;
            if(i<entrada.size()) {
                proxAuxDic = dicionario.get(entrada.get(i));
                auxDic.add(proxAuxDic.get(0));

                // adiciona o vetor de bytes (+1 byte do próximo vetor) ao fim do dicionário
                if(dicionario.size()<Math.pow(2,BITS_POR_INDICE))
                    dicionario.add(auxDic);
            }

        }

        byte[] msgDecodificadaBytes = new byte[msgDecodificada.size()];
        for(i=0; i<msgDecodificada.size(); i++)
            msgDecodificadaBytes[i] = msgDecodificada.get(i);
        return msgDecodificadaBytes;

    }
}


