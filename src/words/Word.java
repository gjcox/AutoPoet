package words;

import java.util.LinkedList;

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

    private JSONObject ipa_syllables; // / e.g. wind "ipa":{"all":[0:"wɪnd"], "noun":[0:"wɪnd"], "verb":[0:"waɪnd"]} -
    private JSONArray synonym_arr; /// / / e.g. lovely [0:"adorable", 1:"endearing", 2:"cover girl", 3:"pin-up"]
    private LinkedList<String> synonym_list;
    private JSONArray rhyme_arr; /// / / / e.g. stumble [0:"bumble", 1:"crumble", 2:"fumble", 3:"grumble",
                                 /// / / / / / / / / / / 4:"jumbal", 5:"jumble", ..., 11:"umbel"]
    private LinkedList<String> rhyme_list;

}
