package words;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import utils.Pair;

public class IPAHandler extends AbstractIPA {

    /**
     * Compute the syllables of a word based on the IPA. Words can have a single
     * primary stressed syllable (denoted with ') and zero or more secondary
     * stressed syllables (denoted with ,).
     * 
     * @param ipa_word
     * @return a Pair<> including the JSONArray of syllables and JSONObject of
     *         stresses
     */
    public static Pair<JSONArray, JSONObject> getSyllables(String ipa_word) {
        /*
         * logic based on
         * https://linguistics.stackexchange.com/questions/30933/how-to-split-ipa-
         * spelling-into-syllables
         */
        JSONObject emphasis = new JSONObject("{primary: 0, has_secondary: false, secondary: []}");
        LinkedList<Syllable> syllables = new LinkedList<>();
        LinkedList<Integer> vowel_indexes = new LinkedList<>();
        LinkedList<Integer> nucleus_indexes = new LinkedList<>();
        LinkedList<Integer> onset_indexes = new LinkedList<>(); // the start of onsets; onset_indexes[i] should
                                                                // correspond to nuclei_indexes[i]

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
        /* if the onset would include a ' or , update the emphases JSONObject */
        for (int i = nucleus_indexes.size() - 1; i >= 0; i--) {
            boolean trial_onset_valid = true;
            String trial_onset = "";
            String onset = "";
            int onset_start = nucleus_indexes.get(i) - 1;
            while (trial_onset_valid && onset_start >= 0) {
                char prev_char = ipa_word.charAt(onset_start);
                trial_onset = prev_char + trial_onset;
                if (AbstractIPA.isValidOnset(trial_onset, syllables.get(i).getNucleus())) {
                    onset = trial_onset;
                    onset_indexes.set(i, onset_start);
                    onset_start--;
                } else if (prev_char == '\'') {
                    /* primary stress */
                    emphasis.put("primary", i);
                    onset_indexes.set(i, onset_start); // prevents character being included in a coda
                    trial_onset_valid = false;
                } else if (prev_char == ',') {
                    /* secondary stress */
                    emphasis.put("has_secondary", true);
                    ((JSONArray) emphasis.get("secondary")).put(i);
                    onset_indexes.set(i, onset_start); // prevents character being included in a coda
                    trial_onset_valid = false;
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

        JSONArray ipa_syllables_arr = new JSONArray();
        for (Syllable syllable : syllables) {
            ipa_syllables_arr.put(syllable.toJsonObject());
        }

        return new Pair<>(ipa_syllables_arr, emphasis);
    }

    /*
     * From my own deduction, two syllables rhyme if they have matching nuclei and
     * coda. For a multi-syllabic rhyme, the onset of all but the first syllable
     * pair must also match, hence the syllables in the for loop are treated
     * differently.
     * 
     * I should account for Abercrombie and Zombie (i.e. secondary emphasis)
     */
    /**
     * 
     * @param array_1
     * @param array_2
     * @param emphasis_position index of syllable to rhyme to ()
     * @return
     */
    public static boolean checkRhyme(JSONArray array_1, JSONArray array_2, int syllables) {

        List<Object> list;

        List<Syllable> word_1 = new LinkedList<>();
        list = array_1.toList();
        for (Object object : list) {
            Map<String, String> map = (Map<String, String>) object;
            word_1.add(new Syllable(map));
        }

        List<Syllable> word_2 = new LinkedList<>();
        list = array_2.toList();
        for (Object object : list) {
            Map<String, String> map = (Map<String, String>) object;
            word_2.add(new Syllable(map));
        }

        if (syllables > word_1.size() || syllables > word_2.size() || syllables < 1) {
            throw new IndexOutOfBoundsException(String.format("Syllables: %d; word_1.size: %d; word_2.size: %d",
                    syllables, word_1.size(), word_2.size()));
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
     * Checks if two words rhyme up to and including the earlier primary stress.
     * 
     * @param word_1
     * @param word_2
     * @return false if the shorter word is does not have enough syllables to
     *         include the earlier stress, e.g. "poet" and "it"
     */
    public static boolean checkRhyme(Word word_1, Word word_2) {
        JSONObject syl_object_1 = word_1.ipaSyllables();
        JSONObject syl_object_2 = word_2.ipaSyllables();

        Iterator<String> parts_of_speech_1 = syl_object_1.keys();
        while (parts_of_speech_1.hasNext()) {
            String part_of_speech_1 = parts_of_speech_1.next();
            JSONArray syllables_1 = (JSONArray) syl_object_1.get(part_of_speech_1);
            int rhyme_length_1 = word_1.rhymeLength(part_of_speech_1);
            
            Iterator<String> parts_of_speech_2 = syl_object_2.keys();
            while (parts_of_speech_2.hasNext()) {
                String part_of_speech_2 = parts_of_speech_2.next();
                JSONArray syllables_2 = (JSONArray) syl_object_2.get(part_of_speech_2);
                int rhyme_length_2 = word_2.rhymeLength(part_of_speech_2);

                int syllables = Math.max(rhyme_length_1, rhyme_length_2);

                if (syllables > Math.min(syllables_1.length(), syllables_2.length())) {
                    continue; // the shorter word is too short to form a rhyme
                }

                if (checkRhyme(syllables_1, syllables_2, syllables))
                    return true;
            }
        }
        return false;
    }

}
