package words;

import java.util.Arrays;

/**
 * This class contains IPA character classifications.
 * 
 * @author 190021081
 */
public abstract class AbstractIPA {

    protected static final Character[] VOWELS = { 'i', 'y', 'ɨ', 'ʉ', 'ɯ', 'u', 'ɪ', 'ʏ', 'ʊ', 'e', 'ø', 'ɘ', 'ɵ', 'ɤ',
            'o', 'ə', 'ɛ', 'œ', 'ɜ', 'ɞ', 'ʌ', 'ɔ', 'æ', 'ɐ', 'a', 'ɶ', 'ä', 'ɑ', 'ɝ'
            /*
             * 'e̞', 'ø̞', 'ɤ̞', 'o̞', were considered invalid by the IDE, but do not appear
             * to feature in WordsAPI
             */ };

    protected static final String[] U_VARIANTS = { "u", "ʊ" }; // String to account for diphthongs in comparison
                                                               // Might not be an exhaustive list yet

    protected static final String[] DIPHTHONGS = {
            /*
             * from https://thesoundofenglish.org/diphthongs/, Anouka Ltd, 2021 [Accessed 15
             * October 2021].
             */
            "ɪə", "ʊə", "eɪ", "əʊ", "ɔɪ", "aʊ", "ʌɪ", /* some charts also include eə */
            /* WordsAPI uses "aɪ" where soundofenglish.org uses "ʌɪ" */
            "aɪ",
            /* WordsAPI uses "oʊ" where soundofenglish.org uses "əʊ" */
            "oʊ" };

    protected static final Character[] CONSONANTS = { 'p', 'b', 't', 'd', 'ʈ', 'ɖ', 'c', 'ɟ', 'k', 'ɡ', 'q', 'ɢ', 'ʔ',
            'm', 'ɱ', 'n', 'ɳ', 'ɲ', 'ŋ', 'ɴ', 'ʙ', 'r', 'ʀ', 'ⱱ', 'ɾ', 'ɽ', 'ɸ', 'β', 'f', 'v', 'θ', 'ð', 's', 'z',
            'ʃ', 'ʒ', 'ʂ', 'ʐ', 'ç', 'ʝ', 'x', 'ɣ', 'χ', 'ʁ', 'ħ', 'ʕ', 'h', 'ɦ', 'ɬ', 'ɮ', 'ʋ', 'ɹ', 'ɻ', 'j', 'ɰ',
            'l', 'ɭ', 'ʎ', 'ʟ' };

    protected static final String[] ONSETS = {
            /*
             * from https://en.wikipedia.org/wiki/English_phonology#Syllable_structure,
             * Wikipedia contributors, 2021 [Accessed 15 October 2021]
             */
            /* An empty onset */
            "",
            /* All single consonant phonemes except /ŋ/ */
            "p", "b", "t", "d", "ʈ", "ɖ", "c", "ɟ", "k", "ɡ", "q", "ɢ", "ʔ", "m", "ɱ", "n", "ɳ", "ɲ", "ɴ", "ʙ", "r",
            "ʀ", "ⱱ", "ɾ", "ɽ", "ɸ", "β", "f", "v", "θ", "ð", "s", "z", "ʃ", "ʒ", "ʂ", "ʐ", "ç", "ʝ", "x", "ɣ", "χ",
            "ʁ", "ħ", "ʕ", "h", "ɦ", "ɬ", "ɮ", "ʋ", "ɹ", "ɻ", "j", "ɰ", "l", "ɭ", "ʎ", "ʟ",
            /* Stop plus approximant other than /j/ */
            "fl", "sl", "θl", "ʃl", "vl", "fr", "θr", "ʃr", "hw", "sw", "θw", "vw",
            /* /s/ plus voiceless stop */
            "sp", "st", "sk",
            /* /s/ plus nasal other than /ŋ/ */
            "sm", "sn",
            /* /s/ plus voiceless non-sibilant fricative */
            "sf", "sθ",
            /* /s/ plus voiceless stop plus approximant */
            "spl", "skl", "spr", "str", "skr", "skw", "smj", "spj", "stj", "skj",
            /* /s/ plus voiceless non-sibilant fricative plus approximant */
            "sfr",
            /* others that I think were missing */
            "w", "pr", "kw", "kr" };

    protected static final String[] U_ONSETS = {
            /* Consonant plus /j/ (before /uː/ or its modified/reduced forms) */
            "pj", "bj", "tj", "dj", "kj", "ɡj", "mj", "nj", "fj", "vj", "θj", "sj", "zj", "hj", "lj" };

    protected static boolean isVowel(char chr) {
        return Arrays.asList(VOWELS).contains(chr);
    }

    protected static boolean isConsonant(char chr) {
        return Arrays.asList(CONSONANTS).contains(chr);
    }

    protected static boolean isValidOnset(String onset, String nucleus) {
        boolean is_onset;
        is_onset = Arrays.asList(ONSETS).contains(onset);
        if (!is_onset && Arrays.asList(U_VARIANTS).contains(nucleus)) { // need to account for modified and reduced
                                                                        // forms of 'u'
            is_onset = Arrays.asList(U_ONSETS).contains(onset);
        }
        return is_onset;
    }

    protected static boolean isDiphthong(String vowel_pair) {
        return Arrays.asList(DIPHTHONGS).contains(vowel_pair);
    }
}
