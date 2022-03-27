package words;

/**
 * This class is a wrapper for SuperWords, so that non-word elements of poems
 * (whitespace, punctuation, etc.) can be included but kept distinct.
 * 
 * @author 190021081
 */
public class Token {
    protected final String plaintext;

    public Token() {
        this.plaintext = "";
    }

    public Token(String string) {
        this.plaintext = string;
    }

    public String toString() {
        return this.plaintext;
    }

    public String getPlaintext() {
        return plaintext;
    }
}
