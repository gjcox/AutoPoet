package words;

import java.util.ArrayList;

public class Emphasis {
    private int primary = 0; // default value for monosyllabic words with no marked emphasis
    private ArrayList<Integer> secondary;

    public int getPrimary() {
        return primary;
    }

    public void setPrimary(int syllableIndex) {
        this.primary = syllableIndex;
    }

    /**
     * @return null if no secondary emphases
     */
    public ArrayList<Integer> getSecondary() {
        return secondary;
    }

    public void addSecondary(int syllableIndex) {
        if (this.secondary == null) {
            this.secondary = new ArrayList<>();
        }
        this.secondary.add(syllableIndex);
    }

    public boolean equals(Object other) {
        if (other == null) return false; 
        if (other.getClass() != Emphasis.class) return false; 

        Emphasis o = (Emphasis) other; 
        if (primary != o.primary) return false; 
        if (secondary == null && o.secondary == null) return true; 
        if (secondary == null || o.secondary == null) return false; 
        if (secondary.size() != o.secondary.size()) return false; 
        for (int i = 0; i < secondary.size(); i++) {
            if (secondary.get(i) != o.secondary.get(i)) return false; 
        }
        return true; 
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        stringBuilder.append("primary: " + primary);
        if (secondary != null) {
            stringBuilder.append(", ");
            stringBuilder.append("secondary: " + secondary.toString());
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}
