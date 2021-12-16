package words;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static config.Configuration.LOG;

public class Stanza {

    private ArrayList<ArrayList<Token>> lines = new ArrayList<>();
    private RhymingScheme desiredScheme; // make rhyming scheme after line count is known
    private RhymingScheme actualScheme; // make rhyming scheme after line count is known
    // an IPA line represenation could allow recognition of longer rhymes

    public void addLine(String line) {
        ArrayList<Token> parsedLine = new ArrayList<>();
        String wordPattern = "(?<word>\\w+)";
        String tokenPattern = "(?<token>\\W+)";
        String masterPattern = String.format("%s|%s", wordPattern, tokenPattern);
        Pattern pattern = Pattern.compile(masterPattern);
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            String token;
            if ((token = matcher.group("word")) != null) {
                parsedLine.add(SuperWord.getSuperWord(token));
            } else if ((token = matcher.group("token")) != null) {
                parsedLine.add(new Token(token));
            } else {
                System.err.println("A line had no words or other tokens.");
            }
        }
        lines.add(parsedLine);
    }

    private SuperWord getLastWord(int lineNumber) {
        ArrayList<Token> line = lines.get(lineNumber);
        int index = line.size() - 1;
        Token token = new Token();
        boolean superword = false;
        while (index >= 0 && !superword) {
            token = line.get(index--);
            superword = (token.getClass() == SuperWord.class);
        }
        return superword ? (SuperWord) token : null;
    }

    public int lineCount() {
        return this.lines.size();
    }

    public RhymingScheme getDesiredRhymeScheme() {
        return this.desiredScheme;
    }

    public RhymingScheme getActualRhymeScheme() {
        return this.actualScheme;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (ArrayList<Token> line : lines) {
            for (Token token : line) {
                builder.append(token.getPlaintext());
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    public void evaluateRhymingScheme() {
        RhymingScheme scheme = new RhymingScheme(this.lineCount());
        for (int i = 0; i < this.lineCount() - 1; i++) {
            SuperWord word1;
            if (scheme.getValue(i) == 0 && (word1 = getLastWord(i)) != null) {
                for (int j = i + 1; j < this.lineCount(); j++) {
                    SuperWord word2;
                    if (scheme.getValue(j) == 0 && (word2 = getLastWord(j)) != null && word1.rhymesWithWrapper(word2)) {
                        switch (scheme.getValue(i)) {
                            case 0:
                                int rhymeValue = scheme.getNextValue();
                                scheme.setValue(i, rhymeValue);
                                scheme.setValue(j, rhymeValue);
                                break;
                            default:
                                scheme.setValue(j, scheme.getValue(i));
                                break;
                        }
                    }
                }
            }
        }

        this.actualScheme = scheme;
        LOG.writeTempLog(String.format("Stanza rhyme scheme analysed"));
    }
}
