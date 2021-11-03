package utils;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class EmphasisKeys {

    public static final String PRIMARY = "primary";
    public static final String SECONDARY = "secondary";

    public static JSONObject newEmphasisObject() {
        return new JSONObject(String.format("{%s: 0, %s: []}", PRIMARY, SECONDARY));
    }

    public static List<Integer> getSecondary(JSONObject object) {
        List<Integer> integer_list = new LinkedList<>();
        List<Object> object_list = ((JSONArray) object.get(SECONDARY)).toList();
        for (Object value : object_list) {
            integer_list.add((Integer) value);
        }
        return integer_list;
    }
}
