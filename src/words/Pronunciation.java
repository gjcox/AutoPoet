package words;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import utils.Pair;

/**
 * I could use a JSONObject instead of this class, which will have a lot of null
 * attributes. Provided they stay null the memory footprint should not be
 * unreasonable.
 */
public class Pronunciation {

    public class SubPronunciation {
        String ipa;
        ArrayList<Syllable> syllables;
        Emphasis emphasis;
    }

    private ArrayList<String> syllables;

    private SubPronunciation noun;
    private SubPronunciation pronoun;
    private SubPronunciation verb;
    private SubPronunciation adjective;
    private SubPronunciation adverb;
    private SubPronunciation preposition;
    private SubPronunciation conjunction;
    private SubPronunciation all;

    public ArrayList<String> getSyllables() {
        return syllables;
    }

    /**
     * 
     * @param syllablesObject JSONObject of the form {"count":x, "list":[]}
     */
    public void setSyllables(JSONObject syllablesObject) {
        if (syllablesObject.has("count") && syllablesObject.has("list")) {
            this.syllables = new ArrayList<>();
            JSONArray syllableArray = syllablesObject.getJSONArray("list");

            for (int i = 0; i < syllablesObject.getInt("count"); i++) {
                this.syllables.add(syllableArray.getString(i));
            }
        }
    }

    /**
     * 
     * @param pronunciationObject JSONObject of the form {<part-of-speech>:<ipa>}
     *                            e.g.
     *                            {"noun":"'kɑntrækt", "verb":"kɑn'trækt"}
     *                            {"all":"'fridəm"}
     * 
     */
    public void setIPA(JSONObject pronunciationObject) {
        Pair<ArrayList<Syllable>, Emphasis> syllablesAndEmphasis;

        if (pronunciationObject.has("noun")) {
            this.noun.ipa = pronunciationObject.getString("noun");
            syllablesAndEmphasis = IPAHandler.getSyllables(this.noun.ipa);
            this.noun.syllables = syllablesAndEmphasis.one();
            this.noun.emphasis = syllablesAndEmphasis.two();
        }

        if (pronunciationObject.has("pronoun")) {
            this.pronoun.ipa = pronunciationObject.getString("pronoun");
            syllablesAndEmphasis = IPAHandler.getSyllables(this.pronoun.ipa);
            this.pronoun.syllables = syllablesAndEmphasis.one();
            this.pronoun.emphasis = syllablesAndEmphasis.two();
        }

        if (pronunciationObject.has("verb")) {
            this.verb.ipa = pronunciationObject.getString("verb");
            syllablesAndEmphasis = IPAHandler.getSyllables(this.verb.ipa);
            this.verb.syllables = syllablesAndEmphasis.one();
            this.verb.emphasis = syllablesAndEmphasis.two();
        }

        if (pronunciationObject.has("adjective")) {
            this.adjective.ipa = pronunciationObject.getString("adjective");
            syllablesAndEmphasis = IPAHandler.getSyllables(this.adjective.ipa);
            this.adjective.syllables = syllablesAndEmphasis.one();
            this.adjective.emphasis = syllablesAndEmphasis.two();
        }

        if (pronunciationObject.has("adverb")) {
            this.adverb.ipa = pronunciationObject.getString("adverb");
            syllablesAndEmphasis = IPAHandler.getSyllables(this.adverb.ipa);
            this.adverb.syllables = syllablesAndEmphasis.one();
            this.adverb.emphasis = syllablesAndEmphasis.two();
        }

        if (pronunciationObject.has("preposition")) {
            this.preposition.ipa = pronunciationObject.getString("preposition");
            syllablesAndEmphasis = IPAHandler.getSyllables(this.preposition.ipa);
            this.preposition.syllables = syllablesAndEmphasis.one();
            this.preposition.emphasis = syllablesAndEmphasis.two();
        }

        if (pronunciationObject.has("conjunction")) {
            this.conjunction.ipa = pronunciationObject.getString("conjunction");
            syllablesAndEmphasis = IPAHandler.getSyllables(this.conjunction.ipa);
            this.conjunction.syllables = syllablesAndEmphasis.one();
            this.conjunction.emphasis = syllablesAndEmphasis.two();
        }

        if (pronunciationObject.has("all")) {
            this.all.ipa = pronunciationObject.getString("all");
            syllablesAndEmphasis = IPAHandler.getSyllables(this.all.ipa);
            this.all.syllables = syllablesAndEmphasis.one();
            this.all.emphasis = syllablesAndEmphasis.two();
        }

    }

    public SubPronunciation getNoun() {
        if (this.all != null)
            return this.all;
        return this.noun;
    }

    public SubPronunciation getPronoun() {
        if (this.all != null)
            return this.all;
        return this.pronoun;
    }

    public SubPronunciation getVerb() {
        if (this.all != null)
            return this.all;
        return this.verb;
    }

    public SubPronunciation getAdjective() {
        if (this.all != null)
            return this.all;
        return this.adjective;
    }

    public SubPronunciation getAdverb() {
        if (this.all != null)
            return this.all;
        return this.adverb;
    }

    public SubPronunciation getPreposition() {
        if (this.all != null)
            return this.all;
        return this.preposition;
    }

    public SubPronunciation getConjunction() {
        if (this.all != null)
            return this.all;
        return this.conjunction;
    }

    public SubPronunciation getAll() {
        return this.all;
    }

}
