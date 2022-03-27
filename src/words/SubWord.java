package words;

import static config.Configuration.LOG;
import static utils.NullListOperations.addAllToNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utils.ParameterWrappers.SuggestionPoolParameters.SuggestionPool;

/**
 * This class encodes a sub word, i.e. a subset of the definition data for a
 * word, including synonyms etc.
 * 
 * @author 190021081
 */
public class SubWord {

    private static Set<String> knownFields = new HashSet<>(
            Arrays.asList("partOfSpeech", "definition", "synonyms", "antonyms", "verbGroup", "typeOf", "hasTypes",
                    "inCategory", "hasCategories", "partOf", "hasParts", "instanceOf", "hasInstances", "substanceOf",
                    "hasSubstances", "memberOf", "hasMembers", "usageOf", "hasUsages", "inRegion", "regionOf",
                    "similarTo", "attribute", "pertainsTo", "also", "entails", "derivation", "examples", "cause"));

    private final SuperWord parent;
    private String definition; // not in use for GUI
    private PartOfSpeech partOfSpeech = PartOfSpeech.UNKNOWN;
    private EnumMap<SuggestionPool, ArrayList<SuperWord>> suggestionPools = new EnumMap<>(SuggestionPool.class);

    private boolean setCommonlyTyped = false;
    private boolean setCommonCategories = false;

    @SuppressWarnings("unchecked")
    public SubWord(SuperWord parent, Map<String, Object> resultObject) {
        this.parent = parent;

        if (resultObject.containsKey("definition")) {
            this.definition = (String) resultObject.get("definition");
        }

        Set<String> unrecognisedFields = new HashSet<>(resultObject.keySet());
        unrecognisedFields.removeAll(knownFields);
        if (!unrecognisedFields.isEmpty()) {
            LOG.writePersistentLog(
                    String.format("Unrecognised field(s) for subword of \"%s\" with definition \"%s\": \"%s\"",
                            parent.getPlaintext(), this.definition, unrecognisedFields.toString()));
        }

        if (resultObject.containsKey("partOfSpeech")) {
            setPartOfSpeech((String) resultObject.get("partOfSpeech"));
        }

        for (SuggestionPool pool : SuggestionPool.values()) {
            if (resultObject.containsKey(pool.getApiString())) {
                List<Object> synonymsList = (List<Object>) resultObject.get(pool.getApiString());
                suggestionPools.put(pool, SuperWord.batchPlaceHolders(synonymsList));
            }
        }

    }

    // (internal) setters

    private void setCommonlyTyped() {
        if (setCommonlyTyped || suggestionPools.get(SuggestionPool.TYPE_OF) == null)
            return;

        ArrayList<SuperWord> commonlyTyped = new ArrayList<>();
        for (SuperWord type : suggestionPools.get(SuggestionPool.TYPE_OF)) {
            commonlyTyped = addAllToNull(commonlyTyped,
                    type.getSuggestionPool(SuggestionPool.HAS_TYPES, this.partOfSpeech, false));
        }
        suggestionPools.put(SuggestionPool.COMMONLY_TYPED, commonlyTyped);
        setCommonlyTyped = true;
    }

    private void setCommonCategories() {
        if (setCommonCategories || suggestionPools.get(SuggestionPool.IN_CATEGORY) == null)
            return;

        ArrayList<SuperWord> commonCategories = new ArrayList<>();
        for (SuperWord category : suggestionPools.get(SuggestionPool.IN_CATEGORY)) {
            commonCategories = addAllToNull(commonCategories,
                    category.getSuggestionPool(SuggestionPool.HAS_CATEGORIES, this.partOfSpeech, false));
        }
        suggestionPools.put(SuggestionPool.COMMON_CATEGORIES, commonCategories);
        setCommonCategories = true;
    }

    private void setPartOfSpeech(String partOfSpeech) {
        if (partOfSpeech == null) {
            partOfSpeech = "null";
        }

        this.partOfSpeech = PartOfSpeech.fromString(partOfSpeech);
    }

    // getters

    public PartOfSpeech getPartOfSpeech() {
        return partOfSpeech;
    }

    /**
     * Returns the unfiltered contents of a given suggestion pool.
     * 
     * @param pool the desired pool.
     * @return the corresponding list. Returns null if the SubWord has no
     *         corresponding list, or if pool is not an expected value.
     */
    public ArrayList<SuperWord> getSuggestionPool(SuggestionPool pool) {
        switch (pool) {
            case COMMONLY_TYPED:
                if (!setCommonlyTyped)
                    setCommonlyTyped();
                return suggestionPools.get(pool);
            case COMMON_CATEGORIES:
                if (!setCommonCategories)
                    setCommonCategories();
                return suggestionPools.get(pool);
            default:
                return suggestionPools.get(pool);
        }
    }

    // other

    /**
     * {@inheritDoc}
     */
    public String toString() {
        String divider = "\n\t";

        StringBuilder stringBuilder = new StringBuilder("{");
        if (definition != null) {
            stringBuilder.append(divider);
            stringBuilder.append("definition: " + definition);
        }

        if (partOfSpeech != null) {
            stringBuilder.append(divider);
            stringBuilder.append("partOfSpeech: " + partOfSpeech.toString());
        }

        for (SuggestionPool pool : SuggestionPool.values()) {
            if (suggestionPools.get(pool) != null) {
                stringBuilder.append(divider);
                stringBuilder.append(String.format("%s: %s", pool.getLabel(), suggestionPools.get(pool)));
            }
        }

        stringBuilder.append("\n}");
        return stringBuilder.toString();
    }

}
