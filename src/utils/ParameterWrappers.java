package utils;

import java.util.EnumMap;

import words.SubWord.PartOfSpeech;
import words.SuperWord;

public interface ParameterWrappers {
    public class SuggestionPoolParameters {

        public enum SuggestionPool {
            SYNONYMS("synonyms", "synonyms", true),
            ANTONYMS("antonyms", "antonyms", true),
            SIMILAR_TO("similar to", "similarTo", true),
            HAS_TYPES("has types", "hasTypes", true),
            TYPE_OF("type of", "typeOf", true),
            COMMONLY_TYPED("commonly typed", null, false),
            IN_CATEGORY("in category", "inCategory", true),
            HAS_CATEGORIES("has categories", "hasCategories", true),
            COMMON_CATEGORIES("commonly categorised", null, false),
            PART_OF("part of", "partOf", true),
            HAS_PARTS("has parts", "hasParts", true),
            ;

            private final String label; // the label for use in a GUI etc 
            private final String apiString; // the label used by WordsAPI
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
            PERFECT_RHYME("perfect rhyme", "Exact match from stressed syllables, e.g. tragic/magic.", true),
            SYLLABIC_RHYME("syllabic rhyme", "Exact match on the last syllables, e.g. fiddle/fuddle.", true),
            IMPERFECT_RHYME("imperfect rhyme",
                    "Exact match from a stressed syllable to an unstressed syllable, or between two secondary stressed syllables, e.g. zombie/bee.",
                    true),
            WEAK_RHYME("weak rhyme", "Exact match between unstressed syllables, e.g. dependent/sediment.", true),
            FORCED_RHYME("forced rhyme", "Inexact match from stressed syllables. Not yet implemented.", true),
            ;

            private final String label;
            private final String explanation;
            private final boolean isRhyme;

            private Filter(String label, String explanation, boolean isRhyme) {
                this.label = label;
                this.explanation = explanation;
                this.isRhyme = isRhyme;
            }

            public String getLabel() {
                return label;
            }

            public String getExplanation() {
                return explanation;
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
