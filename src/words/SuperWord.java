package words;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import apis.WordsAPI;

import static utils.NullListOperations.addToNull;
import static config.Configuration.LOG;

public class SuperWord implements Comparable<SuperWord> {

    private static HashMap<String, SuperWord> cachePopulated = new HashMap<>();
    private static HashMap<String, SuperWord> cachePlaceholder = new HashMap<>();

    private String plaintext;
    private boolean populated = false; // true iff built from a WordsAPI query
    private Pronunciation pronunciation;
    private ArrayList<Word> nouns;
    private ArrayList<Word> pronouns;
    private ArrayList<Word> verbs;
    private ArrayList<Word> adjectives;
    private ArrayList<Word> adverbs;
    private ArrayList<Word> prepositions;
    private ArrayList<Word> conjunctions;
    private ArrayList<Word> definiteArticles;
    private ArrayList<Word> unknowns;

    /**
     * Attempts to get a cached word, before returning a new placeholder.
     * 
     * @param plaintext
     * @return
     */
    public static SuperWord getSuperWord(String plaintext) {
        if (cachePopulated.containsKey(plaintext)) {
            LOG.writeTempLog(String.format("Retrieved populated superword for \"%s\" from cache.", plaintext));
            return cachePopulated.get(plaintext);
        }
        if (cachePlaceholder.containsKey(plaintext)) {
            LOG.writeTempLog(String.format("Retrieved placeholder superword for \"%s\" from cache.", plaintext));
            return cachePlaceholder.get(plaintext);
        }
        return new SuperWord(plaintext);
    }

    public void populate() {
        if (populated) {
            LOG.writeTempLog(String.format("Attempted to repopulate \"%s\": ", plaintext, this.toString()));
            return;
        }

        JSONObject word = WordsAPI.getWord(plaintext);

        if (word.has("word") && !word.getString("word").equals(plaintext)) {
            LOG.writePersistentLog(String.format("WordsAPI responded with word \"%s\" when requesting \"%s\".",
                    word.getString("word"), plaintext));
        }

        this.pronunciation = new Pronunciation();

        if (word.has("syllables")) {
            JSONObject syllablesObject = word.getJSONObject("syllables");
            this.pronunciation.setSyllables(syllablesObject);
        } else {
            LOG.writePersistentLog(String.format("Syllables of \"%s\" is missing", plaintext));
        }

        if (word.has("pronunciation")) {
            try {
                JSONObject pronunciationObject = word.getJSONObject("pronunciation");
                this.pronunciation.setIPA(pronunciationObject);
            } catch (JSONException e) {
                LOG.writePersistentLog(String.format("Pronunciation of \"%s\" is not a JSONObject: \"%s\"", plaintext,
                        word.get("pronunciation").toString()));
                this.pronunciation.setIPA(word.getString("pronunciation"));
            }
        }

        if (word.has("results")) {
            JSONArray resultsArray = word.getJSONArray("results");
            this.setWords(resultsArray);
        }

        populated = true;
        cachePopulated.put(this.plaintext, this);
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
        for (Object result : results) {
            Word word = new Word(plaintext, (Map<String, Object>) result);
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

    public String getPlaintext() {
        return plaintext;
    }

    public ArrayList<Word> getNouns() {
        return this.nouns;
    }

    public ArrayList<Word> getPronouns() {
        return this.pronouns;
    }

    public ArrayList<Word> getVerbs() {
        return this.verbs;
    }

    public ArrayList<Word> getAdjectives() {
        return this.adjectives;
    }

    public ArrayList<Word> getAdverbs() {
        return this.adverbs;
    }

    public ArrayList<Word> getPrepositions() {
        return this.prepositions;
    }

    public ArrayList<Word> getConjuctions() {
        return this.conjunctions;
    }

    public ArrayList<Word> getDefiniteArticles() {
        return this.definiteArticles;
    }

    public String toString() {
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
    public String getSubWords() {
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
     * I only want to order by plaintext, but equality should take more into
     * account. Arguably I should build a custom Comparator rather than setting the
     * default.
     */
    @Override
    public int compareTo(SuperWord anotherWord) {
        if (anotherWord == null) {
            throw new NullPointerException();
        }
        return plaintext.compareTo((anotherWord).plaintext);
    }
}
