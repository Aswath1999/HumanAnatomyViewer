package assignment4.model;

import java.io.*;
import java.util.*;

public class TreeLoader {
    // This method loads the tree from the given input streams.
    public static assignment4.model.ANode load(InputStream partsStream, InputStream elementsStream, InputStream relationsStream) throws IOException {
        // hashmap to store nodes
        Map<String, assignment4.model.ANode> nodes = new HashMap<>();
        Set<String> childIds = new HashSet<>();

        // load partsFile
        try (BufferedReader br = new BufferedReader(new InputStreamReader(partsStream))) {
            // skip header
            br.readLine();
            String line;
            // read each line
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("\\t");
                // check if line has enough tokens
                String conceptId = tokens[0];
                String representationId = tokens[1];
                String name = tokens[2];

                // create ANode object for all nodes
                assignment4.model.ANode node = new assignment4.model.ANode(conceptId, representationId, name, new ArrayList<>(), new HashSet<>());
                // add node to hashmap
                nodes.put(conceptId, node);
                // add node to hashmap with representationId
                nodes.put(representationId, node);
                // add node to hashmap with name
                nodes.put(name, node);
            }
        };


        // load elementsFile
        try (BufferedReader br = new BufferedReader(new InputStreamReader(elementsStream))) {
            // skip header
            br.readLine();
            String line;
            // read each line
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("\\t");
                // check if line has enough tokens
                String conceptId = tokens[0];
                String fileId = tokens[2];

                assignment4.model.ANode node = nodes.get(conceptId);
                // check if node is not null
                if (node != null) {
                    // add fileId to node
                    node.fileIds().add(fileId);
                }
            }
        }


        // load relationsFile
        try (BufferedReader br = new BufferedReader(new InputStreamReader(relationsStream))) {
            // skip header
            br.readLine();
            String line;
            // read each line
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("\\t");
                String parentId = tokens[0];
                String childId = tokens[2];

                // create relation between parent and child
                assignment4.model.ANode parentNode = nodes.get(parentId);
                assignment4.model.ANode childNode = nodes.get(childId);

                // check if parent and child nodes are not null
                if (parentNode != null && childNode != null) {
                    parentNode.children().add(childNode);
                    childIds.add(childId);
                }
            }
        }

        // find root node
        assignment4.model.ANode root = null;
        for (ANode node : nodes.values()) {
            // check if node is not a child of any other node
            if (!childIds.contains(node.conceptId())) {
                root = node;
                break;
            }
        }

        return root;
    }
}


