package words;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exceptions.RhymingSchemeSizeException;
import utils.ParameterWrappers.FilterParameters.Filter;

import static config.Configuration.LOG;

public class Stanza {

    private ArrayList<ArrayList<Token>> lines = new ArrayList<>();
    private RhymingScheme desiredScheme;
    private RhymingScheme actualScheme;
    // an IPA line represenation could allow recognition of longer rhymes

    public void addLine(String line) {
        ArrayList<Token> parsedLine = new ArrayList<>();
        String wordPattern = "(?<word>[-\\p{L}]+)";
        String tokenPattern = "(?<token>[^-\\p{L}]+)";
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
                LOG.writeTempLog("A line had no words or other tokens.");
            }
        }
        lines.add(parsedLine);
    }

    /**
     * 
     * @param lineNumber
     * @return the last SuperWord in a line; null if no SuperWords were found.
     */
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

    public ArrayList<ArrayList<Token>> getLines() {
        return this.lines;
    }

    public boolean setDesiredRhymeScheme(String rhymeScheme) {
        if (rhymeScheme.equals("")) {
            desiredScheme = null;
            return true;
        }
        try {
            this.desiredScheme = new RhymingScheme(lines.size(), rhymeScheme.toCharArray());
            return true;
        } catch (RhymingSchemeSizeException e) {
            LOG.writeTempLog(e.toString());
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean setDesiredRhymeSchemeFromDefault(RhymingScheme rhymeScheme) {
        if (desiredScheme == null && rhymeScheme.getLineCount() == lineCount()) {
            desiredScheme = rhymeScheme;
            return true;
        } else {
            return false;
        }
    }

    public void substituteWord(int lineIndex, int tokenIndex, SuperWord newWord) {
        ArrayList<Token> line = lines.get(lineIndex);
        SuperWord lastWord = getLastWord(lineIndex);
        boolean updateRhymeScheme = lastWord != null && lastWord.equals(line.remove(tokenIndex));
        line.add(tokenIndex, newWord);

        if (updateRhymeScheme)
            evaluateRhymingScheme();

    }

    public boolean joinWords(int lineIndex, int tokenIndex1, int tokenIndex2) {
        ArrayList<Token> line = lines.get(lineIndex);
        SuperWord lastWord = getLastWord(lineIndex);
        Token token1 = line.get(tokenIndex1);
        Token token2 = line.get(tokenIndex2);
        if (tokenIndex2 - tokenIndex1 == 2 // two words seperated by one other token
                && token1.getClass().equals(SuperWord.class)
                && token2.getClass().equals(SuperWord.class)) {

            Token middleToken = line.get(tokenIndex1 + 1);
            LOG.writeTempLog(String.format("Creating joined word %s",
                    token1.getPlaintext() + middleToken.getPlaintext() + token2.getPlaintext()));
            SuperWord combined = SuperWord
                    .getSuperWord(token1.getPlaintext() + middleToken.getPlaintext() + token2.getPlaintext());
            line.remove(tokenIndex1);
            line.remove(tokenIndex1); // i.e. seperator
            line.remove(tokenIndex1); // i.e. token 2
            line.add(tokenIndex1, combined);

            boolean updateRhymeScheme = lastWord != null && lastWord.equals(token2);
            if (updateRhymeScheme)
                evaluateRhymingScheme();
            return true;
        } else {
            return false;
        }

    }

    public boolean splitWord(int lineIndex, int tokenIndex, String seperator) {
        ArrayList<Token> line = lines.get(lineIndex);
        SuperWord lastWord = getLastWord(lineIndex);
        String toSplit = line.get(tokenIndex).getPlaintext();
        if (toSplit.indexOf(seperator) != -1) {

            LOG.writeTempLog(String.format("Splitting word %s", toSplit));
            SuperWord word1 = SuperWord.getSuperWord(toSplit.substring(0, toSplit.indexOf(seperator)));
            SuperWord word2 = SuperWord.getSuperWord(toSplit.substring(toSplit.indexOf(seperator) + 1));
            boolean updateRhymeScheme = lastWord != null && lastWord.equals(line.remove(tokenIndex));

            line.add(tokenIndex, word2);
            line.add(tokenIndex, new Token(seperator));
            line.add(tokenIndex, word1);

            if (updateRhymeScheme)
                evaluateRhymingScheme();
            return true;
        } else {
            return false;
        }
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
                    if (scheme.getValue(j) == 0 && (word2 = getLastWord(j)) != null
                            && word1.matchesWithWrapper(Filter.PERFECT_RHYME, word2)) {
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
