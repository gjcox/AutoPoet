import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Poem {
    
    private int stanza_count = 0;   // index from zero 
    private int line_count = 0;     // don't count empty lines between stanzas 
                                    // equal to the sum of the line_counts of each stanza 
    ArrayList<Stanza> stanzas = new ArrayList<>(); 
    BufferedReader file_reader; 

    public Poem(Path input_file) {
        file_reader = Files.newBufferedReader(input_file);  
        Stanza stanza = new Stanza(); 
        String line = ""; 
        while ((line = file_reader.readLine()) != null) {
            if (line != "") {   // need to check what happens when an empty line is read 
                /* add a line to the current stanza */
                line_count++; 
                stanzas.get(stanza_count).addLine(line); 
                
            }
        }
    }

}
