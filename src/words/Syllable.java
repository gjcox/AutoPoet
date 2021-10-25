package words;

public class Syllable {

    private String nucleus;
    private String onset = "";
    private String coda = "";

    public Syllable(char nucleus) {
        this.nucleus = new String(new char[] {nucleus}); 
    }

    public void setNucleus(String nucleus) {
        this.nucleus = nucleus;
    }

    public String getNucleus() {
        return this.nucleus; 
    }

    public void setOnset(String onset) {
        this.onset = onset;
    }

    public void setCoda(String coda) {
        this.coda = coda;
    }

    public String toString() {
        return onset + nucleus + coda; 
    }

}
