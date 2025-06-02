package assignment4.model;

import java.io.InputStream;
import java.io.IOException;

// Model class for the assignment
public class Model {
    private ANode root;

    // Constructor that initializes the model with the given input streams
    public Model(InputStream partsStream, InputStream elementsStream, InputStream relationsStream) throws IOException {
        this.root = TreeLoader.load(partsStream, elementsStream, relationsStream);
    }

    // Constructor that initializes the model with the given root node
    public ANode getRoot() {
        return root;
    }
}

