package compressao;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

public class Huffman {
    public static final String DATA_PATH = "TP3/data/data.db";

    public static void compactacao(String compactacaoPath, int num) {
        long inicio = System.currentTimeMillis();

        RandomAccessFile arq;
        ByteArrayOutputStream baos;
        DataOutputStream dos;
        byte[] txt = null;
        
        try {
            arq = new RandomAccessFile(DATA_PATH, "rw");
            baos = new ByteArrayOutputStream();
            dos = new DataOutputStream(baos);
            long tam = arq.length();
            for (int i = 0; i < tam; i++) {
                dos.writeByte(arq.readByte());
            }
            txt = baos.toByteArray();

            arq.close();
            dos.close();
            baos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        HashMap<Byte, Integer> freq = new HashMap<>();
        for (byte b : txt) {
            if (freq.containsKey(b)) {
                freq.put(b, freq.get(b) + 1);
            } else {
                freq.put(b, 1);
            }
        }

        ArrayList<No> listNos = new ArrayList<>();
        for (Byte b : freq.keySet()) {
            listNos.add(new No(b, freq.get(b)));
        }

        listNos.sort((a, b) -> Integer.compare(a.frequencia, b.frequencia));
        
        while(listNos.size() > 1) {
            listNos.add(new No(listNos.get(0), listNos.get(1)));
            listNos.remove(0);
            listNos.remove(0);
            listNos.sort((a, b) -> Integer.compare(a.frequencia, b.frequencia));
        }

        HashMap<Byte, String> codigos = new HashMap<>();
        gerarCodigos(listNos.get(0), "", codigos);

        RandomAccessFile arqCompress;
        try {
            arqCompress = new RandomAccessFile(compactacaoPath, "rw");
            BitSet bs = new BitSet();
            int count = 0;
            for (byte b : txt) {
                String codigo = codigos.get(b);
                for (int i = 0; i < codigo.length(); i++) {
                    if (codigo.charAt(i) == '0') {
                        bs.clear(count++);
                    } else {
                        bs.set(count++);
                    }
                }
            }
            byte[] txtCodificado = bs.toByteArray();
            arqCompress.write(txtCodificado);
            arqCompress.close();

            String arvorePath = "TP3/data/dataHuffmanArvore" + num + ".db";
            armazenaArvore(listNos.get(0), arvorePath);

            System.out.println("\nArquivo codificado Huffman tem " + txtCodificado.length + " bytes");
            float taxaCompressao = calculaTaxa(txt.length, txtCodificado.length);
            System.out.printf("Taxa de compressão Huffman: %.2f%n", taxaCompressao);
            long fim = System.currentTimeMillis();
            System.out.println("Compactação Huffman levou " + (fim-inicio) + " milisegundos");

            System.out.println("\nBase de dados compactada com sucesso.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void descompactacao(String compressedPath, int num) {
        long inicio = System.currentTimeMillis();
        RandomAccessFile arqCompress;
        RandomAccessFile arqDescompress;
        try {
            String arvorePath = "TP3/data/dataHuffmanArvore" + num + ".db";
            No raiz = null;
            raiz = recuperarArvore(raiz, 0, arvorePath);

            arqCompress = new RandomAccessFile(compressedPath, "rw");
            String descompressedPath = "TP3/data/data.db";
            excluiArquivos(descompressedPath);
            arqDescompress = new RandomAccessFile(descompressedPath, "rw");

            byte[] txtCodificado = new byte[(int) arqCompress.length()];
            arqCompress.read(txtCodificado);
            arqCompress.close();
            BitSet bs = BitSet.valueOf(txtCodificado);

            No no = raiz;
            for (int i = 0; i < bs.length(); i++) {
                if (!bs.get(i)) {
                    no = no.esq;
                } else {
                    no = no.dir;
                }

                if (no.esq == null && no.dir == null) {
                    arqDescompress.writeByte(no.simbolo);
                    no = raiz;
                }
            }

            arqDescompress.close();

            long fim = System.currentTimeMillis();
            System.out.println("Descompactação Huffman levou " + (fim-inicio) + " milisegundos");

            System.out.println("\nBase de dados descompactada com sucesso.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static No recuperarArvore(No no, long pos, String arvorePath) {
        long posEsq = 0;
        long posDir = 0;
        byte simbolo = 0;

        try {
            RandomAccessFile arqArvore = new RandomAccessFile(arvorePath, "rw");
            arqArvore.seek(pos);
            posEsq = arqArvore.readLong();
            posDir = arqArvore.readLong();
            simbolo = arqArvore.readByte();
            arqArvore.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        no = new No(simbolo);
        if(posEsq != -1 && posDir != -1) {
            no.esq = recuperarArvore(no.esq, posEsq, arvorePath);
            no.dir = recuperarArvore(no.dir, posDir, arvorePath);
        } else if(posEsq != -1 && posDir == -1) {
            no.esq = recuperarArvore(no.esq, posEsq, arvorePath);
        } else if(posEsq == -1 && posDir != -1) {
            no.dir = recuperarArvore(no.dir, posDir, arvorePath);
        }
        return no;
    }

    public static long armazenaArvore(No no, String arvorePath){
        if(no == null){
            return -1;
        } else {
            long tam = 0;
            try {
            RandomAccessFile arqArvore = new RandomAccessFile(arvorePath, "rw");
            tam = arqArvore.length();
            arqArvore.seek(tam);
            arqArvore.writeLong(-1);
            arqArvore.writeLong(-1);
            arqArvore.writeByte(no.simbolo);

            arqArvore.seek(tam);
            arqArvore.writeLong(armazenaArvore(no.esq, arvorePath));
            arqArvore.writeLong(armazenaArvore(no.dir, arvorePath));

            arqArvore.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return tam;
        }
        
    }

    public static void gerarCodigos(No no, String codigo, HashMap<Byte, String> codigos) {
        if (no.esq == null && no.dir == null) {
            codigos.put(no.simbolo, codigo);
            return;
        }

        gerarCodigos(no.esq, codigo + "0", codigos);
        gerarCodigos(no.dir, codigo + "1", codigos);
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

    public static float calculaTaxa(int tamOriginal, int tamComprimido) {
        float tamOriginalFloat = tamOriginal;
        float tamComprimidoFloat = tamComprimido;

        return tamComprimidoFloat / tamOriginalFloat * 100;

    }
    
}

class No {
    byte simbolo;
    int frequencia;
    No esq;
    No dir;

    public No(byte simbolo) {
        this.simbolo = simbolo;
        this.frequencia = -1;
        this.esq = null;
        this.dir = null;
    }

    public No(byte simbolo, int frequencia) {
        this.simbolo = simbolo;
        this.frequencia = frequencia;
        this.esq = null;
        this.dir = null;
    }

    public No(No esq, No dir) {
        this.simbolo = -1;
        this.esq = esq;
        this.dir = dir;
        this.frequencia = esq.frequencia + dir.frequencia;
    }

    public No(byte simbolo, int frequencia, No esq, No dir) {
        this.simbolo = simbolo;
        this.frequencia = frequencia;
        this.esq = esq;
        this.dir = dir;
    }
}

