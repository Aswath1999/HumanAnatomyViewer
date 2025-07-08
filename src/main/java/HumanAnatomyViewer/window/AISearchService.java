package HumanAnatomyViewer.window;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class AISearchService {

    private static final String API_URL = " http://134.2.9.180/v1/chat/completions";
    // ❗ In production, use: System.getenv("OPENAI_API_KEY")
    private static final String API_KEY = System.getenv("OPENAPI_API_KEY");

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String getRegexFromQuery(String query, List<String> termList) throws IOException {
        System.out.println("🔍 [AI SEARCH] Starting request to OpenAI..."+ System.getenv("OPENAPI_API_KEY"));
        System.out.println("🔹 User query: " + query);
        System.out.println("🔹 Number of terms: " + termList.size());

        ArrayNode messages = mapper.createArrayNode();

        // System prompt
        String prompt = "You are an expert in anatomy and Java regular expressions.\n" +
                "When the user asks for a category of structures (e.g., 'all bones in the leg' or 'veins in the heart'), " +
                "generate a Java regular expression that matches terms related to that query, including known synonyms and variants.\n" +
                "Only output a valid Java regex pattern. No explanation.";

        messages.add(object("system", prompt));
        messages.add(object("user", query));

        ObjectNode body = mapper.createObjectNode();
        body.put("model", "gpt-4.1");
        body.put("temperature", 0);
        body.set("messages", messages);

        // Debug: Show JSON body
        System.out.println("📤 [DEBUG] JSON request body:");
        System.out.println(body.toPrettyString());

        HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");

        // Send request
        mapper.writeValue(conn.getOutputStream(), body);
        System.out.println("✅ [AI SEARCH] Request sent. Awaiting response...");

        try (InputStream in = conn.getInputStream()) {
            var response = mapper.readTree(in);
            String result = response.get("choices").get(0).get("message").get("content").asText().trim();
            System.out.println("🎯 [AI SEARCH] Received response: " + result);
            return result;
        } catch (IOException e) {
            System.err.println("❌ [AI SEARCH] IOException: " + e.getMessage());

            // Try reading error stream from OpenAI
            try (InputStream err = conn.getErrorStream()) {
                if (err != null) {
                    var errorJson = mapper.readTree(err);
                    System.err.println("🚫 [AI SEARCH] Error Response from API:");
                    System.err.println(errorJson.toPrettyString());
                }
            } catch (IOException inner) {
                System.err.println("⚠️ [AI SEARCH] Failed to read error response: " + inner.getMessage());
            }

            throw e;
        }
    }

    private static ObjectNode object(String role, String content) {
        ObjectNode node = mapper.createObjectNode();
        node.put("role", role);
        node.put("content", content);
        return node;
    }
}