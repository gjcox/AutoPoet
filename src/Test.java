import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.json.JSONObject;

public class Test {
    
    static void emptyLine() throws IOException {
        Path path = FileSystems.getDefault().getPath("text.txt"); 
        BufferedReader file_reader = Files.newBufferedReader(path); 
        String line = ""; 
        while((line = file_reader.readLine()) != null) {
            System.out.println("\"" + line + "\"");
        }
    }

    static void testJSONObject() {
        String object = "object";
        JSONObject jo = new JSONObject().put(object, new JSONObject().put("string", "abc"));
        JSONObject jo2 = new JSONObject().put(object, "abc");
        System.out.println(jo.toString());
        System.out.println(jo.get(object).getClass());
        System.out.println(jo2.toString());
        System.out.println(jo2.get(object).getClass());
    }

    public static void main(String[] args) {
        /*try {
            emptyLine(); 
        } catch (IOException e) {
            System.err.println("File not found!");
        }*/
        JSONObject emphases = new JSONObject("{primary: 0, has_secondary: false, secondary: []}");
        System.out.println(emphases.toString());
        // testJSONObject(); 

    }

}
