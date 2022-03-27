package words;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

import org.json.JSONObject;
import utils.Pair;
import utils.ParameterWrappers.FilterParameters.RhymeType;

import static config.Configuration.LOG;

/**
 * This class contains pronunciation data divided by part of speech. Has wrapper
 * functions for rhyme
 * recognition.
 * 
 * @author 190021081
 */
public class Pronunciation {

    /**
     * Contains pronunciation data defined as collections of {@link words.Syllables}
     * objects.
     * Also handles rhyme recognition by comparing said collections (and subsets
     * thereof).
     * 
     * @author 190021081
     */
    public static class SubPronunciation {
        String ipa;
        ArrayList<Syllable> syllables;
        ArrayList<Syllable> primaryRhymeSubstring;
        ArrayList<ArrayList<Syllable>> secondaryRhymeSubstrings;
        Emphasis emphasis;

        public SubPronunciation(String ipa, ArrayList<Syllable> syllables, Emphasis emphasis) {
            this.ipa = ipa;
            this.syllables = syllables;
            this.emphasis = emphasis;
            this.populateRhymes();
        }

        /**
         * Creates a rhyme-matching list of syllables.
         * 
         * @param startingEmphasis the index of the first syllable to rhyme from.
         * @return the syllables from @param startingEmphasis to the end of the word,
         *         inclusive.
         */
        private ArrayList<Syllable> getRhymeSubstring(int startingEmphasis) {
            ArrayList<Syllable> rhyme = new ArrayList<>();
            Syllable source = syllables.get(startingEmphasis);
            Syllable start = new Syllable("", source.getNucleus(), source.getCoda());
            rhyme.add(start);
            for (int i = startingEmphasis + 1; i < syllables.size(); i++) {
                rhyme.add(syllables.get(i));
            }
            return rhyme;
        }

        /*
         * Note that this does not create copies of syllables aside from the stressed
         * ones, so changing the syllables later could lead to unexpected behaviour. At
         * present, no such changes occur.
         */
        private void populateRhymes() {
            /* primary rhyme */
            this.primaryRhymeSubstring = getRhymeSubstring(this.emphasis.getPrimary());

            /* secondary rhymes */
            if (this.emphasis.getSecondary() != null) {
                for (Integer secondaryEmphasis : this.emphasis.getSecondary()) {
                    this.secondaryRhymeSubstrings = utils.NullListOperations.addToNull(this.secondaryRhymeSubstrings,
                            getRhymeSubstring(secondaryEmphasis));
                }
            }

        }

        private static boolean rhymeSubstringMatch(ArrayList<Syllable> rhyme1, ArrayList<Syllable> rhyme2) {
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

        /**
         * Wrapper function for different types of rhyme recognition.
         * 
         * @param rhyme the type of rhyme to match.
         * @param other to match against.
         * @return true iff the two subpronunciations rhyme.
         */
        public boolean matchesWith(RhymeType rhyme, SubPronunciation other) {
            switch (rhyme) {
                case PERFECT_RHYME:
                    return perfectRhymesWith(other);
                case SYLLABIC_RHYME:
                    return syllablicRhymesWith(other);
                case FORCED_RHYME:
                    return forcedRhymesWith(other);
                case IMPERFECT_RHYME:
                    return imperfectRhymesWith(other);
                case WEAK_RHYME:
                    return weakRhymesWith(other);
                default:
                    LOG.writeTempLog("Attempted unimplemented RhymeType: " + rhyme.name());
                    return false;
            }
        }

        private boolean perfectRhymesWith(SubPronunciation other) {
            /* primary to primary */
            if (rhymeSubstringMatch(this.primaryRhymeSubstring, other.primaryRhymeSubstring)) {
                return true;
            }

            /* primary to secondary */
            if (other.secondaryRhymeSubstrings != null) {
                for (ArrayList<Syllable> secondary : other.secondaryRhymeSubstrings) {
                    if (rhymeSubstringMatch(this.primaryRhymeSubstring, secondary)) {
                        return true;
                    }
                }
            }

            /* secondary to primary */
            if (this.secondaryRhymeSubstrings != null) {
                for (ArrayList<Syllable> secondary : this.secondaryRhymeSubstrings) {
                    if (rhymeSubstringMatch(secondary, other.primaryRhymeSubstring)) {
                        return true;
                    }
                }
            }

            return false;
        }

        private boolean syllablicRhymesWith(SubPronunciation other) {
            /* last to last; includes onset */
            ArrayList<Syllable> thisLastSyllable = new ArrayList<>(
                    this.syllables.subList(syllables.size() - 1, syllables.size()));
            ArrayList<Syllable> otherLastSyllable = new ArrayList<>(
                    other.syllables.subList(other.syllables.size() - 1, other.syllables.size()));
            return rhymeSubstringMatch(thisLastSyllable, otherLastSyllable);
        }

        private boolean imperfectRhymesWith(SubPronunciation other) {
            /* primary or secondary to unstressed */
            for (int i = other.syllables.size() - 1; i >= 0; i--) {
                // check that syllable in other is not stressed
                if (i == other.emphasis.getPrimary()
                        || other.emphasis.getSecondary() != null && other.emphasis.getSecondary().contains(i)) {
                    continue;
                }

                ArrayList<Syllable> unstressed = other.getRhymeSubstring(i);

                /* primary to unstressed */
                if (rhymeSubstringMatch(this.primaryRhymeSubstring, unstressed)) {
                    return true;
                }

                /* secondary to unstressed */
                if (this.secondaryRhymeSubstrings != null) {
                    for (ArrayList<Syllable> secondary : this.secondaryRhymeSubstrings) {
                        if (rhymeSubstringMatch(secondary, unstressed)) {
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
                ArrayList<Syllable> unstressed = this.getRhymeSubstring(i);

                /* unstressed to primary */
                if (rhymeSubstringMatch(other.primaryRhymeSubstring, unstressed)) {
                    return true;
                }

                /* unstressed to secondary */
                if (other.secondaryRhymeSubstrings != null) {
                    for (ArrayList<Syllable> secondary : other.secondaryRhymeSubstrings) {
                        if (rhymeSubstringMatch(secondary, unstressed)) {
                            return true;
                        }
                    }
                }
            }

            /* secondary to secondary */
            if (this.secondaryRhymeSubstrings != null && other.secondaryRhymeSubstrings != null) {
                for (ArrayList<Syllable> secondary : this.secondaryRhymeSubstrings) {
                    for (ArrayList<Syllable> otherSecondary : other.secondaryRhymeSubstrings)
                        if (rhymeSubstringMatch(secondary, otherSecondary)) {
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
                ArrayList<Syllable> thisUnstressed = this.getRhymeSubstring(i);

                for (int j = other.syllables.size() - 1; j >= 0; j--) {
                    // check that syllable is unstressed
                    if (j == other.emphasis.getPrimary()
                            || other.emphasis.getSecondary() != null && other.emphasis.getSecondary().contains(j)) {
                        continue;
                    }
                    ArrayList<Syllable> otherUnstressed = other.getRhymeSubstring(j);

                    if (rhymeSubstringMatch(thisUnstressed, otherUnstressed)) {
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

        /**
         * {@inheritDoc}
         */
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("{");
            stringBuilder.append("ipa: " + ipa);
            stringBuilder.append(", ");
            stringBuilder.append("syllables: " + syllables.toString());
            stringBuilder.append(", ");
            stringBuilder.append("emphasis: " + emphasis.toString());
            stringBuilder.append(", ");
            stringBuilder.append("primary rhyme: " + primaryRhymeSubstring.toString());
            if (this.secondaryRhymeSubstrings != null) {
                stringBuilder.append(", ");
                stringBuilder.append("secondary rhymes: " + secondaryRhymeSubstrings.toString());
            }
            stringBuilder.append("}");
            return stringBuilder.toString();
        }
    }

    private EnumMap<PartOfSpeech, SubPronunciation> subPronunciations = new EnumMap<>(PartOfSpeech.class);
    private SubPronunciation all;

    /**
     * Attempts to determine the syllable count for a word.
     * 
     * @param pos the SubPronunciation to query.If null, then just
     *            return the first value found.
     * @return 0 if no syllable count could be determined, otherwise the number of
     *         syllables in the requested SubPronunciation.
     */
    public int getSyllableCount(PartOfSpeech pos) {
        if (pos == null) {
            for (Map.Entry<PartOfSpeech, SubPronunciation> entry : subPronunciations.entrySet()) {
                SubPronunciation subP = entry.getValue();
                if (subP != null)
                    return subP.syllables.size();
            }
        }
        SubPronunciation subP = this.getSubPronunciation(null, pos);
        if (subP == null) {
            return 0;
        } else {
            return subP.syllables.size();
        }
    }

    /**
     * Used to add incomplete IPA data based on the WordsAPI "rhyme" attribute (as a
     * JSONObject). The rhyme strings are of form "-aʊtʃ", so the leading hyphen is
     * stripped before treating the IPA as normal.
     * 
     * @param plaintext    used for debugging messages.
     * @param rhymesObject from WordsAPI request.
     * @return true if a pronunciation could be derived.
     */
    public boolean setRhyme(String plaintext, JSONObject rhymesObject) {
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
            return false;
        } else {
            return this.setIPA(plaintext, filteredRhymesObject);
        }
    }

    /**
     * Used to add incomplete IPA data based on the WordsAPI "rhyme" attribute (as
     * string). The rhyme strings are of form "-aʊtʃ", so the leading hyphen is
     * stripped before treating the IPA as normal.
     * 
     * @param plaintext    used for debugging messages.
     * @param rhymesObject from WordsAPI request.
     * @return true if a pronunciation could be derived.
     */
    public boolean setRhyme(String plaintext, String rhymesString) {
        return this.setIPA(plaintext, rhymesString.replaceFirst("-", "'"));
    }

    /**
     * Attempts to generate the subpronunciations from a JSONObject, based on part
     * of speech.
     * 
     * @param plaintext           the superword plaintext, used for debugging.
     * @param pronunciationObject JSONObject of the form {<part-of-speech>:<ipa>}
     *                            e.g.
     *                            {"noun":"'kɑntrækt", "verb":"kɑn'trækt"}
     *                            {"all":"'fridəm"}
     * @return true if at least one subpronunciation could be derived, otherwise
     *         false.
     */
    public boolean setIPA(String plaintext, JSONObject pronunciationObject) {
        Pair<ArrayList<Syllable>, Emphasis> syllablesAndEmphasis = null;
        SubPronunciation sub;

        for (PartOfSpeech pos : PartOfSpeech.values()) {
            if (pronunciationObject.has(pos.getApiString())) {
                String ipa = pronunciationObject.getString(pos.getApiString());
                syllablesAndEmphasis = IPAHandler.getSyllables(ipa);
                if (!syllablesAndEmphasis.one().isEmpty()) {
                    sub = new SubPronunciation(ipa, syllablesAndEmphasis.one(), syllablesAndEmphasis.two());
                    subPronunciations.put(pos, sub);
                } // else IPA was unparsable
            }
        }

        if (pronunciationObject.has("all")) {
            // "present" is a noun, verb and adjective, with pronunications for "noun",
            // "verb" and "all": "all" is useful even when other fields are filled
            String ipa = pronunciationObject.getString("all");
            syllablesAndEmphasis = IPAHandler.getSyllables(ipa);
            if (!syllablesAndEmphasis.one().isEmpty()) {
                this.all = new SubPronunciation(ipa, syllablesAndEmphasis.one(), syllablesAndEmphasis.two());
            } // else IPA was unparsable
        }

        if (syllablesAndEmphasis == null) {
            /* i.e. pronunciation object had no (recognised) keys */
            LOG.writePersistentLog(String.format("Pronunciation of \"%s\" had no recognised keys: %s", plaintext,
                    pronunciationObject.toString()));
        }

        return !(subPronunciations.isEmpty() && this.all == null);
    }

    public boolean setIPA(String plaintext, String allIpa) {
        if (allIpa.equals("")) {
            LOG.writePersistentLog(String.format("Pronunciation of \"%s\" was an empty string", plaintext));
            return false;
        } else {
            Pair<ArrayList<Syllable>, Emphasis> syllablesAndEmphasis = IPAHandler.getSyllables(allIpa);
            if (!syllablesAndEmphasis.one().isEmpty()) {
                this.all = new SubPronunciation(allIpa, syllablesAndEmphasis.one(), syllablesAndEmphasis.two());
                return true;
            } else {
                return false; // IPA was unparsable
            }
        }
    }

    /**
     * Gets the pronunciation for a word based on the part of speech. Defaults to
     * the generic pronunciation (all) if no specific entry found.
     * 
     * @param plaintext for logging.
     * @param pos       the desired part of speech.
     * @return a SubPronunciation object, or null if none could be found for the
     *         given part of speech.
     */
    public SubPronunciation getSubPronunciation(String plaintext, PartOfSpeech pos) {
        SubPronunciation requested = null;
        if (pos != null && subPronunciations.get(pos) != null) {
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

    /**
     * {@inheritDoc}
     */
    public String toString() {
        String divider = ", ";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        boolean hasPoS = false;

        for (PartOfSpeech pos : PartOfSpeech.values()) {
            if (subPronunciations.containsKey(pos)) {
                hasPoS = true;
                stringBuilder.append(divider);
                stringBuilder.append(pos.name() + ": " + subPronunciations.get(pos).toString());
            }
        }

        if (all != null) {
            if (hasPoS)
                stringBuilder.append(divider);
            stringBuilder.append("all: " + all.toString());
        }

        stringBuilder.append("}");
        return stringBuilder.toString();
    }

}
