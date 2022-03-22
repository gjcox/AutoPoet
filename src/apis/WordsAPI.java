package apis;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import static config.Configuration.LOG;

/**
 * Handles interactions with WordsAPI
 */
public class WordsAPI {

    static HttpClient client = HttpClient.newBuilder().proxy(ProxySelector.getDefault()).build();
    static HashMap<String, JSONObject> cache = new HashMap<>();

    /**
     * Attempts to send a request to WordsAPI.
     * 
     * @param uri the request to send, which needs to include an API key.
     * @return the body of the response, in the JSON format used by WordsAPI.
     * @throws IOException          if an I/O error occurs during sending/receiving.
     * @throws InterruptedException if the operation is interrupted.
     */
    private static JSONObject sendRequest(URI uri) throws IOException, InterruptedException {
        JSONObject response;
        try {
            HttpRequest request = getRequest(uri);
            LOG.writeTempLog("sendRequest() sending request: " + request.toString());
            HttpResponse<String> response_string = client.send(request, BodyHandlers.ofString());
            response = new JSONObject(response_string.body());
            LOG.writeTempLog("sendRequest() received response: " + response);
            return response;
        } catch (IOException ioe) {
            LOG.writeTempLog("sendRequest() something went wrong: " + ioe.getMessage());
            throw ioe;
        } catch (InterruptedException ie) {
            LOG.writeTempLog("sendRequest() something went wrong: " + ie.getMessage());
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
    private static HttpRequest getRequest(URI uri) {
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
    private static URI getUri(String word, String _info) {
        word = word.replace(" ", "%20"); // remove spaces
        return URI.create("https://wordsapiv1.p.rapidapi.com/words/" + word + "/" + _info);
    }

    public static JSONObject getWord(String plaintext, boolean isPlural) {
        if (isPlural) {
            return getWord(plaintext.substring(0, plaintext.length() - 1));
        } else {
            return getWord(plaintext);
        }
    }

    /**
     * 
     * @param plaintext a plaintext word.
     * @return the JSONOject returned from WordsAPI, or {word:plaintext} if there
     *         was no result.
     */
    public static JSONObject getWord(String plaintext) {
        if (cache.containsKey(plaintext)) {
            return cache.get(plaintext);
        }

        JSONObject result = new JSONObject();
        result.put("word", plaintext);
        URI uri = getUri(plaintext, "");
        try {
            result = sendRequest(uri);

            if (result.has("success") && !result.getBoolean("success")) {
                cache.put(plaintext, result);
                return result;
            }

        } catch (IOException | JSONException | InterruptedException e) {
            LOG.writeTempLog(String.format("getWord(%s) something went wrong: %s", plaintext, e.getMessage()));
        }

        cache.put(plaintext, result);
        return result;
    }

}
