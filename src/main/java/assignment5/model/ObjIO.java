package assignment5.model;

import javafx.scene.shape.TriangleMesh;
import java.io.File;
import java.io.IOException;

public class ObjIO {
    public static TriangleMesh loadMesh(File file) throws IOException {
        return ObjParser.load(file.getAbsolutePath());
    }
}
