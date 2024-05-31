package compressao;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
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
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        RandomAccessFile arq;
        ByteArrayOutputStream baos;
        DataOutputStream dos;
        try {
            arq = new RandomAccessFile("TP3/data/data.db", "r");
            baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024]; // Um buffer de 1024 bytes
            int bytesRead;
            while ((bytesRead = arq.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            int choice = 0;
            do {
                System.out.println("Menu:");
                System.out.println("1. Compactar arquivo");
                System.out.println("2. Descompactar arquivo");
                System.out.println("3. Sair");
                System.out.print("Digite sua opção: ");
                String input = reader.readLine();
                choice = Integer.parseInt(input.trim());
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
                            countArqCompac++;
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 2:
                        descompactarArquivo();
                        sc.nextLine();

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
            System.out.println();
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

    public static byte[] descompactacao(byte[] txt) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(txt);
        DataInputStream dis = new DataInputStream(bais);
        int n = dis.readInt(); // Número de entradas no bit sequence
        byte[] bytes = new byte[txt.length - 4];
        dis.read(bytes);
        BitSequence bs = new BitSequence(BITS_POR_INDICE);
        bs.setBytes(n, bytes);

        ArrayList<Integer> entrada = new ArrayList<>();
        for (int i = 0; i < bs.size(); i++) {
            entrada.add(bs.get(i));
        }

        ArrayList<ArrayList<Byte>> dicionario = new ArrayList<>();
        for (int i = -128; i < 128; i++) {
            ArrayList<Byte> auxDic = new ArrayList<>();
            auxDic.add((byte) i);
            dicionario.add(auxDic);
        }

        ArrayList<Byte> msgDecodificada = new ArrayList<>();
        int i = 0;
        while (i < entrada.size()) {
            ArrayList<Byte> auxDic = new ArrayList<>(dicionario.get(entrada.get(i)));
            msgDecodificada.addAll(auxDic);

            // Prepara a próxima entrada para adicionar ao dicionário se possível
            if (i + 1 < entrada.size()) {
                ArrayList<Byte> proxAuxDic = new ArrayList<>(dicionario.get(entrada.get(i + 1)));
                auxDic.add(proxAuxDic.get(0));
                if (dicionario.size() < Math.pow(2, BITS_POR_INDICE)) {// verifica se cabe no dicionário
                    dicionario.add(new ArrayList<>(auxDic)); // Adiciona uma nova entrada ao dicionário
                }
            }
            i++;
        }

        byte[] msgDecodificadaBytes = new byte[msgDecodificada.size()];
        for (i = 0; i < msgDecodificada.size(); i++) {
            msgDecodificadaBytes[i] = msgDecodificada.get(i);
        }
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
        try (RandomAccessFile file = new RandomAccessFile(caminhoArqCompCerto, "r")) {
            byte[] bytes = new byte[(int) file.length()];
            file.readFully(bytes);
            return bytes;
        }
    }

}