package words;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import config.Configuration;

public class SuperWord implements Comparable<SuperWord> {

    private String plaintext;
    private ArrayList<Word> nouns;
    private ArrayList<Word> pronouns;
    private ArrayList<Word> verbs;
    private ArrayList<Word> adjectives;
    private ArrayList<Word> adverbs;
    private ArrayList<Word> prepositions;
    private ArrayList<Word> conjunctions;
    private Pronunciation pronunciation = new Pronunciation();

    /**
     * 
     * @param word JSONObject from a word request. See WordsApiNotes.txt for
     *             examples.
     */
    public SuperWord(JSONObject word) {
        if (word.has("word")) {
            this.plaintext = word.getString("word");
        }

        if (word.has("syllables")) {
            JSONObject syllablesObject = word.getJSONObject("syllables");
            this.pronunciation.setSyllables(syllablesObject);
        }

        if (word.has("pronunciation")) {
            JSONObject pronunciationObject = word.getJSONObject("pronunciation");
            this.pronunciation.setIPA(pronunciationObject);
        }

        if (word.has("results")) {
            JSONArray resultsArray = word.getJSONArray("results");
            this.setWords(resultsArray);
        }
    }

    private boolean addWord(List<Word> list, Word word) {
        if (list == null) {
            list = new ArrayList<>();
        }
        return list.add(word);
    }

    private void setWords(JSONArray resultsArray) {
        List<Object> results = resultsArray.toList();
        for (Object result : results) {
            Word word = new Word((JSONObject) result);
            switch (word.partOfSpeech()) {
                case "noun":
                    addWord(nouns, word);
                    break;
                case "pronoun":
                    addWord(pronouns, word);
                    break;
                case "verb":
                    addWord(verbs, word);
                    break;
                case "adjective":
                    addWord(adjectives, word);
                    break;
                case "adverb":
                    addWord(adverbs, word);
                    break;
                case "preposition":
                    addWord(prepositions, word);
                    break;
                case "conjunction":
                    addWord(conjunctions, word);
                    break;
                default:
                    Configuration.LOG.writeLog(String.format("Unrecognised part of speech for %s: %s", this.plaintext,
                            word.partOfSpeech()));
                    break;
            }
        }
    }

    /**
     * I only want to order by plaintext, but equality should take more into
     * account. Arguably I should build a custom Comparator rather than setting the
     * default.
     */
    @Override
    public int compareTo(SuperWord anotherWord) {
        if (anotherWord == null) {
            throw new NullPointerException();
        }
        return plaintext.compareTo((anotherWord).plaintext);
    }
}
