package utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class of static wrapper functions to add to and combine pontentially null
 * lists.
 * 
 * @author 190021081
 */
public class NullListOperations {

    /**
     * Instantiates an empty ArrayList<T> iff the passed list is null. Then
     * behaves as Collection.add().
     * 
     * @param <T>     the parameterised type of @param list.
     * @param list    a potentially null list.
     * @param element an element to add to the list.
     * @return the (potentially newly instantiated) list, with the added element.
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
     * 
     * @param <T>    the parameterised type of @param list.
     * @param list   a potentially null list.
     * @param source a collection of element to add to the list.
     * @return the (potentially newly instantiated) list, with the added elements.
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
     * Combines one or more lists into one. Will include duplicates.
     * 
     * @param lists lists to combine.
     * @return null if no lists to combine, otherwise a combined list.
     */
    @SafeVarargs
    public static <T> ArrayList<T> combineListsVarags(List<T>... lists) {
        ArrayList<T> combined = null;
        for (List<T> list : lists) {
            combined = addAllToNull(combined, list);
        }
        return combined;
    }

    /**
     * Combines one or more lists into one. Will include duplicates.
     * 
     * @param lists lists to combine.
     * @return null if no lists to combine, otherwise a combined list.
     */
    public static <T> ArrayList<T> combineLists(Collection<ArrayList<T>> lists) {
        ArrayList<T> combined = null;
        for (List<T> list : lists) {
            combined = addAllToNull(combined, list);
        }
        return combined;
    }

    private static class SortByCount<T> implements Comparator<T> {

        private Map<T, Integer> counts;

        public SortByCount(Map<T, Integer> counts) {
            this.counts = counts;
        }

        @Override
        public int compare(T o1, T o2) {
            return counts.get(o2) - counts.get(o1);
        }

    }

    /**
     * Combines one or more lists into one. Orders elements based on number of
     * repetitions, in descending order of occurrences.
     * 
     * @param lists lists to combine.
     * @return null if no lists to combine, otherwise a combined list as described.
     */
    public static <T> ArrayList<T> combineListsPrioritiseDuplicates(ArrayList<ArrayList<T>> lists) {
        Map<T, Integer> counts = new HashMap<>();
        ArrayList<T> combined = new ArrayList<>();
        for (List<T> list : lists) {
            if (list != null) {
                for (T element : list) {
                    Integer count;
                    if ((count = counts.get(element)) != null) {
                        counts.replace(element, count + 1);
                    } else {
                        counts.put(element, 1);
                        combined.add(element);
                    }
                }
            }
        }

        if (combined.isEmpty()) {
            return null;
        } else {
            combined.sort(new SortByCount<>(counts));
            return combined;
        }
    }

}
