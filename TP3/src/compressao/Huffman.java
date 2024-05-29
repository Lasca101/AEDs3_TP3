package compressao;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

public class Huffman {
    public static final String COMPRESSED_PATH = "data/dataHuffmanCompressaoX.db";
    public static final String DECOMPRESSED_PATH = "data/dataHuffmanDescompressaoX.db";
    public static final String ARVORE_PATH = "data/dataHuffmanArvoreX.db";
    public static final String DATA_PATH = "data/data.db";
    public static void main(String[] args) {
        excluiArquivos();
        RandomAccessFile arq;
        ByteArrayOutputStream baos;
        DataOutputStream dos;
        
        try {
            arq = new RandomAccessFile(DATA_PATH, "rw");
            baos = new ByteArrayOutputStream();
            dos = new DataOutputStream(baos);
            long tam = arq.length();
            for (int i = 0; i < tam; i++) {
                dos.writeByte(arq.readByte());
            }
            byte[] txt = baos.toByteArray();
            compactacao(txt);
            descompactacao();

            arq.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void compactacao(byte[] txt) {
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
            arqCompress = new RandomAccessFile(COMPRESSED_PATH, "rw");
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

            armazenaArvore(listNos.get(0));

            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void descompactacao() {
        RandomAccessFile arqCompress;
        RandomAccessFile arqDescompress;
        try {
            No raiz = null;
            raiz = recuperarArvore(raiz, 0);

            arqCompress = new RandomAccessFile(COMPRESSED_PATH, "rw");
            arqDescompress = new RandomAccessFile(DECOMPRESSED_PATH, "rw");

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static No recuperarArvore(No no, long pos) {
        long posEsq = 0;
        long posDir = 0;
        byte simbolo = 0;

        try {
            RandomAccessFile arqArvore = new RandomAccessFile(ARVORE_PATH, "rw");
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
            no.esq = recuperarArvore(no.esq, posEsq);
            no.dir = recuperarArvore(no.dir, posDir);
        } else if(posEsq != -1 && posDir == -1) {
            no.esq = recuperarArvore(no.esq, posEsq);
        } else if(posEsq == -1 && posDir != -1) {
            no.dir = recuperarArvore(no.dir, posDir);
        }
        return no;
    }

    public static long armazenaArvore(No no){
        if(no == null){
            return -1;
        } else {
            long tam = 0;
            try {
            RandomAccessFile arqArvore = new RandomAccessFile(ARVORE_PATH, "rw");
            tam = arqArvore.length();
            arqArvore.seek(tam);
            arqArvore.writeLong(-1);
            arqArvore.writeLong(-1);
            arqArvore.writeByte(no.simbolo);

            arqArvore.seek(tam);
            arqArvore.writeLong(armazenaArvore(no.esq));
            arqArvore.writeLong(armazenaArvore(no.dir));

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

    public static void excluiArquivos() {
        try {
            File file = new File(COMPRESSED_PATH);
            if (file.exists()) {
                file.delete();
            }

            file = new File(DECOMPRESSED_PATH);
            if (file.exists()) {
                file.delete();
            }

            file = new File(ARVORE_PATH);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

