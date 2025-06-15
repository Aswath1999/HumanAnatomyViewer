package HumanAnatomyViewer.model;

import java.io.IOException;

/**
 * The Model class serves as a data access layer in the application.
 * It loads and stores the root nodes of two anatomical hierarchies:
 * - "part-of" (structural containment)
 * - "is-a" (conceptual categorization)
 */
public class Model {

    private final ANode partOfRoot;
    private final ANode isARoot;

    /**
     * Constructor initializes the model by loading both hierarchies from resource files.
     *
     * @throws IOException if any of the files cannot be loaded
     */
    public Model() throws IOException {
        // Load the "part-of" hierarchy
        partOfRoot = TreeLoader.load(
                "HumanAnatomy/partof_parts_list_e.txt",
                "HumanAnatomy/partof_element_parts.txt",
                "HumanAnatomy/partof_inclusion_relation_list.txt"
        );

        // Load the "is-a" hierarchy
        isARoot = TreeLoader.load(
                "HumanAnatomy/isa_parts_list_e.txt",
                "HumanAnatomy/isa_element_parts.txt",
                "HumanAnatomy/isa_inclusion_relation_list.txt"
        );
    }

    /**
     * Gets the root of the "part-of" hierarchy.
     */
    public ANode getPartOfRoot() {
        return partOfRoot;
    }



    /**
     * Gets the root of the "is-a" hierarchy.
     */
    public ANode getIsARoot() {
        return isARoot;
    }
}
