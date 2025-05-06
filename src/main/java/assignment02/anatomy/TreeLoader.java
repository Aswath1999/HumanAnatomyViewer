package assignment02.anatomy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class TreeLoader {

    public static ANode load(String partsFile, String elementsFile, String relationsFile) throws IOException {
        Map<String, String> representationIds = new HashMap<>();
        Map<String, String> names = new HashMap<>();
        Map<String, List<String>> fileIdsMap = new HashMap<>();
        Map<String, List<String>> childrenMap = new HashMap<>();
        Map<String, String> parentMap = new HashMap<>();

        // Step 1: Load partof_parts_list_e.txt
        List<String> partsLines = Files.readAllLines(Path.of(partsFile));
        for (String line : partsLines.subList(1, partsLines.size())) {
            String[] tokens = line.trim().split("\\t");
            if (tokens.length >= 3) {
                representationIds.put(tokens[0], tokens[1]);
                names.put(tokens[0], tokens[2]);
            }
        }

        // Step 2: Load partof_element_parts.txt
        List<String> elementsLines = Files.readAllLines(Path.of(elementsFile));
        for (String line : elementsLines.subList(1, elementsLines.size())) {
            String[] tokens = line.trim().split("\\t");
            if (tokens.length >= 3) {
                fileIdsMap.computeIfAbsent(tokens[0], k -> new ArrayList<>()).add(tokens[2]);
            }
        }

        // Step 3: Load partof_inclusion_relation_list.txt
        List<String> relationsLines = Files.readAllLines(Path.of(relationsFile));
        for (String line : relationsLines.subList(1, relationsLines.size())) {
            String[] tokens = line.trim().split("\\t");
            if (tokens.length >= 4) {
                childrenMap.computeIfAbsent(tokens[0], k -> new ArrayList<>()).add(tokens[2]);
                parentMap.put(tokens[2], tokens[0]);
            }
        }

        // Step 4: Create all ANode objects
        Map<String, ANode> nodes = new HashMap<>();
        for (String conceptId : names.keySet()) {
            nodes.put(conceptId, new ANode(
                    conceptId,
                    representationIds.getOrDefault(conceptId, ""),
                    names.get(conceptId),
                    new ArrayList<>(),
                    fileIdsMap.getOrDefault(conceptId, new ArrayList<>())
            ));
        }

        // Step 5: Link children
        for (String parentId : childrenMap.keySet()) {
            ANode parent = nodes.get(parentId);
            if (parent != null) {
                List<ANode> children = (List<ANode>) parent.children();
                for (String childId : childrenMap.get(parentId)) {
                    ANode child = nodes.get(childId);
                    if (child != null) {
                        children.add(child);
                    }
                }
            }
        }

        // Step 6: Find the root (node without a parent)
        String rootId = names.keySet().stream()
                .filter(id -> !parentMap.containsKey(id))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No root node found"));

        return nodes.get(rootId);
    }
}
