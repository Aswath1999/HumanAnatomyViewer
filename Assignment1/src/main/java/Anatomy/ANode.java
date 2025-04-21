package anatomy;

import java.util.*;

public record ANode(String id, String name, List<ANode> children) {

    public ANode(String id, String name) {
        this(id, name, new ArrayList<>());
    }

    public void addChild(ANode child) {
        children.add(child);
    }

    public static ANode createTree(List<Relation> relations) {
        Map<String, ANode> nodeMap = new HashMap<>();
        Set<String> childIds = new HashSet<>();

        for (Relation r : relations) {
            ANode parent = nodeMap.computeIfAbsent(r.parentId(), id -> new ANode(r.parentId(), r.parentName()));
            ANode child = nodeMap.computeIfAbsent(r.childId(), id -> new ANode(r.childId(), r.childName()));
            parent.addChild(child);
            childIds.add(r.childId());
        }

        for (String id : nodeMap.keySet()) {
            if (!childIds.contains(id)) {
                return nodeMap.get(id); // root found
            }
        }
        return null;
    }

    public void printAllPaths(List<String> path) {
        path.add(this.name());
        if (children.isEmpty()) {
            System.out.println(String.join(" -> ", path));
        } else {
            for (ANode child : children) {
                child.printAllPaths(path);
            }
        }
        path.remove(path.size() - 1);
    }

    public void printFilteredPaths(List<String> path, List<String> filters) {
        path.add(this.name());
        if (children.isEmpty()) {
            String fullPath = String.join(" -> ", path).toLowerCase();
            boolean matches = filters.stream().allMatch(fullPath::contains);
            if (matches) {
                System.out.println(String.join(" -> ", path));
            }
        } else {
            for (ANode child : children) {
                child.printFilteredPaths(path, filters);
            }
        }
        path.remove(path.size() - 1);
    }
}
