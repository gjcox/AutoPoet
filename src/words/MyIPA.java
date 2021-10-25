package words;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyIPA {

    static final Character[] VOWELS = { 'i', 'y', 'ɨ', 'ʉ', 'ɯ', 'u', 'ɪ', 'ʏ', 'ʊ', 'e', 'ø', 'ɘ', 'ɵ', 'ɤ', 'o', 'ə', 'ɛ',
            'œ', 'ɜ', 'ɞ', 'ʌ', 'ɔ', 'æ', 'ɐ', 'a', 'ɶ', 'ä', 'ɑ' };
    /*
     * 'e̞', 'ø̞', 'ɤ̞', 'o̞' were considered invalid, but according to Wikipedia
     * are only found in regional accents of English so they are unlikely to feature
     * in WordsAPI
     */

    public static List<Syllable> getSyllables(String ipa_word) {
        /*
         * logic based on
         * https://linguistics.stackexchange.com/questions/30933/how-to-split-ipa-
         * spelling-into-syllables
         */
        ArrayList<Syllable> syllables = new ArrayList<>(); 
        ArrayList<Integer> nuclei_indexes = new ArrayList<>(); 

        /* 1. locate all nuclei (vowels) */
        for (int i = 0; i < ipa_word.length(); i++) {
            char chr = ipa_word.charAt(i); 
            if (Arrays.asList(VOWELS).contains(chr)) {
                syllables.add(new Syllable(chr)); 
                nuclei_indexes.add(i); 
            }
        }

        /* 2. for each nucleus, work backward, adding sounds to the onset */
        for (int i = 0; i < nuclei_indexes.size(); i++) {

        }

        /*
         * 3. if the onset stops being valid, take a step back, then put the rest of the
         * preceding sounds into the previous nucleus' coda
         */

        return syllables;
    }

}
