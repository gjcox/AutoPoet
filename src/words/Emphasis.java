package words;

import java.util.ArrayList;

/**
 * TODO account for 'words' that are actually more than one word, and therefore
 * have multiple primary emphases
 */
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
}
