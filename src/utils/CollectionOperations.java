package utils;

import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;

public interface CollectionOperations {

    /**
     * Returns a version of a JSON array with duplicates removed.
     * 
     * @param array the JSONArray to filter.
     * @return a new JSONArray with no duplicates.
     */
    public static JSONArray removeDuplicates(JSONArray array) {
        List<Object> list = array.toList();
        list = list.stream().distinct().collect(Collectors.toList());
        return new JSONArray(list);
    }
    
}
