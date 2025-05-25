package com.cardio_generator.outputs;
import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class WebSocketDataReaderTest {


        private DataStorage storage;
        private WebSocketDataReader reader;

        @BeforeEach
        public void setup() {
            storage = DataStorage.getInstance();
        }

        @Test
        public void testValidMessageIsParsedCorrectly() throws Exception {
            String testMessage = "patient123,heart_rate,85.0,1716584430000";

            invokeMessageHandler(testMessage);

            List<PatientRecord> records = storage.getRecords(1, 0, Long.MAX_VALUE);
            assertEquals(1, records.size());

            PatientRecord record = records.get(0);
            assertEquals("heart_rate", record.getRecordType());
            assertEquals(85.0, record.getMeasurementValue());
            assertEquals(1716584430000L, record.getTimestamp());
        }

        @Test
        public void testInvalidMessageFormat() throws Exception {
            String badMessage = "bad_data";
            invokeMessageHandler(badMessage);
            // No crash expected, no record stored
            List<PatientRecord> records = storage.getRecords(2, 0, Long.MAX_VALUE);
            assertEquals(0, records.size());
        }

        @Test
        public void testNonNumericValueHandledGracefully() throws Exception {
            String badMessage = "patientX,blood_pressure,notANumber,1716584430000";
            invokeMessageHandler(badMessage);
            List<PatientRecord> records = storage.getRecords(3, 0, Long.MAX_VALUE);
            assertEquals(0, records.size());
        }

        /**
         * Uses reflection to simulate WebSocket message arrival without needing real connection.
         */
        private void invokeMessageHandler(String message) throws Exception {
            reader = new WebSocketDataReader(storage, "ws://localhost:9999");
            Method onMessage = reader.getClass().getDeclaredField("client")
                    .getType().getSuperclass().getDeclaredMethod("onMessage", String.class);
            onMessage.setAccessible(true);
            Object client = reader.getClass().getDeclaredField("client").get(reader);
            onMessage.invoke(client, message);
        }
    }

