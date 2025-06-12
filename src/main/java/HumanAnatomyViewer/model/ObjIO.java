package HumanAnatomyViewer.model;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import java.io.File;

public class ObjIO {

    /**
     * Loads a 3D model (.obj), adjusts it so that its minimal corner vertex is at the origin,
     * applies a given offset to place it elsewhere in the scene via modelGroup translation,
     * and returns it as a Group.
     *
     * @param file   the .obj file to load
     * @return Group containing the loaded 3D model, or null if loading fails
     */
    public static Group openObjFile(File file) {

        if (file == null) return null;

        try {
            // Parse OBJ file to create a TriangleMesh
            TriangleMesh mesh = ObjParser.load(file.getAbsolutePath());

            // Extract mesh points (vertices)
            float[] points = mesh.getPoints().toArray(null);

            // Inizialize minimal vertex coordinates
            float minX = points[0];
            float minY = points[1];
            float minZ = points[2];

            // iterate through all points to find the minimum x,y,z coordinates
            for (int i = 0; i < points.length; i += 3) {
                if (points[i] < minX) minX = points[i];
                if (points[i + 1] < minY) minY = points[i + 1];
                if (points[i + 2] < minZ) minZ = points[i + 2];
            }

            // create material for mesh
            PhongMaterial material = new PhongMaterial();
            material.setSpecularColor(Color.WHITE);

            // Try loading texture image with same name as obj file
            String texturePath = file.getAbsolutePath().replace(".obj", ".png");
            File textureFile = new File(texturePath);

            if (textureFile.exists()) {
                // If texture file exists, apply diffuse map
                Image textureImage = new Image(textureFile.toURI().toString());
                material.setDiffuseMap(textureImage);
            } else {
                // otherwise use green as default color
                material.setDiffuseColor(Color.WHITE);
            }

            // Create mashview to display the mesh
            MeshView meshView = new MeshView(mesh);
            meshView.setMaterial(material);

            // Group into Group node
            Group modelGroup = new Group(meshView);

            return modelGroup;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

