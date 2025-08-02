package HumanAnatomyViewer.window;

import java.util.List;
import javafx.concurrent.Task; // Importing JavaFX Task for running background operations

/**
 * AIRegexTask is a background task that sends a user query and a list of anatomical labels
 * to an AI search service, which returns a regex pattern matching relevant labels.
 *
 * This task extends JavaFX's Task class, which allows it to run asynchronously
 * without freezing the UI thread. It is typically used for time-consuming operations.
 */
public class AIRegexTask extends Task<String> {

    // The natural language query entered by the user
    private final String query;

    // A list of all available labels from the anatomical structure tree (typically leaf nodes)
    private final List<String> leafLabels;

    /**
     * Constructor initializes the task with the query and list of anatomical leaf labels.
     *
     * @param query       the user's natural language search input
     * @param leafLabels  the list of all leaf node labels from the anatomical model
     */
    public AIRegexTask(String query, List<String> leafLabels) {
        this.query = query;
        this.leafLabels = leafLabels;
    }

    /**
     * This method runs in a background thread when the task is executed.
     * It sends the query and label list to the AI service and retrieves a regex string.
     *
     * @return a regex pattern as a String, generated from the AI based on the user query
     * @throws Exception if the AI service call fails
     */
    @Override
    protected String call() throws Exception {
        // Delegates the regex generation to an external AI service
        return AISearchService.getRegexFromQuery(query, leafLabels);
    }
}