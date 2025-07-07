package HumanAnatomyViewer.window;

import java.util.List;

public class AIRegexTask extends javafx.concurrent.Task<String> {
    private final String query;
    private final List<String> leafLabels;

    public AIRegexTask(String query, List<String> leafLabels) {
        this.query = query;
        this.leafLabels = leafLabels;
    }

    @Override
    protected String call() throws Exception {
        return AISearchService.getRegexFromQuery(query, leafLabels);
    }
}