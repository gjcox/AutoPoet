package words;

import java.util.ArrayList;
import java.util.Arrays;
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
import utils.ParameterWrappers.SuggestionParameters;
import words.Pronunciation.SubPronunciation;
import words.SubWord.PartOfSpeech;

import static utils.NullListOperations.addToNull;
import static utils.NullListOperations.addAllToNull;
import static utils.NullListOperations.addAllToNullIfMatching;
import static utils.NullListOperations.combineLists;
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
    private Set<PartOfSpeech> partsOfSpeech = new HashSet<>();
    private ArrayList<SubWord> nouns;
    private ArrayList<SubWord> pronouns;
    private ArrayList<SubWord> verbs;
    private ArrayList<SubWord> adjectives;
    private ArrayList<SubWord> adverbs;
    private ArrayList<SubWord> prepositions;
    private ArrayList<SubWord> conjunctions;
    private ArrayList<SubWord> definiteArticles;
    private ArrayList<SubWord> unknowns;

    /**
     * Attempts to get a cached word, before returning a new placeholder.
     * 
     * @param plaintext
     * @return
     */
    public static SuperWord getSuperWord(String plaintext) {
        if (cachePopulated.containsKey(plaintext)) {
            return cachePopulated.get(plaintext);
        }
        if (cachePlaceholder.containsKey(plaintext)) {
            return cachePlaceholder.get(plaintext);
        }
        return new SuperWord(plaintext);
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
            LOG.writePersistentLog(String.format("Results of \"%s\" was missing", plaintext));
            this.partsOfSpeech.add(PartOfSpeech.UNKNOWN);
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
     * @param plaintexts JSONArrays are not typed, but must be have String elements.
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
            partsOfSpeech.add(PartOfSpeech.UNKNOWN);
        }

        for (Object result : results) {
            SubWord word = new SubWord(this, (Map<String, Object>) result);
            partsOfSpeech.add(word.partOfSpeech());
            switch (word.partOfSpeech()) {
                case NOUN:
                    nouns = addToNull(nouns, word);
                    break;
                case PRONOUN:
                    pronouns = addToNull(pronouns, word);
                    break;
                case VERB:
                    verbs = addToNull(verbs, word);
                    break;
                case ADJECTIVE:
                    adjectives = addToNull(adjectives, word);
                    break;
                case ADVERB:
                    adverbs = addToNull(adverbs, word);
                    break;
                case PREPOSITION:
                    prepositions = addToNull(prepositions, word);
                    break;
                case CONJUCTION:
                    conjunctions = addToNull(conjunctions, word);
                    break;
                case DEFINITE_ARTICLE:
                    definiteArticles = addToNull(definiteArticles, word);
                    break;
                case UNKNOWN:
                    unknowns = addToNull(unknowns, word);
                    break;
            }
        }
    }

    public ArrayList<SubWord> getSubWords(PartOfSpeech pos, boolean inclusiveUnknown) {
        switch (pos) {
            case ADJECTIVE:
                return adjectives;
            case ADVERB:
                return adverbs;
            case CONJUCTION:
                return conjunctions;
            case DEFINITE_ARTICLE:
                return definiteArticles;
            case NOUN:
                return nouns;
            case PREPOSITION:
                return prepositions;
            case PRONOUN:
                return pronouns;
            case UNKNOWN:
                if (inclusiveUnknown) {
                    return combineLists(adjectives, adverbs, conjunctions, definiteArticles, nouns, prepositions,
                            pronouns, verbs, unknowns);
                } else {
                    return unknowns;
                }
            case VERB:
                return verbs;
            default:
                LOG.writePersistentLog(
                        String.format("Attempted to get subwords of \"s\" with invalid POS: \"%s\"", plaintext, pos));
                return unknowns;
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

    /**
     * TODO: remove duplicates; potentially score words based on duplicate count
     * 
     * @param pos
     * @return
     */
    public ArrayList<SuperWord> getSynonyms(PartOfSpeech pos) {
        if (!populated)
            this.populate();

        ArrayList<SuperWord> synonyms = null;
        ArrayList<SubWord> subWords = getSubWords(pos, false);
        if (subWords != null) {
            for (SubWord subWord : subWords) {
                synonyms = addAllToNull(synonyms, subWord.getSynonyms());
            }
        }
        return synonyms;
    }

    /**
     * TODO: remove duplicates; potentially score words based on duplicate count
     * 
     * @param pos
     * @return
     */
    public ArrayList<SuperWord> getTypeOf(PartOfSpeech pos) {
        if (!populated)
            this.populate();

        ArrayList<SuperWord> typesOf = null;
        ArrayList<SubWord> subWords = getSubWords(pos, false);
        if (subWords != null) {
            for (SubWord subWord : subWords) {
                typesOf = addAllToNull(typesOf, subWord.getTypeOf());
            }
        }
        return typesOf;
    }

    /**
     * TODO: remove duplicates; potentially score words based on duplicate count
     * 
     * @param pos
     * @return
     */
    public ArrayList<SuperWord> getHasTypes(PartOfSpeech pos) {
        if (!populated)
            this.populate();

        ArrayList<SuperWord> types = null;
        ArrayList<SubWord> subWords = getSubWords(pos, false);
        if (subWords != null) {
            for (SubWord subWord : subWords) {
                types = addAllToNull(types, subWord.getHasTypes());
            }
        }
        return types;
    }

    /**
     * TODO: remove duplicates; potentially score words based on duplicate count
     * 
     * @param pos
     * @return
     */
    public ArrayList<SuperWord> getHasTypes(PartOfSpeech pos, SuperWord matching) {
        if (!populated)
            this.populate();

        ArrayList<SuperWord> types = null;
        ArrayList<SubWord> subWords = getSubWords(pos, true);
        if (subWords != null) {
            for (SubWord subWord : subWords) {
                types = addAllToNullIfMatching(types, subWord.getHasTypes(), matching);
            }
        }
        return types;
    }

    /**
     * TODO: remove duplicates; potentially score words based on duplicate count
     * 
     * @param pos
     * @return
     */
    public ArrayList<SuperWord> getCommonlyTyped(PartOfSpeech pos) {
        if (!populated)
            this.populate();

        ArrayList<SuperWord> commonlyTyped = null;
        ArrayList<SubWord> subWords = getSubWords(pos, false);
        if (subWords != null) {
            for (SubWord subWord : subWords) {
                commonlyTyped = addAllToNull(commonlyTyped, subWord.getCommonlyTyped());
            }
        }
        return commonlyTyped;
    }

    public ArrayList<SuperWord> getInCategory(PartOfSpeech pos) {
        if (!populated)
            this.populate();

        ArrayList<SuperWord> categories = null;
        ArrayList<SubWord> subWords = getSubWords(pos, false);
        if (subWords != null) {
            for (SubWord subWord : subWords) {
                categories = addAllToNull(categories, subWord.getInCategory());
            }
        }
        return categories;
    }

    public ArrayList<SuperWord> getHasCategories(PartOfSpeech pos) {
        if (!populated)
            this.populate();

        ArrayList<SuperWord> categories = null;
        ArrayList<SubWord> subWords = getSubWords(pos, false);
        if (subWords != null) {
            for (SubWord subWord : subWords) {
                categories = addAllToNull(categories, subWord.getHasCategories());
            }
        }
        return categories;
    }

    public ArrayList<SuperWord> getHasCategories(PartOfSpeech pos, SuperWord matching) {
        if (!populated)
            this.populate();

        ArrayList<SuperWord> categories = null;
        ArrayList<SubWord> subWords = getSubWords(pos, true);
        if (subWords != null) {
            for (SubWord subWord : subWords) {
                categories = addAllToNullIfMatching(categories, subWord.getHasCategories(), matching);
            }
        }

        return categories;
    }

    /**
     * TODO: remove duplicates; potentially score words based on duplicate count
     * TODO: account for UNKNOWN SubWords having categories with a different POS and
     * vice versa
     * 
     * @param pos
     * @return
     */
    public ArrayList<SuperWord> getCommonCategories(PartOfSpeech pos) {
        if (!populated)
            this.populate();

        ArrayList<SuperWord> commonCategories = null;
        ArrayList<SubWord> subWords = getSubWords(pos, false);
        if (subWords != null) {
            for (SubWord subWord : subWords) {
                commonCategories = addAllToNull(commonCategories, subWord.getCommonCategories());
            }
        }
        return commonCategories;
    }

    public ArrayList<SuperWord> getHasParts(PartOfSpeech pos) {
        if (!populated)
            this.populate();

        ArrayList<SuperWord> hasParts = null;
        ArrayList<SubWord> subWords = getSubWords(pos, false);
        if (subWords != null) {
            for (SubWord subWord : subWords) {
                hasParts = addAllToNull(hasParts, subWord.getHasParts());
            }
        }
        return hasParts;
    }

    public ArrayList<SuperWord> getPartOf(PartOfSpeech pos) {
        if (!populated)
            this.populate();

        ArrayList<SuperWord> partOf = null;
        ArrayList<SubWord> subWords = getSubWords(pos, false);
        if (subWords != null) {
            for (SubWord subWord : subWords) {
                partOf = addAllToNull(partOf, subWord.getPartOf());
            }
        }
        return partOf;
    }

    /**
     * TODO: remove duplicates; potentially score words based on duplicate count
     * 
     * @param pos
     * @return
     */
    public ArrayList<SuperWord> getSimilarTo(PartOfSpeech pos) {
        if (!populated)
            this.populate();

        ArrayList<SuperWord> similarTo = null;
        ArrayList<SubWord> subWords = getSubWords(pos, false);
        if (subWords != null) {
            for (SubWord subWord : subWords) {
                similarTo = addAllToNull(similarTo, subWord.getSimilarTo());
            }
        }
        return similarTo;
    }

    private ArrayList<SuperWord> getSuggestions(PartOfSpeech pos, SuggestionParameters params) {
        ArrayList<SuperWord> synonymsList = params.synonyms() ? this.getSynonyms(pos) : null;
        ArrayList<SuperWord> commonlyTypedList = params.commonlyTyped() ? this.getCommonlyTyped(pos) : null;
        ArrayList<SuperWord> commonCategoriesList = params.commonCategories() ? this.getCommonCategories(pos) : null;
        ArrayList<SuperWord> partOfList = params.partOf() ? this.getPartOf(pos) : null;
        ArrayList<SuperWord> hasPartsList = params.hasParts() ? this.getHasParts(pos) : null;
        ArrayList<SuperWord> similarToList = params.similarTo() ? this.getSimilarTo(pos) : null;

        ArrayList<SuperWord> combined = combineListsPrioritiseDuplicates(synonymsList, commonlyTypedList,
                commonCategoriesList, partOfList, hasPartsList, similarToList);
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
     * @return
     */
    private static ArrayList<SuperWord> filterSuggestions(ArrayList<SuperWord> suggestions, PartOfSpeech pos,
            FilterParameters params) {

        if (suggestions == null) {
            return null;
        }

        ArrayList<SuperWord> filtered = new ArrayList<>(suggestions);

        for (SuperWord suggestion : suggestions) {
            if (params.rhyme() && !suggestion.rhymesWithWrapper(params.rhymeWith(), pos, params.rhymePos())) {
                filtered.remove(suggestion);
            }
        }
        return filtered;
    }

    public ArrayList<SuperWord> getFilteredSuggestions(PartOfSpeech pos, SuggestionParameters suggestionParams,
            FilterParameters filterParams) {
        ArrayList<SuperWord> unfiltered = getSuggestions(pos, suggestionParams);
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
        if (!partsOfSpeech.isEmpty()) {
            stringBuilder.append(divider);
            stringBuilder.append("partsOfSpeech: " + partsOfSpeech.toString());
        }
        if (nouns != null) {
            stringBuilder.append(divider);
            stringBuilder.append("nouns: " + nouns.size());
        }
        if (pronouns != null) {
            stringBuilder.append(divider);
            stringBuilder.append("pronouns: " + pronouns.size());
        }
        if (verbs != null) {
            stringBuilder.append(divider);
            stringBuilder.append("verbs: " + verbs.size());
        }
        if (adjectives != null) {
            stringBuilder.append(divider);
            stringBuilder.append("adjectives: " + adjectives.size());
        }
        if (adverbs != null) {
            stringBuilder.append(divider);
            stringBuilder.append("adverbs: " + adverbs.size());
        }
        if (prepositions != null) {
            stringBuilder.append(divider);
            stringBuilder.append("prepositions: " + prepositions.size());
        }
        if (conjunctions != null) {
            stringBuilder.append(divider);
            stringBuilder.append("conjunction: " + conjunctions.size());
        }
        if (definiteArticles != null) {
            stringBuilder.append(divider);
            stringBuilder.append("definiteArticles: " + definiteArticles.size());
        }
        if (unknowns != null) {
            stringBuilder.append(divider);
            stringBuilder.append("unknowns: " + unknowns.size());
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
        if (nouns != null) {
            stringBuilder.append(divider);
            stringBuilder.append("nouns\n" + nouns.toString());
        }
        if (pronouns != null) {
            stringBuilder.append(divider);
            stringBuilder.append("pronouns\n" + pronouns.toString());
        }
        if (verbs != null) {
            stringBuilder.append(divider);
            stringBuilder.append("verbs\n" + verbs.toString());
        }
        if (adjectives != null) {
            stringBuilder.append(divider);
            stringBuilder.append("adjectives\n" + adjectives.toString());
        }
        if (adverbs != null) {
            stringBuilder.append(divider);
            stringBuilder.append("adverbs\n" + adverbs.toString());
        }
        if (prepositions != null) {
            stringBuilder.append(divider);
            stringBuilder.append("prepositions\n" + prepositions.toString());
        }
        if (conjunctions != null) {
            stringBuilder.append(divider);
            stringBuilder.append("conjunction\n" + conjunctions.toString());
        }
        if (definiteArticles != null) {
            stringBuilder.append(divider);
            stringBuilder.append("definiteArticles\n" + definiteArticles.toString());
        }
        if (unknowns != null) {
            stringBuilder.append(divider);
            stringBuilder.append("unknowns\n" + unknowns.toString());
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
    private static boolean rhmyesWith(String plaintext1, PartOfSpeech pos1, SubPronunciation subPronunciation1,
            String plaintext2, PartOfSpeech pos2, SubPronunciation subPronunciation2) {
        if (subPronunciation1 == null) {
            LOG.writePersistentLog(
                    String.format("\"%s\" (%s) did not have a pronunciation for \"%s\" (%s) to rhyme against",
                            plaintext1, pos1, plaintext2, pos2));
            return false;
        }
        if (subPronunciation2 == null) {
            LOG.writePersistentLog(
                    String.format("\"%s\" (%s) did not have a pronunciation for \"%s\" (%s) to rhyme against",
                            plaintext2, pos2, plaintext1, pos1));
            return false;
        }
        return subPronunciation1.rhymesWith(subPronunciation2);
    }

    /**
     * Iterates over the parts of speech of both words (worst case is full cross
     * product evaluation) and returns true if any part of speech pairing produces a
     * rhyme.
     * 
     * @param other
     * @return
     */
    public boolean rhymesWithWrapper(SuperWord other) {
        if (!this.populated)
            this.populate();
        if (!other.populated)
            other.populate();

        if (this.pronunciation == null || other.pronunciation == null) {
            return false;
        }

        for (PartOfSpeech pos1 : this.partsOfSpeech) {
            SubPronunciation subPronunciation1 = this.getSubPronunciation(pos1);
            for (PartOfSpeech pos2 : other.partsOfSpeech) {
                SubPronunciation subPronunciation2 = other.getSubPronunciation(pos2);
                if (rhmyesWith(this.plaintext, pos1, subPronunciation1, other.plaintext, pos2, subPronunciation2)) {
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
     * @param other
     * @param pos1
     * @param pos2
     * @return
     */
    public boolean rhymesWithWrapper(SuperWord other, PartOfSpeech pos1, PartOfSpeech pos2) {
        if (!this.populated)
            this.populate();
        if (!other.populated)
            other.populate();

        if (pos1 == null && pos2 == null) {
            return rhymesWithWrapper(other);
        } else if (pos1 != null && pos2 != null) {
            SubPronunciation subPronunciation1 = this.getSubPronunciation(pos1);
            SubPronunciation subPronunciation2 = other.getSubPronunciation(pos2);
            return rhmyesWith(this.plaintext, pos1, subPronunciation1, other.plaintext, pos2, subPronunciation2);
        } else if (pos1 != null) {
            SubPronunciation subPronunciation1 = this.getSubPronunciation(pos1);
            for (PartOfSpeech pos2b : other.partsOfSpeech) {
                SubPronunciation subPronunciation2 = other.getSubPronunciation(pos2b);
                if (rhmyesWith(this.plaintext, pos1, subPronunciation1, other.plaintext, pos2, subPronunciation2)) {
                    return true;
                }
            }
        } else {
            SubPronunciation subPronunciation2 = other.getSubPronunciation(pos2);
            for (PartOfSpeech pos1b : this.partsOfSpeech) {
                SubPronunciation subPronunciation1 = this.getSubPronunciation(pos1b);
                if (rhmyesWith(this.plaintext, pos1, subPronunciation1, other.plaintext, pos2, subPronunciation2)) {
                    return true;
                }
            }
        }
        return false;
    }

}
