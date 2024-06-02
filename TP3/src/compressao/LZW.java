package compressao;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class LZW {

    public static final int BITS_POR_INDICE = 12; // Define o número de bits por índice no LZW

    public static void compactacao(String camArqSaida) throws Exception {
        long inicio = System.currentTimeMillis(); // Marca o início da compressão

        RandomAccessFile arq;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos;
        try {
            arq = new RandomAccessFile("TP3/data/data.db", "rw"); // Abre o arquivo original para leitura
            dos = new DataOutputStream(baos);
            long tam = arq.length();
            for (int i = 0; i < tam; i++) {
                dos.writeByte(arq.readByte()); // Lê o arquivo byte a byte e escreve no ByteArrayOutputStream
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] txt = baos.toByteArray(); // Converte o conteúdo lido para um array de bytes

        ArrayList<ArrayList<Byte>> dicionario = new ArrayList<>();
        ArrayList<Byte> auxDic;
        ArrayList<Integer> saida = new ArrayList<>();

        // Inicializando o dicionário com todos os possíveis valores de byte
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

            saida.add(ultimoIdx); // Adiciona o índice do último match encontrado à saída

            if (dicionario.size() < (Math.pow(2, BITS_POR_INDICE))) {
                dicionario.add(auxDic); // Adiciona a nova sequência ao dicionário
            }
        }

        BitSequence bs = new BitSequence(BITS_POR_INDICE);
        for (i = 0; i < saida.size(); i++) {
            bs.add(saida.get(i)); // Adiciona os índices à sequência de bits
        }

        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        DataOutputStream dos2 = new DataOutputStream(baos2);
        dos2.writeInt(bs.size()); // Escreve o tamanho da sequência de bits
        dos2.write(bs.getBytes()); // Escreve a sequência de bits

        RandomAccessFile arqComp;
        try {
            arqComp = new RandomAccessFile(camArqSaida, "rw"); // Abre o arquivo de saída para escrita
            arqComp.write(baos2.toByteArray()); // Escreve o conteúdo comprimido no arquivo
            arqComp.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] compactado = baos2.toByteArray(); // Array de bytes do arquivo comprimido

        System.out.println("\nArquivo original data.db tem " + txt.length + " bytes\n");
        System.out.println("Arquivo codificado LZW tem " + compactado.length + " bytes");
        float taxaCompressao = calculaTaxa(txt.length, compactado.length); // Calcula a taxa de compressão
        System.out.printf("Taxa de compressão LZW: %.2f%n", taxaCompressao);

        long fim = System.currentTimeMillis();
        System.out.println("Compactação LZW levou " + (fim-inicio) + " milisegundos");
    }

    public static void descompactacao(String camCompactado) throws Exception {
        long inicio = System.currentTimeMillis(); // Marca o início da descompressão

        RandomAccessFile arq;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos;
        try {
            arq = new RandomAccessFile(camCompactado, "rw"); // Abre o arquivo comprimido para leitura
            dos = new DataOutputStream(baos);
            long tam = arq.length();
            for (int i = 0; i < tam; i++) {
                dos.writeByte(arq.readByte()); // Lê o arquivo byte a byte e escreve no ByteArrayOutputStream
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] txt = baos.toByteArray(); // Converte o conteúdo lido para um array de bytes

        ByteArrayInputStream bais = new ByteArrayInputStream(txt);
        DataInputStream dis = new DataInputStream(bais);
        int n = dis.readInt(); // Lê o tamanho da sequência de bits
        byte[] bytes = new byte[txt.length - 4];
        dis.read(bytes);
        BitSequence bs = new BitSequence(BITS_POR_INDICE);
        bs.setBytes(n, bytes); // Define os bytes na sequência de bits

        // Recupera os números do bitset
        ArrayList<Integer> entrada = new ArrayList<>();
        int i, j;
        for (i = 0; i < bs.size(); i++) {
            j = bs.get(i);
            entrada.add(j); // Adiciona cada índice decodificado à lista de entrada
        }

        // Inicializa o dicionário com todos os possíveis valores de byte
        ArrayList<ArrayList<Byte>> dicionario = new ArrayList<>(); // Dicionário
        ArrayList<Byte> auxDic; // Auxiliar para cada elemento do dicionário
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

            // Decodifica o número
            auxDic = (ArrayList<Byte>) (dicionario.get(entrada.get(i)).clone());
            msgDecodificada.addAll(auxDic); // Adiciona a sequência decodificada à mensagem

            // Decodifica o próximo número
            i++;
            if (i < entrada.size()) {
                proxAuxDic = dicionario.get(entrada.get(i));
                auxDic.add(proxAuxDic.get(0)); // Adiciona o primeiro byte da próxima sequência à atual

                // Adiciona o vetor de bytes (+1 byte do próximo vetor) ao fim do dicionário
                if (dicionario.size() < Math.pow(2, BITS_POR_INDICE))
                    dicionario.add(auxDic);
            }
        }

        byte[] msgDecodificadaBytes = new byte[msgDecodificada.size()];
        for (i = 0; i < msgDecodificada.size(); i++)
            msgDecodificadaBytes[i] = msgDecodificada.get(i); // Converte a mensagem decodificada para array de bytes

        long fim = System.currentTimeMillis();
        System.out.println("\nDescompactação LZW levou " + (fim-inicio) + " milisegundos");
    }

    public static float calculaTaxa(int tamOriginal, int tamComprimido) {
        float tamOriginalFloat = tamOriginal;
        float tamComprimidoFloat = tamComprimido;

        return tamComprimidoFloat / tamOriginalFloat * 100; // Calcula a taxa de compressão em percentual
    }
}
