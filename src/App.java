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

public class App {

    static HttpClient client = HttpClient.newBuilder().proxy(ProxySelector.getDefault()).build();

    /**
     * 
     * @param uri
     * @return
     * @throws Exception
     */
    static HttpResponse<String> sendRequest(URI uri) throws Exception {
        HttpResponse<String> response;
        try {
            response = client.send(getRequest(uri), BodyHandlers.ofString());
            return response;
        } catch (Exception e) {
            System.err.println("Something went wrong: " + e.getMessage());
            throw e;
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
        return URI.create("https://wordsapiv1.p.rapidapi.com/words/" + word + "/" + _info); // need to account for
                                                                                            // spaces
    }

    /**
     * 
     * @param word
     * @return
     */
    static JSONArray getSynonyms(String word) {
        JSONArray synonyms = new JSONArray();
        URI uri = getUri(word, "synonyms");
        try {
            HttpResponse<String> response = sendRequest(uri);
            JSONObject jo = new JSONObject(response.body());
            synonyms = (JSONArray) jo.get("synonyms");
            System.out.println("Synonyms of \"" + word + "\":" + synonyms);

        } catch (Exception e) {
            System.err.println(e.getMessage());
            // just trying to make this work
        }
        return synonyms;
    }

    /***
     * 
     * @param word
     * @return
     */
    static JSONArray getRhymes(String word) {
        JSONArray rhymes = new JSONArray();
        URI uri = getUri(word, "rhymes");
        try {
            HttpResponse<String> response = sendRequest(uri);
            JSONObject jo = new JSONObject(response.body());
            rhymes = (JSONArray) ((JSONObject) jo.get("rhymes")).get("all"); // might want to filter more than "all"
                                                                             // someday
            System.out.println("Rhymes of \"" + word + "\":" + rhymes);

        } catch (Exception e) {
            System.err.println(e.getMessage());
            // just trying to make this work
        }
        return rhymes;
    }

    public static void main(String[] args) {

        String flag = args.length < 1 ? "default" : args[0];
        String base;
        String secondary;
        JSONArray j_synonyms;
        JSONArray j_rhymes;

        switch (flag) {
            case "-demo":
                if (args.length != 3) {
                    System.err.println(
                            "Expecting exactly three arguments of form \"-demo <base word> <word to rhyme with>\"");
                    System.exit(-1);
                }
                base = args[1];
                secondary = args[2];
                j_synonyms = getSynonyms(base);
                j_rhymes = getRhymes(secondary);

                List<String> synonyms = (List<String>) (List<?>) j_synonyms.toList();
                List<String> rhymes = (List<String>) (List<?>) j_rhymes.toList();
                List<String> rhyming_synonyms = synonyms.stream().filter(rhymes::contains).collect(Collectors.toList());

                int suggestion_count = rhyming_synonyms.size();
                if (suggestion_count == 0) {
                    System.out.println("No suggestions found.");

                } else {
                    System.out.println(String.format("Found %d suggestion%s:", suggestion_count,
                            suggestion_count == 1 ? "" : "s"));
                    for (int i = 0; i < suggestion_count - 1; i++) {
                        System.out.print("\t" + rhyming_synonyms.get(i) + ", ");
                    }
                    System.out.println("\t" + rhyming_synonyms.get(suggestion_count - 1));
                }
                break;

            case "-rhymes":
                if (args.length != 2) {
                    System.err.println("Expecting exactly two arguments of form \"-rhyme <word to rhyme with>\"");
                    System.exit(-1);
                }
                base = args[1];
                getRhymes(base);
                break;

            case "-synonyms":
                if (args.length != 2) {
                    System.err
                            .println("Expecting exactly two arguments of form \"-synonym <word to get synonyms of>\"");
                    System.exit(-1);
                }
                base = args[1];
                getSynonyms(base);
                break;

            default:
                System.err.println("Expecting one of \"-demo\", \"-rhymes\" or \"-synonyms\" as flag.");
                break;
        }
    }
}
