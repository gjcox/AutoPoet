package utils;

public class Pair<O, T> {
    
    private O one; 
    private T two; 

    public Pair(O one, T two){
        this.one = one;
        this.two = two; 
    }

    public O one() {
        return this.one; 
    }

    public T two() {
        return this.two; 
    }

}
