package compressao;

import java.util.Scanner;

public class Main {
    public static int numArqCompactados = 0;

    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);

        int opcao = 1;
        while(opcao > 0 && opcao < 4){
            System.out.println("Selecione uma opção:");
            System.out.println("1)Compactar um arquivo.\n2)Descompactar um arquivo.\n3)Ler um arquivo\nOutra para sair");
            opcao = lerInteiro(sc);
            menu(sc,opcao);
        }

        sc.close();
    }

    public static void menu(Scanner sc, int opcao){
        if(opcao == 1){
            int algEscolhido = 1;
            while(algEscolhido > 0 && algEscolhido < 3){
                System.out.println("Escolha um algortimo de compactação:");
                System.out.println("1)Huffman\n2)LZW\nOutro para sair");
                algEscolhido = lerInteiro(sc);
            }
            if(algEscolhido == 1){
                numArqCompactados++;
                //Huffman
            }else{
                numArqCompactados++;
                //lzw
            }
        }else if(opcao == 2){
            System.out.println("Digite o número do arquivo que deseja descompactar(entre 1 e " + numArqCompactados + "): ");
            //descompactação            
        }
    }

    private static int lerInteiro(Scanner sc) {
        while (!sc.hasNextInt()) {
            System.out.println("Isso não é um número inteiro.");
            System.out.print("Insira um valor válido: ");
            sc.next(); // Consume o valor não inteiro para evitar um loop infinito
        }
        return sc.nextInt();
    }

    
    
}
