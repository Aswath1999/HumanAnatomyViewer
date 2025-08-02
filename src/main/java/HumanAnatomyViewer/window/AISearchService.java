package HumanAnatomyViewer.window;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * AISearchService handles communication with an external AI API
 * to convert natural-language queries into:
 *   - Java regex expressions for anatomy term matching
 *   - color mappings for anatomical structures
 *
 * The service uses OpenAI-compatible endpoints and formats messages
 * as system/user roles for the AI to process.
 */
public class AISearchService {

    // === Configuration ===

    // The endpoint of the AI API (assumed to be OpenAI-compatible)
    private static final String API_URL = "http://134.2.9.180/v1/chat/completions";

    // API key should be set as an environment variable for security
    private static final String API_KEY = System.getenv("OPENAPI_API_KEY");

    // Jackson object mapper for building JSON requests and reading responses
    private static final ObjectMapper mapper = new ObjectMapper();

    // === 1. Regex pattern request ===

    /**
     * Sends a natural language query along with a list of anatomical terms
     * and receives a regex pattern that matches relevant terms.
     *
     * @param query     The user's natural language input
     * @param termList  List of all anatomical leaf node terms
     * @return          Java regex string that matches relevant terms
     * @throws IOException if the request fails
     */
    public static String getRegexFromQuery(String query, List<String> termList) throws IOException {
        System.out.println("🔍 [AI SEARCH] Starting regex query...");
        System.out.println("🔹 User query: " + query);
        System.out.println("🔹 Number of terms: " + termList.size());

        // Construct AI message sequence
        ArrayNode messages = mapper.createArrayNode();

        String prompt = "You are an expert in anatomy and Java regular expressions.\n" +
                "You have access to the following list of available anatomical terms:\n" +
                String.join(", ", termList) + "\n" +
                "When the user asks for a subset (e.g., 'veins in the brain' or 'bones in the leg'), " +
                "respond with a single Java regex string using the OR operator (|) that matches all relevant terms.\n" +
                "Only return the regex — no explanation, no list, no markdown.";

        // Add system role (instructions) and user role (actual query)
        messages.add(object("system", prompt));
        messages.add(object("user", query));

        // Prepare the API request body
        ObjectNode body = mapper.createObjectNode();
        body.put("model", "gpt-4.1");
        body.put("temperature", 0); // deterministic output
        body.set("messages", messages);

        // Open connection and send request
        HttpURLConnection conn = openConnection();
        mapper.writeValue(conn.getOutputStream(), body); // write JSON body to output stream

        try (InputStream in = conn.getInputStream()) {
            // Parse the JSON response and extract the content field
            var response = mapper.readTree(in);
            String result = response.get("choices").get(0).get("message").get("content").asText().trim();
            System.out.println("🎯 [AI SEARCH] Regex received: " + result);
            return result;
        } catch (IOException e) {
            // Print error if response failed
            printError(conn, e);
            throw e;
        }
    }

    // === 2. Color mapping request ===

    /**
     * Sends a natural language color query and receives a mapping of
     * anatomical terms to HEX color codes (as a JSON object).
     *
     * @param query     The user's input (e.g., "Color the heart red and veins blue")
     * @param termList  List of anatomical labels
     * @return          Map of term -> HEX color code (e.g., {"heart": "#FF0000"})
     * @throws IOException if the request fails
     */
    public static Map<String, String> getColorMapFromQuery(String query, List<String> termList) throws IOException {
        System.out.println("🎨 [AI COLOR] Starting color suggestion query...");
        System.out.println("🔹 User query: " + query);
        System.out.println("🔹 Number of terms: " + termList.size());

        // Create prompt and message array
        ArrayNode messages = mapper.createArrayNode();

        String prompt = "You are an expert in anatomy and color design.\n" +
                "Here is a list of available anatomical terms:\n" +
                String.join(", ", termList) + "\n\n" +
                "If the user asks to color,colored,  paint, or fill certain parts (e.g., 'color the brain and nerves'), " +
                "respond with a JSON object mapping each matched term to a HEX color code like '#FF0000'. " +
                "Only include terms from the provided list. Respond ONLY with the JSON object.";

        // Add prompt and user query
        messages.add(object("system", prompt));
        messages.add(object("user", query));

        // Create request body
        ObjectNode body = mapper.createObjectNode();
        body.put("model", "gpt-4.1");
        body.put("temperature", 0); // no randomness
        body.set("messages", messages);

        // Send request
        HttpURLConnection conn = openConnection();
        mapper.writeValue(conn.getOutputStream(), body);

        try (InputStream in = conn.getInputStream()) {
            var response = mapper.readTree(in);
            String content = response.get("choices").get(0).get("message").get("content").asText().trim();

            System.out.println("🎯 [AI COLOR] Raw content:\n" + content);

            // Clean up response if it's wrapped in ```json or ```
            if (content.startsWith("```")) {
                content = content.replaceAll("(?s)```(?:json)?\\s*", "").replaceAll("```\\s*$", "").trim();
            }

            // Parse cleaned JSON content into a Map<String, String>
            return mapper.readValue(content, Map.class);
        } catch (IOException e) {
            printError(conn, e);
            throw e;
        }
    }

    // === Helper: Open HTTP POST connection ===

    /**
     * Opens and configures an HTTP POST connection to the API URL
     *
     * @return configured HttpURLConnection
     * @throws IOException if connection fails
     */
    private static HttpURLConnection openConnection() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true); // enable POST
        conn.setRequestMethod("POST");
        return conn;
    }

    // === Helper: Create message node ===

    /**
     * Constructs a message node with the specified role and content.
     *
     * @param role    "system" or "user"
     * @param content message text
     * @return ObjectNode representing one message
     */
    private static ObjectNode object(String role, String content) {
        ObjectNode node = mapper.createObjectNode();
        node.put("role", role);
        node.put("content", content);
        return node;
    }

    // === Helper: Print and parse API error messages ===

    /**
     * Logs error messages from the API response if the request fails.
     *
     * @param conn the HttpURLConnection that failed
     * @param e    the exception thrown
     */
    private static void printError(HttpURLConnection conn, IOException e) {
        System.err.println("❌ [AI SERVICE] IOException: " + e.getMessage());
        try (InputStream err = conn.getErrorStream()) {
            if (err != null) {
                var errorJson = mapper.readTree(err);
                System.err.println("🚫 [AI SERVICE] Error Response from API:");
                System.err.println(errorJson.toPrettyString());
            }
        } catch (IOException inner) {
            System.err.println("⚠️ [AI SERVICE] Failed to read error response: " + inner.getMessage());
        }
    }
}