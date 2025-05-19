package assignment4.model;

import java.util.function.Consumer;

public class NewickExporter {

    public static String toNewick(ANode root) {
        StringBuilder sb = new StringBuilder();

        // Use inOrder traversal to construct the Newick string
        inOrder(root,
                v -> {  // preVisitor
                    if (!v.children().isEmpty()) sb.append("(");
                },
                v -> {  // betweenVisitor
                    sb.append(",");
                },
                v -> {  // postVisitor
                    if (v.children().isEmpty()) {
                        sb.append(sanitize(v.label()));
                    } else {
                        sb.append(")").append(sanitize(v.label()));
                    }
                }
        );

        sb.append(";");
        return sb.toString();
    }

    private static String sanitize(String label) {
        return label == null ? "" : label.replaceAll("[\\s():;,]", "_");
    }

    public static void inOrder(
            ANode v,
            Consumer<ANode> preVisitor,
            Consumer<ANode> betweenVisitor,
            Consumer<ANode> postVisitor
    ) {
        preVisitor.accept(v);
        var remaining = v.children().size();
        for (var w : v.children()) {
            inOrder(w, preVisitor, betweenVisitor, postVisitor);
            if (--remaining > 0) {
                betweenVisitor.accept(w);
            }
        }
        postVisitor.accept(v);
    }
}
