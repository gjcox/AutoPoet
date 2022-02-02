package utils;

import words.SuperWord;
import words.SubWord.PartOfSpeech;

public interface ParameterWrappers {
    public class SuggestionParameters {

        public enum SuggestionPool {
            SYNONYMS("synonyms"),
            HAS_TYPES("has types"),
            TYPE_OF("type of"),
            COMMONLY_TYPED("commonly typed"),
            COMMON_CATEGORIES("commonly categorised"),
            PART_OF("part of"),
            HAS_PARTS("has parts"),
            SIMILAR_TO("similar to");

            private final String name;

            private SuggestionPool(String name) {
                this.name = name;
            }

            public static SuggestionPool fromString(String string) {
                for (SuggestionPool pool : SuggestionPool.values()) {
                    if (pool.name.equals(string))
                        return pool;
                }
                return null;
            }
        }

        boolean synonyms = false;
        boolean hasTypes = false;
        boolean typeOf = false;
        boolean commonlyTyped = false;
        boolean commonCategories = false;
        boolean partOf = false;
        boolean hasParts = false;
        boolean similarTo = false;

        public SuggestionParameters() {
            // leave all as false
        }

        public String toString() {
            StringBuilder builder = new StringBuilder("[");
            if (synonyms) {
                builder.append("synonyms,");
            }
            if (commonlyTyped) {
                builder.append("commonlyTyped,");
            }
            if (commonCategories) {
                builder.append("commonCategories,");
            }
            if (partOf) {
                builder.append("partOf,");
            }
            if (hasParts) {
                builder.append("hasParts,");
            }
            if (similarTo) {
                builder.append("similarTo,");
            }

            int commaIndex = builder.lastIndexOf(",");
            if (commaIndex > -1) {
                builder.deleteCharAt(commaIndex);
            }
            builder.append("]");
            return builder.toString();
        }

        public boolean includes(SuggestionPool pool) {
            switch (pool) {
                case COMMONLY_TYPED:
                    return commonlyTyped;
                case COMMON_CATEGORIES:
                    return commonCategories;
                case HAS_PARTS:
                    return hasParts;
                case PART_OF:
                    return partOf;
                case SIMILAR_TO:
                    return similarTo;
                case SYNONYMS:
                    return synonyms;
                default:
                    return false;
            }
        }

        public void togglePool(SuggestionPool pool, boolean include) {
            switch (pool) {
                case COMMONLY_TYPED:
                    commonlyTyped = include;
                    break;
                case COMMON_CATEGORIES:
                    commonCategories = include;
                    break;
                case HAS_PARTS:
                    hasParts = include;
                    break;
                case HAS_TYPES:
                    hasTypes = include;
                    break;
                case PART_OF:
                    partOf = include;
                    break;
                case SIMILAR_TO:
                    similarTo = include;
                    break;
                case SYNONYMS:
                    synonyms = include;
                    break;
                case TYPE_OF:
                    typeOf = include;
                    break;
                default:
                    break;
            }
        }

        public boolean isEmpty() {
            for (SuggestionPool pool : SuggestionPool.values()) {
                if (includes(pool)) {
                    return false;
                }
            }
            return true;
        }

        public boolean synonyms() {
            return synonyms;
        }

        public boolean commonlyTyped() {
            return commonlyTyped;
        }

        public boolean commonCategories() {
            return commonCategories;
        }

        public boolean partOf() {
            return partOf;
        }

        public boolean hasParts() {
            return hasParts;
        }

        public boolean similarTo() {
            return similarTo;
        }
    }

    public class FilterParameters {
        boolean rhyme;
        SuperWord rhymeWith;
        PartOfSpeech rhymePos;

        public FilterParameters(boolean rhyme, SuperWord rhymeWith, PartOfSpeech pos) {
            this.rhyme = rhyme;
            this.rhymeWith = rhymeWith;
            this.rhymePos = pos;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder("{");
            if (rhyme) {
                builder.append("rhyme:");
                builder.append(String.format("\"%s\"", rhymeWith.getPlaintext()));
                builder.append(String.format("(%s),", rhymePos));
            }

            int commaIndex = builder.lastIndexOf(",");
            if (commaIndex > -1) {
                builder.deleteCharAt(commaIndex);
            }
            builder.append("}");
            return builder.toString();
        }

        public boolean rhyme() {
            return rhyme;
        }

        public SuperWord rhymeWith() {
            return rhymeWith;
        }

        public PartOfSpeech rhymePos() {
            return rhymePos;
        }
    }
}
