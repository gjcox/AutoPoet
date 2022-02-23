package utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * Instantiates an empty ArrayList<T> iff the passed list is null. Then behaves
     * as Collection.addAll().
     */
    public static <T> ArrayList<T> addAllToNullIfMatching(ArrayList<T> list, Collection<T> source, T matching) {
        if (source == null) {
            return list;
        }
        if (!source.contains(matching)) {
            return list;
        }
        if (list == null) {
            list = new ArrayList<>();
        }
        list.addAll(source);
        return list;
    }

    @SafeVarargs
    public static <T> ArrayList<T> combineListsVarags(List<T>... lists) {
        ArrayList<T> combined = null;
        for (List<T> list : lists) {
            combined = addAllToNull(combined, list);
        }
        return combined;
    }

    public static <T> ArrayList<T> combineLists(Collection<ArrayList<T>> lists) {
        ArrayList<T> combined = null;
        for (List<T> list : lists) {
            combined = addAllToNull(combined, list);
        }
        return combined;
    }

    static class SortByCount<T> implements Comparator<T> {

        private Map<T, Integer> counts;

        public SortByCount(Map<T, Integer> counts) {
            this.counts = counts;
        }

        @Override
        public int compare(T o1, T o2) {
            return counts.get(o2) - counts.get(o1);
        }

    }

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
