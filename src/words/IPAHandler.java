package words;

import java.util.ArrayList;

import config.Configuration;
import utils.Pair;

/**
 * This class parses IPA strings to derive syllable breakdowns and emphases.
 * 
 * @author 190021081
 */
public class IPAHandler extends AbstractIPA {

    private static final char P_EMPHASIS = '\'';
    private static final char S_EMPHASIS = ',';
    private static final char SPACE = '_';

    /**
     * Compute the syllables of a word based on the IPA. Words can have a single
     * primary stressed syllable (denoted with ') and zero or more secondary
     * stressed syllables (denoted with ,).
     * 
     * The logic used is based on forum response by Draconis, 19 March 2019
     * https://linguistics.stackexchange.com/questions/30933/how-to-split-ipa-spelling-into-syllables,
     * [Accessed 25 October 2021].
     * 
     * 
     * @param ipaWord
     * @return a Pair<> of an ArrayList of syllables and the emphases.
     */
    public static Pair<ArrayList<Syllable>, Emphasis> getSyllables(String ipaWord) {

        ArrayList<Syllable> syllables = new ArrayList<>(); // included in return
        Emphasis emphasis = new Emphasis(); // included in return

        ArrayList<Integer> vowelIndexes = new ArrayList<>();
        ArrayList<Integer> nucleusIndexes = new ArrayList<>();
        ArrayList<Integer> onsetIndexes = new ArrayList<>();
        /*
         * onsetIndexes[i] should correspond to nucleusIndexes[i], i.e. i is syllable
         * index
         */

        /*
         * if the IPA contains multiple words, add primary emphasis markes to
         * monosyllabic words
         */
        ipaWord = addMissingEmphasis(ipaWord);

        if (ipaWord.equals("")) {
            Configuration.LOG.writeTempLog(
                    String.format("getSyllables() passed an empty string \"%s\". Returning empty pair.", ipaWord));
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

        if (nucleusIndexes.isEmpty()) {
            Configuration.LOG.writePersistentLog(
                    String.format("getSyllables() passed a string with no vowels \"%s\". Returning empty pair.",
                            ipaWord));
            return new Pair<>(syllables, emphasis);
        }

        /* 1.5 check for diphthongs */
        int diphthongCount = 0; // number of diphthongs found
        for (int i = 0; i < vowelIndexes.size() - 1; i++) {
            int index1 = vowelIndexes.get(i);
            int index2 = vowelIndexes.get(i + 1);
            if (index2 - index1 == 1 && nucleusIndexes.contains(index1) && nucleusIndexes.contains(index2))
            /* i.e. if two vowels are next to one another and not already in a diphthong */
            {
                char vowel_1 = ipaWord.charAt(vowelIndexes.get(i));
                char vowel_2 = ipaWord.charAt(vowelIndexes.get(i + 1));
                String potential_diphthong = new String(new char[] { vowel_1, vowel_2 });

                if (AbstractIPA.isDiphthong(potential_diphthong)) {
                    nucleusIndexes.remove((Integer) index2);
                    syllables.remove(i + 1 - diphthongCount);
                    syllables.get(i - diphthongCount).setNucleus(potential_diphthong);
                    diphthongCount++;
                }
            }
        }

        /* 2. for each nucleus, work backward, adding sounds to the onset */
        /* if the onset would include a ' or , update the Emphasis object */
        boolean foundPrimary = false;
        for (int i = nucleusIndexes.size() - 1; i >= 0; i--) {
            boolean trialOnsetIsValid = true;
            String trialOnset = "";
            String onset = "";
            int onsetStart = nucleusIndexes.get(i) - 1;
            while (trialOnsetIsValid && onsetStart >= 0) {
                char prevChar = ipaWord.charAt(onsetStart);
                trialOnset = prevChar + trialOnset;
                if (AbstractIPA.isValidOnset(trialOnset, syllables.get(i).getNucleus())) {
                    onset = trialOnset;
                    onsetIndexes.set(i, onsetStart);
                    onsetStart--;
                } else if (prevChar == P_EMPHASIS && !foundPrimary) {
                    /* primary stress */
                    foundPrimary = true;
                    emphasis.setPrimary(i);
                    onsetIndexes.set(i, onsetStart); // prevents character being included in a coda
                    trialOnsetIsValid = false;
                } else if (prevChar == S_EMPHASIS || (prevChar == P_EMPHASIS && foundPrimary)) {
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
        Configuration.LOG
                .writeTempLog(String.format("getSyllables(\"%s\") returning: \"%s\"", ipaWord, pair.toString()));
        return pair;
    }

    /**
     * Adds primary emphasis markers to monosyllablic words.
     * 
     * @param ipa the IPA of a short phrase.
     * @return the IPA cleaned of underscores and with all emphasis markers.
     */
    private static String addMissingEmphasis(String ipa) {
        if (!ipa.contains(Character.toString(SPACE))) {
            return ipa;
        }
        String[] words = ipa.split(Character.toString(SPACE));
        StringBuilder corrected = new StringBuilder();
        for (String word : words) {
            if (!word.contains(Character.toString(P_EMPHASIS))) {
                // assume monosyllabic word
                // add emphasis marker that would be implicit in a single word
                word = P_EMPHASIS + word;
            }
            corrected.append(word);
        }

        return corrected.toString();
    }

}
