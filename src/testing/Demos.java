package testing;

import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import apis.WordsAPI;
import utils.JSONConstructors.Emphasis;
import utils.Pair;
import words.IPAHandler;
import words.Word;

public class Demos {

    private static List<String> plaintexts = Arrays.asList("example", "mastery", "testing", "mistake", "sky", "cure",
            "hideous", "insidious", "attack", "aback", "wind", "wind", "hideous", "bilious");
    private static List<String> ipas = Arrays.asList("ɪɡ'zæmpəl", "'mæstəri", "'tɛstɪŋ", "mɪ'steɪk", "skaɪ", "kjʊr",
            "hɪdiəs", "ɪn'sɪdiəs", "ə'tæk", "ə'bæk", "wɪnd", "waɪnd", "hɪdiəs", "'bɪljəs");

    private static void demoGetSyllables() {
        for (int i = 0; i < plaintexts.size(); i++) {
            demoGetSyllables(plaintexts.get(i), ipas.get(i));
        }
    }

    private static void demoGetSyllables(String word, String ipa) {
        Pair<JSONArray, JSONObject> pair = IPAHandler.getSyllables(ipa);
        JSONArray syllables = pair.one();
        JSONObject emphasis = pair.two();
        System.out.println(word + ";" + ipa + ": ");
        System.out.println(syllables.toString());
        System.out.println(emphasis.toString());

    }

    private static void demoRhymes() {
        List<String> words_1 = Arrays.asList("test", "test", "hideous", "attack", "whim", "painted", "painted",
                "overstated", "stated", "fated", "zombie", "zombie");
        List<String> ipa_words_1 = Arrays.asList("tɛst", "tɛst", "hɪdiəs", "ə'tæk", "wɪm", "'peɪntɪd", "'peɪntɪd",
                "'oʊvɝr,steɪtɪd", "'steɪtɪd", "'feɪtɪd", "'zɑmbi", "'zɑmbi");
        List<String> words_2 = Arrays.asList("guest", "human", "insidious", "aback", "dim", "acquainted", "understated",
                "understated", "understated", "understated", "abercrombie", "bee");
        List<String> ipa_words_2 = Arrays.asList("ɡɛst", "'hjumən", "ɪn'sɪdiəs", "ə'bæk", "dɪm", "ə'kweɪntɪd",
                "'ʌndɝr,steɪtɪd", "'ʌndɝr,steɪtɪd", "'ʌndɝr,steɪtɪd", "'ʌndɝr,steɪtɪd", "'æbər,krɑmbi", "bi");

        for (int i = 0; i < words_1.size(); i++) {
            Word word_1 = new Word(words_1.get(i), ipa_words_1.get(i));
            Word word_2 = new Word(words_2.get(i), ipa_words_2.get(i));
            boolean rhymes = IPAHandler.checkRhyme(word_1, word_2);
            System.out.println(String.format("\"%s\" rhymes with \"%s\": %b", words_1.get(i), words_2.get(i), rhymes));
        }
    }

    public static void getIPA() {
        List<String> words = Arrays.asList("hideous", "monsters", "the", "wind", "winds");
        for (String word : words) {
            JSONObject ipa = WordsAPI.getIPA(word);
            System.out.print(word + ": ");
            System.out.println(ipa.toString());
        }
    }

    public static void wordConstructor() {
        List<String> words = Arrays.asList("hideous", "monsters", "the", "wind", "winds", "present", "understated");
        for (String word : words) {
            Word word_object = new Word(word);
            System.out.print(word + ": ");
            System.out.println(word_object.toString());
        }
    }

    public static void wordPopulator() {
        List<String> words = Arrays.asList(/*"hideous", */"monsters"/*, "the", "wind", "winds", "present", "understated"*/);
        for (String word : words) {
            Word word_object = new Word(word);
            word_object.populateSynonyms();
            word_object.populateCommonTypes();
            System.out.print(word + ": ");
            System.out.println(word_object.toString());
        }
    }

    public static void presentPresent() {
        Word present = new Word("present");
        Word resent = new Word("resent");
        boolean rhymes = IPAHandler.checkRhyme(present, resent);
        System.out.println(
                String.format("\"%s\" rhymes with \"%s\": %b", present.plaintext(), resent.plaintext(), rhymes));

    }

    public static void rhymeLengths() {
        Word understated = new Word("understated");
        System.out.println(understated);
        JSONObject rhyme_lengths = understated.rhymeLengths("all");
        System.out.println(rhyme_lengths);
    }

    public static void newEmphasisObject() {
        JSONObject object = Emphasis.newEmphasisObject();
        System.out.println(object);
        object.put(Emphasis.PRIMARY, 1);
        System.out.println(object);
        ((JSONArray) object.get(Emphasis.SECONDARY)).put(2);
        System.out.println(object);
        ((JSONArray) object.get(Emphasis.SECONDARY)).put(3);
        System.out.println(object);
    }

    public static void abercrombieZombie() {
        Word abercrombie = new Word("abercrombie");
        Word zombie = new Word("zombie");
        System.out.println(abercrombie);
        System.out.println(zombie);
    }

    public static void main(String[] args) {
        // demoGetSyllables("acquainted", "ə'kweɪntɪd");
        // demoGetSyllables("painted", "'peɪntɪd");
        // demoRhymes();
        // getIPA();
        // wordConstructor();
        // wordPopulator();
        // presentPresent();
        // rhymeLengths();
        // newEmphasisObject();
        // abercrombieZombie();
        
        Word word_object = new Word("devil");
        System.out.print("devil" + ": ");
        System.out.println(word_object.toString());
    }
}
