package words;

import java.util.ArrayList;
import java.util.List;

public class IPAHandler extends AbstractIPA {

    public static List<Syllable> getSyllables(String ipa_word) {
        /*
         * logic based on
         * https://linguistics.stackexchange.com/questions/30933/how-to-split-ipa-
         * spelling-into-syllables
         */
        ArrayList<Syllable> syllables = new ArrayList<>();
        ArrayList<Integer> nuclei_indexes = new ArrayList<>();
        ArrayList<Integer> onset_indexes = new ArrayList<>(); // the start of onsets; onset_indexes[i] should correspond
                                                              // to nuclei_indexes[i]

        // need to account for the emphasis character
        // need to account for dipthongs
        /* 1. locate all nuclei (vowels) */
        for (int i = 0; i < ipa_word.length(); i++) {
            char chr = ipa_word.charAt(i);
            if (isVowel(chr)) {
                syllables.add(new Syllable(chr));
                nuclei_indexes.add(i);
                onset_indexes.add(-1); // gets list to correct size with placeholder values
            }
        }

        /* 2. for each nucleus, work backward, adding sounds to the onset */
        boolean trial_onset_valid = true;
        String trial_onset = "";
        String onset = "";
        int onset_start;
        for (int i = nuclei_indexes.size() - 1; i >= 0; i--) {
            onset_start = nuclei_indexes.get(i) - 1;
            while (trial_onset_valid) {
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
        for (int i = 0; i < nuclei_indexes.size(); i++) {
            int end_of_coda = nuclei_indexes.get(i) + 1; 
            StringBuilder coda = new StringBuilder(); 
            while (end_of_coda < onset_indexes.get(i + 1)) {
                coda.append(ipa_word.charAt(end_of_coda)); 
            }
            syllables.get(i).setCoda(coda.toString());
        }

        return syllables;
    }

}
