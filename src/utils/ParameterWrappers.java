package utils;

import words.SuperWord;
import words.SubWord.PartOfSpeech;

public interface ParameterWrappers {
    public class SuggestionParameters {
        boolean synonyms;
        boolean commonlyTyped;
        boolean commonCategories;
        boolean partOf;
        boolean hasParts;
        boolean similarTo;

        public SuggestionParameters(boolean synonyms, boolean commonlyTyped, boolean commonCategories, boolean partOf,
                boolean hasParts, boolean similarTo) {
            this.synonyms = synonyms;
            this.commonlyTyped = commonlyTyped;
            this.commonCategories = commonCategories;
            this.partOf = partOf;
            this.hasParts = hasParts;
            this.similarTo = similarTo;
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

            builder.deleteCharAt(builder.lastIndexOf(",")); 
            builder.append("]");
            return builder.toString();
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
