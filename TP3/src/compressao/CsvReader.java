package compressao;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CsvReader {

    // Método para ler um arquivo CSV e retornar seu conteúdo como uma String
    public String readCsvToString(String filePath) {
        StringBuilder result = new StringBuilder(); // Usado para construir a string final
        String line; // Variável para armazenar cada linha lida do arquivo

        // Bloco try-with-resources para garantir que o BufferedReader seja fechado automaticamente
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Lê o arquivo linha por linha
            while ((line = br.readLine()) != null) {
                result.append(line).append("\n"); // Adiciona cada linha lida ao StringBuilder, seguido por uma nova linha
            }
        } catch (IOException e) {
            e.printStackTrace(); // Em caso de exceção, imprime o stack trace para debug
        }

        return result.toString(); // Retorna o conteúdo completo do arquivo como uma única string
    }
}
