package words;

import java.util.LinkedList;
import java.util.List;

public class IPAHandler extends AbstractIPA {

    public static List<Syllable> getSyllables(String ipa_word) {
        /*
         * logic based on
         * https://linguistics.stackexchange.com/questions/30933/how-to-split-ipa-
         * spelling-into-syllables
         */
        ipa_word = ipa_word.replace("\'", ""); // remove emphasis character
        LinkedList<Syllable> syllables = new LinkedList<>();
        LinkedList<Integer> vowel_indexes = new LinkedList<>();
        LinkedList<Integer> nucleus_indexes = new LinkedList<>();
        LinkedList<Integer> onset_indexes = new LinkedList<>(); // the start of onsets; onset_indexes[i] should
                                                                // correspond
                                                                // to nuclei_indexes[i]

        /* 1. locate all nuclei (vowels) */
        for (int i = 0; i < ipa_word.length(); i++) {
            char chr = ipa_word.charAt(i);
            if (isVowel(chr)) {
                syllables.add(new Syllable(chr));
                vowel_indexes.add(i);
                nucleus_indexes.add(i);
                onset_indexes.add(-1); // gets list to correct size with placeholder values
            }
        }

        /* 1.5 check for dipthongs */
        for (int i = 0; i < vowel_indexes.size() - 1; i++) {
            if (vowel_indexes.get(i + 1) - vowel_indexes.get(i) == 1)
            /* i.e. if two vowels are next to one another */
            {
                char vowel_1 = ipa_word.charAt(vowel_indexes.get(i));
                char vowel_2 = ipa_word.charAt(vowel_indexes.get(i + 1));
                String potential_dipthong = new String(new char[] { vowel_1, vowel_2 });

                if (AbstractIPA.isDipthong(potential_dipthong)) {
                    nucleus_indexes.remove(i + 1);
                    syllables.remove(i + 1);
                    syllables.get(i).setNucleus(potential_dipthong);
                }
            }
        }

        /* 2. for each nucleus, work backward, adding sounds to the onset */
        for (int i = nucleus_indexes.size() - 1; i >= 0; i--) {
            boolean trial_onset_valid = true;
            String trial_onset = "";
            String onset = "";
            int onset_start = nucleus_indexes.get(i) - 1;
            while (trial_onset_valid && onset_start >= 0) {
                trial_onset = ipa_word.charAt(onset_start) + trial_onset;
                if (AbstractIPA.isValidOnset(trial_onset, syllables.get(i).getNucleus())) {
                    onset = trial_onset;
                    onset_indexes.set(i, onset_start);
                    onset_start--;
                } else {
                    trial_onset_valid = false;
                }
            }
            syllables.get(i).setOnset(onset);
        }

        /* 3. put remaining characters into codas */
        for (int i = 0; i < nucleus_indexes.size() - 1; i++) {
            /*
             * for all but last nucleus, set the coda as any character between the nucleus
             * and start of next onset
             */
            int end_of_coda = nucleus_indexes.get(i) + syllables.get(i).getNucleus().length();
            StringBuilder coda = new StringBuilder();
            while (end_of_coda < onset_indexes.get(i + 1)) {
                coda.append(ipa_word.charAt(end_of_coda++));
            }
            syllables.get(i).setCoda(coda.toString());
        }
        /*
         * for the last nucleus, set the coda as the characters between the nucleus and
         * the end of the word
         */
        syllables.getLast()
                .setCoda(ipa_word.substring(nucleus_indexes.getLast() + syllables.getLast().getNucleus().length()));

        return syllables;
    }

    /*
     * From my own deduction, two syllables rhyme if they have matching nuclei and
     * coda. For a multi-syllabic rhyme, the onset of all but the first syllable
     * pair must also match, hence the syllables in the for loop are treated
     * differently.
     */
    public static boolean checkRhyme(List<Syllable> word_1, List<Syllable> word_2, int syllables) {

        if (syllables > word_1.size() || syllables > word_2.size()) {
            throw new IndexOutOfBoundsException();
        }
        boolean rhymes = true;
        for (int i = 1; i < syllables; i++) {
            Syllable syllable_1 = word_1.get(word_1.size() - i);
            Syllable syllable_2 = word_2.get(word_2.size() - i);
            rhymes = syllable_1.equals(syllable_2);
            if (!rhymes)
                return false;
        }

        int i = syllables;
        Syllable syllable_1 = word_1.get(word_1.size() - i);
        Syllable syllable_2 = word_2.get(word_2.size() - i);
        rhymes = syllable_1.rhymes(syllable_2);

        return rhymes;
    }

    /**
     * Checks if two words rhyme for as many syllables as possible (i.e. all
     * syllables in the word with fewer syllables)
     * 
     * @param word_1
     * @param word_2
     * @return
     */
    public static boolean checkRhyme(List<Syllable> word_1, List<Syllable> word_2) {
        int syllables = Math.min(word_1.size(), word_2.size());
        return checkRhyme(word_1, word_2, syllables);
    }

    public static void main(String[] args) {

    }

}
