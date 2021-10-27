package words;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Word {

    private String plaintext;
    private JSONObject plain_syllables; // e.g. incredible "syllables":{"count":4,
                                        /// / / / / / / / / / / / / / / "list":[0:"in", 1:"cred", 2:"i", 3:"ble"]} -
    private JSONObject ipa; // / / / / / / e.g. wind "ipa":{"all":"wɪnd", "noun":"wɪnd", "verb":"waɪnd"} -

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

    private JSONObject ipa_syllables = new JSONObject(); // / e.g. wind "ipa":{"all":[0:"wɪnd"], "noun":[0:"wɪnd"], "verb":[0:"waɪnd"]} -
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
                    List<Syllable> ipa_syllables_list = IPAHandler.getSyllables(ipa_); 
                    JSONArray ipa_syllables_arr = new JSONArray(); 
                    for (Syllable syllable : ipa_syllables_list) {
                        ipa_syllables_arr.put(syllable.toJsonObject());
                    }
                    this.ipa_syllables.put(key, ipa_syllables_arr); 
                }
        
    }

    public JSONObject toJsonObject() {
        JSONObject jo = new JSONObject(); 
        jo.put("plaintext", plaintext);
        jo.put("plain_syllables", plain_syllables);
        jo.put("ipa", ipa);
        jo.put("ipa_syllables", ipa_syllables);
        jo.put("synonym_arr", synonym_arr);
        jo.put("synonym_list", synonym_list);
        jo.put("rhyme_arr", rhyme_arr);
        jo.put("rhyme_list", rhyme_list);
        return jo; 
    }

    public String toString() {
        return this.toJsonObject().toString();
    }
}
