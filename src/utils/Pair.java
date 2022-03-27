package utils;

/**
 * This class is primarily to allow me to return two values from one function,
 * and behaves as a heterogenous tuple.
 * 
 * @author 190021081
 */
public class Pair<O, T> {

    private O one;
    private T two;

    public Pair(O one, T two) {
        this.one = one;
        this.two = two;
    }

    public O one() {
        return this.one;
    }

    public T two() {
        return this.two;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return String.format("(%s,%s)", one.toString(), two.toString());
    }

}
