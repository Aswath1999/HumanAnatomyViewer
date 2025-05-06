package assignment1.anatomy;



import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public record Relation(String parentId, String parentName, String childId, String childName) {
    public static List<Relation> loadFromFile(String filePath) {
        List<Relation> relations = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(Path.of(filePath))) {
                if (line.isBlank()) continue;
                String[] parts = line.split("\t");
                if (parts.length == 4) {
                    relations.add(new Relation(parts[0], parts[1], parts[2], parts[3]));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath);
            e.printStackTrace();
        }
        return relations;
    }
}
