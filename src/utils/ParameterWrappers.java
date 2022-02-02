package utils;

import java.util.EnumMap;

import words.SubWord.PartOfSpeech;
import words.SuperWord;

public interface ParameterWrappers {
    public class SuggestionPoolParameters {

        public enum SuggestionPool {
            SYNONYMS("synonyms"),
            HAS_TYPES("has types"),
            TYPE_OF("type of"),
            COMMONLY_TYPED("commonly typed"),
            COMMON_CATEGORIES("commonly categorised"),
            PART_OF("part of"),
            HAS_PARTS("has parts"),
            SIMILAR_TO("similar to");

            private final String label;

            private SuggestionPool(String label) {
                this.label = label;
            }

            public static SuggestionPool fromString(String string) {
                for (SuggestionPool pool : SuggestionPool.values()) {
                    if (pool.label.equals(string))
                        return pool;
                }
                return null;
            }

            public String getLabel() {
                return label;
            }
        }

        private EnumMap<SuggestionPool, Boolean> pools = new EnumMap<>(SuggestionPool.class);

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
