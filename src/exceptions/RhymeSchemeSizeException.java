package exceptions;

/**
 * An extension of the Exception class, for catching when a stanza is assigned a
 * rhyme scheme of incorrect length.
 * 
 * @author 190021081
 */
public class RhymeSchemeSizeException extends Exception {

    public RhymeSchemeSizeException(String message) {
        super(message);
    }

}
