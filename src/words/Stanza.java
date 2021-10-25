package words;
import java.util.LinkedList;
import java.util.regex.Pattern;

public class Stanza {

    private LinkedList<String> lines = new LinkedList<>();
    private LinkedList<LinkedList<String>> lines_words = new LinkedList<>(); // doesn't include punctuation
                                                                             // allows easy access of last word
                                                                             // could be changed to use custom Word
                                                                             // ... class with API data
    private RhymingScheme scheme; // make rhyming scheme after line count is known

    // an IPA line represenation could allow recognition of longer rhymes

    public void addLine(String line) {
        /* add the plain string line */
        this.lines.add(line);
        this.lines_words.add(new LinkedList<>());

        /* add the words-only version of the line */
        String[] words = line.split(Pattern.compile("\\s").toString()); // split on whitespace
        for (int i = 0; i < words.length; i++) {
            lines_words.getLast().add(words[i].replaceAll("a-zA-Z-", "")); // remove all non-word characters
                                                                           // except '-'
        }

        /* building the phonetic line requires WordsAPI use */
    }

    public int getLineCount() {
        return this.lines.size();
    }

    public String getString() {
        StringBuilder builder = new StringBuilder(); 
        for (String line : lines) {
            builder.append(line);
            builder.append("\n");  
        }
        return builder.toString(); 
    }

    /**
     * 
     * @param syllables the number of syllables at the end of the line to match 
     * @return
     */
    public RhymingScheme evaluateRhymingScheme(int syllables) {
        RhymingScheme scheme_ = new RhymingScheme(this.getLineCount()); 

        return scheme_; 
    }
}
