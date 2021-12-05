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

import config.Configuration;

public class App {


     /**
      * Try looking for overlap between different categorisations of words 
      */
    static HttpClient client = HttpClient.newBuilder().proxy(ProxySelector.getDefault()).build();

    /**
     * 
     * @param uri
     * @return
     * @throws Exception
     */
    static JSONObject sendRequest(URI uri) throws Exception {
        JSONObject response;
        try {
            HttpResponse<String> response_string = client.send(getRequest(uri), BodyHandlers.ofString());
            response = new JSONObject(response_string.body());
            // System.out.println(response);
            return response;
        } catch (Exception e) {
            Configuration.LOG.writeTempLog("Something went wrong: " + e.getMessage());
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
        word = word.replace(" ", "%20");
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
        String quality = "synonyms";
        URI uri = getUri(word, quality);
        try {
            JSONObject jo = sendRequest(uri);

            if (((JSONArray) jo.get(quality)).isEmpty() && word.endsWith("s")) {
                word = word.substring(0, word.length() - 1);
                Configuration.LOG.writeTempLog("\"" + word + "s\" may be a plural. Attempting search for " + quality + " of \""
                        + word + "\".");
                return getSynonyms(word);
            } else if (((JSONArray) jo.get(quality)).isEmpty()) {
                Configuration.LOG.writeTempLog("No " + quality + " found for \"" + word + "\".");
            } else {
                synonyms = (JSONArray) jo.get(quality);

                System.out.println("Synonyms of \"" + word + "\":" + synonyms);
            }

        } catch (Exception e) {
            Configuration.LOG.writeTempLog(e.getMessage());
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
                Configuration.LOG.writeTempLog("\"" + word + "s\" may be a plural. Attempting search for " + quality + " of \""
                        + word + "\".");
                return getSynonyms(word);
            } else if (((JSONArray) jo.get(quality)).isEmpty()) {
                Configuration.LOG.writeTempLog("No " + quality + " found for \"" + word + "\".");
            } else {
                types = (JSONArray) jo.get(quality);

                System.out.println("Types of \"" + word + "\":" + types);
            }

        } catch (Exception e) {
            Configuration.LOG.writeTempLog(e.getMessage());
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
                Configuration.LOG.writeTempLog("\"" + word + "s\" may be a plural. Attempting search for " + quality + " of \""
                        + word + "\".");
                return getCommonType(word);
            } else if (((JSONArray) jo.get(quality)).isEmpty()) {
                Configuration.LOG.writeTempLog("No " + quality + " found for \"" + word + "\".");
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
            Configuration.LOG.writeTempLog(e.getMessage());
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
                Configuration.LOG.writeTempLog("\"" + word + "s\" may be a plural. Attempting search for " + quality + " of \""
                        + word + "\".");
                return getRhymes(word);
            } else if (((JSONObject) jo.get(quality)).isEmpty()) {
                Configuration.LOG.writeTempLog("No " + quality + " found for \"" + word + "\".");
            } else {
                rhymes = (JSONArray) ((JSONObject) jo.get(quality)).get("all"); // might want to filter more than "all"
                                                                                // someday
                System.out.println("Rhymes of \"" + word + "\":" + rhymes);
            }

        } catch (

        Exception e) {
            Configuration.LOG.writeTempLog(e.getMessage());
            // just trying to make this work
        }
        return rhymes;
    }

    static JSONArray removeDuplicates(JSONArray array) {
        List<Object> list = array.toList();
        list = list.stream().distinct().collect(Collectors.toList()); 
        return new JSONArray(list); 
    }

    public static void main(String[] args) {

        String flag = args.length < 1 ? "default" : args[0];
        String primary_word;
        String secondary_word;
        List<String> primary_list;
        List<String> secondary_list;
        List<String> overlap_list;
        int overlap_count;
        JSONArray primary_array;
        JSONArray secondary_array;

        switch (flag) {
            case "-demo-1":
                if (args.length != 3) {
                    Configuration.LOG.writeTempLog("Expecting exactly three arguments of form \"" + flag
                            + " <base word> <word to rhyme with>\"");
                    System.exit(-1);
                }
                primary_word = args[1];
                secondary_word = args[2];
                primary_array = getSynonyms(primary_word);
                secondary_array = getRhymes(secondary_word);

                primary_list = (List<String>) (List<?>) primary_array.toList();
                secondary_list = (List<String>) (List<?>) secondary_array.toList();
                overlap_list = primary_list.stream().filter(secondary_list::contains).distinct()
                        .collect(Collectors.toList());

                overlap_count = overlap_list.size();
                if (overlap_count == 0) {
                    System.out.println("No suggestions found.");

                } else {
                    System.out.println(
                            String.format("Found %d suggestion%s:", overlap_count, overlap_count == 1 ? "" : "s"));
                    for (int i = 0; i < overlap_count - 1; i++) {
                        System.out.print("\t" + overlap_list.get(i) + ", ");
                    }
                    System.out.println("\t" + overlap_list.get(overlap_count - 1));
                }
                break;

            case "-demo-2":
                if (args.length != 3) {
                    Configuration.LOG.writeTempLog("Expecting exactly three arguments of form \"" + flag
                            + " <base word> <word to rhyme with>\"");
                    System.exit(-1);
                }
                primary_word = args[1];
                secondary_word = args[2];
                primary_array = getCommonType(primary_word);
                secondary_array = getRhymes(secondary_word);

                primary_list = (List<String>) (List<?>) primary_array.toList();
                secondary_list = (List<String>) (List<?>) secondary_array.toList();
                overlap_list = primary_list.stream().filter(secondary_list::contains).distinct()
                        .collect(Collectors.toList());

                overlap_count = overlap_list.size();
                if (overlap_count == 0) {
                    System.out.println("No suggestions found.");

                } else {
                    System.out.println(
                            String.format("Found %d suggestion%s:", overlap_count, overlap_count == 1 ? "" : "s"));
                    for (int i = 0; i < overlap_count - 1; i++) {
                        System.out.print("\t" + overlap_list.get(i) + ", ");
                    }
                    System.out.println("\t" + overlap_list.get(overlap_count - 1));
                }
                break;

            case "-rhymes":
                if (args.length != 2) {
                    Configuration.LOG.writeTempLog("Expecting exactly two arguments of form \"" + flag + " <word to rhyme with>\"");
                    System.exit(-1);
                }
                primary_word = args[1];
                getRhymes(primary_word);
                break;

            case "-synonyms":
                if (args.length != 2) {
                    Configuration.LOG.writeTempLog(
                            "Expecting exactly two arguments of form \"" + flag + " <word to get synonyms of>\"");
                    System.exit(-1);
                }
                primary_word = args[1];
                getSynonyms(primary_word);
                break;

            case "-common-type":
                if (args.length != 2) {
                    Configuration.LOG.writeTempLog(
                            "Expecting exactly two arguments of form \"" + flag + " <word to get synonyms of>\"");
                    System.exit(-1);
                }
                primary_word = args[1];
                getCommonType(primary_word);
                break;

            default:
                Configuration.LOG.writeTempLog(
                        "Expecting one of \"-demo-1\", \"-demo-2\", \"-rhymes\", \"-synonyms\", \"-common-type\" as flag.");
                break;
        }
    }
}
