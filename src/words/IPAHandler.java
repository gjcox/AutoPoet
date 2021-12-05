package words;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import config.Configuration;
import utils.Pair;

public class IPAHandler extends AbstractIPA {

    /**
     * Compute the syllables of a word based on the IPA. Words can have a single
     * primary stressed syllable (denoted with ') and zero or more secondary
     * stressed syllables (denoted with ,).
     * 
     * @param ipaWord
     * @return a Pair<> including the JSONArray of syllables and JSONObject of
     *         stresses.
     */
    public static Pair<ArrayList<Syllable>, Emphasis> getSyllables(String ipaWord) {
        /*
         * logic based on
         * https://linguistics.stackexchange.com/questions/30933/how-to-split-ipa-
         * spelling-into-syllables
         */
        ArrayList<Syllable> syllables = new ArrayList<>(); // included in return
        Emphasis emphasis = new Emphasis(); // included in return

        ArrayList<Integer> vowelIndexes = new ArrayList<>();
        ArrayList<Integer> nucleusIndexes = new ArrayList<>();
        ArrayList<Integer> onsetIndexes = new ArrayList<>(); // the start of onsets; onset_indexes[i] should
                                                              // correspond to nuclei_indexes[i]

        if (ipaWord.equals("")) {
            Configuration.LOG.writeTempLog(
                    String.format("getSyllables(%s) passed an empty string. Returning empty pair.", ipaWord));
            return new Pair<>(syllables, emphasis);
        }

        /* 1. locate all nuclei (vowels) */
        for (int i = 0; i < ipaWord.length(); i++) {
            char chr = ipaWord.charAt(i);
            if (isVowel(chr)) {
                syllables.add(new Syllable(chr));
                vowelIndexes.add(i);
                nucleusIndexes.add(i);
                onsetIndexes.add(-1); // gets list to correct size with placeholder values
            }
        }

        /* 1.5 check for dipthongs */
        for (int i = 0; i < vowelIndexes.size() - 1; i++) {
            int index1 = vowelIndexes.get(i);
            int index2 = vowelIndexes.get(i + 1);
            if (index2 - index1 == 1 && nucleusIndexes.contains(index1) && nucleusIndexes.contains(index2))
            /* i.e. if two vowels are next to one another and not already in a dipthong */
            {
                char vowel_1 = ipaWord.charAt(vowelIndexes.get(i));
                char vowel_2 = ipaWord.charAt(vowelIndexes.get(i + 1));
                String potential_dipthong = new String(new char[] { vowel_1, vowel_2 });

                if (AbstractIPA.isDipthong(potential_dipthong)) {
                    nucleusIndexes.remove((Integer) index2);
                    syllables.remove(i + 1);
                    syllables.get(i).setNucleus(potential_dipthong);
                }
            }
        }

        /* 2. for each nucleus, work backward, adding sounds to the onset */
        /* if the onset would include a ' or , update the emphases JSONObject */
        for (int i = nucleusIndexes.size() - 1; i >= 0; i--) {
            boolean trialOnsetIsValid = true;
            String trialOnset = "";
            String onset = "";
            int onsetStart = nucleusIndexes.get(i) - 1;
            while (trialOnsetIsValid && onsetStart >= 0) {
                char prev_char = ipaWord.charAt(onsetStart);
                trialOnset = prev_char + trialOnset;
                if (AbstractIPA.isValidOnset(trialOnset, syllables.get(i).getNucleus())) {
                    onset = trialOnset;
                    onsetIndexes.set(i, onsetStart);
                    onsetStart--;
                } else if (prev_char == '\'') {
                    /* primary stress */
                    emphasis.setPrimary(i);
                    onsetIndexes.set(i, onsetStart); // prevents character being included in a coda
                    trialOnsetIsValid = false;
                } else if (prev_char == ',') {
                    /* secondary stress */
                    emphasis.addSecondary(i);
                    onsetIndexes.set(i, onsetStart); // prevents character being included in a coda
                    trialOnsetIsValid = false;
                } else {
                    trialOnsetIsValid = false;
                }
            }
            syllables.get(i).setOnset(onset);
        }

        /* 3. put remaining characters into codas */
        for (int i = 0; i < nucleusIndexes.size() - 1; i++) {
            /*
             * for all but last nucleus, set the coda as any character between the nucleus
             * and start of next onset
             */
            int endOfCoda = nucleusIndexes.get(i) + syllables.get(i).getNucleus().length();
            StringBuilder coda = new StringBuilder();
            while (endOfCoda < onsetIndexes.get(i + 1)) {
                coda.append(ipaWord.charAt(endOfCoda++));
            }
            syllables.get(i).setCoda(coda.toString());
        }

        /*
         * for the last nucleus, set the coda as the characters between the nucleus and
         * the end of the word
         */
        int lastNucleusIndex = nucleusIndexes.get(nucleusIndexes.size() - 1);
        int lastNucleusLength = syllables.get(syllables.size() - 1).getNucleus().length();
        syllables.get(syllables.size() - 1).setCoda(ipaWord.substring(lastNucleusIndex + lastNucleusLength));

        Pair<ArrayList<Syllable>, Emphasis> pair = new Pair<>(syllables, emphasis);
        Configuration.LOG.writeTempLog(String.format("getSyllables(%s) returning: ", pair.toString()));
        return pair;
    }

    /*
     * From my own deduction, two syllables rhyme if they have matching nuclei and
     * coda. For a multi-syllabic rhyme, the onset of all but the first syllable
     * pair must also match, hence the syllables in the for loop are treated
     * differently.
     * 
     */
    /**
     * 
     * @param array1   of IPA syllables in word 1.
     * @param array2   of IPA syllables in word 2.
     * @param syllables the number of syllables to match against, starting at the
     *                  end of the words.
     * @return true if the two words rhyme to the given number of syllables.
     */
    public static boolean checkRhyme(JSONArray array1, JSONArray array2, int syllables) {

        List<Object> list;

        /* convert first JSONArray of syllables to a List<Syllables> */
        List<Syllable> word1 = new LinkedList<>();
        list = array1.toList();
        for (Object object : list) {
            @SuppressWarnings("unchecked")
            Map<String, String> map = (Map<String, String>) object;
            word1.add(new Syllable(map));
        }

        /* convert second JSONArray of syllables to a List<Syllables> */
        List<Syllable> word2 = new LinkedList<>();
        list = array2.toList();
        for (Object object : list) {
            @SuppressWarnings("unchecked")
            Map<String, String> map = (Map<String, String>) object;
            word2.add(new Syllable(map));
        }

        /*
         * check that both words are long enough to accomodate the desired rhyme length
         * also check that the length is at least 1 syllable
         */
        if (syllables > word1.size() || syllables > word2.size() || syllables < 1) {
            throw new IndexOutOfBoundsException(String.format("Syllables: %d; word1.size: %d; word2.size: %d",
                    syllables, word1.size(), word2.size()));
        }

        /* check that syllables after the emphasis match exactly */
        boolean rhymes = true;
        for (int i = 1; i < syllables; i++) {
            Syllable syllable1 = word1.get(word1.size() - i);
            Syllable syllable2 = word2.get(word2.size() - i);
            rhymes = syllable1.equals(syllable2);
            if (!rhymes)
                return false;
        }

        /* check that the emphasised vowels match */
        int i = syllables;
        Syllable syllable1 = word1.get(word1.size() - i);
        Syllable syllable2 = word2.get(word2.size() - i);
        rhymes = syllable1.rhymes(syllable2);

        return rhymes;
    }

    /**
     * Checks if two words rhyme. Tries all pairs of pronunciants for the words and
     * potential rhyme lengths.
     * 
     * @param word1
     * @param word2
     * @return false if the shorter word is does not have enough syllables to
     *         include the earlier stress, e.g. "poet" and "it"
     */
    public static boolean checkRhyme(Word word1, Word word2) {
        JSONObject sylObject1 = word1.ipaSyllables();
        JSONObject sylObject2 = word2.ipaSyllables();

        /* iterate over the parts of speech (verb, noun, all) associated with word 1 */
        Iterator<String> partsOfSpeech1 = sylObject1.keys();
        while (partsOfSpeech1.hasNext()) {
            String partOfSpeech1 = partsOfSpeech1.next();
            JSONArray syllables1 = (JSONArray) sylObject1.get(partOfSpeech1);
            JSONObject rhymeLengths1 = word1.rhymeLengths(partOfSpeech1);

            /* iterate over the parts of speech (verb, noun, all) associated with word 2 */
            Iterator<String> partsOfSpeech2 = sylObject2.keys();
            while (partsOfSpeech2.hasNext()) {
                String partOfSpeech2 = partsOfSpeech2.next();
                JSONArray syllables2 = (JSONArray) sylObject2.get(partOfSpeech2);
                JSONObject rhymeLengths2 = word2.rhymeLengths(partOfSpeech2);

                List<Integer> rhymeLengths = getCommonRhymeLengths(rhymeLengths1, rhymeLengths2);
                if (rhymeLengths.isEmpty()) {
                    continue; // the words don't share a rhyme length
                }

                /* test if any of common rhyme lengths produce a rhyme */
                for (Integer syllables : rhymeLengths) {
                    if (checkRhyme(syllables1, syllables2, syllables))
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Finds common rhyme lengths, defined as matching primary/primary emphases, and
     * matching primary/secondary emphases. Here, "matching" emphases are the same
     * number of syllables from the end of their respective word.
     * 
     * @param rhyme_lengths_1
     * @param rhyme_lengths_2
     * @return
     */
    private static List<Integer> getCommonRhymeLengths(JSONObject rhyme_lengths_1, JSONObject rhyme_lengths_2) {
        List<Integer> common_rhyme_lengths = new LinkedList<>();

        /* match primary_1 to primary_2 */
        Integer primary_1 = (Integer) rhyme_lengths_1.get(Emphasis.PRIMARY);
        Integer primary_2 = (Integer) rhyme_lengths_2.get(Emphasis.PRIMARY);
        if (primary_1.equals(primary_2)) {
            common_rhyme_lengths.add(primary_1);
        }

        /* match primary_1 to secondary_2 */
        List<Integer> secondaries_2 = Emphasis.getSecondary(rhyme_lengths_2);
        for (Integer secondary_2 : secondaries_2) {
            if (primary_1.equals(secondary_2)) {
                common_rhyme_lengths.add(primary_1);
            }
        }

        /* match secondary_1 to primary_2 */
        List<Integer> secondaries_1 = Emphasis.getSecondary(rhyme_lengths_1);
        for (Integer secondary_1 : secondaries_1) {
            if (primary_2.equals(secondary_1)) {
                common_rhyme_lengths.add(primary_2);
            }
        }

        return common_rhyme_lengths;
    }

}
