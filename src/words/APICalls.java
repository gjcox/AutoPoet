package words;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Handles interactions with WordsAPI
 * ! Need to handle 404 errors as well as empty results 
 * Should find a way to wrap the API handling further - lots of code duplication for handling error cases 
 */
public class APICalls {

    static HttpClient client = HttpClient.newBuilder().proxy(ProxySelector.getDefault()).build();

    /**
     * 
     * @param uri
     * @return
     * @throws Exception
     */
    static JSONObject sendRequest(URI uri) throws IOException, InterruptedException {
        JSONObject response;
        try {
            HttpResponse<String> response_string = client.send(getRequest(uri), BodyHandlers.ofString());
            response = new JSONObject(response_string.body());
            // System.out.println(response);
            return response;
        } catch (IOException ioe) {
            System.err.println("Something went wrong: " + ioe.getMessage());
            throw ioe;
        } catch (InterruptedException ie) {
            System.err.println("Something went wrong: " + ie.getMessage());
            throw ie;
        }
    }

    /**
     * 
     * @param uri
     * @return
     */
    static HttpRequest getRequest(URI uri) {
        return HttpRequest.newBuilder().uri(uri).header("x-rapidapi-host", "wordsapiv1.p.rapidapi.com")
                .header("x-rapidapi-key", "9aa07eab5amsh819a29a74fb1a8bp14a516jsna22ffeb0ecfc")
                .method("GET", HttpRequest.BodyPublishers.noBody()).build();
    }

    static URI getUri(String word, String _info) {
        word = word.replace(" ", "%20"); // remove spaces
        return URI.create("https://wordsapiv1.p.rapidapi.com/words/" + word + "/" + _info);
    }

    /**
     * 
     * @param word
     * @return
     */
    static JSONArray getSynonyms(String word) {
        JSONArray synonyms = new JSONArray();
        String quality = "synonyms";
        URI uri = getUri(word, quality);
        try {
            JSONObject jo = sendRequest(uri);

            if (((JSONArray) jo.get(quality)).isEmpty() && word.endsWith("s")) {
                word = word.substring(0, word.length() - 1);
                System.err.println("\"" + word + "s\" may be a plural. Attempting search for " + quality + " of \""
                        + word + "\".");
                return getSynonyms(word);
            } else if (((JSONArray) jo.get(quality)).isEmpty()) {
                System.err.println("No " + quality + " found for \"" + word + "\".");
            } else {
                synonyms = (JSONArray) jo.get(quality);

                System.out.println("Synonyms of \"" + word + "\":" + synonyms);
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
            // just trying to make this work
        }
        return synonyms;
    }

    static JSONArray getTypesOf(String word) {
        JSONArray types = new JSONArray();
        String quality = "hasTypes";
        URI uri = getUri(word, quality);
        try {
            JSONObject jo = sendRequest(uri);

            if (((JSONArray) jo.get(quality)).isEmpty() && word.endsWith("s")) {
                word = word.substring(0, word.length() - 1);
                System.err.println("\"" + word + "s\" may be a plural. Attempting search for " + quality + " of \""
                        + word + "\".");
                return getSynonyms(word);
            } else if (((JSONArray) jo.get(quality)).isEmpty()) {
                System.err.println("No " + quality + " found for \"" + word + "\".");
            } else {
                types = (JSONArray) jo.get(quality);

                System.out.println("Types of \"" + word + "\":" + types);
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
            // just trying to make this work
        }
        return types;
    }

    static JSONArray getCommonType(String word) {
        JSONArray common_type = new JSONArray(); // not strictly synonyms, rather having a common type
        String quality = "typeOf";
        URI uri = getUri(word, quality);
        try {
            JSONObject jo = sendRequest(uri);

            if (((JSONArray) jo.get(quality)).isEmpty() && word.endsWith("s")) {
                word = word.substring(0, word.length() - 1);
                System.err.println("\"" + word + "s\" may be a plural. Attempting search for " + quality + " of \""
                        + word + "\".");
                return getCommonType(word);
            } else if (((JSONArray) jo.get(quality)).isEmpty()) {
                System.err.println("No " + quality + " found for \"" + word + "\".");
            } else {
                JSONArray types = (JSONArray) jo.get(quality);
                System.out.println("\"" + word + "\" is a type of:" + types);

                List<String> types_list = (List<String>) (List<?>) types.toList();
                for (String type : types_list) {
                    common_type.putAll(getTypesOf(type));
                }

                common_type = removeDuplicates(common_type);

                System.out.println("Words with common types as \"" + word + "\":" + common_type);
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
            // just trying to make this work
        }
        return common_type;

    }

    /***
     * 
     * @param word
     * @return
     */
    static JSONArray getRhymes(String word) {
        JSONArray rhymes = new JSONArray();
        String quality = "rhymes";
        URI uri = getUri(word, quality);
        try {
            JSONObject jo = sendRequest(uri);

            if (((JSONObject) jo.get(quality)).isEmpty() && word.endsWith("s")) {
                word = word.substring(0, word.length() - 1);
                System.err.println("\"" + word + "s\" may be a plural. Attempting search for " + quality + " of \""
                        + word + "\".");
                return getRhymes(word);
            } else if (((JSONObject) jo.get(quality)).isEmpty()) {
                System.err.println("No " + quality + " found for \"" + word + "\".");
            } else {
                rhymes = (JSONArray) ((JSONObject) jo.get(quality)).get("all"); // might want to filter more than "all"
                                                                                // someday
                System.out.println("Rhymes of \"" + word + "\":" + rhymes);
            }

        } catch (

        Exception e) {
            System.err.println(e.getMessage());
            // just trying to make this work
        }
        return rhymes;
    }

    static JSONObject getIPA(String word) {
        JSONObject ipa = new JSONObject();
        String quality = "pronunciation";
        URI uri = getUri(word, quality);
        try {
            JSONObject jo = sendRequest(uri);
            boolean removed_s = (jo.get("word").toString() + "s").equals(word); // need to add "z" to ipa 

            if (!jo.optString("success").equals("") || ((JSONObject) jo.opt("pronunciation")).isEmpty()) {
                return ipa; // no IPA found for the word
            }

            if (removed_s) {
                // obviosuly not finished 
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
            // just trying to make this work
        } catch (InterruptedException ie) {
            System.err.println(ie.getMessage());
            return getIPA(word); // optimistically try again after an interruption
            // could cause an infinite loop...
        }
        return ipa;
    }

    static JSONArray removeDuplicates(JSONArray array) {
        List<Object> list = array.toList();
        list = list.stream().distinct().collect(Collectors.toList());
        return new JSONArray(list);
    }

}
