package words;

public enum PartOfSpeech {
    NOUN("noun"),
    PRONOUN("pronoun"),
    VERB("verb"),
    ADJECTIVE("adjective"),
    ADVERB("adverb"),
    PREPOSITION("preposition"),
    CONJUCTION("conjunction"),
    DEFINITE_ARTICLE("definite article"),
    UNKNOWN(null);

    private final String apiString;

    public String getApiString() {
        return apiString;
    }

    private PartOfSpeech(String apiString) {
        this.apiString = apiString;
    }

    public static PartOfSpeech fromString(String pos) {
        switch (pos.toLowerCase()) {
            case "noun":
                return PartOfSpeech.NOUN;
            case "pronoun":
                return PartOfSpeech.PRONOUN;
            case "verb":
                return PartOfSpeech.VERB;
            case "adjective":
                return PartOfSpeech.ADJECTIVE;
            case "adverb":
                return PartOfSpeech.ADVERB;
            case "preposition":
                return PartOfSpeech.PREPOSITION;
            case "conjunction":
                return PartOfSpeech.CONJUCTION;
            case "definite article":
                return PartOfSpeech.DEFINITE_ARTICLE;
            default:
                return PartOfSpeech.UNKNOWN;
        }
    }
}