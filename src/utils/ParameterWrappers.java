package utils;

import java.util.EnumMap;

import words.SubWord.PartOfSpeech;
import words.SuperWord;

public interface ParameterWrappers {
    public class SuggestionPoolParameters {

        public enum SuggestionPool {
            SYNONYMS("synonyms", "synonyms", true),
            HAS_TYPES("has types", "hasTypes", true),
            TYPE_OF("type of", "typeOf", true),
            COMMONLY_TYPED("commonly typed", null, false),
            IN_CATEGORY("inCategory", "inCategory", true),
            HAS_CATEGORIES("hasCategories", "hasCategories", true),
            COMMON_CATEGORIES("commonly categorised", null, false),
            PART_OF("part of", "partOf", true),
            HAS_PARTS("has parts", "hasParts", true),
            SIMILAR_TO("similar to", "similarTo", true);

            private final String label;
            private final String apiString; // true if the field is an attribute of WordsAPI subwords
            private final boolean apiProperty; // true if the field is an attribute of WordsAPI subwords

            private SuggestionPool(String label, String apiString, boolean subWordProperty) {
                this.label = label;
                this.apiString = apiString;
                this.apiProperty = subWordProperty;
            }

            public static SuggestionPool fromString(String string) {
                for (SuggestionPool pool : SuggestionPool.values()) {
                    if (pool.label.equals(string) || (pool.apiString != null && pool.apiString.equals(string)))
                        return pool;
                }
                return null;
            }

            public String getLabel() {
                return label;
            }

            public String getApiString() {
                return apiString;
            }

            public boolean isApiProperty() {
                return apiProperty;
            }
        }

        private EnumMap<SuggestionPool, Boolean> pools = new EnumMap<>(SuggestionPool.class);
        private boolean inclusiveUnknown = false;

        public SuggestionPoolParameters() {
            for (SuggestionPool pool : SuggestionPool.values()) {
                pools.put(pool, false);
            }
        }

        public boolean includes(SuggestionPool pool) {
            return pools.get(pool).booleanValue();
        }

        public void togglePool(SuggestionPool pool, boolean include) {
            pools.put(pool, include);
        }

        public void setInclusiveUnknown(boolean inclusiveUnknown) {
            this.inclusiveUnknown = inclusiveUnknown;
        }

        public boolean hasInclusiveUnknown() {
            return inclusiveUnknown;
        }

        public boolean isEmpty() {
            for (SuggestionPool pool : SuggestionPool.values()) {
                if (includes(pool)) {
                    return false;
                }
            }
            return true;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder("[");
            for (SuggestionPool pool : SuggestionPool.values()) {
                if (pools.get(pool).booleanValue()) {
                    builder.append(pool.name() + ",");
                }
            }

            int commaIndex = builder.lastIndexOf(",");
            if (commaIndex > -1) {
                builder.deleteCharAt(commaIndex);
            }

            builder.append("]");
            return builder.toString();
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
