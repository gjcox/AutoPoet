package words;

/**
 * This class encodes a syllable, defined as a nucleus (a vowel or diphthong),
 * and optional onset (the consonants preceding the nucleus) and coda (the
 * consontants after the nucleus) components.
 * 
 * @author 190021081
 */
public class Syllable {

    private String nucleus; // the vowel sound of a syllable - either a single vowel or a diphthong
    private String onset = ""; // the consonants preceding the vowel within the syllable
    private String coda = ""; // the consonants after the vowel within the syllable

    /**
     * Single vowel constructor.
     * 
     * @param nucleus a vowel.
     */
    public Syllable(char nucleus) {
        this.nucleus = new String(new char[] { nucleus });
    }

    /**
     * Diphthong constructor.
     * 
     * @param nucleus one or two vowels.
     */
    public Syllable(String nucleus) {
        this.nucleus = nucleus;
    }

    public Syllable(String onset, String nucleus, String coda) {
        this.nucleus = nucleus;
        this.onset = onset;
        this.coda = coda;
    }

    // getters

    public String getNucleus() {
        return this.nucleus;
    }

    public String getCoda() {
        return this.coda;
    }

    // setters

    public void setNucleus(String nucleus) {
        this.nucleus = nucleus;
    }

    public void setOnset(String onset) {
        this.onset = onset;
    }

    public void setCoda(String coda) {
        this.coda = coda;
    }

    // other

    public String toString() {
        return onset + nucleus + coda;
    }

    /**
     * @return true if the onset, nucleus and coda all match.
     */
    public boolean equals(Object o2) {
        return o2 != null && this.toString().equals(o2.toString());
    }

    public int hashCode() {
        return this.toString().hashCode();
    }

}
