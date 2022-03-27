package words;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exceptions.RhymeSchemeSizeException;
import utils.ParameterWrappers.FilterParameters.RhymeType;

import static config.Configuration.LOG;

/**
 * This class encodes a stanza as a collection of lines, a desired rhyming
 * scheme, and an intended rhyme scheme.
 */
public class Stanza {

    private final int startLine; // the index of the first line of the stanza
    private ArrayList<ArrayList<Token>> lines = new ArrayList<>();
    private RhymeScheme desiredScheme;
    private RhymeScheme actualScheme;

    // construction methods

    public Stanza(int startLine) {
        this.startLine = startLine;
    }

    /**
     * Breaks a string into tokens (either words or whitespace, punctuation etc.)
     * and adds the collection of tokens to the stanza as a line.
     * 
     * @param line
     */
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

    // getters

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

    public int getStartLine() {
        return this.startLine;
    }

    public int lineCount() {
        return this.lines.size();
    }

    public RhymeScheme getDesiredRhymeScheme() {
        return this.desiredScheme;
    }

    public RhymeScheme getActualRhymeScheme() {
        return this.actualScheme;
    }

    public ArrayList<ArrayList<Token>> getLines() {
        return this.lines;
    }

    // setters

    /**
     * Overwrites current desired rhyme scheme. Nullifies desired rhyme scheme if
     * passed empty string.
     * 
     * @param rhymeScheme new rhyme scheme.
     * @return true if successful update.
     */
    public boolean setDesiredRhymeScheme(String rhymeScheme) {
        if (rhymeScheme.equals("")) {
            desiredScheme = null;
            return true;
        }
        try {
            this.desiredScheme = new RhymeScheme(lines.size(), rhymeScheme.toCharArray());
            return true;
        } catch (RhymeSchemeSizeException e) {
            LOG.writeTempLog(e.toString());
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Avoids overwriting prior intended rhyme scheme.
     * 
     * @param rhymeScheme the poem's default rhyme scheme.
     * @return true if change occured.
     */
    public boolean setDesiredRhymeSchemeFromDefault(RhymeScheme rhymeScheme) {
        if (desiredScheme == null && rhymeScheme.getLineCount() == lineCount()) {
            desiredScheme = rhymeScheme;
            return true;
        } else {
            return false;
        }
    }

    // other

    /**
     * Replaces a word.
     * 
     * @param lineIndex  which line the word is in.
     * @param tokenIndex the token index of the word (including non-word tokens).
     * @param newWord    the replacement word.
     */
    public void substituteWord(int lineIndex, int tokenIndex, SuperWord newWord) {
        ArrayList<Token> line = lines.get(lineIndex);
        SuperWord lastWord = getLastWord(lineIndex);
        boolean updateRhymeScheme = lastWord != null && lastWord.equals(line.remove(tokenIndex));
        line.add(tokenIndex, newWord);

        if (updateRhymeScheme)
            evaluateRhymingScheme();

    }

    /**
     * Joins two adjacent words into one, to allow users to make use of WordsAPI's
     * entries for short phrases. Updates the actual rhyme scheme if this affects
     * the last
     * word of the line.
     * 
     * @param lineIndex   which line the words are in.
     * @param tokenIndex1 the token index of one word (including non-word tokens).
     * @param tokenIndex2 the token index of the other word (including non-word
     *                    tokens).
     * @return true if a substitution was made.
     */
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
            line.remove(tokenIndex1); // i.e. separator
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

    /**
     * Separates one word into two adjacent words, to allow users to get around
     * WordsAPI's entries for short phrases. Updates the actual rhyme scheme if this
     * affects the last word of the line.
     * 
     * @param lineIndex  which line the word is in.
     * @param tokenIndex the token index of the word (including non-word tokens).
     * @param separator  the separator string (typically "-" or " ").
     * @return true if a substitution was made.
     */
    public boolean splitWord(int lineIndex, int tokenIndex, String separator) {
        ArrayList<Token> line = lines.get(lineIndex);
        SuperWord lastWord = getLastWord(lineIndex);
        String toSplit = line.get(tokenIndex).getPlaintext();
        if (toSplit.indexOf(separator) != -1) {

            LOG.writeTempLog(String.format("Splitting word %s", toSplit));
            SuperWord word1 = SuperWord.getSuperWord(toSplit.substring(0, toSplit.indexOf(separator)));
            SuperWord word2 = SuperWord.getSuperWord(toSplit.substring(toSplit.indexOf(separator) + 1));
            boolean updateRhymeScheme = lastWord != null && lastWord.equals(line.remove(tokenIndex));

            line.add(tokenIndex, word2);
            line.add(tokenIndex, new Token(separator));
            line.add(tokenIndex, word1);

            if (updateRhymeScheme)
                evaluateRhymingScheme();
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * Determines the current rhyming scheme of the poem, using perfect rhyme.
     */
    public void evaluateRhymingScheme() {
        RhymeScheme scheme = new RhymeScheme(this.lineCount());
        for (int i = 0; i < this.lineCount() - 1; i++) {
            SuperWord word1;
            if ((word1 = getLastWord(i)) != null) {
                for (int j = i + 1; j < this.lineCount(); j++) {
                    SuperWord word2;
                    if (scheme.getValue(j) == 0 && (word2 = getLastWord(j)) != null
                            && word1.matchesWithWrapper(RhymeType.PERFECT_RHYME, word2)) {
                        if (scheme.getValue(i) == 0) {
                            int rhymeValue = scheme.getNextValue();
                            scheme.setValue(i, rhymeValue);
                            scheme.setValue(j, rhymeValue);
                        } else {
                            scheme.setValue(j, scheme.getValue(i));
                        }
                    }
                }
            }
        }

        this.actualScheme = scheme;
        LOG.writeTempLog(String.format("Stanza rhyme scheme analysed"));
    }
}
