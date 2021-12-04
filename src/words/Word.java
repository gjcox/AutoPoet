package words;

import java.util.ArrayList;

public class Word {

    public enum PartOfSpeech {
        NOUN, PRONOUN, VERB, ADJECTIVE, ADVERB, PREPOSITION, CONJUCTION
    }

    private String definition;
    private PartOfSpeech partOfSpeech;

    private ArrayList<SuperWord> synonyms;
    private ArrayList<SuperWord> antonyms; // not in use

    private ArrayList<String> typeOf;
    private ArrayList<String> hasTypes;
    private ArrayList<SuperWord> commonTyped; 

    private ArrayList<String> inCategory;
    private ArrayList<String> hasCategories;

    private ArrayList<String> partOf;
    private ArrayList<String> hasParts;

    private ArrayList<String> instanceOf; // not in use
    private ArrayList<String> hasInstances; // not in use

    private ArrayList<String> substanceOf; // not in use
    private ArrayList<String> hasSubstances; // not in use

    private ArrayList<String> memberOf; // not in use
    private ArrayList<String> hasMembers; // not in use

    private ArrayList<String> usageOf; // not in use 
    private ArrayList<String> hasUsages; // not in use 

    private ArrayList<String> inRegion; // not in use
    private ArrayList<String> regionOf; // not in use

    private ArrayList<String> similarTo;

    private ArrayList<String> attribute; // not in use 
    private ArrayList<String> pertainsTo; // not in use 
    private ArrayList<String> also; // not in use
    private ArrayList<String> entails; // not in use
    private ArrayList<String> derivation; // not in use
    private ArrayList<String> examples; // not in use

    /*
     * public JSONObject rhymeLengths(String part_of_speech) {
     * JSONObject rhyme_lengths = Emphasis.newEmphasisObject();
     * int length = ((JSONArray) this.ipa_syllables.get(part_of_speech)).length();
     * JSONObject emphasis_object = (JSONObject)
     * this.ipa_emphasis.get(part_of_speech);
     * 
     * /* get number of syllables between primary emphasis and end of word
     */
    /*
     * int primary_index = (int) emphasis_object.get(Emphasis.PRIMARY);
     * int primary_rhyme_length = length - primary_index;
     * rhyme_lengths.put(Emphasis.PRIMARY, primary_rhyme_length);
     * 
     * /* get numbers of syllables between secondary emphases and end of word
     */
    /*
     * JSONArray secondaries = (JSONArray) emphasis_object.get(Emphasis.SECONDARY);
     * List<Object> secondary_indexes = secondaries.toList();
     * for (Object index : secondary_indexes) {
     * int secondary_rhyme_length = length - (Integer) index;
     * ((JSONArray)
     * rhyme_lengths.get(Emphasis.SECONDARY)).put(secondary_rhyme_length);
     * }
     * return rhyme_lengths;
     * }
     * 
     * 
     * 
     * public List<Word> getSubsRhyme(Word wordToRhyme, boolean synonyms, boolean
     * common_types) {
     * List<Word> substitutions = new ArrayList<>();
     * if (synonyms) {
     * for (Word synonym : this.synonyms) {
     * if (IPAHandler.checkRhyme(synonym, wordToRhyme)) {
     * substitutions.add(synonym);
     * }
     * }
     * }
     * if (common_types) {
     * for (Word common_type : this.common_types) {
     * if (IPAHandler.checkRhyme(common_type, wordToRhyme)) {
     * substitutions.add(common_type);
     * }
     * }
     * }
     * return substitutions;
     * }
     */
}
