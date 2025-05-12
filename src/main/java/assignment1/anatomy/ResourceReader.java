package assignment1.anatomy;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;

public class ResourceReader {

    public static void main(String[] args) {
/*
        URL root = ClassLoader.getSystemResource("");
        System.out.println("Classpath root: " + root);

        URL fileUrl = ClassLoader.getSystemResource("partof_parts_list_e.txt");
        System.out.println("Resource path: " + fileUrl);*/


        try (InputStream inputStream = ResourceReader.class.getModule().getResourceAsStream("partof_parts_list_e.txt")) {
            if (inputStream == null) {
                System.err.println("Resource not found in module: partof_parts_list_e.txt");
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String firstLine = reader.readLine();
                System.out.println("First line: " + firstLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
