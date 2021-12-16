package words;

public class Token {
    protected String plaintext;

    public Token() {
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
