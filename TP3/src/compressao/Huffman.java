package compressao;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

public class Huffman {
    public static void main(String[] args) {
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

        RandomAccessFile arq;
        try {
            arq = new RandomAccessFile("TP3/data/dataHuffmanCompressaoX.db", "rw");
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
            arq.write(txtCodificado);
            arq.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void descompactacao(byte[] txt) {
        
    }

    public static void gerarCodigos(No no, String codigo, HashMap<Byte, String> codigos) {
        if (no.esquerda == null && no.direita == null) {
            codigos.put(no.simbolo, codigo);
            return;
        }

        gerarCodigos(no.esquerda, codigo + "0", codigos);
        gerarCodigos(no.direita, codigo + "1", codigos);
    }
}

class No {
    byte simbolo;
    int frequencia;
    No esquerda;
    No direita;

    public No(byte simbolo, int frequencia) {
        this.simbolo = simbolo;
        this.frequencia = frequencia;
        this.esquerda = null;
        this.direita = null;
    }

    public No(No esquerda, No direita) {
        this.esquerda = esquerda;
        this.direita = direita;
        this.frequencia = esquerda.frequencia + direita.frequencia;
    }
}

