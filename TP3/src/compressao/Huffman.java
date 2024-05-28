package compressao;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

public class Huffman {
    public static final String COMPRESSED_PATH = "TP3/data/dataHuffmanCompressaoX.db";
    public static final String DECOMPRESSED_PATH = "TP3/data/dataHuffmanDescompressaoX.db";
    public static final String ARVORE_PATH = "TP3/data/dataHuffmanArvoreX.db";
    public static final String DATA_PATH = "TP3/data/data.db";
    private static long ultimaPosicao = 8;
    public static void main(String[] args) {
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
        RandomAccessFile arqArvore;
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

            

            // arqArvore = new RandomAccessFile(ARVORE_PATH, "rw");
            // arqArvore.writeLong(0);
            // inicializaArvore(arqArvore, listNos.get(0), listNos.get(0));
            
            // arqArvore.writeLong(0);
            // for (Byte b : codigos.keySet()) {
            //     if(b == listNos.get(0).simbolo) {
            //         long raiz = arqArvore.getFilePointer();
            //         arqArvore.seek(0);
            //         arqArvore.writeLong(raiz);
            //         arqArvore.seek(raiz);
            //     }
            //     arqArvore.writeLong(-1);
            //     arqArvore.writeLong(-1);
            //     arqArvore.writeByte(b);
            //     arqArvore.writeUTF(codigos.get(b));
            // }

            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // public static long inicializaArvore(RandomAccessFile arqArvore, No no , No raiz){
    //     inicializaArvore(arqArvore, no.esq, raiz);
    //     inicializaArvore(arqArvore, no.dir, raiz);
    //     try {
    //          if(no.esq == null && no.dir == null){
    //             if (no.simbolo == raiz.simbolo) {
    //                 arqArvore.seek(0);
    //                 arqArvore.writeLong(ultimaPosicao);
    //                 arqArvore.seek(ultimaPosicao);
    //             }
    //             arqArvore.writeLong(-1);
    //             arqArvore.writeLong(-1);
    //             arqArvore.writeByte(no.simbolo);
    //             ultimaPosicao += 17;
    //         } else if(no.esq != null && no.dir == null){
    //             long pontRaiz = arqArvore.getFilePointer();
    //             arqArvore.seek(0);
    //             arqArvore.writeLong(pontRaiz);
    //             arqArvore.seek(pontRaiz);
    //         } else if(no.esq == null && no.dir != null){
    //             long pontRaiz = arqArvore.getFilePointer();
    //             arqArvore.seek(0);
    //             arqArvore.writeLong(pontRaiz);
    //             arqArvore.seek(pontRaiz);
    //         } 
    //         arqArvore.writeLong(-1);
    //         arqArvore.writeLong(-1);
    //         arqArvore.writeByte(no.simbolo);
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }

    public static void descompactacao(byte[] txt) {
        // HashMap<Byte, Integer> freq = new HashMap<>();
        // for (byte b : txt) {
        //     if (freq.containsKey(b)) {
        //         freq.put(b, freq.get(b) + 1);
        //     } else {
        //         freq.put(b, 1);
        //     }
        // }

        // ArrayList<No> listNos = new ArrayList<>();
        // for (Byte b : freq.keySet()) {
        //     listNos.add(new No(b, freq.get(b)));
        // }

        // listNos.sort((a, b) -> Integer.compare(a.frequencia, b.frequencia));
        
        // while(listNos.size() > 1) {
        //     listNos.add(new No(listNos.get(0), listNos.get(1)));
        //     listNos.remove(0);
        //     listNos.remove(0);
        //     listNos.sort((a, b) -> Integer.compare(a.frequencia, b.frequencia));
        // }

        // HashMap<Byte, String> codigos = new HashMap<>();
        // gerarCodigos(listNos.get(0), "", codigos);

        // RandomAccessFile arq;
        // try {
        //     arq = new RandomAccessFile(DECOMPRESSED_PATH, "rw");
        //     BitSet bs = BitSet.valueOf(txt);
        //     ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //     DataOutputStream dos = new DataOutputStream(baos);
        //     StringBuilder sb = new StringBuilder();
        //     for (int i = 0; i < bs.length(); i++) {
        //         if (bs.get(i)) {
        //             sb.append("1");
        //         } else {
        //             sb.append("0");
        //         }
        //         for (Byte b : codigos.keySet()) {
        //             if (codigos.get(b).equals(sb.toString())) {
        //                 dos.writeByte(b);
        //                 sb = new StringBuilder();
        //             }
        //         }
        //     }
        //     byte[] txtDecodificado = baos.toByteArray();
        //     arq.write(txtDecodificado);
        //     arq.close();
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
    }

    public static void gerarCodigos(No no, String codigo, HashMap<Byte, String> codigos) {
        if (no.esq == null && no.dir == null) {
            codigos.put(no.simbolo, codigo);
            return;
        }

        gerarCodigos(no.esq, codigo + "0", codigos);
        gerarCodigos(no.dir, codigo + "1", codigos);
    }
}

class No {
    byte simbolo;
    int frequencia;
    No esq;
    No dir;

    public No(byte simbolo, int frequencia) {
        this.simbolo = simbolo;
        this.frequencia = frequencia;
        this.esq = null;
        this.dir = null;
    }

    public No(No esq, No dir) {
        this.esq = esq;
        this.dir = dir;
        this.frequencia = esq.frequencia + dir.frequencia;
    }
}

