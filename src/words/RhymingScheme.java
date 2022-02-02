package words;

import java.util.Arrays;

import exceptions.RhymingSchemeSizeException;

/*
 * Whose woods these are I think I know.
 * His house is in the village though;
 * He will not see me stopping here
 * To watch his woods fill up with snow.
 * 
 * This would be encoded as {lineCount = 4, scheme = [1,1,0,1]}
 */

public class RhymingScheme {

    private int lineCount = 0; // need at least two lines for rhymes
    private int[] scheme; // zero is a special case that denotes no rhyming necessary
    private int nextValue = 1;

    public RhymingScheme(int lineCount) {
        this.lineCount = lineCount;
        this.scheme = new int[lineCount];
        for (int i = 0; i < lineCount; i++) {
            this.scheme[i] = 0;
        }
    }

    public RhymingScheme(int lineCount, int[] scheme) throws RhymingSchemeSizeException {
        if (scheme.length != lineCount) {
            throw new RhymingSchemeSizeException(
                    String.format("%s did not match line count %d", Arrays.toString(scheme), lineCount));
        }
        this.lineCount = lineCount;
        this.scheme = scheme.clone(); // could save me a headache if I change scheme's data type
    }

    public RhymingScheme(int lineCount, char[] scheme) throws RhymingSchemeSizeException {
        if (scheme.length != lineCount) {
            throw new RhymingSchemeSizeException(
                    String.format("%s did not match line count %d", Arrays.toString(scheme), lineCount));
        }
        this.lineCount = lineCount;
        this.scheme = convertChars(scheme); // could save me a headache if I change scheme's data type
    }

    public RhymingScheme(char[] scheme) {
        this.lineCount = scheme.length;
        this.scheme = convertChars(scheme); // could save me a headache if I change scheme's data type
    }

    public void setValue(int index, int value) {
        this.scheme[index] = value;
    }

    public int getLineCount() {
        return this.lineCount;
    }

    public int[] getScheme() {
        return this.scheme;
    }

    public int getValue(int index) {
        return this.scheme[index];
    }

    public int getNextValue() {
        return this.nextValue++;
    }

    private char convertInt(int n) {
        if (n == 0) {
            return '#';
        } else {
            return (char) ('A' + ((n - 1) % 26));
        }
    }

    private int[] convertChars(char[] chars) {
        int[] ints = new int[chars.length];
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '#') {
                ints[i] = 0;
            } else {
                ints[i] = chars[i] - 'A' + 1;
            }
        }
        return ints;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int value : scheme) {
            builder.append(convertInt(value));
        }
        return builder.toString();
    }
}
