package words;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static config.Configuration.LOG;

public class Poem {

    private int lineCount = 0; 
    private ArrayList<Stanza> stanzas = new ArrayList<>();
    private String title = "poem";
    private BufferedReader fileReader;

    public Poem(Path inputFile) throws IOException {
        this.title = inputFile.toString();
        if (title.contains(File.separator)) {
            title = title.substring(title.lastIndexOf(File.separator) + 1);
        }

        fileReader = Files.newBufferedReader(inputFile); // relative to where program is executed
        stanzas.add(new Stanza());
        String line = "";
        boolean in_stanza = false; // to prevent counting double empty lines
        while ((line = fileReader.readLine()) != null) {
            if (!line.matches("\\s+") && !line.matches("")) {
                /* if line not empty */
                /* add a line to the current stanza */
                in_stanza = true;
                lineCount++;
                stanzas.get(stanzas.size() - 1).addLine(line);

            } else if (in_stanza) {
                /* if line empty */
                /* end the current stanza */
                in_stanza = false;
                LOG.writeTempLog(String.format("End of stanza %d reached", stanzas.size()));
                stanzas.get(stanzas.size() - 1).evaluateRhymingScheme();
                stanzas.add(new Stanza());

            } else {
                // ignore double empty line
            }
        }
        LOG.writeTempLog(String.format("End of stanza %d reached", stanzas.size()));
        stanzas.get(stanzas.size() - 1).evaluateRhymingScheme();

        LOG.writeTempLog(String.format("Read poem: %s", this.getString()));
    }

    public String getTitle() {
        return this.title; 
    }

    public int getStanzaCount() {
        return this.stanzas.size(); 
    }

    public int getLines() {
        return this.lineCount;
    }

    public String getString() {
        String divider = "\n";
        StringBuilder builder = new StringBuilder();
        builder.append(title);
        builder.append(String.format("%s- Line count: %d", divider, lineCount));
        builder.append(String.format("%s- Stanza count: %d", divider, stanzas.size()));
        builder.append(divider);
        int stanzaIndex = 1;
        for (Stanza stanza : stanzas) {
            builder.append(String.format("%sStanza %d", divider, stanzaIndex++));
            builder.append(String.format("%s- Desired rhyme scheme: %s", divider, stanza.getDesiredRhymeScheme()));
            builder.append(String.format("%s- Current rhyme scheme: %s", divider, stanza.getActualRhymeScheme()));
            builder.append(divider);
            builder.append(stanza.toString());
        }
        return builder.toString();
    }

    public String toString() {
        String divider = "\n";
        StringBuilder builder = new StringBuilder();
        for (Stanza stanza : stanzas) {
            builder.append(stanza.toString());
            builder.append(divider);
        }
        return builder.toString();
    }
}
