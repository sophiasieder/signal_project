package com.cardio_generator.outputs;
import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.data_management.DataReader;

public class WebSocketDataReader implements DataReader {

    private final DataStorage storage;
    private WebSocketClient client;
    private final String websocketUrl;

    public WebSocketDataReader(DataStorage storage, String websocketUrl) {
        this.storage = storage;
        this.websocketUrl = websocketUrl;
        initializeWebSocketClient();
    }

    private void initializeWebSocketClient() {
        try {
            this.client = new WebSocketClient(new URI(websocketUrl)) {
                @Override
                public void onOpen(ServerHandshake handshakeData) {
                    System.out.println("connected to WebSocket server at: " + websocketUrl);
                    System.out.println("server status: " + handshakeData.getHttpStatus());
                }

                @Override
                public void onMessage(String message) {
                    try {
                        //expected format from signal generator: patientId,timestamp,label,data
                        //this matches the WebSocketOutputStrategy format
                        String[] parts = message.split(",");
                        if (parts.length != 4) {
                            System.err.println("Invalid message format. Expected: patientId,timestamp,label,data");
                            System.err.println(" Received: " + message);
                            return;
                        }

                        //parse message components
                        int patientId = Integer.parseInt(parts[0].trim());
                        long timestamp = Long.parseLong(parts[1].trim());
                        String recordType = parts[2].trim(); // This is the 'label'
                        String dataValue = parts[3].trim();  // This is the 'data'

                        //convert data to double (handle non-numeric gracefully)
                        double measurementValue;
                        try {
                            measurementValue = Double.parseDouble(dataValue);
                        } catch (NumberFormatException e) {
                            System.err.println("Non-numeric data received for patient " + patientId +
                                    ", type: " + recordType + ", data: " + dataValue);
                            System.err.println("Skipping this record as DataStorage expects numeric values");
                            return;
                        }

                        //store using the existing DataStorage method
                        storage.addPatientData(patientId, measurementValue, recordType, timestamp);

                        System.out.println("Stored data - Patient ID: " + patientId +
                                ", Type: " + recordType +
                                ", Value: " + measurementValue +
                                ", Timestamp: " + timestamp);

                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing numeric values in message: " + message);
                        e.printStackTrace();
                    } catch (Exception e) {
                        System.err.println("Error processing message: " + message);
                        e.printStackTrace();
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("🔌 Connection closed. Code: " + code + ", Reason: " + reason +
                            ", Remote: " + remote);
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("WebSocket error:");
                    ex.printStackTrace();
                }
            };
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid WebSocket URL: " + websocketUrl, e);
        }
    }

    @Override
    public void readData(DataStorage dataStorage) throws IOException {

    }

    public void readData() {
        try {
            System.out.println("Starting WebSocket connection to: " + websocketUrl);
            boolean connected = client.connectBlocking();

            if (connected) {
                System.out.println("Successfully connected and listening for data...");
                System.out.println("Press Ctrl+C to stop the application");

                // Add shutdown hook for graceful cleanup
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println("Shutting down WebSocket connection...");
                    client.close();
                }));

                //to keep connection alive
                while (client.isOpen()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } else {
                System.err.println("Failed to connect to WebSocket server");
            }
        } catch (InterruptedException e) {
            System.err.println("Connection interrupted");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Error during WebSocket connection:");
            e.printStackTrace();
        }
    }

    /**
     *  reconnect with retry logic
     * @param maxRetries maximum number of retry attempts
     * @param retryDelayMs delay between retries in milliseconds
     * @return true if connection successful, false otherwise
     */
    public boolean readDataWithRetry(int maxRetries, long retryDelayMs) {
        int attempts = 0;

        while (attempts < maxRetries) {
            try {
                System.out.println("Attempting to connect... (Attempt " +
                        (attempts + 1) + "/" + maxRetries + ")");

                boolean connected = client.connectBlocking();

                if (connected) {
                    System.out.println("Successfully connected and listening for data...");

                    //add shutdown hook for graceful cleanup
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        System.out.println(" Shutting down WebSocket connection...");
                        client.close();
                    }));

                    //keep the connection alive
                    while (client.isOpen()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return true;
                        }
                    }
                    return true;
                } else {
                    System.out.println("Failed to connect to WebSocket server");
                }

            } catch (InterruptedException e) {
                System.err.println("Connection attempt interrupted");
                Thread.currentThread().interrupt();
                return false;
            } catch (Exception e) {
                System.err.println("Error during connection attempt: " + e.getMessage());
            }

            attempts++;

            if (attempts < maxRetries) {
                try {
                    System.out.println(" Waiting " + retryDelayMs + "ms before next attempt...");
                    Thread.sleep(retryDelayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }

        System.err.println("Failed to connect after " + maxRetries + " attempts");
        return false;
    }

    @Override
    public void onMessage(String message) {
        try {
            // Parse the JSON using Jackson or built-in methods
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(message);

            int patientId = jsonNode.get("patientId").asInt();
            long timestamp = jsonNode.get("timestamp").asLong();
            String recordType = jsonNode.get("label").asText();
            String dataValue = jsonNode.get("data").asText();

            double measurementValue;
            try {
                measurementValue = Double.parseDouble(dataValue);
            } catch (NumberFormatException e) {
                System.err.println("invalid numeric format for patient " + patientId + ": " + dataValue);
                return;
            }

            storage.addPatientData(patientId, measurementValue, recordType, timestamp);
            System.out.println("stored JSON data: " + jsonNode.toPrettyString());

        } catch (Exception e) {
            System.err.println("failed to parse JSON message: " + message);
            e.printStackTrace();
        }
    }

    public void handleIncomingMessage(String message) {
        try {
            // Format: patientId,recordType,value,timestamp
            String[] parts = message.split(",");
            if (parts.length != 4) {
                System.err.println("Invalid message: " + message);
                return;
            }

            int patientId = Integer.parseInt(parts[0]);
            String recordType = parts[1];
            double value = Double.parseDouble(parts[2]);
            long timestamp = Long.parseLong(parts[3]);

            // Use the correct method from DataStorage
            storage.addPatientData(patientId, value, recordType, timestamp);

            System.out.println("Received and stored: " + message);
        } catch (Exception e) {
            System.err.println("Error parsing message: " + message);
            e.printStackTrace();
        }
    }

    /**
     * closes the WebSocket connection
     */
    public void close() {
        if (client != null) {
            client.close();
        }
    }}