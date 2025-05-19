package assignment4.model;

// Importing the Collection interface from the Java standard library
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
/**
 * ANode is a record representing a node in a conceptual or hierarchical structure.
 *
 * Fields:
 * - conceptId: A unique identifier for the concept this node represents.
 * - representationId: An ID indicating how this node might be visualized or represented.
 * - name: A human-readable name for the node.
 * - children: A collection of child ANode objects, representing the tree or graph structure.
 * - fileIds: A collection of file identifiers that may be associated with this node.
 */
public record ANode(
        String conceptId,
        String representationId,
        String name,
        Collection<ANode> children,
        Collection<String> fileIds
) {
    @Override
    public String toString() {
        return name + " (" + conceptId + ")";
    }

    public String label() {
        return name;
    }

    // Optional safe helper
    public List<ANode> childList() {
        return new ArrayList<>(children);  // returns a copy
    }
}