package HumanAnatomyViewer.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class TreeLoader {

    /**
     * Loads a tree or graph structure from three data files.
     *
     *
     * @param partsFile /isA file     File containing node metadata (conceptId, representationId, name).
     * @param elementsFile/isAelements   File mapping conceptIds to fileIds.
     * @param relationsFile  File describing parent-child relationships between nodes.
     * @return The root ANode of the constructed tree.
     */
    public static ANode load(String partsFile, String elementsFile, String relationsFile) throws IOException {
        Map<String, String> representationIds = new HashMap<>();
        Map<String, String> names = new HashMap<>();
        Map<String, List<String>> fileIdsMap = new HashMap<>();
        Map<String, List<String>> childrenMap = new HashMap<>();
        Map<String, String> parentMap = new HashMap<>();

        // Step 1: Load parts/isA metadata
        List<String> partsLines = readLines(partsFile);
        for (String line : partsLines.subList(1, partsLines.size())) {
            String[] tokens = line.trim().split("\\t");
            if (tokens.length >= 3) {
                representationIds.put(tokens[0], tokens[1]);
                names.put(tokens[0], tokens[2]);
            }
        }

        // Step 2: Load file ID mappings
        List<String> elementsLines = readLines(elementsFile);
        for (String line : elementsLines.subList(1, elementsLines.size())) {
            String[] tokens = line.trim().split("\\t");
            if (tokens.length >= 3) {
                fileIdsMap.computeIfAbsent(tokens[0], k -> new ArrayList<>()).add(tokens[2]);
            }
        }

        // Step 3: Load parent-child relationships
        List<String> relationsLines = readLines(relationsFile);
        for (String line : relationsLines.subList(1, relationsLines.size())) {
            String[] tokens = line.trim().split("\\t");
            if (tokens.length >= 4) {
                String parentId = tokens[0];
                String childId = tokens[2];
                childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(childId);
                parentMap.put(childId, parentId);
            }
        }

        // Step 4: Find root node (has no parent)
        String rootId = names.keySet().stream()
                .filter(id -> !parentMap.containsKey(id))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No root node found"));

        // Step 5: Recursively build tree
        return buildTree(rootId, representationIds, names, fileIdsMap, childrenMap);
    }

    /**
     * Recursively builds the ANode tree.
     */
    private static ANode buildTree(
            String conceptId,
            Map<String, String> representationIds,
            Map<String, String> names,
            Map<String, List<String>> fileIdsMap,
            Map<String, List<String>> childrenMap
    ) {
        List<ANode> children = new ArrayList<>();
        for (String childId : childrenMap.getOrDefault(conceptId, List.of())) {
            children.add(buildTree(childId, representationIds, names, fileIdsMap, childrenMap));
        }

        return new ANode(
                conceptId,
                representationIds.getOrDefault(conceptId, ""),
                names.getOrDefault(conceptId, ""),
                children,
                fileIdsMap.getOrDefault(conceptId, List.of())
        );
    }

    /**
     * Reads all lines from a resource file in the classpath.
     *
     * @param resourceName File name in resources directory.
     * @return List of lines in the file.
     */
    private static List<String> readLines(String resourceName) throws IOException {
        InputStream in = TreeLoader.class.getResourceAsStream("/" + resourceName);
        if (in == null) {
            throw new IOException("Resource not found: " + resourceName);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            return reader.lines().toList();
        }
    }
}
