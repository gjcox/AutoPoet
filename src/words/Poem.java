package words;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;

public class Poem {

    private int line_count = 0; // don't count empty lines between stanzas
                                // equal to the sum of the line counts of each stanza
    LinkedList<Stanza> stanzas = new LinkedList<>();
    BufferedReader file_reader;

    public Poem(Path input_file) throws IOException {
        file_reader = Files.newBufferedReader(input_file); // relative to where program is executed
        stanzas.add(new Stanza());
        String line = "";
        boolean in_stanza = false; // to prevent counting double empty lines 
        while ((line = file_reader.readLine()) != null) {
            if (!line.equals("")) {
                /* if line not empty */
                /* add a line to the current stanza */
                in_stanza = true; 
                line_count++;
                stanzas.getLast().addLine(line);

            } else if (in_stanza) {
                /* if line empty */ 
                /* end the current stanza */
                in_stanza = false; 
                stanzas.add(new Stanza());

            } else {
                // ignore double empty line 
            }
        }
    }

    public int getLines() {
        return this.line_count; 
    }

    public String getString() {
        StringBuilder builder = new StringBuilder(); 
        for (Stanza stanza : stanzas) {
            builder.append(stanza.getString()); 
            builder.append("\n");
        }
        return builder.toString(); 
    }
}
