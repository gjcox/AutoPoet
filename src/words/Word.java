package words;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import utils.EmphasisKeys;
import utils.Pair;

public class Word {

    private String plaintext;
    private JSONObject plain_syllables; // e.g. incredible "syllables":{"count":4,
                                        /// / / / / / / / / / / / / / / "list":[0:"in", 1:"cred", 2:"i", 3:"ble"]} -
    private JSONObject ipa; // / / / / / / e.g. wind "ipa":{"all":"'prɛzənt", "noun":"'prɛzənt",
                            /// / / / / / / / / / / / / / / "verb":"prɪ'zɛnt"} -

    /*
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

    private JSONObject ipa_syllables = new JSONObject(); // / e.g. present
                                                         // :{"all":[{"coda":"","nucleus":"ɛ","onset":"pr"},{"coda":"nt","nucleus":"ə","onset":"z"}],
                                                         // "noun":[{"coda":"","nucleus":"ɛ","onset":"pr"},{"coda":"nt","nucleus":"ə","onset":"z"}],
                                                         // "verb":[{"coda":"","nucleus":"ɪ","onset":"pr"},{"coda":"nt","nucleus":"ɛ","onset":"z"}]}-
    private JSONObject ipa_emphasis = new JSONObject(); // e.g."ipa_emphasis":{"all":{"secondary":[],"has_secondary":false,"primary":0},
                                                        // "verb":{"secondary":[],"has_secondary":false,"primary":1},
                                                        // "noun":{"secondary":[],"has_secondary":false,"primary":0}}-
    private JSONArray synonym_arr; /// / / e.g. lovely [0:"adorable", 1:"endearing", 2:"cover girl", 3:"pin-up"]
    private LinkedList<String> synonym_list;
    private JSONArray rhyme_arr; /// / / / e.g. stumble [0:"bumble", 1:"crumble", 2:"fumble", 3:"grumble",
                                 /// / / / / / / / / / / 4:"jumbal", 5:"jumble", ..., 11:"umbel"]
    private LinkedList<String> rhyme_list;

    public Word(String plaintext) {
        this.plaintext = plaintext;
        this.ipa = APICalls.getIPA(plaintext);

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

    public JSONObject toJsonObject() {
        JSONObject jo = new JSONObject();
        jo.put("plaintext", plaintext);
        jo.put("plain_syllables", plain_syllables);
        jo.put("ipa", ipa);
        jo.put("ipa_syllables", ipa_syllables);
        jo.put("ipa_emphasis", ipa_emphasis);
        jo.put("synonym_arr", synonym_arr);
        jo.put("synonym_list", synonym_list);
        jo.put("rhyme_arr", rhyme_arr);
        jo.put("rhyme_list", rhyme_list);
        return jo;
    }

    public String toString() {
        return this.toJsonObject().toString();
    }

    /* Start of getters and setters */
    public JSONObject ipaSyllables() {
        return this.ipa_syllables;
    }

    public JSONObject ipaEmphasis() {
        return this.ipa_emphasis;
    }

    public JSONObject rhymeLengths(String part_of_speech) {
        JSONObject rhyme_lengths = EmphasisKeys.newEmphasisObject(); 
        int length = ((JSONArray) this.ipa_syllables.get(part_of_speech)).length();
        JSONObject emphasis_object = (JSONObject) this.ipa_emphasis.get(part_of_speech);

        /* get number of syllables between primary emphasis and end of word */
        int primary_index = (int) emphasis_object.get(EmphasisKeys.PRIMARY);
        int primary_rhyme_length = length - primary_index;
        rhyme_lengths.put(EmphasisKeys.PRIMARY, primary_rhyme_length);

        /* get numbers of syllables between secondary emphases and end of word */
        JSONArray secondaries = (JSONArray) emphasis_object.get(EmphasisKeys.SECONDARY);
        List<Object> secondary_indexes = secondaries.toList();
        for (Object index : secondary_indexes) {
            int secondary_rhyme_length = length - (Integer) index;
            ((JSONArray) rhyme_lengths.get(EmphasisKeys.SECONDARY)).put(secondary_rhyme_length);
        }
        return rhyme_lengths;
    }

    public String plaintext() {
        return plaintext;
    }
}
