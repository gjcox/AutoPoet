package words;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import utils.ParameterWrappers.FilterParameters;
import utils.ParameterWrappers.SuggestionPoolParameters;
import utils.ParameterWrappers.FilterParameters.RhymeType;
import utils.ParameterWrappers.SuggestionPoolParameters.SuggestionPool;
import words.Pronunciation.SubPronunciation;
import words_api.WordsAPI;

import static utils.NullListOperations.addToNull;
import static utils.NullListOperations.addAllToNull;
import static utils.NullListOperations.combineLists;
import static utils.NullListOperations.combineListsVarags;
import static utils.NullListOperations.combineListsPrioritiseDuplicates;
import static config.Configuration.LOG;

/**
 * This class encodes a super word, defined as a plaintext spelling,
 * pronunciation data, and collection of {@link words.SubWord}s. Keeps a cache
 * of created SuperWords to prevent duplicate WordsAPI queries.
 */
public class SuperWord extends Token {

    private static HashMap<String, SuperWord> cachePopulated = new HashMap<>();
    private static HashMap<String, SuperWord> cachePlaceholder = new HashMap<>();

    private static Set<String> knownFields = new HashSet<>(
            Arrays.asList("word", "results", "syllables", "pronunciation", "frequency", "rhymes", "success",
                    "message"));

    // true iff built from a WordsAPI query
    private boolean populated = false;
    // constructed from IPA (if available)
    private Pronunciation pronunciation;
    // a fallback for syllable count if no IPA
    private ArrayList<String> plaintextSyllables = new ArrayList<>();
    // SubWords, grouped by part of speech
    private EnumMap<PartOfSpeech, ArrayList<SubWord>> subWords = new EnumMap<>(PartOfSpeech.class);

    /**
     * Attempts to get a cached word, before returning a new placeholder.
     * 
     * @param plaintext
     * @return
     */
    public static SuperWord getSuperWord(String plaintext) {
        String cleanedPlaintext = Normalizer.normalize(plaintext, Form.NFD).replaceAll("\\p{M}", "");

        if (cachePopulated.containsKey(cleanedPlaintext)) {
            return cachePopulated.get(cleanedPlaintext);
        }
        if (cachePlaceholder.containsKey(cleanedPlaintext)) {
            return cachePlaceholder.get(cleanedPlaintext);
        }
        return new SuperWord(cleanedPlaintext);
    }

    /**
     * Sends a request for this word to WordsAPI, and attempts to populate this
     * SuperWord's fields from the response. Updates the populated SuperWord cache.
     */
    public void populate() {
        if (populated) {
            LOG.writeTempLog(String.format("Attempted to repopulate \"%s\": %s", plaintext, this.toString()));
            return;
        }

        JSONObject word = WordsAPI.getWord(plaintext);

        Set<String> unrecognisedFields = new HashSet<>(word.keySet());
        unrecognisedFields.removeAll(knownFields);
        if (!unrecognisedFields.isEmpty()) {
            LOG.writePersistentLog(
                    String.format("Unrecognised field(s) for superword \"%s\": \"%s\"",
                            plaintext, unrecognisedFields.toString()));
        }

        if (word.has("word") && !word.getString("word").equals(plaintext)) {
            LOG.writePersistentLog(String.format("WordsAPI responded with word \"%s\" when requesting \"%s\".",
                    word.getString("word"), plaintext));
        }

        if (word.has("syllables")) {
            JSONObject syllablesObject = word.getJSONObject("syllables");
            this.setSyllables(this.plaintext, syllablesObject);
        } else {
            LOG.writePersistentLog(String.format("Syllables of \"%s\" was missing", plaintext));
        }

        if (word.has("pronunciation")) {
            this.pronunciation = new Pronunciation();
            try {
                JSONObject pronunciationObject = word.getJSONObject("pronunciation");
                if (!this.pronunciation.setIPA(this.plaintext, pronunciationObject)) {
                    // failed to derive subpronunciation from object
                    this.pronunciation = null;
                }
            } catch (JSONException e) {
                LOG.writePersistentLog(String.format("Pronunciation of \"%s\" was not a JSONObject: \"%s\"", plaintext,
                        word.get("pronunciation").toString()));
                if (!this.pronunciation.setIPA(this.plaintext, word.getString("pronunciation"))) {
                    // failed to derive subpronunciation from string
                    this.pronunciation = null;
                }
            }
        } else {
            LOG.writePersistentLog(String.format("Pronunciation of \"%s\" was missing", plaintext));
        }

        if (word.has("rhymes")) {
            LOG.writePersistentLog(
                    String.format("Rhymes of \"%s\" was present: %s", plaintext, word.get("rhymes").toString()));
            if (pronunciation == null) {
                this.pronunciation = new Pronunciation();
                try {
                    JSONObject rhymeObject = word.getJSONObject("rhymes");
                    if (!this.pronunciation.setRhyme(this.plaintext, rhymeObject)) {
                        // failed to derive subpronunciation from object
                        this.pronunciation = null;
                    }
                } catch (JSONException e) {
                    LOG.writePersistentLog(String.format("Rhymes of \"%s\" was not a JSONObject: \"%s\"", plaintext,
                            word.get("Rhymes").toString()));
                    if (!this.pronunciation.setRhyme(this.plaintext, word.getString("Rhymes"))) {
                        // failed to derive subpronunciation from string
                        this.pronunciation = null;
                    }
                }
            }
        }

        if (word.has("results")) {
            JSONArray resultsArray = word.getJSONArray("results");
            this.setSubWords(resultsArray);
        } else {
            subWords.put(PartOfSpeech.UNKNOWN, null); // so that matchesWith has something to iterate over
            LOG.writePersistentLog(String.format("Results of \"%s\" was missing", plaintext));
        }

        populated = true;
        cachePopulated.put(this.plaintext, this);
        LOG.writeTempLog(String.format("Populated and cached \"%s\": %s", plaintext, this.toString()));
    }

    /**
     * For creating placeholders.
     * 
     * @param plaintext the string of the SuperWord.
     */
    private SuperWord(String plaintext) {
        super(plaintext);
        cachePlaceholder.put(this.plaintext, this);
    }

    /**
     * For getting lots of SuperWords from the cache.
     * 
     * @param plaintexts generically typed, but must have String elements.
     * @return the SuperWords returned.
     */
    public static ArrayList<SuperWord> batchPlaceHolders(List<Object> plaintexts) {
        ArrayList<SuperWord> list = new ArrayList<>();
        for (Object plaintext : plaintexts) {
            list.add(getSuperWord((String) plaintext));
        }
        return list;
    }

    // (internal) setters

    @SuppressWarnings("unchecked")
    private void setSubWords(JSONArray resultsArray) {
        List<Object> results = resultsArray.toList();

        if (results.isEmpty()) {
            LOG.writePersistentLog(String.format("Results of \"%s\" was an empty array", plaintext));
        }

        for (Object result : results) {
            try {
                SubWord word = new SubWord(this, (Map<String, Object>) result);
                PartOfSpeech pos = word.getPartOfSpeech();
                subWords.compute(pos,
                        (PartOfSpeech key, ArrayList<SubWord> subWordList) -> addToNull(subWordList, word));
            } catch (ClassCastException e) {
                LOG.writePersistentLog(
                        String.format("Results of \"%s\" contained an invalid entry: \"%s\"", plaintext, result));
            }
        }
    }

    /*
     * syllablesObject should be a JSONObject of the form {"count":x, "list":[]} #
     */
    private void setSyllables(String plaintext, JSONObject syllablesObject) {
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

    // getters

    /**
     * Gets the collection of {@link words.SubWord}s for a given part of speech.
     * 
     * If @param inclusiveUnknown is true and @param pos is
     * {@link words.PartOfSpeech.UNKNOWN}, then all {@link words.SubWord}s are
     * returned. If @param inclusiveUnknown is true and @param pos is not
     * {@link words.PartOfSpeech.UNKNOWN}, then the {@link words.SubWord}s with
     * unknown part of speech are added to the resilts.
     * 
     * @param pos              the desired part of speech.
     * @param inclusiveUnknown whether or not {@link words.SubWord}s with
     *                         unknown part of speech should be included.
     * @return null if the SuperWord has no {@link words.SubWord}s of that type.
     */
    public ArrayList<SubWord> getSubWords(PartOfSpeech pos, boolean inclusiveUnknown) {
        if (!this.populated) {
            this.populate();
        }
        if (pos.equals(PartOfSpeech.UNKNOWN) && inclusiveUnknown) {
            // if inclusively querying subwords with unknown PoS, return all subwords
            return combineLists(subWords.values());
        } else if (inclusiveUnknown) {
            // combine subwords with unknown PoS, and desired PoS
            return combineListsVarags(subWords.get(pos), subWords.get(PartOfSpeech.UNKNOWN));
        } else {
            // just return the subwords with desired PoS
            return subWords.get(pos);
        }
    }

    /**
     * Gets the pronunciation data for this SuperWord, populating it first if
     * needed.
     * 
     * @return a {@link words.Pronunciation} object.
     */
    public Pronunciation getPronunciation() {
        if (!this.populated) {
            this.populate();
        }
        return this.pronunciation;
    }

    /**
     * Attempts to get the pronunciation data for this SuperWord specific to a part
     * of speech, populating it first if needed.
     * 
     * @return a {@link words.Pronunciation.SubPronunciation} object.
     */
    public Pronunciation.SubPronunciation getSubPronunciation(PartOfSpeech partOfSpeech) {
        if (!this.populated) {
            this.populate();
        }
        if (this.pronunciation == null) {
            return null;
        }
        return this.pronunciation.getSubPronunciation(plaintext, partOfSpeech);
    }

    /**
     * Returns the number of syllables in the word, populating it first if
     * necessary.
     * 
     * Attempts to use part-of-speech specific IPA data first, falling back to
     * plaintext syllable data.
     * 
     * @param pos the part of speech of the
     *            {@link words.Pronunciation.SubPronunciation} to attempt to query.
     * @return 0 if no count could be determined.
     */
    public int getSyllableCount(PartOfSpeech pos) {
        if (!this.populated) {
            this.populate();
        }
        if (this.pronunciation == null) {
            return this.plaintextSyllables.size(); // fallback in case no IPA pronunciation data
        } else {
            return this.pronunciation.getSyllableCount(pos);
        }
    }

    /**
     * Returns an array of plaintext syllables, populating itself first if
     * necessary. Not used in the GUI.
     * 
     * @return the plaintext syllables provided by WordsAPI.
     */
    public ArrayList<String> getPlaintextSyllables() {
        if (!this.populated) {
            this.populate();
        }
        return plaintextSyllables;
    }

    /**
     * Determines if this superword's {@link words.SubWord}s of the desired part of
     * speech have a suggestion pool, populating it first if necessary.
     * 
     * Used to disable/enable checkboxes in the GUI.
     * 
     * @param pool the suggestion pool to check for.
     * @param pos  the part of speech to get {@link words.SubWord}s from.
     * @return true if at least one {@link words.SubWord} of the desired part of
     *         speech has the suggestion pool.
     */
    public boolean validPool(SuggestionPool pool, PartOfSpeech pos) {
        if (!this.populated) {
            this.populate();
        }
        switch (pool) {
            case COMMONLY_TYPED:
                // if not a sub-type, no super-types to query
                return getSuggestionPool(SuggestionPool.TYPE_OF, pos, false) != null;
            case COMMON_CATEGORIES:
                // if not in a category, cannot find others of same category
                return getSuggestionPool(SuggestionPool.IN_CATEGORY, pos, false) != null;
            case HAS_PARTS:
            case PART_OF:
            case SIMILAR_TO:
            case SYNONYMS:
            case HAS_TYPES:
            case TYPE_OF:
            default:
                return getSuggestionPool(pool, pos, false) != null;
        }

    }

    /**
     * Returns the unfiltered contents of a suggestion pool.
     * 
     * @param pool             the pool to get.
     * @param pos              the part of speech to get {@link words.SubWord}s
     *                         from.
     * @param inclusiveUnknown whether or not to include {@link words.SubWord}s with
     *                         an unknown part of speech.
     * @return a collection of suggestions.
     */
    public ArrayList<SuperWord> getSuggestionPool(SuggestionPool pool, PartOfSpeech pos, boolean inclusiveUnknown) {
        if (!populated)
            this.populate();

        ArrayList<SuperWord> suggestionPool = null;
        ArrayList<SubWord> subWordsList = getSubWords(pos, inclusiveUnknown);
        if (subWordsList != null) {
            for (SubWord subWord : subWordsList) {
                suggestionPool = addAllToNull(suggestionPool, subWord.getSuggestionPool(pool));
            }
        }
        return suggestionPool;
    }

    /*
     * Collates multiple suggestion pools into one, ordering elements in descending
     * order of the number of times they occured in the unfiltered pools.
     * 
     * @param thisPos the part of speech to use.
     * 
     * @param params the suggestions pools to use, and whether or not to be
     * inclusive of unknowns.
     * 
     * @return a collection of unique suggestions.
     */
    private ArrayList<SuperWord> getAggregatedSuggestions(PartOfSpeech thisPos, SuggestionPoolParameters params) {
        ArrayList<ArrayList<SuperWord>> suggestions = new ArrayList<>();
        for (SuggestionPool pool : SuggestionPool.values()) {
            if (params.includes(pool)) {
                if (!params.hasInclusiveUnknown()) {
                    // only add the matching PoS suggestions
                    suggestions.add(this.getSuggestionPool(pool, thisPos, params.hasInclusiveUnknown()));
                } else if (!thisPos.equals(PartOfSpeech.UNKNOWN)) {
                    // add the matching PoS suggestions
                    suggestions.add(this.getSuggestionPool(pool, thisPos, params.hasInclusiveUnknown()));
                    // ... and the unknown PoS suggestions
                    suggestions.add(this.getSuggestionPool(pool, PartOfSpeech.UNKNOWN, params.hasInclusiveUnknown()));
                } else {
                    // add all suggestions for all PoSs
                    for (PartOfSpeech pos : PartOfSpeech.values())
                        suggestions.add(this.getSuggestionPool(pool, pos, params.hasInclusiveUnknown()));
                }
            }
        }

        ArrayList<SuperWord> combined = combineListsPrioritiseDuplicates(suggestions);
        if (combined != null) {
            combined.remove(this); // prevent suggesting the original word
        }

        LOG.writeTempLog(String.format("Combined suggestions for \"%s\" (%s) including %s: %s", plaintext, thisPos,
                params.toString(), combined));
        return combined;
    }

    /*
     * Removes suggestions that fail to meet rhyme and syllable count requirements.
     * 
     * @param suggestions the unfiltered suggestions.
     * 
     * @param thisPos the part of speech to use for suggestions when making
     * rhyme comparisons.
     * 
     * @param params the types of rhyme to match, the words to match against and
     * their parts of speech, and whether or not to filter by syllable count.
     * 
     * @return the filtered set of suggestions.
     */
    private ArrayList<SuperWord> filterSuggestions(ArrayList<SuperWord> suggestions, PartOfSpeech thisPos,
            FilterParameters params) {

        if (suggestions == null) {
            return new ArrayList<>();
        }

        ArrayList<SuperWord> filtered = new ArrayList<>(suggestions);

        for (SuperWord suggestion : suggestions) {
            boolean shouldRhyme = false;
            boolean rhymes = false;
            boolean syllableCountMatch = true;

            // check that at least one rhyme subtype passes
            // ... for at least one corresponding matchWith
            for (RhymeType filter : RhymeType.values()) {
                List<SuperWord> matchWithList;
                if ((matchWithList = params.getMatchWith(filter)) != null) {
                    shouldRhyme = true;
                    for (SuperWord matchWith : matchWithList) {
                        if (suggestion.rhymesWithWrapper(filter, matchWith, thisPos, params.getMatchPoS())) {
                            rhymes = true;
                            break;
                        }
                    }
                }
                if (rhymes)
                    break;
            }

            // check that syllable count matches (if required)
            if (params.syllableCountFilter() && getSyllableCount(thisPos) != suggestion.getSyllableCount(null)) {
                syllableCountMatch = false;
            }

            if (shouldRhyme && !rhymes || !syllableCountMatch)
                filtered.remove(suggestion);
        }

        LOG.writeTempLog(String.format("Filtered suggestions for \"%s\" (%s) including %s: %s", plaintext, thisPos,
                params.toString(), filtered));
        return filtered;
    }

    /**
     * Attempts to generate a collection of suggestions to substitute this word,
     * based on the passed parameters.
     * 
     * @param thisPos          the part of speech of this word, which the
     *                         suggestions should match; also used for rhmye
     *                         recognition.
     * @param suggestionParams which suggestion pools (e.g. synonyms, parts of) to
     *                         draw from, and if SubWords with unknown PoS should be
     *                         included.
     * @param filterParams     the type(s) of desired rhyme and the word(s) to rhyme
     *                         with, and if syllable count should be maintained.
     * @return a list of filtered suggestions, which can be empty.
     */
    public ArrayList<SuperWord> getFilteredSuggestions(PartOfSpeech thisPos, SuggestionPoolParameters suggestionParams,
            FilterParameters filterParams) {
        if (!this.populated) {
            this.populate();
        }
        ArrayList<SuperWord> unfiltered = getAggregatedSuggestions(thisPos, suggestionParams);
        return filterSuggestions(unfiltered, thisPos, filterParams);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return String.format("%s: {populated: %b}", this.plaintext, this.populated);
    }

    /**
     * Used for debugging and logging.
     * 
     * @return a(n honestly rather messy) string representation of the word and its
     *         subwords.
     */
    public String toFullString() {
        String divider = "\n\t";

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(plaintext + ": {");
        if (populated) {
            stringBuilder.append(divider);
        }
        stringBuilder.append("populated: " + populated);
        stringBuilder.append(divider);
        stringBuilder.append("plaintext-syllables: " + plaintextSyllables.toString());
        if (pronunciation != null) {
            stringBuilder.append(divider);
            stringBuilder.append("pronunciation: " + pronunciation.toString());
        }
        for (PartOfSpeech pos : PartOfSpeech.values()) {
            if (subWords.get(pos) != null) {
                stringBuilder.append(divider);
                stringBuilder.append(String.format("%s: %d", pos.getApiString(), subWords.get(pos).size()));
            }
        }
        if (populated) {
            stringBuilder.append("\n");
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    /**
     * Used for debugging and logging.
     * 
     * @return a String formatted for debugging that includes all datafields of
     *         subwords.
     */
    public String subWordsString() {
        String divider = "\n//////// ";

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(plaintext + ": {");
        for (PartOfSpeech pos : PartOfSpeech.values()) {
            if (subWords.get(pos) != null) {
                stringBuilder.append(divider);
                stringBuilder.append(String.format("%s\n%s", pos.getApiString(), subWords.get(pos).toString()));
            }
        }
        if (populated) {
            stringBuilder.append("\n");
        }
        stringBuilder.append("}");
        return stringBuilder.toString();

    }

    private static boolean matchesWith(RhymeType rhmyeType,
            String plaintext1, PartOfSpeech pos1, SubPronunciation subPronunciation1,
            String plaintext2, PartOfSpeech pos2, SubPronunciation subPronunciation2) {
        if (subPronunciation1 == null) {
            LOG.writePersistentLog(
                    String.format("\"%s\" (%s) did not have a pronunciation for \"%s\" (%s) to match against",
                            plaintext1, pos1, plaintext2, pos2));
            return false;
        }
        if (subPronunciation2 == null) {
            LOG.writePersistentLog(
                    String.format("\"%s\" (%s) did not have a pronunciation for \"%s\" (%s) to match against",
                            plaintext2, pos2, plaintext1, pos1));
            return false;
        }
        return subPronunciation1.matchesWith(rhmyeType, subPronunciation2);
    }

    /**
     * Iterates over the parts of speech of both words (worst case is full cross
     * product evaluation) and returns true if any part of speech pairing produces a
     * rhyme.
     * 
     * @param filter the type of matching to perform (e.g. perfect rhyme).
     * @param other  the word to match against.
     * @return true if the two words match for any part of speech pair.
     */
    public boolean matchesWithWrapper(RhymeType filter, SuperWord other) {
        if (!this.populated)
            this.populate();
        if (!other.populated)
            other.populate();

        if (this.pronunciation == null || other.pronunciation == null) {
            return false;
        }

        for (PartOfSpeech pos1 : subWords.keySet()) {
            SubPronunciation subPronunciation1 = this.getSubPronunciation(pos1);
            for (PartOfSpeech pos2 : other.subWords.keySet()) {
                SubPronunciation subPronunciation2 = other.getSubPronunciation(pos2);
                if (matchesWith(filter, this.plaintext, pos1, subPronunciation1, other.plaintext, pos2,
                        subPronunciation2)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Uses specific parts of speech if arguments are not null. For null
     * PartOfSpeech arguments, iterates over all parts of speech of corresponding
     * word.
     * 
     * @param filter the type of matching to perform (e.g. perfect rhyme).
     * @param other  the word to match against.
     * @param pos1   part of speech of this word. Can be null.
     * @param pos2   part of speech of the other word. Can be null.
     * @return true if the words rhyme for the given parts of speech.
     */
    public boolean rhymesWithWrapper(RhymeType filter, SuperWord other, PartOfSpeech pos1, PartOfSpeech pos2) {
        if (!this.populated)
            this.populate();
        if (!other.populated)
            other.populate();

        if (pos1 == null && pos2 == null) {
            return matchesWithWrapper(filter, other);
        } else if (pos1 != null && pos2 != null) {
            SubPronunciation subPronunciation1 = this.getSubPronunciation(pos1);
            SubPronunciation subPronunciation2 = other.getSubPronunciation(pos2);
            return matchesWith(filter, this.plaintext, pos1, subPronunciation1, other.plaintext, pos2,
                    subPronunciation2);
        } else if (pos1 != null) {
            SubPronunciation subPronunciation1 = this.getSubPronunciation(pos1);
            for (PartOfSpeech pos2b : other.subWords.keySet()) {
                SubPronunciation subPronunciation2 = other.getSubPronunciation(pos2b);
                if (matchesWith(filter, this.plaintext, pos1, subPronunciation1, other.plaintext, pos2,
                        subPronunciation2)) {
                    return true;
                }
            }
        } else {
            SubPronunciation subPronunciation2 = other.getSubPronunciation(pos2);
            for (PartOfSpeech pos1b : this.subWords.keySet()) {
                SubPronunciation subPronunciation1 = this.getSubPronunciation(pos1b);
                if (matchesWith(filter, this.plaintext, pos1, subPronunciation1, other.plaintext, pos2,
                        subPronunciation2)) {
                    return true;
                }
            }
        }
        return false;
    }

}
