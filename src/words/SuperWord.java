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

import apis.WordsAPI;
import utils.ParameterWrappers.FilterParameters;
import utils.ParameterWrappers.SuggestionPoolParameters;
import utils.ParameterWrappers.FilterParameters.Filter;
import utils.ParameterWrappers.SuggestionPoolParameters.SuggestionPool;
import words.Pronunciation.SubPronunciation;
import words.SubWord.PartOfSpeech;

import static utils.NullListOperations.addToNull;
import static utils.NullListOperations.addAllToNull;
import static utils.NullListOperations.combineLists;
import static utils.NullListOperations.combineListsVarags;
import static utils.NullListOperations.combineListsPrioritiseDuplicates;
import static config.Configuration.LOG;

public class SuperWord extends Token {

    private static HashMap<String, SuperWord> cachePopulated = new HashMap<>();
    private static HashMap<String, SuperWord> cachePlaceholder = new HashMap<>();

    private static Set<String> knownFields = new HashSet<>(
            Arrays.asList("word", "results", "syllables", "pronunciation", "frequency", "rhymes", "success",
                    "message"));

    private boolean populated = false; // true iff built from a WordsAPI query
    private Pronunciation pronunciation;
    private EnumMap<PartOfSpeech, ArrayList<SubWord>> partsOfSpeech = new EnumMap<>(PartOfSpeech.class);

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

        if (word.has("syllables") || word.has("pronunciation"))
            this.pronunciation = new Pronunciation();

        if (word.has("syllables")) {
            JSONObject syllablesObject = word.getJSONObject("syllables");
            this.pronunciation.setSyllables(this.plaintext, syllablesObject);
        } else {
            LOG.writePersistentLog(String.format("Syllables of \"%s\" was missing", plaintext));
        }

        if (word.has("pronunciation")) {
            try {
                JSONObject pronunciationObject = word.getJSONObject("pronunciation");
                this.pronunciation.setIPA(this.plaintext, pronunciationObject);
            } catch (JSONException e) {
                LOG.writePersistentLog(String.format("Pronunciation of \"%s\" was not a JSONObject: \"%s\"", plaintext,
                        word.get("pronunciation").toString()));
                this.pronunciation.setIPA(this.plaintext, word.getString("pronunciation"));
            }
        } else {
            LOG.writePersistentLog(String.format("Pronunciation of \"%s\" was missing", plaintext));
        }

        if (word.has("rhymes")) {
            LOG.writePersistentLog(
                    String.format("Rhymes of \"%s\" was present: %s", plaintext, word.get("rhymes").toString()));
            try {
                JSONObject rhymeObject = word.getJSONObject("rhymes");
                this.pronunciation.setRhyme(this.plaintext, rhymeObject);
            } catch (JSONException e) {
                LOG.writePersistentLog(String.format("Rhymes of \"%s\" was not a JSONObject: \"%s\"", plaintext,
                        word.get("Rhymes").toString()));
                this.pronunciation.setRhyme(this.plaintext, word.getString("Rhymes"));
            }
        }

        if (word.has("results")) {
            JSONArray resultsArray = word.getJSONArray("results");
            this.setWords(resultsArray);
        } else {
            partsOfSpeech.put(PartOfSpeech.UNKNOWN, null); // so that matchesWith has something to iterate over
            LOG.writePersistentLog(String.format("Results of \"%s\" was missing", plaintext));
        }

        populated = true;
        cachePopulated.put(this.plaintext, this);
        LOG.writeTempLog(String.format("Populated and cached \"%s\": %s", plaintext, this.toString()));
    }

    /**
     * For creating placeholders
     * 
     * @param plaintext
     */
    private SuperWord(String plaintext) {
        this.plaintext = plaintext;
        cachePlaceholder.put(this.plaintext, this);
    }

    /**
     * 
     * @param plaintexts JSONArrays are not typed, but must have String elements.
     * @return
     */
    public static ArrayList<SuperWord> batchPlaceHolders(List<Object> plaintexts) {
        ArrayList<SuperWord> list = new ArrayList<>();
        for (Object plaintext : plaintexts) {
            list.add(getSuperWord((String) plaintext));
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private void setWords(JSONArray resultsArray) {
        List<Object> results = resultsArray.toList();

        if (results.isEmpty()) {
            LOG.writePersistentLog(String.format("Results of \"%s\" was an empty array", plaintext));
        }

        for (Object result : results) {
            SubWord word = new SubWord(this, (Map<String, Object>) result);
            PartOfSpeech pos = word.partOfSpeech();
            ArrayList<SubWord> subWords = partsOfSpeech.get(pos);
            partsOfSpeech.put(word.partOfSpeech(), addToNull(subWords, word));
        }
    }

    /**
     * 
     * @param pos
     * @param inclusiveUnknown
     * @return null if the SuperWord has no SubWords of that type.
     */
    public ArrayList<SubWord> getSubWords(PartOfSpeech pos, boolean inclusiveUnknown) {
        if (!this.populated) {
            this.populate();
        }
        if (pos.equals(PartOfSpeech.UNKNOWN) && inclusiveUnknown) {
            return combineLists(partsOfSpeech.values());
        } else if (inclusiveUnknown) {
            return combineListsVarags(partsOfSpeech.get(pos), partsOfSpeech.get(PartOfSpeech.UNKNOWN));
        } else {
            return partsOfSpeech.get(pos);
        }
    }

    public Pronunciation getPronunciation() {
        return this.pronunciation;
    }

    public Pronunciation.SubPronunciation getSubPronunciation(SubWord.PartOfSpeech partOfSpeech) {
        if (this.pronunciation == null) {
            return null;
        }
        return this.pronunciation.getSubPronunciation(plaintext, partOfSpeech);
    }

    public boolean validPool(SuggestionPool pool, PartOfSpeech pos) {
        switch (pool) {
            case COMMONLY_TYPED:
                return getSuggestionPool(SuggestionPool.TYPE_OF, pos, false) != null;
            case COMMON_CATEGORIES:
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

    public ArrayList<SuperWord> getSuggestionPool(SuggestionPool pool, PartOfSpeech pos, boolean inclusiveUnknown) {
        if (!populated)
            this.populate();

        ArrayList<SuperWord> suggestionPool = null;
        ArrayList<SubWord> subWords = getSubWords(pos, inclusiveUnknown);
        if (subWords != null) {
            for (SubWord subWord : subWords) {
                suggestionPool = addAllToNull(suggestionPool, subWord.getSuggestionPool(pool));
            }
        }
        return suggestionPool;
    }

    private ArrayList<SuperWord> getAggregatedSuggestions(PartOfSpeech pos, SuggestionPoolParameters params) {
        ArrayList<ArrayList<SuperWord>> suggestions = new ArrayList<>();
        for (SuggestionPool pool : SuggestionPool.values()) {
            if (params.includes(pool)) {
                suggestions.add(this.getSuggestionPool(pool, pos, params.hasInclusiveUnknown()));
            }
        }

        ArrayList<SuperWord> combined = combineListsPrioritiseDuplicates(suggestions);
        if (combined != null) {
            combined.remove(this); // prevent suggesting the original word
        }
        LOG.writeTempLog(String.format("Combined suggestions for \"%s\" (%s) including %s: %s", plaintext, pos,
                params.toString(), combined));
        return combined;
    }

    /**
     * 
     * @param suggestions
     * @param pos
     * @param rhyme
     * @param rhymeWith   can be null if rhyme == false.
     * @param rhymePos    can be null if rhyme == false.
     * @return empty list if there are no suggestions, or no suggestions that pass
     *         at least one of the filters.
     */
    private ArrayList<SuperWord> filterSuggestions(ArrayList<SuperWord> suggestions, PartOfSpeech pos,
            List<FilterParameters> paramsList) {

        if (suggestions == null) {
            return new ArrayList<>();
        }

        ArrayList<SuperWord> filtered = new ArrayList<>(suggestions);

        for (SuperWord suggestion : suggestions) {
            boolean matched = false;
            boolean paramsAreEmpty = true;
            for (FilterParameters params : paramsList) {
                SuperWord matchWith;
                for (Filter filter : Filter.values()) {
                    if ((matchWith = params.getMatchWith(filter)) != null) {
                        paramsAreEmpty = false;
                        if (suggestion.rhymesWithWrapper(filter, matchWith, pos, params.getMatchPoS())) {
                            matched = true;
                            break;
                        }
                    }
                }
                if (matched)
                    break;
            }
            if (!paramsAreEmpty && !matched)
                filtered.remove(suggestion);
        }

        LOG.writeTempLog(String.format("Filtered suggestions for \"%s\" (%s) including %s: %s", plaintext, pos,
                paramsList.toString(), filtered));
        return filtered;
    }

    /**
     * 
     * @param pos
     * @param suggestionParams
     * @param filterParams
     * @return a list of filtered suggestions, which can be empty.
     */
    public ArrayList<SuperWord> getFilteredSuggestions(PartOfSpeech pos, SuggestionPoolParameters suggestionParams,
            List<FilterParameters> filterParams) {
        ArrayList<SuperWord> unfiltered = getAggregatedSuggestions(pos, suggestionParams);
        return filterSuggestions(unfiltered, pos, filterParams);
    }

    @Override
    public String toString() {
        return String.format("%s: {populated: %b}", this.plaintext, this.populated);
    }

    public String toFullString() {
        String divider = "\n\t";

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(plaintext + ": {");
        if (populated) {
            stringBuilder.append(divider);
        }
        stringBuilder.append("populated: " + populated);
        if (pronunciation != null) {
            stringBuilder.append(divider);
            stringBuilder.append("pronunciation: " + pronunciation.toString());
        }
        for (PartOfSpeech pos : PartOfSpeech.values()) {
            if (partsOfSpeech.get(pos) != null) {
                stringBuilder.append(divider);
                stringBuilder.append(String.format("%s: %d", pos.getApiString(), partsOfSpeech.get(pos).size()));
            }
        }
        if (populated) {
            stringBuilder.append("\n");
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    /**
     * Warning: this is a messy-looking string.
     * 
     * @return a String formatted for debugging.
     */
    public String subWordsString() {
        String divider = "\n//////// ";

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(plaintext + ": {");
        for (PartOfSpeech pos : PartOfSpeech.values()) {
            if (partsOfSpeech.get(pos) != null) {
                stringBuilder.append(divider);
                stringBuilder.append(String.format("%s\n%s", pos.getApiString(), partsOfSpeech.get(pos).toString()));
            }
        }
        if (populated) {
            stringBuilder.append("\n");
        }
        stringBuilder.append("}");
        return stringBuilder.toString();

    }

    /**
     * 
     * @param plaintext1        for logging.
     * @param pos1              for logging.
     * @param subPronunciation1
     * @param plaintext2        for logging.
     * @param pos2              for logging.
     * @param subPronunciation2
     * @return
     */
    private static boolean matchesWith(Filter filter,
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
        return subPronunciation1.matchesWith(filter, subPronunciation2);
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
    public boolean matchesWithWrapper(Filter filter, SuperWord other) {
        if (!this.populated)
            this.populate();
        if (!other.populated)
            other.populate();

        if (this.pronunciation == null || other.pronunciation == null) {
            return false;
        }

        for (PartOfSpeech pos1 : partsOfSpeech.keySet()) {
            SubPronunciation subPronunciation1 = this.getSubPronunciation(pos1);
            for (PartOfSpeech pos2 : other.partsOfSpeech.keySet()) {
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
     * @param pos1   part of speech of this word.
     * @param pos2   part of speech of the other word.
     * @return
     */
    public boolean rhymesWithWrapper(Filter filter, SuperWord other, PartOfSpeech pos1, PartOfSpeech pos2) {
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
            for (PartOfSpeech pos2b : other.partsOfSpeech.keySet()) {
                SubPronunciation subPronunciation2 = other.getSubPronunciation(pos2b);
                if (matchesWith(filter, this.plaintext, pos1, subPronunciation1, other.plaintext, pos2,
                        subPronunciation2)) {
                    return true;
                }
            }
        } else {
            SubPronunciation subPronunciation2 = other.getSubPronunciation(pos2);
            for (PartOfSpeech pos1b : this.partsOfSpeech.keySet()) {
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
