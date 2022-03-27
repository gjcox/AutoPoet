package testing;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import utils.Pair;
import utils.ParameterWrappers.FilterParameters;
import utils.ParameterWrappers.SuggestionPoolParameters;
import utils.ParameterWrappers.FilterParameters.RhymeType;
import utils.ParameterWrappers.SuggestionPoolParameters.SuggestionPool;
import words.Emphasis;
import words.IPAHandler;
import words.PartOfSpeech;
import words.Poem;
import words.SuperWord;
import words.Syllable;

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

    private static void demoGetSyllables(String ipa) {
        Pair<ArrayList<Syllable>, Emphasis> pair = IPAHandler.getSyllables(ipa);
        ArrayList<Syllable> syllables = pair.one();
        Emphasis emphasis = pair.two();
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
            boolean rhymes = superword1.matchesWithWrapper(RhymeType.PERFECT_RHYME, superword2);
            System.out.println(String.format("\"%s\" rhymes with \"%s\": %b",
                    words1.get(i), words2.get(i), rhymes));
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

    public static void subWordConstructor() {
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

        boolean rhymes = abercrombie.matchesWithWrapper(RhymeType.PERFECT_RHYME, zombie);

        System.out.println(String.format("\"%s\" rhymes with \"%s\": %b",
                abercrombie.getPlaintext(), zombie.getPlaintext(), rhymes));
    }

    public static void demoSynonyms() {
        SuperWord present = SuperWord.getSuperWord("present");
        System.out.println(String.format("Synonyms of \"%s\" (NOUN): %s", present.getPlaintext(),
                present.getSuggestionPool(SuggestionPool.SYNONYMS, PartOfSpeech.NOUN, false)));
    }

    public static void demoPoemConstructor(String poemFile) {
        try {
            Path path = FileSystems.getDefault().getPath(poemFile);
            Poem poem = new Poem(path);
            System.out.println(poem.getString());
        } catch (IOException e) {
            System.err.println("Something went wrong: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String usage = "java -cp lib/json-20210307.jar:src/ testing.Demos [ swc | swp | wc | rhyme ]"
                + "\njava -cp lib/json-20210307.jar:src/ testing.Demos [ swc | swp | wc ] <word>"
                + "\njava -cp lib/json-20210307.jar:src/ testing.Demos syllables <ipa>"
                + "\njava -cp lib/json-20210307.jar:src/ testing.Demos [ rhyme ] <word1> <word2>"
                + "\njava -cp lib/json-20210307.jar:src/ testing.Demos [ synonyms | typeOf | has types | commonly typed | in category | has categories | commonly categorised | part of | has parts | similar to ] <word> <part of speech (PoS)>"
                + "\njava -cp lib/json-20210307.jar:src/ testing.Demos rhyme <word1> <PoS1> <word2> <Pos2>"
                + "\njava -cp lib/json-20210307.jar:src/ testing.Demos suggestions <word> <PoS1> <rhyme with> <Pos2>"
                + "\njava -cp lib/json-20210307.jar:src/ testing.Demos poem <poem.txt>";

        if (args.length < 1) {
            /* for use within VS Code */
            // superWordConstructor();
            // superWordPopulator();
            // wordConstructor();
            // demoRhymes();
            abercrombieZombie();
            // demoSynonyms();
            // demoPoemConstructor("temp.txt");
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
                    subWordConstructor();
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
                case "poem":
                    demoPoemConstructor(args[1]);
                    break;
                case "syllables":
                    demoGetSyllables(args[1]);
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
                    boolean rhymes = superWord1.matchesWithWrapper(RhymeType.PERFECT_RHYME, superWord2);
                    System.out.println(String.format("\"%s\" rhymes with \"%s\": %b",
                            superWord1.getPlaintext(), superWord2.getPlaintext(), rhymes));
                    break;
                default:
                    SuggestionPool pool = SuggestionPool.fromString(args[0]);
                    superWord1 = SuperWord.getSuperWord(args[1]);
                    pos = PartOfSpeech.fromString(args[2]);
                    if (pool == null || pos == null) {
                        System.err.println(usage);
                    } else {
                        System.out.println(String.format("\"%s\" (%s) %s: %s", superWord1.getPlaintext(), pos,
                                pool.getLabel(), superWord1.getSuggestionPool(pool, pos, false)));
                        break;
                    }
            }
            LOG.closeLogWriters();
            return;
        }

        if (args.length == 5) {
            SuperWord superWord1;
            SuperWord superWord2;
            PartOfSpeech pos1;
            PartOfSpeech pos2;
            SuggestionPoolParameters suggestionParams;
            FilterParameters filterParams;

            switch (args[0].toLowerCase()) {
                case "rhyme":
                    superWord1 = SuperWord.getSuperWord(args[1]);
                    pos1 = PartOfSpeech.fromString(args[2]);
                    superWord2 = SuperWord.getSuperWord(args[3]);
                    pos2 = PartOfSpeech.fromString(args[4]);
                    System.out.println(
                            String.format("\"%s\" (%s) rhymes with \"%s\" (%s): %s",
                                    superWord1.getPlaintext(), pos1,
                                    superWord2.getPlaintext(), pos2,
                                    superWord1.rhymesWithWrapper(RhymeType.PERFECT_RHYME, superWord2, pos1, pos2)));
                    break;
                case "suggestions":
                    superWord1 = SuperWord.getSuperWord(args[1]);
                    pos1 = PartOfSpeech.fromString(args[2]);
                    superWord2 = SuperWord.getSuperWord(args[3]);
                    pos2 = PartOfSpeech.fromString(args[4]);
                    suggestionParams = new SuggestionPoolParameters();
                    filterParams = new FilterParameters();
                    filterParams.setRhymeFilter(RhymeType.PERFECT_RHYME, superWord2);
                    filterParams.setMatchPoS(pos2);
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
