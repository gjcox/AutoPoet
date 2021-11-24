package utils;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * JSONObjects can nest more easily than other Objects can be placed inside
 * JSONObjects, which is why I used these rather than actual Java classes.
 */
public interface JSONConstructors {

    public abstract static class Emphasis {

        public static final String PRIMARY = "primary";
        public static final String SECONDARY = "secondary";

        /**
         * Sets the default primary emphasis to position zero, to account for
         * single-syllable words not having a marked stress.
         * 
         * @return a JSONObject {primary: 0, secondary: []}.
         */
        public static JSONObject newEmphasisObject() {
            return new JSONObject(String.format("{%s: 0, %s: []}", PRIMARY, SECONDARY));
        }

        /**
         * Converts the JSONArray of secondary emphases indexes into a (Linked)List.
         * 
         * @param object a JSONObject made with the above constructor.
         * @return a LinkedList<Integer> of the emphasis point. Can be empty. 
         */
        public static List<Integer> getSecondary(JSONObject object) {
            List<Integer> integer_list = new LinkedList<>();
            List<Object> object_list = ((JSONArray) object.get(SECONDARY)).toList();
            for (Object value : object_list) {
                integer_list.add((Integer) value);
            }
            return integer_list;
        }
    }
}
