package com.data_management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

    public class PatientTest {

        private Patient patient;

        @BeforeEach
        void setUp() {
            patient = new Patient(1); // Create a patient for each test
        }


        /**
         * verifies functionality of adding a single record & retrieving
         * purpose: Ensures that {@code addRecord} correctly stores data and
         * {@code getRecords} can retrieve a single, correctly stored record.
         */
        @Test
        void testAddRecord() {
            patient.addRecord(120.0, "BloodPressure", 1678886400000L); // March 15, 2023 00:00:00 GMT
            List<PatientRecord> records = patient.getRecords(0, Long.MAX_VALUE); // Get all records
            assertEquals(1, records.size());
            PatientRecord record = records.get(0);
            assertAll("record properties",
                    () -> assertEquals(1, record.getPatientId()),
                    () -> assertEquals(120.0, record.getMeasurementValue()),
                    () -> assertEquals("BloodPressure", record.getRecordType()),
                    () -> assertEquals(1678886400000L, record.getTimestamp())
            );
        }
        /**
         * ensures behavior when the specified time range contains no records.
         * purpose: ensures that an empty list is returned if no records fall within
         * the requested time frame
         */
        @Test
        void testGetRecords_emptyRange() {
            patient.addRecord(70.0, "HeartRate", 1000L);
            patient.addRecord(72.0, "HeartRate", 2000L);
            patient.addRecord(75.0, "HeartRate", 3000L);

            List<PatientRecord> records = patient.getRecords(5000L, 6000L); // Range outside existing records
            assertTrue(records.isEmpty(), "Should return an empty list for an empty time range");
        }

        /**
         * verifies behavior when the specified
         * time range covers all existing records.
         * purpose: ensures that all relevant records are retrieved when the range
         * encompasses them.
         */
        @Test
        void testGetRecords_fullRange() {
            patient.addRecord(70.0, "HeartRate", 1000L);
            patient.addRecord(72.0, "HeartRate", 2000L);
            patient.addRecord(75.0, "HeartRate", 3000L);

            List<PatientRecord> records = patient.getRecords(0, 4000L); // Range covering all records
            assertEquals(3, records.size(), "Should return all three records");
        }

        /**
         * verifies behavior when the specified
         * time range covers only a subset of existing records.
         * purpose: ensures that only records within the partial range are retrieved.
         */
        @Test
        void testGetRecords_partialRange() {
            patient.addRecord(70.0, "HeartRate", 1000L);
            patient.addRecord(72.0, "HeartRate", 2000L);
            patient.addRecord(75.0, "HeartRate", 3000L);
            patient.addRecord(80.0, "HeartRate", 4000L);

            List<PatientRecord> records = patient.getRecords(1500L, 3500L); // Partial range
            assertEquals(2, records.size(), "Should return two records within the partial range");
            assertEquals(72.0, records.get(0).getMeasurementValue()); // Record at 2000L
            assertEquals(75.0, records.get(1).getMeasurementValue()); // Record at 3000L
        }

        /**
         * verifies behavior with boundary conditions,
         * specifically when startTime or endTime exactly match a record's timestamp.
         * purpose: ensures inclusivity of records at the exact start and end timestamps.
         */
        @Test
        void testGetRecords_boundaryConditions() {
            patient.addRecord(60.0, "SpO2", 1000L);
            patient.addRecord(65.0, "SpO2", 2000L);
            patient.addRecord(70.0, "SpO2", 3000L);

            // Test with startTime equal to a record's timestamp
            List<PatientRecord> records1 = patient.getRecords(2000L, 3000L);
            assertEquals(2, records1.size(), "Should include records exactly at startTime and endTime");
            assertEquals(65.0, records1.get(0).getMeasurementValue());
            assertEquals(70.0, records1.get(1).getMeasurementValue());

            // Test with endTime equal to a record's timestamp
            List<PatientRecord> records2 = patient.getRecords(1000L, 2000L);
            assertEquals(2, records2.size(), "Should include records exactly at startTime and endTime");
            assertEquals(60.0, records2.get(0).getMeasurementValue());
            assertEquals(65.0, records2.get(1).getMeasurementValue());
        }

        /**
         * verifies behavior when no records have been added to patient
         * purpose: ensures robustness when querying an empty patient record list
         */
        @Test
        void testGetRecords_noRecordsAdded() {
            List<PatientRecord> records = patient.getRecords(0, 1000L);
            assertTrue(records.isEmpty(), "Should return an empty list if no records have been added");
        }
        /**
         * verifies behavior with multiple overlapping time ranges
         * purpose: ensures correct filtering across different query ranges
         */
        @Test
        void testGetRecords_overlappingRanges() {
            patient.addRecord(10.0, "Temp", 100L);
            patient.addRecord(20.0, "Temp", 200L);
            patient.addRecord(30.0, "Temp", 300L);

            List<PatientRecord> records1 = patient.getRecords(50L, 250L);
            assertEquals(2, records1.size()); // Should include 100L, 200L

            List<PatientRecord> records2 = patient.getRecords(150L, 350L);
            assertEquals(2, records2.size()); // Should include 200L, 300L
        }
    }

