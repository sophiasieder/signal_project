package com.cardio_generator.outputs;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WebSocketIntegrationTest {

    private DataStorage storage;
    private WebSocketDataReader reader;

    @BeforeEach
    public void setup() {
        storage = DataStorage.getInstance();
        reader = new WebSocketDataReader(storage, "ws://localhost:9999");  // URL egal für Test
    }

    @Test
    public void testCombinedHypotensiveHypoxemiaAlert() {
        // Beispielhafte simulierte Daten
        reader.handleIncomingMessage("42,1716680000000,blood_oxygen,91.0");
        reader.handleIncomingMessage("42,1716680005000,blood_pressure_systolic,85.0");

        // Überprüfe, ob beide Records gespeichert wurden
        List<PatientRecord> records = storage.getRecords(42, 0, Long.MAX_VALUE);
        assertEquals(2, records.size());

        assertTrue(records.stream().anyMatch(r -> r.getRecordType().equals("blood_oxygen") && r.getMeasurementValue() == 91.0));
        assertTrue(records.stream().anyMatch(r -> r.getRecordType().equals("blood_pressure_systolic") && r.getMeasurementValue() == 85.0));
    }
}