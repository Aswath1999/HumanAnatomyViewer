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

public class AISearchService {

    private static final String API_URL = "http://134.2.9.180/v1/chat/completions";
    private static final String API_KEY = System.getenv("OPENAPI_API_KEY");

    private static final ObjectMapper mapper = new ObjectMapper();

    // === 1. Regex pattern request ===
    public static String getRegexFromQuery(String query, List<String> termList) throws IOException {
        System.out.println("🔍 [AI SEARCH] Starting regex query...");
        System.out.println("🔹 User query: " + query);
        System.out.println("🔹 Number of terms: " + termList.size());

        ArrayNode messages = mapper.createArrayNode();

        String prompt = "You are an expert in anatomy and Java regular expressions.\n" +
                "You have access to the following list of available anatomical terms:\n" +
                String.join(", ", termList) + "\n" +
                "When the user asks for a subset (e.g., 'veins in the brain' or 'bones in the leg'), " +
                "respond with a single Java regex string using the OR operator (|) that matches all relevant terms.\n" +
                "Only return the regex — no explanation, no list, no markdown.";

        messages.add(object("system", prompt));
        messages.add(object("user", query));

        ObjectNode body = mapper.createObjectNode();
        body.put("model", "gpt-4.1");
        body.put("temperature", 0);
        body.set("messages", messages);

        HttpURLConnection conn = openConnection();
        mapper.writeValue(conn.getOutputStream(), body);

        try (InputStream in = conn.getInputStream()) {
            var response = mapper.readTree(in);
            String result = response.get("choices").get(0).get("message").get("content").asText().trim();
            System.out.println("🎯 [AI SEARCH] Regex received: " + result);
            return result;
        } catch (IOException e) {
            printError(conn, e);
            throw e;
        }
    }

    // === 2. Color mapping request ===
    public static Map<String, String> getColorMapFromQuery(String query, List<String> termList) throws IOException {
        System.out.println("🎨 [AI COLOR] Starting color suggestion query...");
        System.out.println("🔹 User query: " + query);
        System.out.println("🔹 Number of terms: " + termList.size());

        ArrayNode messages = mapper.createArrayNode();

        String prompt = "You are an expert in anatomy and color design.\n" +
                "Here is a list of available anatomical terms:\n" +
                String.join(", ", termList) + "\n\n" +
                "If the user asks to color,colored,  paint, or fill certain parts (e.g., 'color the brain and nerves'), " +
                "respond with a JSON object mapping each matched term to a HEX color code like '#FF0000'. " +
                "Only include terms from the provided list. Respond ONLY with the JSON object.";

        messages.add(object("system", prompt));
        messages.add(object("user", query));

        ObjectNode body = mapper.createObjectNode();
        body.put("model", "gpt-4.1");
        body.put("temperature", 0);
        body.set("messages", messages);

        HttpURLConnection conn = openConnection();
        mapper.writeValue(conn.getOutputStream(), body);

        try (InputStream in = conn.getInputStream()) {
            var response = mapper.readTree(in);
            String content = response.get("choices").get(0).get("message").get("content").asText().trim();
            System.out.println("🎯 [AI COLOR] Raw content:\n" + content);


            if (content.startsWith("```")) {
                content = content.replaceAll("(?s)```(?:json)?\\s*", "").replaceAll("```\\s*$", "").trim();
            }

            return mapper.readValue(content, Map.class);
        } catch (IOException e) {
            printError(conn, e);
            throw e;
        }
    }

    // === Helper for request connection ===
    private static HttpURLConnection openConnection() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        return conn;
    }

    // === Helper for prompt formatting ===
    private static ObjectNode object(String role, String content) {
        ObjectNode node = mapper.createObjectNode();
        node.put("role", role);
        node.put("content", content);
        return node;
    }

    // === Helper for error printing ===
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