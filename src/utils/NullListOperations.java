package utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NullListOperations {

    /**
     * Instantiates an empty ArrayList<T> iff the passed list is null. Then behaves
     * as Collection.add().
     */
    public static <T> ArrayList<T> addToNull(ArrayList<T> list, T element) {
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(element); 
        return list;
    }

    /**
     * Instantiates an empty ArrayList<T> iff the passed list is null. Then behaves
     * as Collection.addAll().
     */
    public static <T> ArrayList<T> addAllToNull(ArrayList<T> list, Collection<T> source) {
        if (source == null) {
            return list; 
        }
        if (list == null) {
            list = new ArrayList<>();
        }
        list.addAll(source);
        return list; 
    }

    /**
     * Instantiates an empty ArrayList<T> iff the passed list is null. Then casts
     * contents of source and behaves as Collection.addAll().
     */
    @SuppressWarnings("unchecked")
    public static <T1, T2> boolean castAndAddAllToNull(List<T1> list, Collection<T2> source) {
        if (list == null) {
            list = new ArrayList<>();
        }
        return list.addAll((Collection<T1>) source);
    }

}
