import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class Test {
    
    static void emptyLine() throws IOException {
        Path path = FileSystems.getDefault().getPath("text.txt"); 
        BufferedReader file_reader = Files.newBufferedReader(path); 
        String line = ""; 
        while((line = file_reader.readLine()) != null) {
            System.out.println("\"" + line + "\"");
        }
    }

    public static void main(String[] args) {
        try {
            emptyLine(); 
        } catch (IOException e) {
            System.err.println("File not found!");
        }
    }

}
