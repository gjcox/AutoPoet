package words;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import utils.Pair;

import static config.Configuration.LOG;

/**
 * I could use a JSONObject instead of this class, which will have a lot of null
 * attributes. Provided they stay null the memory footprint should not be
 * unreasonable.
 */
public class Pronunciation {

    public static class SubPronunciation {
        String ipa;
        ArrayList<Syllable> syllables;
        ArrayList<Syllable> primaryRhyme;
        ArrayList<ArrayList<Syllable>> secondaryRhymes;
        ArrayList<Syllable> lastSyllable;
        Emphasis emphasis;

        ArrayList<Syllable> populateRhyme(int startingEmphasis) {
            ArrayList<Syllable> rhyme = new ArrayList<>();
            Syllable source = syllables.get(startingEmphasis);
            Syllable start = new Syllable("", source.getNucleus(), source.getCoda());
            rhyme.add(start);
            for (int i = startingEmphasis + 1; i < syllables.size(); i++) {
                rhyme.add(syllables.get(i));
            }
            return rhyme;
        }

        /**
         * Note that this does not create copies of syllables aside from the stressed
         * ones, so changing the syllables later could lead to unexpected behaviour. At
         * present, no such changes occur.
         */
        private void populateRhymes() {
            /* primary rhyme */
            this.primaryRhyme = populateRhyme(this.emphasis.getPrimary());

            /* secondary rhymes */
            if (this.emphasis.getSecondary() != null) {
                for (Integer secondaryEmphasis : this.emphasis.getSecondary()) {
                    this.secondaryRhymes = utils.NullListOperations.addToNull(this.secondaryRhymes,
                            populateRhyme(secondaryEmphasis));
                }
            }

            this.lastSyllable = populateRhyme(this.syllables.size() - 1);
        }

        private static boolean rhymeMatch(ArrayList<Syllable> rhyme1, ArrayList<Syllable> rhyme2) {
            if (rhyme1.size() != rhyme2.size()) {
                return false;
            }
            for (int i = 0; i < rhyme1.size(); i++) {
                if (!rhyme1.get(i).equals(rhyme2.get(i))) {
                    return false;
                }
            }
            return true;
        }

        public boolean rhymesWith(SubPronunciation other) {
            /* primary to primary */
            if (rhymeMatch(this.primaryRhyme, other.primaryRhyme)) {
                return true;
            }

            /* primary to secondary */
            if (other.secondaryRhymes != null) {
                for (ArrayList<Syllable> secondary : other.secondaryRhymes) {
                    if (rhymeMatch(this.primaryRhyme, secondary)) {
                        return true;
                    }
                }
            }

            /* secondary to primary */
            if (this.secondaryRhymes != null) {
                for (ArrayList<Syllable> secondary : this.secondaryRhymes) {
                    if (rhymeMatch(secondary, other.primaryRhyme)) {
                        return true;
                    }
                }
            }

            return false;
        }

        public boolean syllablicRhymesWith(SubPronunciation other) {
            /* last to last */
            return rhymeMatch(this.lastSyllable, other.lastSyllable);
        }

        public boolean imperfectRhymesWith(SubPronunciation other) {

            /* primary to unstressed */
            for (int i = other.syllables.size() - 1; i >= 0; i--) {
                // check that syllable in other is unstressed
                if (i == other.emphasis.getPrimary() || other.emphasis.getSecondary().contains(i)) {
                    continue;
                }

                ArrayList<Syllable> unstressed = new ArrayList<>(other.syllables.subList(i, other.syllables.size()));
                if (rhymeMatch(this.primaryRhyme, unstressed)) {
                    return true;
                }
            }

            /* unstressed to primary */
            for (int i = this.syllables.size() - 1; i >= 0; i--) {
                // check that syllable in other is unstressed
                if (i == this.emphasis.getPrimary() || this.emphasis.getSecondary().contains(i)) {
                    continue;
                }

                ArrayList<Syllable> unstressed = new ArrayList<>(this.syllables.subList(i, this.syllables.size()));
                if (rhymeMatch(other.primaryRhyme, unstressed)) {
                    return true;
                }
            }

            /* unstressed to secondary */
            if (other.secondaryRhymes != null) {
                for (ArrayList<Syllable> secondary : other.secondaryRhymes) {
                    for (int i = this.syllables.size() - 1; i >= 0; i--) {
                        // check that syllable is unstressed
                        if (i == this.emphasis.getPrimary() || this.emphasis.getSecondary().contains(i)) {
                            continue;
                        }

                        ArrayList<Syllable> unstressed = new ArrayList<>(
                                this.syllables.subList(i, this.syllables.size()));
                        if (rhymeMatch(secondary, unstressed)) {
                            return true;
                        }
                    }
                }
            }

            /* secondary to unstressed */
            if (this.secondaryRhymes != null) {
                for (ArrayList<Syllable> secondary : this.secondaryRhymes) {
                    for (int i = other.syllables.size() - 1; i >= 0; i--) {
                        // check that syllable is unstressed
                        if (i == other.emphasis.getPrimary() || other.emphasis.getSecondary().contains(i)) {
                            continue;
                        }

                        ArrayList<Syllable> unstressed = new ArrayList<>(
                                other.syllables.subList(i, other.syllables.size()));
                        if (rhymeMatch(secondary, unstressed)) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        public  boolean weakRhymesWith(SubPronunciation other) {
            /* unstressed to unstressed */
            for (int i = other.syllables.size() - 1; i >= 0; i--) {
                // check that syllable is unstressed
                if (i == other.emphasis.getPrimary() || other.emphasis.getSecondary().contains(i)) {
                    continue;
                }
                ArrayList<Syllable> thisUnstressed = new ArrayList<>(
                        this.syllables.subList(i, this.syllables.size()));

                for (int j = other.syllables.size() - 1; j >= 0; j--) {
                    // check that syllable is unstressed
                    if (j == other.emphasis.getPrimary() || other.emphasis.getSecondary().contains(j)) {
                        continue;
                    }
                    ArrayList<Syllable> otherUnstressed = new ArrayList<>(
                            other.syllables.subList(j, other.syllables.size()));

                    if (rhymeMatch(thisUnstressed, otherUnstressed)) {
                        return true;
                    }
                }
            }

            return false;
        }

        boolean forcedRhymesWith(SubPronunciation other) {
            // TODO this 
            /* primary to primary */

            /* primary to secondary */

            /* secondary to primary */

            return false;

        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("{");
            stringBuilder.append("ipa: " + ipa);
            stringBuilder.append(", ");
            stringBuilder.append("syllables: " + syllables.toString());
            stringBuilder.append(", ");
            stringBuilder.append("emphasis: " + emphasis.toString());
            stringBuilder.append(", ");
            stringBuilder.append("primary rhyme: " + primaryRhyme.toString());
            if (this.secondaryRhymes != null) {
                stringBuilder.append(", ");
                stringBuilder.append("secondary rhymes: " + secondaryRhymes.toString());
            }
            stringBuilder.append("}");
            return stringBuilder.toString();
        }
    }

    private ArrayList<String> plaintextSyllables = new ArrayList<>();

    private SubPronunciation noun;
    private SubPronunciation pronoun;
    private SubPronunciation verb;
    private SubPronunciation adjective;
    private SubPronunciation adverb;
    private SubPronunciation preposition;
    private SubPronunciation conjunction;
    private SubPronunciation all;

    public ArrayList<String> getPlaintextSyllables() {
        return plaintextSyllables;
    }

    /**
     * 
     * @param syllablesObject JSONObject of the form {"count":x, "list":[]}
     */
    public void setSyllables(String plaintext, JSONObject syllablesObject) {
        String count = "count";
        String list = "list";
        if (syllablesObject.has(count) && syllablesObject.has(list)) {
            this.plaintextSyllables = new ArrayList<>();
            JSONArray syllableArray = syllablesObject.getJSONArray(list);

            for (int i = 0; i < syllablesObject.getInt(count); i++) {
                try {
                    this.plaintextSyllables.add(syllableArray.getString(i));
                } catch (JSONException e) {
                    LOG.writePersistentLog(
                            String.format("\"%s\"'s syllables count did not match the syllables array: %s",
                                    plaintext, syllablesObject.toString()));
                }
            }
        } else {
            LOG.writePersistentLog(
                    String.format("\"%s\"'s syllables field did not have a count or list field: %s", plaintext,
                            syllablesObject.toString()));
        }
    }

    public void setRhyme(String plaintext, JSONObject rhymesObject) {
        JSONObject filteredRhymesObject = new JSONObject();
        boolean empty = true;

        /* strings are of form "-aʊtʃ" */
        if (rhymesObject.has("noun") && this.noun == null) {
            String original = rhymesObject.getString("noun");
            filteredRhymesObject.put("noun", original.replaceFirst("-", "'"));
            empty = false;
        }

        if (rhymesObject.has("pronoun") && this.pronoun == null) {
            String original = rhymesObject.getString("pronoun");
            filteredRhymesObject.put("pronoun", original.replaceFirst("-", "'"));
            empty = false;
        }

        if (rhymesObject.has("verb") && this.verb == null) {
            String original = rhymesObject.getString("verb");
            filteredRhymesObject.put("verb", original.replaceFirst("-", "'"));
            empty = false;
        }

        if (rhymesObject.has("adjective") && this.adjective == null) {
            String original = rhymesObject.getString("adjective");
            filteredRhymesObject.put("adjective", original.replaceFirst("-", "'"));
            empty = false;
        }

        if (rhymesObject.has("adverb") && this.adverb == null) {
            String original = rhymesObject.getString("adverb");
            filteredRhymesObject.put("adverb", original.replaceFirst("-", "'"));
            empty = false;
        }

        if (rhymesObject.has("preposition") && this.preposition == null) {
            String original = rhymesObject.getString("preposition");
            filteredRhymesObject.put("preposition", original.replaceFirst("-", "'"));
            empty = false;
        }

        if (rhymesObject.has("conjunction") && this.conjunction == null) {
            String original = rhymesObject.getString("conjunction");
            filteredRhymesObject.put("conjunction", original.replaceFirst("-", "'"));
            empty = false;
        }

        if (rhymesObject.has("all") && this.all == null) {
            String original = rhymesObject.getString("all");
            filteredRhymesObject.put("all", original.replaceFirst("-", "'"));
            empty = false;
        }

        if (empty) {
            /* i.e. pronunciation object had no (recognised) keys */
            LOG.writePersistentLog(String.format(
                    "Rhymes of \"%s\" had no recognised keys not found in pronunciation field", plaintext));
        } else {
            this.setIPA(plaintext, filteredRhymesObject);
        }
    }

    public void setRhyme(String plaintext, String rhymesString) {
        this.setIPA(plaintext, rhymesString.replaceFirst("-", "'"));
    }

    /**
     * I did try implementing this with less code repetition, but passing the
     * subpronunciations by reference was messy and prone to failure.
     * 
     * @param pronunciationObject JSONObject of the form {<part-of-speech>:<ipa>}
     *                            e.g.
     *                            {"noun":"'kɑntrækt", "verb":"kɑn'trækt"}
     *                            {"all":"'fridəm"}
     * 
     */
    public void setIPA(String plaintext, JSONObject pronunciationObject) {
        Pair<ArrayList<Syllable>, Emphasis> syllablesAndEmphasis = null;

        if (pronunciationObject.has("noun")) {
            this.noun = new SubPronunciation();
            this.noun.ipa = pronunciationObject.getString("noun");
            syllablesAndEmphasis = IPAHandler.getSyllables(this.noun.ipa);
            this.noun.syllables = syllablesAndEmphasis.one();
            this.noun.emphasis = syllablesAndEmphasis.two();
            this.noun.populateRhymes();
        }

        if (pronunciationObject.has("pronoun")) {
            this.pronoun = new SubPronunciation();
            this.pronoun.ipa = pronunciationObject.getString("pronoun");
            syllablesAndEmphasis = IPAHandler.getSyllables(this.pronoun.ipa);
            this.pronoun.syllables = syllablesAndEmphasis.one();
            this.pronoun.emphasis = syllablesAndEmphasis.two();
            this.pronoun.populateRhymes();
        }

        if (pronunciationObject.has("verb")) {
            this.verb = new SubPronunciation();
            this.verb.ipa = pronunciationObject.getString("verb");
            syllablesAndEmphasis = IPAHandler.getSyllables(this.verb.ipa);
            this.verb.syllables = syllablesAndEmphasis.one();
            this.verb.emphasis = syllablesAndEmphasis.two();
            this.verb.populateRhymes();
        }

        if (pronunciationObject.has("adjective")) {
            this.adjective = new SubPronunciation();
            this.adjective.ipa = pronunciationObject.getString("adjective");
            syllablesAndEmphasis = IPAHandler.getSyllables(this.adjective.ipa);
            this.adjective.syllables = syllablesAndEmphasis.one();
            this.adjective.emphasis = syllablesAndEmphasis.two();
            this.adjective.populateRhymes();
        }

        if (pronunciationObject.has("adverb")) {
            this.adverb = new SubPronunciation();
            this.adverb.ipa = pronunciationObject.getString("adverb");
            syllablesAndEmphasis = IPAHandler.getSyllables(this.adverb.ipa);
            this.adverb.syllables = syllablesAndEmphasis.one();
            this.adverb.emphasis = syllablesAndEmphasis.two();
            this.adverb.populateRhymes();
        }

        if (pronunciationObject.has("preposition")) {
            this.preposition = new SubPronunciation();
            this.preposition.ipa = pronunciationObject.getString("preposition");
            syllablesAndEmphasis = IPAHandler.getSyllables(this.preposition.ipa);
            this.preposition.syllables = syllablesAndEmphasis.one();
            this.preposition.emphasis = syllablesAndEmphasis.two();
            this.preposition.populateRhymes();
        }

        if (pronunciationObject.has("conjunction")) {
            this.conjunction = new SubPronunciation();
            this.conjunction.ipa = pronunciationObject.getString("conjunction");
            syllablesAndEmphasis = IPAHandler.getSyllables(this.conjunction.ipa);
            this.conjunction.syllables = syllablesAndEmphasis.one();
            this.conjunction.emphasis = syllablesAndEmphasis.two();
            this.conjunction.populateRhymes();
        }

        if (pronunciationObject.has("all")) {
            // "present" is a noun, verb and adjective, with pronunications for "noun",
            // "verb" and "all": "all" is useful even when other fields are filled
            this.all = new SubPronunciation();
            this.all.ipa = pronunciationObject.getString("all");
            syllablesAndEmphasis = IPAHandler.getSyllables(this.all.ipa);
            this.all.syllables = syllablesAndEmphasis.one();
            this.all.emphasis = syllablesAndEmphasis.two();
            this.all.populateRhymes();
        }

        if (syllablesAndEmphasis == null) {
            /* i.e. pronunciation object had no (recognised) keys */
            LOG.writePersistentLog(String.format("Pronunciation of \"%s\" had no recognised keys: %s", plaintext,
                    pronunciationObject.toString()));
        }
    }

    public void setIPA(String plaintext, String all) {
        if (all.equals("")) {
            LOG.writePersistentLog(String.format("Pronunciation of \"%s\" was an empty string", plaintext));
        } else {
            this.all = new SubPronunciation();
            this.all.ipa = all;
            Pair<ArrayList<Syllable>, Emphasis> syllablesAndEmphasis = IPAHandler.getSyllables(this.all.ipa);
            this.all.syllables = syllablesAndEmphasis.one();
            this.all.emphasis = syllablesAndEmphasis.two();
            this.all.populateRhymes();
        }
    }

    public SubPronunciation getSubPronunciation(String plaintext, SubWord.PartOfSpeech partOfSpeech) {
        SubPronunciation requested = this.all;
        switch (partOfSpeech) {
            case ADJECTIVE:
                if (this.adjective != null)
                    requested = adjective;
                break;
            case ADVERB:
                if (this.adverb != null)
                    requested = adverb;
                break;
            case CONJUCTION:
                if (this.conjunction != null)
                    requested = conjunction;
                break;
            case NOUN:
                if (this.noun != null)
                    requested = noun;
                break;
            case PREPOSITION:
                if (this.preposition != null)
                    requested = preposition;
                break;
            case PRONOUN:
                if (this.pronoun != null)
                    requested = pronoun;
                break;
            case VERB:
                if (this.verb != null)
                    requested = verb;
                break;
            case DEFINITE_ARTICLE:
            case UNKNOWN:
            default:
                // leave request as all
                break;
        }
        if (requested == null) {
            LOG.writePersistentLog(String.format("No pronunciation of \"%s\" could be found for \"%s\"",
                    plaintext, partOfSpeech));
        }
        return requested;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        stringBuilder.append("plaintext-syllables: " + plaintextSyllables.toString());
        if (noun != null) {
            stringBuilder.append(", ");
            stringBuilder.append("noun: " + noun.toString());
        }
        if (pronoun != null) {
            stringBuilder.append(", ");
            stringBuilder.append("pronoun: " + pronoun.toString());
        }
        if (verb != null) {
            stringBuilder.append(", ");
            stringBuilder.append("verb: " + verb.toString());
        }
        if (adjective != null) {
            stringBuilder.append(", ");
            stringBuilder.append("adjective: " + adjective.toString());
        }
        if (adverb != null) {
            stringBuilder.append(", ");
            stringBuilder.append("adverb: " + adverb.toString());
        }
        if (preposition != null) {
            stringBuilder.append(", ");
            stringBuilder.append("preposition: " + preposition.toString());
        }
        if (conjunction != null) {
            stringBuilder.append(", ");
            stringBuilder.append("conjunction: " + conjunction.toString());
        }
        if (all != null) {
            stringBuilder.append(", ");
            stringBuilder.append("all: " + all.toString());
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

}
