package compressao;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CsvReader {

    public String readCsvToString(String filePath) {
        StringBuilder result = new StringBuilder();
        String line;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            while ((line = br.readLine()) != null) {
                result.append(line).append("\n");  // Append each line and add a newline character
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();
    }

    
}

