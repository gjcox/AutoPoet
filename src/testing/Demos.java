package testing;

import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import words.APICalls;
import words.IPAHandler;
import words.Syllable;
import words.Word;

public class Demos {

    private static void demoGetSyllables() {
        List<String> words = Arrays.asList("example", "mastery", "testing", "mistake", "sky", "cure", "hideous",
                "insidious", "attack", "aback", "wind", "wind");
        List<String> ipa_words = Arrays.asList("ɪɡ'zæmpəl", "'mæstəri", "'tɛstɪŋ", "mɪ'steɪk", "skaɪ", "kjʊr", "hɪdiəs",
                "ɪn'sɪdiəs", "ə'tæk", "ə'bæk", "wɪnd", "waɪnd");

        for (int i = 0; i < words.size(); i++) {
            List<Syllable> syllables = IPAHandler.getSyllables(ipa_words.get(i));
            System.out.print(words.get(i) + ";" + ipa_words.get(i) + ": ");
            for (Syllable syl : syllables) {
                System.out.print(syl + ".");
            }
            System.out.println();
        }
    }

    private static void demoRhymes1() {
        List<String> words_1 = Arrays.asList("test", "test", "attack");
        List<String> ipa_words_1 = Arrays.asList("tɛst", "tɛst", "ə'tæk");
        List<String> words_2 = Arrays.asList("guest", "human", "aback");
        List<String> ipa_words_2 = Arrays.asList("ɡɛst", "'hjumən", "ə'bæk");

        for (int i = 0; i < words_1.size(); i++) {
            List<Syllable> syllables_1 = IPAHandler.getSyllables(ipa_words_1.get(i));
            List<Syllable> syllables_2 = IPAHandler.getSyllables(ipa_words_2.get(i));
            boolean rhymes = IPAHandler.checkRhyme(syllables_1, syllables_2, 1);
            System.out.println(String.format("\"%s\" rhymes with \"%s\" for 1 syllable: %b", words_1.get(i),
                    words_2.get(i), rhymes));
        }
    }

    private static void demoRhymes2() {
        List<String> words_1 = Arrays.asList("hideous", "attack", "whim");
        List<String> ipa_words_1 = Arrays.asList("hɪdiəs", "ə'tæk", "wɪm");
        List<String> words_2 = Arrays.asList("insidious", "aback", "dim");
        List<String> ipa_words_2 = Arrays.asList("ɪn'sɪdiəs", "ə'bæk", "dɪm");

        for (int i = 0; i < words_1.size(); i++) {
            List<Syllable> syllables_1 = IPAHandler.getSyllables(ipa_words_1.get(i));
            List<Syllable> syllables_2 = IPAHandler.getSyllables(ipa_words_2.get(i));
            boolean rhymes = IPAHandler.checkRhyme(syllables_1, syllables_2);
            int syllables = Math.min(syllables_1.size(), syllables_2.size());
            System.out.println(String.format("\"%s\" rhymes with \"%s\" for %d syllable%s: %b", words_1.get(i),
                    words_2.get(i), syllables, syllables == 1 ? "" : "s", rhymes));
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
        List<String> words = Arrays.asList("hideous", "monsters", "the", "wind", "winds");
        for (String word : words) {
            Word word_object = new Word(word);
            System.out.print(word + ": ");
            System.out.println(word_object.toString());
        }
    }

    public static void main(String[] args) {
        // demoGetSyllables();
        // demoRhymes1();
        // demoRhymes2();
        // getIPA();
        // wordConstructor();
    }
}
