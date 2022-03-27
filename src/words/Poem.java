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

/**
 * This class contains a poem, defined as a collection of stanzas with a title.
 * It also handles file I/O for poems.
 * 
 * @author 190021081
 */
public class Poem {

    private int lineCount = 0;
    private ArrayList<Stanza> stanzas = new ArrayList<>();
    private String title = "poem";
    private BufferedReader fileReader;

    /**
     * Poem constructor from text file. Uses file name as poem title.
     * 
     * @param inputFile path to text file.
     * @throws IOException
     */
    public Poem(Path inputFile) throws IOException {
        this.title = inputFile.toString();
        if (title.contains(File.separator)) {
            title = title.substring(title.lastIndexOf(File.separator) + 1);
        }

        fileReader = Files.newBufferedReader(inputFile); // relative to where program is executed

        fillPoem();
    }

    /**
     * Poem constructor from strings.
     * 
     * @param title
     * @param poemString the body of the poem.
     * @throws IOException
     */
    public Poem(String title, String poemString) throws IOException {
        this.title = title;
        if (title.contains(File.separator)) {
            title = title.substring(title.lastIndexOf(File.separator) + 1);
        }

        InputStream stream = new ByteArrayInputStream((poemString.trim()).getBytes());
        fileReader = new BufferedReader(new InputStreamReader(stream));

        fillPoem();
    }

    /**
     * Attempts to read the input stream pointed to by {@link words.Poem#fileReader}
     * and populate this poem with its contents.
     * 
     * @throws IOException
     */
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

    /**
     * Writes this poem to a text file.
     * 
     * @param outputFile the desired output file (will overwrite).
     * @throws IOException
     */
    public void savePoem(File outputFile) throws IOException {
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputFile, StandardCharsets.UTF_8));
        fileWriter.write(this.toString());
        fileWriter.close();
    }

    // getters

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

    // model update wrappers

    public void substituteWord(int stanzaIndex, int lineIndex, int tokenIndex, SuperWord newWord) {
        stanzas.get(stanzaIndex).substituteWord(lineIndex, tokenIndex, newWord);
    }

    public boolean joinWords(int stanzaIndex, int lineIndex, int tokenIndex1, int tokenIndex2) {
        return stanzas.get(stanzaIndex).joinWords(lineIndex, tokenIndex1, tokenIndex2);
    }

    public boolean splitWord(int stanzaIndex, int lineIndex, int tokenIndex, String separator) {
        return stanzas.get(stanzaIndex).splitWord(lineIndex, tokenIndex, separator);
    }

    // setters

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

    // other

    /**
     * Distinct from toString() as it includes meta data. Used for
     * logging/debugging.
     */
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

    /**
     * {@inheritDoc}
     */
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
