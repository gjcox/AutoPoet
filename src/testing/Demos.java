package testing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import apis.WordsAPI;
import utils.Pair;
import words.Emphasis;
import words.IPAHandler;
import words.SuperWord;
import words.Syllable;

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

    private static void demoGetSyllables(String SuperWord, String ipa) {
        Pair<ArrayList<Syllable>, Emphasis> pair = IPAHandler.getSyllables(ipa);
        ArrayList<Syllable> syllables = pair.one();
        Emphasis emphasis = pair.two();
        System.out.println(SuperWord + ";" + ipa + ": ");
        System.out.println(syllables.toString());
        System.out.println(emphasis.toString());

    }

    private static void demoRhymes() {
        List<String> words1 = Arrays.asList("test", "test", "hideous", "attack", "whim", "painted", "painted",
                "overstated", "stated", "fated", "zombie", "zombie");
        List<String> words2 = Arrays.asList("guest", "human", "insidious", "aback", "dim", "acquainted", "understated",
                "understated", "understated", "understated", "abercrombie", "bee");

        for (int i = 0; i < words1.size(); i++) {
            SuperWord superword1 = SuperWord.getSuperWord(words1.get(i));
            SuperWord superword2 = SuperWord.getSuperWord(words2.get(i));
            boolean rhymes = superword1.rhymesWith(superword2);
            System.out.println(String.format("\"%s\" rhymes with \"%s\": %b",
                    words1.get(i), words2.get(i), rhymes));
        }
    }

    public static void getIPA() {
        List<String> words = Arrays.asList("hideous", "monsters", "the", "wind", "winds");
        for (String SuperWord : words) {
            JSONObject ipa = WordsAPI.getIPA(SuperWord);
            System.out.print(SuperWord + ": ");
            System.out.println(ipa.toString());
        }
    }

    public static void superWordConstructor() {
        List<String> words = Arrays.asList("hideous", "monsters", "the", "wind", "winds", "present", "understated");
        for (String word : words) {
            SuperWord wordObject = SuperWord.getSuperWord(word);
            System.out.println(wordObject.toString());
        }
    }

    public static void superWordPopulator() {
        List<String> words = Arrays.asList("hideous", "monsters", "the", "wind", "winds", "present",
                "understated");
        for (String word : words) {
            SuperWord wordObject = SuperWord.getSuperWord(word);
            wordObject.populate();
            System.out.println(wordObject.toString());
        }
    }

    public static void wordConstructor() {
        SuperWord present = SuperWord.getSuperWord("present");
        present.populate();
        System.out.println(present.getSubWords());
    }

    public static void abercrombieZombie() {
        SuperWord abercrombie = SuperWord.getSuperWord("abercrombie");
        abercrombie.populate();
        System.out.println(abercrombie);

        SuperWord zombie = SuperWord.getSuperWord("zombie");
        zombie.populate();
        System.out.println(zombie);

        boolean rhymes = abercrombie.rhymesWith(zombie);

        System.out.println(String.format("\"%s\" rhymes with \"%s\": %b",
                abercrombie.getPlaintext(), zombie.getPlaintext(), rhymes));
    }

    public static void main(String[] args) {
        String usage = "java -cp src/ testing.Demos [ swc | swp | wc | rhyme ]";

        if (args.length < 1) {
            /* for use within VS Code */
            // superWordConstructor();
            // superWordPopulator();
            // wordConstructor();
            demoRhymes();
            // abercrombieZombie();
            return;
        }

        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "swc":
                    superWordConstructor();
                    break;
                case "swp":
                    superWordPopulator();
                    break;
                case "wc":
                    wordConstructor();
                    break;
                case "rhyme":
                    demoRhymes();
                    break;
                default:
                    System.err.println(usage);
            }
            return;
        }

        if (args.length == 2) {
            SuperWord superWord;
            switch (args[0].toLowerCase()) {
                case "swc":
                    superWord = SuperWord.getSuperWord(args[1]);
                    System.out.println(superWord.toString());
                    break;
                case "swp":
                    superWord = SuperWord.getSuperWord(args[1]);
                    superWord.populate();
                    System.out.println(superWord.toString());
                    break;
                case "wc":
                    superWord = SuperWord.getSuperWord(args[1]);
                    superWord.populate();
                    System.out.println(superWord.getSubWords());
                    break;
                default:
                    System.err.println(usage);
                    break;
            }
            return;
        }

        if (args.length == 3) {
            SuperWord superWord1;
            SuperWord superWord2;
            switch (args[0].toLowerCase()) {
                case "rhyme":
                    superWord1 = SuperWord.getSuperWord(args[1]);
                    superWord2 = SuperWord.getSuperWord(args[2]);
                    boolean rhymes = superWord1.rhymesWith(superWord2);
                    System.out.println(String.format("\"%s\" rhymes with \"%s\": %b",
                            superWord1.getPlaintext(), superWord2.getPlaintext(), rhymes));
                    break;
                default:
                    System.err.println(usage);
                    break;
            }
            return;
        }

        System.err.println(usage);
    }
}
