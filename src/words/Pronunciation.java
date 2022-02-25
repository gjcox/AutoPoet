package words;

import java.util.ArrayList;
import java.util.EnumMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import utils.Pair;
import utils.ParameterWrappers.FilterParameters.Filter;
import words.SubWord.PartOfSpeech;

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
        Emphasis emphasis;

        /**
         * Creates a rhyme-matching list of syllables.
         * 
         * @param startingEmphasis the index of the first syllable to rhyme from.
         * @return the syllables from @param startingEmphasis to the end of the word,
         *         inclusive.
         */
        private ArrayList<Syllable> getRhymeList(int startingEmphasis) {
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
            this.primaryRhyme = getRhymeList(this.emphasis.getPrimary());

            /* secondary rhymes */
            if (this.emphasis.getSecondary() != null) {
                for (Integer secondaryEmphasis : this.emphasis.getSecondary()) {
                    this.secondaryRhymes = utils.NullListOperations.addToNull(this.secondaryRhymes,
                            getRhymeList(secondaryEmphasis));
                }
            }

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

        public boolean matchesWith(Filter filter, SubPronunciation other) {
            switch (filter) {
                case PERFECT_RHYME:
                    return rhymesWith(other);
                case SYLLABIC_RHYME:
                    return syllablicRhymesWith(other);
                case FORCED_RHYME:
                    return forcedRhymesWith(other);
                case IMPERFECT_RHYME:
                    return imperfectRhymesWith(other);
                case WEAK_RHYME:
                    return weakRhymesWith(other);
                default:
                    LOG.writeTempLog("Attempted unimplemented matchFilter: " + filter.name());
                    return false;
            }
        }

        private boolean rhymesWith(SubPronunciation other) {
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

        /* Standard rhyme ignores the inital onset; should that be the case here? */
        private boolean syllablicRhymesWith(SubPronunciation other) {
            /* last to last */
            ArrayList<Syllable> thisLastSyllable = getRhymeList(syllables.size() - 1);
            ArrayList<Syllable> otherLastSyllable = other.getRhymeList(other.syllables.size() - 1);
            return rhymeMatch(thisLastSyllable, otherLastSyllable);
        }

        private boolean imperfectRhymesWith(SubPronunciation other) {
            /* primary or secondary to unstressed */
            for (int i = other.syllables.size() - 1; i >= 0; i--) {
                // check that syllable in other is not stressed
                if (i == other.emphasis.getPrimary()
                        || other.emphasis.getSecondary() != null && other.emphasis.getSecondary().contains(i)) {
                    continue;
                }

                ArrayList<Syllable> unstressed = other.getRhymeList(i);

                /* primary to unstressed */
                if (rhymeMatch(this.primaryRhyme, unstressed)) {
                    return true;
                }

                /* secondary to unstressed */
                if (this.secondaryRhymes != null) {
                    for (ArrayList<Syllable> secondary : this.secondaryRhymes) {
                        if (rhymeMatch(secondary, unstressed)) {
                            return true;
                        }
                    }
                }
            }

            /* unstressed to primary or secondary */
            for (int i = this.syllables.size() - 1; i >= 0; i--) {
                // check that syllable in this is unstressed
                if (i == this.emphasis.getPrimary()
                        || this.emphasis.getSecondary() != null && this.emphasis.getSecondary().contains(i)) {
                    continue;
                }
                ArrayList<Syllable> unstressed = this.getRhymeList(i);

                /* unstressed to primary */
                if (rhymeMatch(other.primaryRhyme, unstressed)) {
                    return true;
                }

                /* unstressed to secondary */
                if (other.secondaryRhymes != null) {
                    for (ArrayList<Syllable> secondary : other.secondaryRhymes) {
                        if (rhymeMatch(secondary, unstressed)) {
                            return true;
                        }
                    }
                }
            }

            /* secondary to secondary */
            if (this.secondaryRhymes != null && other.secondaryRhymes != null) {
                for (ArrayList<Syllable> secondary : this.secondaryRhymes) {
                    for (ArrayList<Syllable> otherSecondary : other.secondaryRhymes)
                        if (rhymeMatch(secondary, otherSecondary)) {
                            return true;
                        }
                }
            }

            return false;
        }

        private boolean weakRhymesWith(SubPronunciation other) {
            /* unstressed to unstressed */
            for (int i = this.syllables.size() - 1; i >= 0; i--) {
                // check that syllable is unstressed
                if (i == this.emphasis.getPrimary()
                        || this.emphasis.getSecondary() != null && this.emphasis.getSecondary().contains(i)) {
                    continue;
                }
                ArrayList<Syllable> thisUnstressed = this.getRhymeList(i);

                for (int j = other.syllables.size() - 1; j >= 0; j--) {
                    // check that syllable is unstressed
                    if (j == other.emphasis.getPrimary()
                            || other.emphasis.getSecondary() != null && other.emphasis.getSecondary().contains(j)) {
                        continue;
                    }
                    ArrayList<Syllable> otherUnstressed = other.getRhymeList(j);

                    if (rhymeMatch(thisUnstressed, otherUnstressed)) {
                        return true;
                    }
                }
            }

            return false;
        }

        private boolean forcedRhymesWith(SubPronunciation other) {
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

    private EnumMap<PartOfSpeech, SubPronunciation> subPronunciations = new EnumMap<>(PartOfSpeech.class);
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

    /**
     * Used to add incomplete IPA data based on the WordsAPI "rhyme" attribute. The
     * rhyme strings are of form "-aʊtʃ", so the leading hyphen is stripped before
     * treating the IPA as normal.
     * 
     * @param plaintext    used for debugging messages
     * @param rhymesObject from WordsAPI request
     */
    public void setRhyme(String plaintext, JSONObject rhymesObject) {
        JSONObject filteredRhymesObject = new JSONObject();
        boolean empty = true;

        for (PartOfSpeech pos : PartOfSpeech.values()) {
            if (rhymesObject.has(pos.getApiString()) && subPronunciations.get(pos) == null) {
                String original = rhymesObject.getString(pos.getApiString());
                filteredRhymesObject.put(pos.getApiString(), original.replaceFirst("-", "'"));
                empty = false;
            }
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
     * 
     * @param pronunciationObject JSONObject of the form {<part-of-speech>:<ipa>}
     *                            e.g.
     *                            {"noun":"'kɑntrækt", "verb":"kɑn'trækt"}
     *                            {"all":"'fridəm"}
     * 
     */
    public void setIPA(String plaintext, JSONObject pronunciationObject) {
        Pair<ArrayList<Syllable>, Emphasis> syllablesAndEmphasis = null;
        SubPronunciation sub = new SubPronunciation();

        for (PartOfSpeech pos : PartOfSpeech.values()) {
            if (pronunciationObject.has(pos.getApiString())) {
                sub.ipa = pronunciationObject.getString(pos.getApiString());
                syllablesAndEmphasis = IPAHandler.getSyllables(sub.ipa);
                sub.syllables = syllablesAndEmphasis.one();
                sub.emphasis = syllablesAndEmphasis.two();
                sub.populateRhymes();
                subPronunciations.put(pos, sub);
            }
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

    public SubPronunciation getSubPronunciation(String plaintext, PartOfSpeech pos) {
        SubPronunciation requested = null;
        if (subPronunciations.get(pos) != null) {
            requested = subPronunciations.get(pos);
        } else {
            requested = all;
        }

        if (requested == null) {
            LOG.writePersistentLog(String.format("No pronunciation of \"%s\" could be found for \"%s\"",
                    plaintext, pos));
        }

        return requested;
    }

    public String toString() {
        String divider = ", ";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        stringBuilder.append("plaintext-syllables: " + plaintextSyllables.toString());

        for (PartOfSpeech pos : PartOfSpeech.values()) {
            if (subPronunciations.containsKey(pos)) {
                stringBuilder.append(divider);
                stringBuilder.append("noun: " + subPronunciations.get(pos).toString());
            }
        }

        if (all != null) {
            stringBuilder.append(", ");
            stringBuilder.append("all: " + all.toString());
        }

        stringBuilder.append("}");
        return stringBuilder.toString();
    }

}
