package testing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

import words.IPAHandler;
import words.Syllable;

public class UnitTests {

    /*
     * Personally I tend to find unit test of questionable value, given that no
     * class actually runs in isolation, but I will likely set some up in the
     * fullness of time.
     */

    /**
     * Tests that words.IPAHandler.getSyllables() behaves as expected 
     */
    @Test
    public void testGetSyllable() {
        List<String> words = Arrays.asList("example", "mastery", "testing", "mistake", "sky", "cure");
        List<String> ipa_words = Arrays.asList("ɪɡ'zæmpəl", "'mæstəri", "'tɛstɪŋ", "mɪ'steɪk", "skaɪ", "kjʊr");
        List<Syllable> example = new LinkedList<>();
        example.add(new Syllable("", "ɪ", "ɡ"));
        example.add(new Syllable("z", "æ", "m"));
        example.add(new Syllable("p", "ə", "l"));
        List<Syllable> mastery = new LinkedList<>();
        mastery.add(new Syllable("m", "æ", ""));
        mastery.add(new Syllable("st", "ə", ""));
        mastery.add(new Syllable("r", "i", ""));
        List<Syllable> testing = new LinkedList<>();
        testing.add(new Syllable("t", "ɛ", ""));
        testing.add(new Syllable("st", "ɪ", "ŋ"));
        List<Syllable> mistake = new LinkedList<>();
        mistake.add(new Syllable("m", "ɪ", ""));
        mistake.add(new Syllable("st", "eɪ", "k"));
        List<Syllable> sky = new LinkedList<>();
        sky.add(new Syllable("sk", "aɪ", ""));
        List<Syllable> cure = new LinkedList<>();
        cure.add(new Syllable("kj", "ʊ", "r"));
        List<List<Syllable>> checks = new LinkedList<>();
        checks.add(example);
        checks.add(mastery);
        checks.add(testing);
        checks.add(mistake);
        checks.add(sky);
        checks.add(cure);

        for (int i = 0; i < words.size(); i++) {
            List<Syllable> syllables = IPAHandler.getSyllables(ipa_words.get(i));
            List<Syllable> check = checks.get(i);
            for (int j = 0; j < syllables.size(); j++) {
                System.out.println(syllables.size());
                System.out.println(check.size());
                assertEquals(check.get(j), syllables.get(j));
            }
        }
    }

    public static void main(String[] args) {
    }
}
