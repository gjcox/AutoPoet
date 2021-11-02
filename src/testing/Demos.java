package testing;

import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import utils.Pair;
import words.APICalls;
import words.IPAHandler;
import words.Syllable;
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
                "overstated", "zombie");
        List<String> ipa_words_1 = Arrays.asList("tɛst", "tɛst", "hɪdiəs", "ə'tæk", "wɪm", "'peɪntɪd", "'peɪntɪd",
                "'oʊvɝr,steɪtɪd", "'zɑmbi");
        List<String> words_2 = Arrays.asList("guest", "human", "insidious", "aback", "dim", "acquainted", "understated",
                "understated", "bee");
        List<String> ipa_words_2 = Arrays.asList("ɡɛst", "'hjumən", "ɪn'sɪdiəs", "ə'bæk", "dɪm", "ə'kweɪntɪd",
                "'ʌndɝr,steɪtɪd", "'ʌndɝr,steɪtɪd", "bi");

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
            JSONObject ipa = APICalls.getIPA(word);
            System.out.print(word + ": ");
            System.out.println(ipa.toString());
        }
    }

    public static void wordConstructor() {
        List<String> words = Arrays.asList("hideous", "monsters", "the", "wind", "winds", "present");
        for (String word : words) {
            Word word_object = new Word(word);
            System.out.print(word + ": ");
            System.out.println(word_object.toString());
        }
    }

    public static void main(String[] args) {
        // demoGetSyllables("acquainted", "ə'kweɪntɪd");
        // demoGetSyllables("painted", "'peɪntɪd");
        demoRhymes();
        // getIPA();
        // wordConstructor();
    }
}
