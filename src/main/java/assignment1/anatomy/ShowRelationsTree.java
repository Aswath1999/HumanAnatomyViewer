package assignment1.anatomy;

import java.util.*;

public class ShowRelationsTree {
    public static void main(String[] args) {
        String filePath = "partof_inclusion_relation_list.txt";

        List<Relation> relations = Relation.loadFromFile(filePath);
        System.out.println("Relations: " + relations.size());

        ANode root = ANode.createTree(relations);

        if (root == null) {
            System.out.println("No root found.");
            return;
        }

        System.out.println("Tree: " + countNodes(root));

        List<String> filters = Arrays.stream(args).map(String::toLowerCase).toList();

        if (filters.isEmpty()) {
            root.printAllPaths(new ArrayList<>());
        } else {
            root.printFilteredPaths(new ArrayList<>(), filters);
        }
    }

    private static int countNodes(ANode node) {
        int count = 1;
        for (ANode child : node.children()) {
            count += countNodes(child);
        }
        return count;
    }
}
