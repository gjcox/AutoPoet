package words;

import java.util.Map;

import org.json.JSONObject;

public class Syllable {

    private String nucleus; // the vowel sound of a syllable - either a single vowel or a dipthong
    private String onset = ""; // the consonants preceding the vowel within the syllable
    private String coda = ""; // the consonants after the vowel within the syllable

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

    /**
     * For converting from a JSONObject, the the intermediate step of a JSONArray
     * containing the object being made into a List<Object>, which turns JSONObjects
     * to Map<String,String>.
     * 
     * @param map a JSONObject encoded as a Map<String, String>.
     * @throws ClassCastException if the Map does not have a "nucleus" key, or has a
     *                            null value for it.
     */
    public Syllable(Map<String, String> map) throws ClassCastException {
        String o = "onset";
        String n = "nucleus";
        String c = "coda";

        String onset_;
        String nucleus_;
        String coda_;

        if ((nucleus_ = map.get(n)) == null) {
            throw new ClassCastException("Syllable must have a nucleus.");
        } else {
            this.nucleus = nucleus_;
        }

        if ((onset_ = map.get(o)) != null) {
            this.onset = onset_;
        }
        if ((coda_ = map.get(c)) != null) {
            this.coda = coda_;
        }
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

    public String getCoda() {
        return this.coda;
    }

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

    /**
     * 
     * @return true if the nucleus and coda of two syllables match.
     */
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
