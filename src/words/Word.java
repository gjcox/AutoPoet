package words;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import apis.WordsAPI;
import utils.JSONConstructors.Emphasis;
import utils.Pair;

/**
 * The ipa object includes "part of speech" for the pronunciations, but WordsAPI
 * doesn't document what the options are. Most words will just have an "all"
 * value, but some have "all", "noun" and "verb". Seemingly "all" will just be
 * the noun for some reason. Other parts of speech exist in English: pronouns,
 * adjectives, adverbs, prepositions, conjunctions, articles/determiners, and
 * interjections. Some words (e.g. "the", "ouch") have pronunciation data but it
 * doesn't have a part of speech (e.g. for "the": "pronunciation":"ðʌ").
 * 
 * I can't think of an example of a word to test that has multiple parts of
 * speech that are pronounced differently that aren't nouns and adjectives.
 */
public class Word implements Comparable {

    private String plaintext;
    private JSONObject plain_syllables = new JSONObject(); // e.g. incredible "syllables":{"count":4,
    /// / / / / / / / / / / / / / / "list":[0:"in", 1:"cred", 2:"i", 3:"ble"]} -
    private JSONObject ipa = new JSONObject(); // / / / / / / e.g. wind "ipa":{"all":"'prɛzənt", "noun":"'prɛzənt",
    /// / / / / / / / / / / / / / / "verb":"prɪ'zɛnt"} -
    private JSONObject ipa_syllables = new JSONObject(); // / e.g. present
                                                         // :{"all":[{"coda":"","nucleus":"ɛ","onset":"pr"},{"coda":"nt","nucleus":"ə","onset":"z"}],
                                                         // "noun":[{"coda":"","nucleus":"ɛ","onset":"pr"},{"coda":"nt","nucleus":"ə","onset":"z"}],
                                                         // "verb":[{"coda":"","nucleus":"ɪ","onset":"pr"},{"coda":"nt","nucleus":"ɛ","onset":"z"}]}-
    private JSONObject ipa_emphasis = new JSONObject(); // e.g."ipa_emphasis":{"all":{"secondary":[],"has_secondary":false,"primary":0},
                                                        // "verb":{"secondary":[],"has_secondary":false,"primary":1},
                                                        // "noun":{"secondary":[],"has_secondary":false,"primary":0}}-
    private ArrayList<Word> synonyms = new ArrayList<>();
    private ArrayList<Word> common_types = new ArrayList<>();

    public Word(String plaintext) {
        this.plaintext = plaintext;
        this.ipa = WordsAPI.getIPA(plaintext);

        Iterator<String> keys = ipa.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String ipa_ = ipa.getString(key);
            Pair<JSONArray, JSONObject> ipa_syllables_ = IPAHandler.getSyllables(ipa_);
            this.ipa_syllables.put(key, ipa_syllables_.one());
            this.ipa_emphasis.put(key, ipa_syllables_.two());
        }
    }

    public Word(String plaintext, String ipa) {
        this.plaintext = plaintext;
        this.ipa = new JSONObject(String.format("{all: \"%s\"}", ipa));

        Iterator<String> keys = this.ipa.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String ipa_ = this.ipa.getString(key);
            Pair<JSONArray, JSONObject> ipa_syllables_ = IPAHandler.getSyllables(ipa_);
            this.ipa_syllables.put(key, ipa_syllables_.one());
            this.ipa_emphasis.put(key, ipa_syllables_.two());
        }
    }

    /*
     * public JSONObject toJsonObject() { JSONObject jo = new JSONObject();
     * jo.put("plaintext", plaintext); jo.put("plain_syllables", plain_syllables);
     * jo.put("ipa", ipa); jo.put("ipa_syllables", ipa_syllables);
     * jo.put("ipa_emphasis", ipa_emphasis); jo.put("synonyms", synonyms);
     * jo.put("common_types", common_types); return jo; }
     * 
     * public String toString() { return this.toJsonObject().toString(); }
     */

    private List<String> plaintextSynonyms() {
        List<String> list = new ArrayList<>();
        for (Word synonym : synonyms) {
            list.add(synonym.plaintext);
        }
        return list;
    }

    private List<String> plaintextCommonTypes() {
        List<String> list = new ArrayList<>();
        for (Word common_type : common_types) {
            list.add(common_type.plaintext);
        }
        return list;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(String.format("plaintext:\"%s\"", plaintext));
        builder.append(String.format(", plain_syllables:%s", plain_syllables.toString()));
        builder.append(String.format(", ipa:%s", ipa.toString()));
        builder.append(String.format(", ipa_syllables:%s", ipa_syllables.toString()));
        builder.append(String.format(", ipa_emphasis:%s", ipa_emphasis.toString()));
        builder.append(String.format(", synonyms:%s", plaintextSynonyms().toString()));
        builder.append(String.format(", common_types:%s", plaintextCommonTypes().toString()));
        return builder.toString();
    }

    /* Start of getters and setters */
    public JSONObject ipaSyllables() {
        return this.ipa_syllables;
    }

    public JSONObject ipaEmphasis() {
        return this.ipa_emphasis;
    }

    public JSONObject rhymeLengths(String part_of_speech) {
        JSONObject rhyme_lengths = Emphasis.newEmphasisObject();
        int length = ((JSONArray) this.ipa_syllables.get(part_of_speech)).length();
        JSONObject emphasis_object = (JSONObject) this.ipa_emphasis.get(part_of_speech);

        /* get number of syllables between primary emphasis and end of word */
        int primary_index = (int) emphasis_object.get(Emphasis.PRIMARY);
        int primary_rhyme_length = length - primary_index;
        rhyme_lengths.put(Emphasis.PRIMARY, primary_rhyme_length);

        /* get numbers of syllables between secondary emphases and end of word */
        JSONArray secondaries = (JSONArray) emphasis_object.get(Emphasis.SECONDARY);
        List<Object> secondary_indexes = secondaries.toList();
        for (Object index : secondary_indexes) {
            int secondary_rhyme_length = length - (Integer) index;
            ((JSONArray) rhyme_lengths.get(Emphasis.SECONDARY)).put(secondary_rhyme_length);
        }
        return rhyme_lengths;
    }

    /**
     * Queries WordsAPI for the list of synonyms of this word, creates a Word object
     * from each (also queries WordsAPI), and adds them to this word's list of
     * synonyms.
     */
    public void populateSynonyms() {
        JSONArray synonyms_arr = apis.WordsAPI.getSynonyms(plaintext);

        for (int i = 0; i < synonyms_arr.length(); i++) {
            Word synonym = new Word(synonyms_arr.getString(i));
            synonyms.add(synonym);
        }
        synonyms.sort(null);
    }

    /**
     * Queries WordsAPI for the list of words with a type in common with this word,
     * creates a Word object from each (also queries WordsAPI), and adds them to
     * this word's list of such words.
     */
    public void populateCommonTypes() {
        JSONArray common_types_arr = apis.WordsAPI.getCommonType(plaintext);

        for (int i = 0; i < common_types_arr.length(); i++) {
            Word common_type = new Word(common_types_arr.getString(i));
            common_types.add(common_type);
        }

        common_types.sort(null);
    }

    public String plaintext() {
        return plaintext;
    }

    /**
     * I only want to order by plaintext, but equality should take more into
     * account. Arguably I should build a custom Comparator rather than setting the
     * default.
     */
    @Override
    public int compareTo(Object anotherWord) {
        if (anotherWord == null) {
            throw new NullPointerException();
        }
        if (anotherWord.getClass().equals(this.getClass())) {
            return plaintext.compareTo(((Word) anotherWord).plaintext);
        } else {
            throw new ClassCastException();
        }
    }

    public List<Word> getSubsRhyme(Word wordToRhyme, boolean synonyms, boolean common_types) {
        List<Word> substitutions = new ArrayList<>();
        if (synonyms) {
            for (Word synonym : this.synonyms) {
                if (IPAHandler.checkRhyme(synonym, wordToRhyme)) {
                    substitutions.add(synonym);
                }
            }
        }
        if (common_types) {
            for (Word common_type : this.common_types) {
                if (IPAHandler.checkRhyme(common_type, wordToRhyme)) {
                    substitutions.add(common_type);
                }
            }
        }
        return substitutions;
    }
}
