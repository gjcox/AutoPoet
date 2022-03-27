package utils;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import words.PartOfSpeech;
import words.SuperWord;

/**
 * This class was to make passing arguments into suggestion getting and
 * filtering more managable.
 * 
 * @author 190021081
 */
public interface ParameterWrappers {

    /**
     * Used to determine which suggestion pools to draw suggestions from, and
     * whether or not to treat unknowns inclusively.
     * 
     * Method comments considered self-explanatory.
     */
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

        /**
         * {@inheritDoc}
         */
        public String toString() {
            StringBuilder builder = new StringBuilder("{");
            for (SuggestionPool pool : SuggestionPool.values()) {
                if (pools.get(pool).booleanValue()) {
                    builder.append(pool.name() + ",");
                }
            }

            builder.append(String.format("inclusiveUnknown: %b", inclusiveUnknown));

            builder.append("}");
            return builder.toString();
        }
    }

    /**
     * Used to determine which rhyme types to check for (and against which words and
     * part of speech), and whether or not to maintain syllable count.
     * 
     * Method comments considered self-explanatory.
     */
    public class FilterParameters {
        public enum RhymeType {
            PERFECT_RHYME("perfect rhyme", "Exact match from stressed syllables, e.g. tragic/magic."),
            SYLLABIC_RHYME("syllabic rhyme", "Exact match on the last syllables, e.g. fiddle/fuddle."),
            IMPERFECT_RHYME("imperfect rhyme",
                    "Exact match from a stressed syllable to an unstressed syllable, or between two secondary stressed syllables, e.g. zombie/bee."),
            WEAK_RHYME("weak rhyme", "Exact match between unstressed syllables, e.g. dependent/sediment."),
            FORCED_RHYME("forced rhyme", "Inexact match from stressed syllables. Not yet implemented.");

            private final String label;
            private final String explanation;

            private RhymeType(String label, String explanation) {
                this.label = label;
                this.explanation = explanation;
            }

            public String getLabel() {
                return label;
            }

            public String getExplanation() {
                return explanation;
            }

        }

        private EnumMap<RhymeType, List<SuperWord>> rhymeFilters = new EnumMap<>(RhymeType.class);
        private PartOfSpeech matchWithPoS = null; // if null, match all PoS
        private boolean syllableCountFilter = false;

        public void setRhymeFilter(RhymeType rhymeType, SuperWord matchWith) {
            rhymeFilters.computeIfAbsent(rhymeType, k -> new ArrayList<SuperWord>()).add(matchWith);
        }

        public void setMatchPoS(PartOfSpeech matchPoS) {
            this.matchWithPoS = matchPoS;
        }

        public void setSyllableCountFilter(boolean syllableCountFilter) {
            this.syllableCountFilter = syllableCountFilter;
        }

        public void removeFilter(RhymeType filter) {
            rhymeFilters.remove(filter);
        }

        public List<SuperWord> getMatchWith(RhymeType filter) {
            return rhymeFilters.get(filter);
        }

        public PartOfSpeech getMatchPoS() {
            return matchWithPoS;
        }

        public boolean syllableCountFilter() {
            return syllableCountFilter;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            StringBuilder builder = new StringBuilder("{");
            for (RhymeType filter : RhymeType.values()) {
                if (rhymeFilters.get(filter) != null) {
                    builder.append(String.format("%s(%s), ", filter.name(), rhymeFilters.get(filter)));
                }
            }

            builder.append(String.format("syllableCountFilter: %b", syllableCountFilter));
            builder.append("}");
            return builder.toString();
        }
    }
}
