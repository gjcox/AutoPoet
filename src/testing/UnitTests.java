package testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

import org.junit.jupiter.api.Test;

import exceptions.RhymingSchemeSizeException;
import utils.Pair;
import utils.ParameterWrappers.FilterParameters.Filter;
import words.Emphasis;
import words.IPAHandler;
import words.RhymingScheme;
import words.SuperWord;
import words.Syllable;

public class UnitTests {


    /**
     * Tests that words.IPAHandler.getSyllables() behaves as expected
     */
    @Test
    public void testGetSyllable() {
        // plaintext words : "example", "mastery", "testing", "mistake", "sky", "cure", "even so" 
        List<String> ipaWords = Arrays.asList("ɪɡ'zæmpəl", "'mæstəri", "'tɛstɪŋ",
                "mɪ'steɪk", "skaɪ", "kjʊr", "kəm_ə'lɔŋ");
        Pair<List<Syllable>, Emphasis> example = new Pair<>(new LinkedList<>(), new Emphasis());
        example.one().add(new Syllable("", "ɪ", "ɡ"));
        example.one().add(new Syllable("z", "æ", "m"));
        example.one().add(new Syllable("p", "ə", "l"));
        example.two().setPrimary(1);
        Pair<List<Syllable>, Emphasis> mastery = new Pair<>(new LinkedList<>(), new Emphasis());
        mastery.one().add(new Syllable("m", "æ", ""));
        mastery.one().add(new Syllable("st", "ə", ""));
        mastery.one().add(new Syllable("r", "i", ""));
        Pair<List<Syllable>, Emphasis> testing = new Pair<>(new LinkedList<>(), new Emphasis());
        testing.one().add(new Syllable("t", "ɛ", ""));
        testing.one().add(new Syllable("st", "ɪ", "ŋ"));
        Pair<List<Syllable>, Emphasis> mistake = new Pair<>(new LinkedList<>(), new Emphasis());
        mistake.one().add(new Syllable("m", "ɪ", ""));
        mistake.one().add(new Syllable("st", "eɪ", "k"));
        mistake.two().setPrimary(1); 
        Pair<List<Syllable>, Emphasis> sky = new Pair<>(new LinkedList<>(), new Emphasis());
        sky.one().add(new Syllable("sk", "aɪ", ""));
        Pair<List<Syllable>, Emphasis> cure = new Pair<>(new LinkedList<>(), new Emphasis());
        cure.one().add(new Syllable("kj", "ʊ", "r"));
        Pair<List<Syllable>, Emphasis> come_along = new Pair<>(new LinkedList<>(), new Emphasis());
        come_along.one().add(new Syllable("k", "ə", ""));
        come_along.one().add(new Syllable("m", "ə", ""));
        come_along.one().add(new Syllable("l", "ɔ", "ŋ"));
        come_along.two().setPrimary(2);
        come_along.two().addSecondary(0);
        List<Pair<List<Syllable>, Emphasis>> checks = new LinkedList<>();
        checks.add(example);
        checks.add(mastery);
        checks.add(testing);
        checks.add(mistake);
        checks.add(sky);
        checks.add(cure);
        checks.add(come_along);
        
        for (int i = 0; i < ipaWords.size(); i++) {
            Pair<ArrayList<Syllable>, Emphasis> output = IPAHandler.getSyllables(ipaWords.get(i));
            Pair<List<Syllable>, Emphasis> check = checks.get(i);
            assertEquals(check.two(), output.two()); 
            for (int j = 0; j < output.one().size(); j++) {
                assertEquals(check.one().get(j), output.one().get(j));
            }
        }
    }

    @Test
    public void testDuplicateSorting() {
        ArrayList<String> list1 = new ArrayList<>(Arrays.asList("a", "b", "m"));
        ArrayList<String> list2 = new ArrayList<>(Arrays.asList("c", "n", "m"));
        ArrayList<String> list3 = new ArrayList<>(Arrays.asList("n", "x", "m"));
        ArrayList<String> list4 = new ArrayList<>(Arrays.asList("y", "z", "m"));
        ArrayList<String> list5 = null;
        ArrayList<ArrayList<String>> precombined = new ArrayList<>(Arrays.asList(list1, list2, list3, list4, list5));
        List<String> combined = utils.NullListOperations.combineListsPrioritiseDuplicates(precombined);
        assertEquals(8, combined.size());
        assertEquals(Arrays.asList("m", "n", "a", "b", "c", "x", "y", "z"), combined);
    }

    @Test
    public void testRhymingSchemeException() {
        assertThrows(RhymingSchemeSizeException.class, () -> {
            new RhymingScheme(1, new int[] { 0, 0 });
        });
    }

    @Test
    public void testRhymingSchemeString() {
        RhymingScheme scheme = new RhymingScheme(0);
        try {
            scheme = new RhymingScheme(4, new int[] { 0, 1, 26, 28 });
        } catch (RhymingSchemeSizeException e) {
            fail();
        }
        assertEquals("#AZB", scheme.toString());
    }

    @Test
    public void testPerfectRhymeTrue() {
        SuperWord word = SuperWord.getSuperWord("word");
        SuperWord curd = SuperWord.getSuperWord("curd");
        assertTrue(word.matchesWithWrapper(Filter.PERFECT_RHYME, curd));

        SuperWord magic = SuperWord.getSuperWord("magic");
        SuperWord tragic = SuperWord.getSuperWord("tragic");
        assertTrue(magic.matchesWithWrapper(Filter.PERFECT_RHYME, tragic));

        SuperWord abercrombie = SuperWord.getSuperWord("abercrombie");
        SuperWord zombie = SuperWord.getSuperWord("zombie");
        assertTrue(abercrombie.matchesWithWrapper(Filter.PERFECT_RHYME, zombie));
    }

    @Test
    public void testPerfectRhymeFalse() {
        SuperWord word = SuperWord.getSuperWord("word");
        SuperWord worm = SuperWord.getSuperWord("worm");
        assertFalse(word.matchesWithWrapper(Filter.PERFECT_RHYME, worm));

        SuperWord understated = SuperWord.getSuperWord("understated");
        SuperWord overstated = SuperWord.getSuperWord("overstated");
        assertFalse(understated.matchesWithWrapper(Filter.PERFECT_RHYME, overstated));

        SuperWord bee = SuperWord.getSuperWord("bee");
        SuperWord zombie = SuperWord.getSuperWord("zombie");
        assertFalse(bee.matchesWithWrapper(Filter.PERFECT_RHYME, zombie));
    }

    @Test
    public void testSyllabicRhymeTrue() {
        SuperWord fuddle = SuperWord.getSuperWord("fuddle");
        SuperWord fiddle = SuperWord.getSuperWord("fiddle");
        assertTrue(fuddle.matchesWithWrapper(Filter.SYLLABIC_RHYME, fiddle));

        SuperWord cleaver = SuperWord.getSuperWord("cleaver");
        SuperWord silver = SuperWord.getSuperWord("silver");
        assertTrue(cleaver.matchesWithWrapper(Filter.SYLLABIC_RHYME, silver));
    }

    @Test
    public void testImperfectRhymeTrue() {
        SuperWord bee = SuperWord.getSuperWord("bee");
        SuperWord zombie = SuperWord.getSuperWord("zombie");
        assertTrue(bee.matchesWithWrapper(Filter.IMPERFECT_RHYME, zombie));

        SuperWord understated = SuperWord.getSuperWord("understated");
        SuperWord overstated = SuperWord.getSuperWord("overstated");
        assertTrue(understated.matchesWithWrapper(Filter.IMPERFECT_RHYME, overstated));
    }

    @Test
    public void testWeakRhymeTrue() {
        SuperWord hammer = SuperWord.getSuperWord("hammer");
        SuperWord carpenter = SuperWord.getSuperWord("carpenter");
        assertTrue(hammer.matchesWithWrapper(Filter.WEAK_RHYME, carpenter));

        SuperWord sediment = SuperWord.getSuperWord("sediment");
        SuperWord dependent = SuperWord.getSuperWord("dependent");
        assertTrue(sediment.matchesWithWrapper(Filter.WEAK_RHYME, dependent));
    }

    public static void main(String[] args) {
    }
}
