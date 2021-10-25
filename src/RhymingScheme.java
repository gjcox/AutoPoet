public class RhymingScheme {

    private int line_count = 0; // need at least two lines for rhymes
    private int[] scheme; // zero is a special case that denotes no rhyming necessary

    /**
     * Whose woods these are I think I know. His house is in the village though; He
     * will not see me stopping here To watch his woods fill up with snow.
     * 
     * This would be encoded as {line_count = 4, scheme = [1,1,0,1]}
     */

    /**
     * Constructor for a rhyming scheme.
     *
     * @param line_count
     * @param scheme
     */
    public RhymingScheme(int line_count, int[] scheme) {
        this.line_count = line_count;
        this.scheme = scheme.clone(); // could save me a headache if I change scheme's data type
    }

    public changeScheme(int index, int value) {
         this.scheme[index] = value; 
    }

    public _lineCount() {
        return this.line_count; 
    }

    public _scheme() {
        return this.scheme; 
    }

}
