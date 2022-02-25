package words;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static config.Configuration.LOG;

public class Poem {

    private int lineCount = 0;
    private ArrayList<Stanza> stanzas = new ArrayList<>();
    private String title = "poem";
    private BufferedReader fileReader;

    private void fillPoem() throws IOException {
        stanzas.add(new Stanza(lineCount));
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
                stanzas.add(new Stanza(lineCount + stanzas.size())); // + stanzas.size for lines between stanzas 

            } else {
                // ignore double empty line
            }
        }
        LOG.writeTempLog(String.format("End of stanza %d reached", stanzas.size()));
        stanzas.get(stanzas.size() - 1).evaluateRhymingScheme();

        LOG.writeTempLog(String.format("Read poem: %s", this.getString()));
    }

    public Poem(Path inputFile) throws IOException {
        this.title = inputFile.toString();
        if (title.contains(File.separator)) {
            title = title.substring(title.lastIndexOf(File.separator) + 1);
        }

        fileReader = Files.newBufferedReader(inputFile); // relative to where program is executed

        fillPoem();
    }

    public Poem(String title, String poemString) throws IOException {
        this.title = title;
        if (title.contains(File.separator)) {
            title = title.substring(title.lastIndexOf(File.separator) + 1);
        }

        InputStream stream = new ByteArrayInputStream((poemString.trim()).getBytes());
        fileReader = new BufferedReader(new InputStreamReader(stream));

        fillPoem();
    }

    public void savePoem(File outputFile) throws IOException {
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputFile, StandardCharsets.UTF_8));
        fileWriter.write(this.toString());
        fileWriter.close();
    }

    public String getTitle() {
        return this.title;
    }

    public ArrayList<Stanza> getStanzas() {
        return this.stanzas;
    }

    public int getStanzaCount() {
        return this.stanzas.size();
    }

    public int getLineCount() {
        return this.lineCount;
    }

    public void substituteWord(int stanzaIndex, int lineIndex, int tokenIndex, SuperWord newWord) {
        stanzas.get(stanzaIndex).substituteWord(lineIndex, tokenIndex, newWord);
    }

    public boolean joinWords(int stanzaIndex, int lineIndex, int tokenIndex1, int tokenIndex2) {
        return stanzas.get(stanzaIndex).joinWords(lineIndex, tokenIndex1, tokenIndex2);
    }

    public boolean splitWord(int stanzaIndex, int lineIndex, int tokenIndex, String seperator) {
        return stanzas.get(stanzaIndex).splitWord(lineIndex, tokenIndex, seperator);
    }

    public void setTitle(String newTitle) {
        title = newTitle;
        if (title.contains(File.separator)) {
            title = title.substring(title.lastIndexOf(File.separator) + 1);
        }
    }

    public void setDefaultRhymeScheme(String rhymeScheme) {
        RhymingScheme defaultRhymingScheme = new RhymingScheme(rhymeScheme.toCharArray()); 
        for (Stanza stanza : stanzas) {
            stanza.setDesiredRhymeSchemeFromDefault(defaultRhymingScheme);
        }
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
