package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;
//name didn't match file name,f needs to be uppercase, class names: UpperCamelCase
public class FileOutputStrategy implements OutputStrategy {
    //B needs to be small, lowerCamelCase
    private String baseDirectory;
    //upper case, bc final, constant, UPPER_SNAKE_CASE
    public final ConcurrentHashMap<String, String> FILE_MAP = new ConcurrentHashMap<>();

    //constructor name didn't match, f needs to be uppercase, added method-level Javadoc
    /**
     * Creates a FileOutputStrategy with the specified base directory.
     *
     * @param baseDirectory the directory where output files will be created
     */
    public FileOutputStrategy(String baseDirectory) {

        this.baseDirectory = baseDirectory;
    }

    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // Set the FilePath variable
        // FilePath --> filePath, variables become lowerCamelCase
        String filePath = FILE_MAP.computeIfAbsent(label,
                k -> Paths.get(baseDirectory, label + ".txt").toString());

        // Write the data to the file
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filePath),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n",
                    patientId, timestamp, label, data);
        } catch (Exception e) { //adjust filePath
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}