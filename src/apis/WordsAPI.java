package apis;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import utils.CollectionOperations;

/**
 * Handles interactions with WordsAPI TODO: Need to handle 404 errors as well as
 * empty results TODO: Should find a way to wrap the API handling further - lots
 * of code duplication for handling error cases
 */
public class WordsAPI {

    static HttpClient client = HttpClient.newBuilder().proxy(ProxySelector.getDefault()).build();

    /**
     * Attempts to send a request to WordsAPI.
     * 
     * @param uri the request to send, which needs to include an API key.
     * @return the body of the response, in the JSON format used by WordsAPI.
     * @throws IOException          if an I/O error occurs during sending/receiving.
     * @throws InterruptedException if the operation is interrupted.
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
     * Attaches the appropriate headers to a URI and converts it into an
     * HttpRequest. Called within sendRequest().
     * 
     * @param uri a URI supplied by getUri().
     * @return the HttpRequest, complete with API key.
     */
    static HttpRequest getRequest(URI uri) {
        return HttpRequest.newBuilder().uri(uri).header("x-rapidapi-host", "wordsapiv1.p.rapidapi.com")
                .header("x-rapidapi-key", "9aa07eab5amsh819a29a74fb1a8bp14a516jsna22ffeb0ecfc")
                .method("GET", HttpRequest.BodyPublishers.noBody()).build();
    }

    /**
     * Creates a URI for a WordsAPI request.
     * 
     * @param word  the plaintext of the word to get data about.
     * @param _info the type of data needed, corresponding to one from the API
     *              https://rapidapi.com/dpventures/api/wordsapi/.
     * @return a formatted uri, with any spaces in the word replaced with %20 for
     *         HTTP use.
     */
    static URI getUri(String word, String _info) {
        word = word.replace(" ", "%20"); // remove spaces
        return URI.create("https://wordsapiv1.p.rapidapi.com/words/" + word + "/" + _info);
    }

    /**
     * Sends a request for the synonyms of a word.
     * 
     * @param word the plaintext word to get synonyms of.
     * @return a populated JSONArray if the request had results, else an empty one.
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

    /**
     * Sends a request for the sub-types of a word (e.g. purple has types violet,
     * lavender, mauve, reddish blue, reddish purple, royal purple).
     * 
     * @param word the plaintext word to get sub-types of.
     * @return a populated JSONArray if the request had results, else an empty one.
     */
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

    /**
     * Finds the words that have a common super-type as a given word (e.g. hat and
     * cap have the common types headgear and headdress).
     * 
     * @param word the plaintext word to get words with a common super-type.
     * @return a populated JSONArray if the request had results, else an empty one.
     */
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

                @SuppressWarnings("unchecked")
                List<String> types_list = (List<String>) (List<?>) types.toList();
                for (String type : types_list) {
                    common_type.putAll(getTypesOf(type));
                }

                common_type = CollectionOperations.removeDuplicates(common_type);

                System.out.println("Words with common types as \"" + word + "\":" + common_type);
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
            // just trying to make this work
        }
        return common_type;

    }

    /**
     * Sends a request for the rhymes of a word. Potentially redundant now that I
     * have implemented rhyme recognition.
     * 
     * @param word the plaintext word to get rhymes of.
     * @return a populated JSONArray if the request had results, else an empty one.
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

    /**
     * Sends a request for the IPA information of a word.
     * 
     * @param word the plaintext word to get the IPA of.
     * @return a populated JSONObject if the request had results, else an empty one.
     */
    public static JSONObject getIPA(String word) {
        JSONObject ipa = new JSONObject();
        String quality = "pronunciation";
        URI uri = getUri(word, quality);
        try {
            JSONObject jo = sendRequest(uri);
            boolean removed_s = (jo.get("word").toString() + "s").equals(word); // need to add "z" to ipa

            if (!jo.optString("success").equals("")) {
                return ipa; // WordsAPI didn't recognise the word
            }

            Object pronunciation = jo.get(quality);
            switch (pronunciation.getClass().toString()) {
            case "class java.lang.String":
                ipa.put("all", pronunciation);
                break;
            case "class org.json.JSONObject":
                Iterator<String> keys = ((JSONObject) pronunciation).keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = removed_s ? ((JSONObject) pronunciation).getString(key) + "z"
                            : ((JSONObject) pronunciation).getString(key);
                    ipa.put(key, value);
                }
                break;
            default:
                // unexpected
                break;
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

}
