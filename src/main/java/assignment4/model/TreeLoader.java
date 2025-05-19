package assignment4.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class TreeLoader {

    /**
     * Loads a tree or graph structure from three data files.
     * @param partsFile      File containing node metadata (conceptId, representationId, name).
     * @param elementsFile   File mapping conceptIds to fileIds.
     * @param relationsFile  File describing parent-child relationships between nodes.
     * @return The root ANode of the constructed tree.
     */
    public static ANode load(String partsFile, String elementsFile, String relationsFile) throws IOException {
        // Maps to hold intermediate data while building the graph
        Map<String, String> representationIds = new HashMap<>();
        Map<String, String> names = new HashMap<>();
        Map<String, List<String>> fileIdsMap = new HashMap<>();
        Map<String, List<String>> childrenMap = new HashMap<>();
        Map<String, String> parentMap = new HashMap<>();

        // Step 1: Load parts metadata (conceptId → name, representationId)
        List<String> partsLines = readLines(partsFile);
        for (String line : partsLines.subList(1, partsLines.size())) {  // skip header
            String[] tokens = line.trim().split("\\t");
            if (tokens.length >= 3) {
                representationIds.put(tokens[0], tokens[1]); // conceptId → representationId
                names.put(tokens[0], tokens[2]);             // conceptId → name
            }
        }

        // Step 2: Load conceptId-to-fileId mappings
        List<String> elementsLines = readLines(elementsFile);
        for (String line : elementsLines.subList(1, elementsLines.size())) {
            String[] tokens = line.trim().split("\\t");
            if (tokens.length >= 3) {
                fileIdsMap.computeIfAbsent(tokens[0], k -> new ArrayList<>()).add(tokens[2]);
                // tokens[0] = conceptId, tokens[2] = fileId
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
                parentMap.put(childId, parentId); // Track who the parent of each node is
            }
        }

        // Step 4: Create all ANode instances without children
        Map<String, ANode> nodes = new HashMap<>();
        for (String conceptId : names.keySet()) {
            nodes.put(conceptId, new ANode(
                    conceptId,
                    representationIds.getOrDefault(conceptId, ""),
                    names.get(conceptId),
                    new ArrayList<>(), // empty children list to be filled later
                    fileIdsMap.getOrDefault(conceptId, new ArrayList<>())
            ));
        }

        // Step 5: Populate children by referencing other ANode objects
        for (String parentId : childrenMap.keySet()) {
            ANode parent = nodes.get(parentId);
            if (parent != null) {
                List<ANode> children = (List<ANode>) parent.children(); // record is immutable but this returns mutable list
                for (String childId : childrenMap.get(parentId)) {
                    ANode child = nodes.get(childId);
                    if (child != null) {
                        children.add(child);
                    }
                }
            }
        }

        // Step 6: Find the root node (a node that has no parent)
        String rootId = names.keySet().stream()
                .filter(id -> !parentMap.containsKey(id)) // has no parent
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No root node found"));

        return nodes.get(rootId);
    }

    /**
     * Reads all lines from a resource file in the classpath.
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
