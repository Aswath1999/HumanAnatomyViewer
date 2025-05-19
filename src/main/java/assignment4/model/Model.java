package assignment4.model;

import java.io.IOException;

/**
 * The Model class serves as a data access layer in the application.
 * It loads and stores the root of a hierarchical "part-of" structure,
 * typically used in a tree or graph view (e.g. in a JavaFX UI).
 */
public class Model {

    // The root node of the graph/tree structure built from external files
    private final ANode partOfRoot;

    /**
     * Constructor initializes the model by loading data from resource files.
     *
     * It uses the TreeLoader class to parse and build a tree of ANode objects
     * from three TSV files:
     * - partof_parts_list_e.txt: contains concept IDs and names
     * - partof_element_parts.txt: maps concept IDs to file IDs
     * - partof_inclusion_relation_list.txt: defines parent-child relationships
     *
     * @throws IOException if there is an error reading any of the files
     */
    public Model() throws IOException {
        partOfRoot = TreeLoader.load(
                "partof_parts_list_e.txt",                // Node metadata
                "partof_element_parts.txt",              // File ID associations
                "partof_inclusion_relation_list.txt"     // Parent-child links
        );
    }

    /**
     * Returns the root of the part-of hierarchy (tree or graph).
     * This node can be used to start traversing or rendering the data structure.
     *
     * @return the root ANode object
     */
    public ANode getPartOfRoot() {
        return partOfRoot;
    }
}
