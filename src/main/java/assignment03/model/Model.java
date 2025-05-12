package assignment03.model;

import java.io.IOException;

public class Model {
    private final ANode partOfRoot;

    public Model() throws IOException {
        partOfRoot = TreeLoader.load(
                "partof_parts_list_e.txt",
                "partof_element_parts.txt",
                "partof_inclusion_relation_list.txt"
        );
    }

    public ANode getPartOfRoot() {
        return partOfRoot;
    }
}