package words;

import org.json.JSONObject;

public class Syllable {

    private String nucleus;
    private String onset = "";
    private String coda = "";

    public Syllable(char nucleus) {
        this.nucleus = new String(new char[] { nucleus });
    }

    public Syllable(String nucleus) {
        this.nucleus = nucleus;
    }

    public Syllable(String onset, String nucleus, String coda) {
        this.nucleus = nucleus;
        this.onset = onset;
        this.coda = coda;
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

    public boolean equals(Object o2) {
        return o2 != null && this.toString().equals(o2.toString());
    }

    public int hashCode() {
        return this.toString().hashCode(); 
    }

    public boolean rhymes(Syllable other) {
        return this.nucleus.equals(other.nucleus) && this.coda.equals(other.coda);
    }

    public JSONObject toJsonObject() {
        JSONObject jo = new JSONObject(); 
        jo.put("onset", onset); 
        jo.put("nucleus", nucleus); 
        jo.put("coda", coda); 
        return jo; 
    }
}
