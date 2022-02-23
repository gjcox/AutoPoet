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
            IN_CATEGORY("inCategory", "in category", true),
            HAS_CATEGORIES("hasCategories", "has categories", true),
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
        public enum Filter {
            PERFECT_RHYME("perfect rhyme", true),
            SYLLABIC_RHYME("syllabic rhyme", true), // only the last syllable
            IMPERFECT_RHYME("imperfect rhyme", true), // stressed syllable to unstressed syllable
            WEAK_RHYME("weak rhyme", true), // unstressed syllable to unstressed syllable
            FORCED_RHYME("forced rhyme", true), // stressed to stressed, similar sound but not exact match
            ;

            private final String label;
            private final boolean isRhyme;

            private Filter(String label, boolean isRhyme) {
                this.label = label;
                this.isRhyme = isRhyme;
            }

            public String getLabel() {
                return label;
            }

            public boolean isRhymeType() {
                return isRhyme;
            }
        }

        EnumMap<Filter, SuperWord> filters = new EnumMap<>(Filter.class);
        PartOfSpeech matchPoS = null; // if null, match all PoS
        boolean inclusiveUnknown = false; // TODO implement effect of this in matchesWith()

        public void setFilter(Filter filter, SuperWord matchWith) {
            filters.put(filter, matchWith);
        }

        public void setMatchPoS(PartOfSpeech matchPoS) {
            this.matchPoS = matchPoS; 
        }

        public void setInclusiveUnknown(boolean inclusiveUnknown) {
            this.inclusiveUnknown = inclusiveUnknown; 
        }

        public void removeFilter(Filter filter) {
            filters.remove(filter);
        }

        public SuperWord getMatchWith(Filter filter) {
            return filters.get(filter);
        }

        public PartOfSpeech getMatchPoS() {
            return matchPoS;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder("{");
            for (Filter filter : Filter.values()) {
                if (filters.get(filter) != null) {
                    builder.append(String.format("%s(%s), ", filter.name(), filters.get(filter)));
                }
            }

            int commaIndex = builder.lastIndexOf(",");
            if (commaIndex > -1) {
                builder.deleteCharAt(commaIndex);
            }
            builder.append("}");
            return builder.toString();
        }
    }
}
