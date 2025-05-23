package com.data_management;
import java.io.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

    /**
     * reads CSV files from folder, stores each record in DataStorage
     */
    public class FileDataReader implements DataReader {
        private final Path directory;

        public FileDataReader(String directoryPath) {
            this.directory = Paths.get(directoryPath);
        }

        @Override
        public void readData(DataStorage dataStorage) throws IOException {
            try (DirectoryStream<Path> files = Files.newDirectoryStream(directory, "*.csv")) {
                for (Path file : files) {
                    try (Stream<String> lines = Files.lines(file)) {
                        lines.forEach(line -> {
                            try {
                                String[] parts = line.split(",");
                                if (parts.length != 4) return;

                                long ts = Long.parseLong(parts[0].trim());
                                int pid = Integer.parseInt(parts[1].trim());
                                String type = parts[2].trim();
                                double val = Double.parseDouble(parts[3].trim());

                                dataStorage.addPatientData(pid, val, type, ts);
                            } catch (Exception e) {
                                System.err.println("Skipping malformed line: " + line);
                            }
                        });
                    }
                }
            }
        }
    }