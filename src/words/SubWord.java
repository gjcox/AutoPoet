package words;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static config.Configuration.LOG;
import static utils.NullListOperations.addAllToNull;

public class SubWord {

    public enum PartOfSpeech {
        NOUN, PRONOUN, VERB, ADJECTIVE, ADVERB, PREPOSITION, CONJUCTION, DEFINITE_ARTICLE, UNKNOWN
    }

    private static Set<String> knownFields = new HashSet<>(
            Arrays.asList("partOfSpeech", "definition", "synonyms", "antonyms", "verbGroup", "typeOf", "hasTypes",
                    "inCategory", "hasCategories", "partOf", "hasParts", "instanceOf", "hasInstances", "substanceOf",
                    "hasSubstances", "memberOf", "hasMembers", "usageOf", "hasUsages", "inRegion", "regionOf",
                    "similarTo", "attribute", "pertainsTo", "also", "entails", "derivation", "examples"));

    private SuperWord parent;
    private String definition;
    private PartOfSpeech partOfSpeech = PartOfSpeech.UNKNOWN;

    private ArrayList<SuperWord> synonyms;
    private ArrayList<SuperWord> typeOf;
    private ArrayList<SuperWord> hasTypes;
    private ArrayList<SuperWord> commonlyTyped;
    private boolean setCommonlyTyped = false;

    private ArrayList<SuperWord> inCategory;
    private ArrayList<SuperWord> hasCategories;
    private ArrayList<SuperWord> commonCategories;
    private boolean setCommonCategories = false;

    private ArrayList<SuperWord> partOf;
    private ArrayList<SuperWord> hasParts;

    private ArrayList<SuperWord> similarTo;

    /**
     * 
     * @param plaintext    used to make log messages more readable.
     * @param resultObject
     */
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

        if (resultObject.containsKey("synonyms")) {
            List<Object> synonymsList = (List<Object>) resultObject.get("synonyms");
            synonyms = SuperWord.batchPlaceHolders(synonymsList);
        }

        if (resultObject.containsKey("typeOf")) {
            List<Object> typeOfList = (List<Object>) resultObject.get("typeOf");
            typeOf = SuperWord.batchPlaceHolders(typeOfList);
        }

        if (resultObject.containsKey("hasTypes")) {
            List<Object> hasTypesList = (List<Object>) resultObject.get("hasTypes");
            hasTypes = SuperWord.batchPlaceHolders(hasTypesList);
        }

        if (resultObject.containsKey("inCategory")) {
            List<Object> inCategoryList = (List<Object>) resultObject.get("inCategory");
            inCategory = SuperWord.batchPlaceHolders(inCategoryList);
        }

        if (resultObject.containsKey("hasCategories")) {
            List<Object> hasCategoriesList = (List<Object>) resultObject.get("hasCategories");
            hasCategories = SuperWord.batchPlaceHolders(hasCategoriesList);
        }

        if (resultObject.containsKey("partOf")) {
            List<Object> partOfList = (List<Object>) resultObject.get("partOf");
            partOf = SuperWord.batchPlaceHolders(partOfList);
        }

        if (resultObject.containsKey("hasParts")) {
            List<Object> hasPartsList = (List<Object>) resultObject.get("hasParts");
            hasParts = SuperWord.batchPlaceHolders(hasPartsList);
        }

        if (resultObject.containsKey("similarTo")) {
            List<Object> similarToList = (List<Object>) resultObject.get("similarTo");
            similarTo = SuperWord.batchPlaceHolders(similarToList);
        }
    }

    public void populateSynonyms() {
        for (SuperWord synonym : synonyms) {
            synonym.populate();
        }
    }

    public void populateTypeOf() {
        for (SuperWord type : typeOf) {
            type.populate();
        }
    }

    public void populateSimilarTo() {
        for (SuperWord similarToElement : similarTo) {
            similarToElement.populate();
        }
    }

    public void setCommonlyTyped() {
        if (setCommonlyTyped || typeOf == null)
            return;

        for (SuperWord type : typeOf) {
            commonlyTyped = addAllToNull(commonlyTyped, type.getHasTypes(this.partOfSpeech, parent));
        }
        setCommonlyTyped = true;
    }

    public void setCommonCategories() {
        if (setCommonCategories || inCategory == null)
            return;

        for (SuperWord category : inCategory) {
            commonlyTyped = addAllToNull(commonlyTyped, category.getHasCategories(this.partOfSpeech));
        }
        setCommonCategories = true;
    }

    private void setPartOfSpeech(String partOfSpeech) {
        if (partOfSpeech == null) {
            partOfSpeech = "null";
        }
        switch (partOfSpeech) {
            case "noun":
                this.partOfSpeech = PartOfSpeech.NOUN;
                break;
            case "pronoun":
                this.partOfSpeech = PartOfSpeech.PRONOUN;
                break;
            case "verb":
                this.partOfSpeech = PartOfSpeech.VERB;
                break;
            case "adjective":
                this.partOfSpeech = PartOfSpeech.ADJECTIVE;
                break;
            case "adverb":
                this.partOfSpeech = PartOfSpeech.ADVERB;
                break;
            case "preposition":
                this.partOfSpeech = PartOfSpeech.PREPOSITION;
                break;
            case "conjunction":
                this.partOfSpeech = PartOfSpeech.CONJUCTION;
                break;
            case "definite article":
                this.partOfSpeech = PartOfSpeech.DEFINITE_ARTICLE;
                break;
            default:
                LOG.writePersistentLog(
                        String.format("Unrecognised part of speech for \"%s\" with definition \"%s\": \"%s\"",
                                parent.getPlaintext(), this.definition, partOfSpeech));
                // partOfSpeech will be left as UNKNOWN
                break;
        }

    }

    public PartOfSpeech partOfSpeech() {
        return partOfSpeech;
    }

    public ArrayList<SuperWord> getSynonyms() {
        return synonyms;
    }

    public ArrayList<SuperWord> getTypeOf() {
        return typeOf;
    }

    public ArrayList<SuperWord> getHasTypes() {
        return hasTypes;
    }

    public ArrayList<SuperWord> getCommonlyTyped() {
        if (!setCommonlyTyped)
            setCommonlyTyped();
        return commonlyTyped;
    }

    public ArrayList<SuperWord> getHasCategories() {
        return hasCategories;
    }

    public ArrayList<SuperWord> getCommonCategories() {
        if (!setCommonCategories)
            setCommonCategories();
        return commonCategories;
    }

    public ArrayList<SuperWord> getPartOf() {
        return partOf;
    }

    public ArrayList<SuperWord> getHasParts() {
        return hasParts;
    }

    public ArrayList<SuperWord> getSimilarTo() {
        return similarTo;
    }

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
        if (synonyms != null) {
            stringBuilder.append(divider);
            stringBuilder.append("synonyms: " + synonyms.toString());
        }
        if (typeOf != null) {
            stringBuilder.append(divider);
            stringBuilder.append("typeOf: " + typeOf.toString());
        }
        if (hasTypes != null) {
            stringBuilder.append(divider);
            stringBuilder.append("hasTypes: " + hasTypes.toString());
        }
        if (commonlyTyped != null) {
            stringBuilder.append(divider);
            stringBuilder.append("commonlyTyped: " + commonlyTyped.toString());
        }
        if (inCategory != null) {
            stringBuilder.append(divider);
            stringBuilder.append("inCategory: " + inCategory.toString());
        }
        if (hasCategories != null) {
            stringBuilder.append(divider);
            stringBuilder.append("hasCategories: " + hasCategories.toString());
        }
        if (commonCategories != null) {
            stringBuilder.append(divider);
            stringBuilder.append("commonCategories: " + commonCategories.toString());
        }
        if (partOf != null) {
            stringBuilder.append(divider);
            stringBuilder.append("partOf: " + partOf.toString());
        }
        if (hasParts != null) {
            stringBuilder.append(divider);
            stringBuilder.append("hasParts: " + hasParts.toString());
        }
        if (similarTo != null) {
            stringBuilder.append(divider);
            stringBuilder.append("similarTo: " + similarTo.toString());
        }
        stringBuilder.append("\n}");
        return stringBuilder.toString();
    }

}
