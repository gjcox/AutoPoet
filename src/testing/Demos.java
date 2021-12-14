package testing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import apis.WordsAPI;
import utils.Pair;
import utils.ParameterWrappers.FilterParameters;
import utils.ParameterWrappers.SuggestionParameters;
import words.Emphasis;
import words.IPAHandler;
import words.SuperWord;
import words.Syllable;
import words.SubWord.PartOfSpeech;

import static config.Configuration.LOG;

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
            boolean rhymes = superword1.rhymesWithWrapper(superword2);
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
            System.out.println(wordObject.toFullString());
        }
    }

    public static void superWordPopulator() {
        List<String> words = Arrays.asList("hideous", "monsters", "the", "wind", "winds", "present",
                "understated");
        for (String word : words) {
            SuperWord wordObject = SuperWord.getSuperWord(word);
            wordObject.populate();
            System.out.println(wordObject.toFullString());
        }
    }

    public static void wordConstructor() {
        SuperWord present = SuperWord.getSuperWord("present");
        present.populate();
        System.out.println(present.subWordsString());
    }

    public static void abercrombieZombie() {
        SuperWord abercrombie = SuperWord.getSuperWord("abercrombie");
        abercrombie.populate();
        System.out.println(abercrombie);

        SuperWord zombie = SuperWord.getSuperWord("zombie");
        zombie.populate();
        System.out.println(zombie);

        boolean rhymes = abercrombie.rhymesWithWrapper(zombie);

        System.out.println(String.format("\"%s\" rhymes with \"%s\": %b",
                abercrombie.getPlaintext(), zombie.getPlaintext(), rhymes));
    }

    public static void demoSynonyms() {
        SuperWord present = SuperWord.getSuperWord("present");
        System.out.println(String.format("Synonyms of \"%s\" (NOUN): %s", present.getPlaintext(),
                present.getSynonyms(PartOfSpeech.NOUN)));
    }

    private static PartOfSpeech parsePoS(String pos) {
        switch (pos.toLowerCase()) {
            case "noun":
                return PartOfSpeech.NOUN;
            case "pronoun":
                return PartOfSpeech.PRONOUN;
            case "verb":
                return PartOfSpeech.VERB;
            case "adjective":
                return PartOfSpeech.ADJECTIVE;
            case "adverb":
                return PartOfSpeech.ADVERB;
            case "preposition":
                return PartOfSpeech.PREPOSITION;
            case "conjunction":
                return PartOfSpeech.CONJUCTION;
            case "definite article":
                return PartOfSpeech.DEFINITE_ARTICLE;
            default:
                return PartOfSpeech.UNKNOWN;
        }
    }

    public static void main(String[] args) {
        String usage = "java -cp src/ testing.Demos [ swc | swp | wc ] <word>" +
                "\njava -cp src/ testing.Demos [ rhyme ] <word1> <word2>" +
                "\njava -cp src/ testing.Demos [ synonyms | typeOf | hasTypes | commonlyTyped | inCategory | hasCategories | commonCategories | partOf | hasParts | similarTo ] <word> <part of speech (PoS)>"
                +
                "\njava -cp src/ testing.Demos rhyme <word1> <PoS1> <word2> <Pos2>" +
                "\njava -cp src/ testing.Demos suggestions <word> <PoS1> <rhyme with> <Pos2>";

        if (args.length < 1) {
            /* for use within VS Code */
            // superWordConstructor();
            // superWordPopulator();
            // wordConstructor();
            // demoRhymes();
            // abercrombieZombie();
            demoSynonyms();
            LOG.closeLogWriters();
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
            LOG.closeLogWriters();
            return;
        }

        if (args.length == 2) {
            SuperWord superWord;
            switch (args[0].toLowerCase()) {
                case "swc":
                    superWord = SuperWord.getSuperWord(args[1]);
                    System.out.println(superWord.toFullString());
                    break;
                case "swp":
                    superWord = SuperWord.getSuperWord(args[1]);
                    superWord.populate();
                    System.out.println(superWord.toFullString());
                    break;
                case "wc":
                    superWord = SuperWord.getSuperWord(args[1]);
                    superWord.populate();
                    System.out.println(superWord.subWordsString());
                    break;
                default:
                    System.err.println(usage);
                    break;
            }
            LOG.closeLogWriters();
            return;
        }

        if (args.length == 3) {
            SuperWord superWord1;
            SuperWord superWord2;
            PartOfSpeech pos;
            switch (args[0].toLowerCase()) {
                case "rhyme":
                    superWord1 = SuperWord.getSuperWord(args[1]);
                    superWord2 = SuperWord.getSuperWord(args[2]);
                    boolean rhymes = superWord1.rhymesWithWrapper(superWord2);
                    System.out.println(String.format("\"%s\" rhymes with \"%s\": %b",
                            superWord1.getPlaintext(), superWord2.getPlaintext(), rhymes));
                    break;
                case "synonyms":
                    superWord1 = SuperWord.getSuperWord(args[1]);
                    pos = parsePoS(args[2]);
                    System.out.println(String.format("Synonyms of \"%s\" (%s): %s", superWord1.getPlaintext(), pos,
                            superWord1.getSynonyms(pos)));
                    break;
                case "typeof":
                    superWord1 = SuperWord.getSuperWord(args[1]);
                    pos = parsePoS(args[2]);
                    System.out.println(String.format("\"%s\" (%s) is a type of: %s", superWord1.getPlaintext(), pos,
                            superWord1.getTypeOf(pos)));
                    break;
                case "hastypes":
                    superWord1 = SuperWord.getSuperWord(args[1]);
                    pos = parsePoS(args[2]);
                    System.out.println(String.format("\"%s\" (%s) has types: %s", superWord1.getPlaintext(), pos,
                            superWord1.getHasTypes(pos)));
                    break;
                case "commonlytyped":
                    superWord1 = SuperWord.getSuperWord(args[1]);
                    pos = parsePoS(args[2]);
                    System.out.println(
                            String.format("Words of the same type as \"%s\" (%s): %s", superWord1.getPlaintext(), pos,
                                    superWord1.getCommonlyTyped(pos)));
                    break;
                case "incategory":
                    superWord1 = SuperWord.getSuperWord(args[1]);
                    pos = parsePoS(args[2]);
                    System.out.println(String.format("\"%s\" (%s) is a category of: %s", superWord1.getPlaintext(), pos,
                            superWord1.getInCategory(pos)));
                    break;
                case "hascategories":
                    superWord1 = SuperWord.getSuperWord(args[1]);
                    pos = parsePoS(args[2]);
                    System.out.println(String.format("\"%s\" (%s) has categories: %s", superWord1.getPlaintext(), pos,
                            superWord1.getHasCategories(pos)));
                    break;
                case "commoncategories":
                    superWord1 = SuperWord.getSuperWord(args[1]);
                    pos = parsePoS(args[2]);
                    System.out.println(
                            String.format("Words of the same category as \"%s\" (%s): %s", superWord1.getPlaintext(),
                                    pos, superWord1.getCommonCategories(pos)));
                    break;
                case "partof":
                    superWord1 = SuperWord.getSuperWord(args[1]);
                    pos = parsePoS(args[2]);
                    System.out.println(String.format("\"%s\" (%s) is a part of: %s", superWord1.getPlaintext(), pos,
                            superWord1.getPartOf(pos)));
                    break;
                case "hasparts":
                    superWord1 = SuperWord.getSuperWord(args[1]);
                    pos = parsePoS(args[2]);
                    System.out.println(String.format("\"%s\" (%s) has parts: %s", superWord1.getPlaintext(), pos,
                            superWord1.getHasParts(pos)));
                    break;
                case "similarto":
                    superWord1 = SuperWord.getSuperWord(args[1]);
                    pos = parsePoS(args[2]);
                    System.out.println(
                            String.format("\"%s\" (%s) is similar to: %s", superWord1.getPlaintext(), pos,
                                    superWord1.getSimilarTo(pos)));
                    break;
                default:
                    System.err.println(usage);
                    break;
            }
            LOG.closeLogWriters();
            return;
        }

        if (args.length == 5) {
            SuperWord superWord1;
            SuperWord superWord2;
            PartOfSpeech pos1;
            PartOfSpeech pos2;
            SuggestionParameters suggestionParams;
            FilterParameters filterParams;

            switch (args[0].toLowerCase()) {
                case "rhyme":
                superWord1 = SuperWord.getSuperWord(args[1]);
                pos1 = parsePoS(args[2]);
                superWord2 = SuperWord.getSuperWord(args[3]);
                pos2 = parsePoS(args[4]);
                System.out.println(
                        String.format("\"%s\" (%s) rhymes with \"%s\" (%s): %s",
                                superWord1.getPlaintext(), pos1,
                                superWord2.getPlaintext(), pos2,
                                superWord1.rhymesWithWrapper(superWord2, pos1, pos2)));
                break;
                                case "suggestions":
                    // <word> <PoS1> <rhyme with> <Pos2>
                    superWord1 = SuperWord.getSuperWord(args[1]);
                    pos1 = parsePoS(args[2]);
                    superWord2 = SuperWord.getSuperWord(args[3]);
                    pos2 = parsePoS(args[4]);
                    suggestionParams = new SuggestionParameters(true, true, true, true, true, true);
                    filterParams = new FilterParameters(true, superWord2, pos2);
                    System.out.println(
                            String.format("Suggestions for \"%s\" (%s) that rhyme with \"%s\" (%s): %s",
                                    superWord1.getPlaintext(), pos1,
                                    superWord2.getPlaintext(), pos2,
                                    superWord1.getFilteredSuggestions(pos1, suggestionParams, filterParams)));
                    break;
                default:
                    System.err.println(usage);
                    break;
            }
            LOG.closeLogWriters();
            return;
        }

        System.err.println(usage);
        LOG.closeLogWriters();

    }
}
