package compressao;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

public class Huffman {
    public static final String DATA_PATH = "TP3/data/data.db"; // Caminho do arquivo de dados original

    public static void compactacao(String compactacaoPath, int num) {
        long inicio = System.currentTimeMillis(); // Marca o início da compressão

        RandomAccessFile arq;
        ByteArrayOutputStream baos;
        DataOutputStream dos;
        byte[] txt = null;
        
        try {
            arq = new RandomAccessFile(DATA_PATH, "rw"); // Abre o arquivo de dados para leitura e escrita
            baos = new ByteArrayOutputStream();
            dos = new DataOutputStream(baos);
            long tam = arq.length();
            for (int i = 0; i < tam; i++) {
                dos.writeByte(arq.readByte()); // Lê o arquivo byte a byte e escreve no ByteArrayOutputStream
            }
            txt = baos.toByteArray(); // Converte o conteúdo lido para um array de bytes

            arq.close();
            dos.close();
            baos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        HashMap<Byte, Integer> freq = new HashMap<>();
        for (byte b : txt) {
            if (freq.containsKey(b)) {
                freq.put(b, freq.get(b) + 1); // Incrementa a frequência do byte se já existir no HashMap
            } else {
                freq.put(b, 1); // Adiciona o byte ao HashMap com frequência inicial de 1
            }
        }

        ArrayList<No> listNos = new ArrayList<>();
        for (Byte b : freq.keySet()) {
            listNos.add(new No(b, freq.get(b))); // Cria nós para cada byte com sua respectiva frequência
        }

        listNos.sort((a, b) -> Integer.compare(a.frequencia, b.frequencia)); // Ordena os nós pela frequência
        
        while(listNos.size() > 1) {
            listNos.add(new No(listNos.get(0), listNos.get(1))); // Combina dois nós de menor frequência
            listNos.remove(0); // Remove os nós combinados
            listNos.remove(0);
            listNos.sort((a, b) -> Integer.compare(a.frequencia, b.frequencia)); // Ordena novamente a lista
        }

        HashMap<Byte, String> codigos = new HashMap<>();
        gerarCodigos(listNos.get(0), "", codigos); // Gera os códigos de Huffman para cada byte

        RandomAccessFile arqCompress;
        try {
            arqCompress = new RandomAccessFile(compactacaoPath, "rw"); // Abre o arquivo para escrita da compactação
            BitSet bs = new BitSet();
            int count = 0;
            for (byte b : txt) {
                String codigo = codigos.get(b); // Obtém o código de Huffman para o byte
                for (int i = 0; i < codigo.length(); i++) {
                    if (codigo.charAt(i) == '0') {
                        bs.clear(count++); // Define o bit como 0
                    } else {
                        bs.set(count++); // Define o bit como 1
                    }
                }
            }
            byte[] txtCodificado = bs.toByteArray(); // Converte os bits para um array de bytes
            arqCompress.write(txtCodificado); // Escreve o array de bytes codificado no arquivo
            arqCompress.close();

            String arvorePath = "TP3/data/dataHuffmanArvore" + num + ".db";
            armazenaArvore(listNos.get(0), arvorePath); // Armazena a árvore de Huffman em um arquivo

            System.out.println("\nArquivo codificado Huffman tem " + txtCodificado.length + " bytes");
            float taxaCompressao = calculaTaxa(txt.length, txtCodificado.length); // Calcula a taxa de compressão
            System.out.printf("Taxa de compressão Huffman: %.2f%n", taxaCompressao);
            long fim = System.currentTimeMillis();
            System.out.println("Compactação Huffman levou " + (fim-inicio) + " milisegundos");

            System.out.println("\nBase de dados compactada com sucesso.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void descompactacao(String compressedPath, int num) {
        long inicio = System.currentTimeMillis(); // Marca o início da descompactação
        RandomAccessFile arqCompress;
        RandomAccessFile arqDescompress;
        try {
            String arvorePath = "TP3/data/dataHuffmanArvore" + num + ".db";
            No raiz = null;
            raiz = recuperarArvore(raiz, 0, arvorePath); // Recupera a árvore de Huffman do arquivo

            arqCompress = new RandomAccessFile(compressedPath, "rw"); // Abre o arquivo compactado para leitura
            String descompressedPath = "TP3/data/data.db";
            excluiArquivos(descompressedPath); // Exclui o arquivo descompactado anterior, se existir
            arqDescompress = new RandomAccessFile(descompressedPath, "rw"); // Cria um novo arquivo para descompactação

            byte[] txtCodificado = new byte[(int) arqCompress.length()];
            arqCompress.read(txtCodificado); // Lê o arquivo compactado
            arqCompress.close();
            BitSet bs = BitSet.valueOf(txtCodificado); // Converte o array de bytes para BitSet

            No no = raiz;
            for (int i = 0; i < bs.length(); i++) {
                if (!bs.get(i)) {
                    no = no.esq; // Navega para a esquerda na árvore
                } else {
                    no = no.dir; // Navega para a direita na árvore
                }

                if (no.esq == null && no.dir == null) {
                    arqDescompress.writeByte(no.simbolo); // Escreve o símbolo no arquivo descompactado
                    no = raiz; // Reinicia a navegação da árvore
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
            arqArvore.seek(pos); // Move o ponteiro do arquivo para a posição especificada
            posEsq = arqArvore.readLong(); // Lê a posição do nó esquerdo
            posDir = arqArvore.readLong(); // Lê a posição do nó direito
            simbolo = arqArvore.readByte(); // Lê o símbolo do nó
            arqArvore.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        no = new No(simbolo); // Cria um novo nó com o símbolo lido
        if(posEsq != -1 && posDir != -1) {
            no.esq = recuperarArvore(no.esq, posEsq, arvorePath); // Recupera a subárvore esquerda
            no.dir = recuperarArvore(no.dir, posDir, arvorePath); // Recupera a subárvore direita
        } else if(posEsq != -1 && posDir == -1) {
            no.esq = recuperarArvore(no.esq, posEsq, arvorePath); // Recupera apenas a subárvore esquerda
        } else if(posEsq == -1 && posDir != -1) {
            no.dir = recuperarArvore(no.dir, posDir, arvorePath); // Recupera apenas a subárvore direita
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
            arqArvore.seek(tam); // Move o ponteiro para o final do arquivo
            arqArvore.writeLong(-1); // Placeholder para a posição do nó esquerdo
            arqArvore.writeLong(-1); // Placeholder para a posição do nó direito
            arqArvore.writeByte(no.simbolo); // Escreve o símbolo do nó

            arqArvore.seek(tam); // Move o ponteiro de volta para o início da nova entrada
            arqArvore.writeLong(armazenaArvore(no.esq, arvorePath)); // Escreve a posição da subárvore esquerda
            arqArvore.writeLong(armazenaArvore(no.dir, arvorePath)); // Escreve a posição da subárvore direita

            arqArvore.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return tam; // Retorna a posição do nó atual
        }
    }

    public static void gerarCodigos(No no, String codigo, HashMap<Byte, String> codigos) {
        if (no.esq == null && no.dir == null) {
            codigos.put(no.simbolo, codigo); // Associa o código binário ao símbolo
            return;
        }

        gerarCodigos(no.esq, codigo + "0", codigos); // Gera códigos para a subárvore esquerda
        gerarCodigos(no.dir, codigo + "1", codigos); // Gera códigos para a subárvore direita
    }

    public static void excluiArquivos(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete(); // Exclui o arquivo se ele existir
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static float calculaTaxa(int tamOriginal, int tamComprimido) {
        float tamOriginalFloat = tamOriginal;
        float tamComprimidoFloat = tamComprimido;

        return tamComprimidoFloat / tamOriginalFloat * 100; // Calcula a taxa de compressão em percentual
    }
    
}

class No {
    byte simbolo;
    int frequencia;
    No esq;
    No dir;

    public No(byte simbolo) {
        this.simbolo = simbolo; // Inicializa o símbolo do nó
        this.frequencia = -1; // Inicializa a frequência como -1 para nós folha
        this.esq = null;
        this.dir = null;
    }

    public No(byte simbolo, int frequencia) {
        this.simbolo = simbolo; // Inicializa o símbolo do nó
        this.frequencia = frequencia; // Inicializa a frequência do nó
        this.esq = null;
        this.dir = null;
    }

    public No(No esq, No dir) {
        this.simbolo = -1; // Simbolo -1 para nós internos
        this.esq = esq;
        this.dir = dir;
        this.frequencia = esq.frequencia + dir.frequencia; // A frequência é a soma das frequências dos filhos
    }

    public No(byte simbolo, int frequencia, No esq, No dir) {
        this.simbolo = simbolo; // Inicializa o símbolo do nó
        this.frequencia = frequencia; // Inicializa a frequência do nó
        this.esq = esq;
        this.dir = dir;
    }
}
