package words;

import java.util.ArrayList;

import config.Configuration;
import utils.Pair;

public class IPAHandler extends AbstractIPA {

    private static char primaryEmphasis = '\''; 
    private static char secondaryEmphasis = ',';  
    private static char space = '_'; 

    /**
     * Compute the syllables of a word based on the IPA. Words can have a single
     * primary stressed syllable (denoted with ') and zero or more secondary
     * stressed syllables (denoted with ,).
     * 
     * @param ipaWord
     * @return a Pair<> of an ArrayList of syllables and the emphases.
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

        ipaWord = addMissingEmphasis(ipaWord); // if the IPA contains multiple words,
        // add primary emphasis of monosyllabic words that are be missing

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
        int dipthongCount = 0; // number of dipthongs found
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
                    syllables.remove(i + 1 - dipthongCount);
                    syllables.get(i - dipthongCount).setNucleus(potential_dipthong);
                    dipthongCount++;
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
                } else if (prevChar == primaryEmphasis && !foundPrimary) {
                    /* primary stress */
                    foundPrimary = true;
                    emphasis.setPrimary(i);
                    onsetIndexes.set(i, onsetStart); // prevents character being included in a coda
                    trialOnsetIsValid = false;
                } else if (prevChar == secondaryEmphasis || (prevChar == primaryEmphasis && foundPrimary)) {
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
     * 
     * @param ipa
     * @return
     */
    private static String addMissingEmphasis(String ipa) {
        if (!ipa.contains(Character.toString(space))) {
            return ipa;
        }
        String[] words = ipa.split(Character.toString(space));
        StringBuilder corrected = new StringBuilder();
        for (String word : words) {
            if (!word.contains(Character.toString(primaryEmphasis))) {
                // assume monosyllabic word
                // add emphasis marker that would be implicit in a single word
                word = primaryEmphasis + word;
            }
            corrected.append(word);
        }

        return corrected.toString();
    }


}
