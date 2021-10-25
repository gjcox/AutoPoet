import java.util.ArrayList;

public class Stanza {

    private int line_count = 1;
    private ArrayList<String> lines = new ArrayList<>();
    private ArrayList<ArrayList<String>> lines_words = new ArrayList<>(); // doesn't include punctuation; allows easy
                                                                          // access of last word
    private ArrayList<ArrayList<String>> lines_ipa = new ArrayList<>(); // populated with IPA syllables; could allow
                                                                        // recognition of longer rhymes
    private RhymingScheme scheme; // make rhyming scheme after line count is known

    public addLine(String line) {
        this.line_count++; 
        this.lines.add(line); 
        String[] words = line.split("\s"); // split on whitespace  
        for (int i = 0; i < words.length; i++) {
             words[i] = words[i].replaceAll("a-zA-Z\-", ""); // remove all non-word characters except '-'
        }
        // building the phonetic line requires WordsAPI use  
    }

}
