package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;


public class ECGAlertStrategyTest {
    private ECGAlertStrategy strategy;
    private Patient testPatient;

    @BeforeEach
    void setUp() {
        strategy = new ECGAlertStrategy();
        testPatient = new Patient(789);
    }

    @Test
    @DisplayName("Should return strategy name")
    void testGetStrategyName() {
        assertEquals("ECGAlertStrategy", strategy.getStrategyName());
    }

    @Test
    @DisplayName("Should return empty alerts for empty records")
    void testEmptyRecords() {
        List<PatientRecord> emptyRecords = new ArrayList<>();
        List<Alert> alerts = strategy.evaluateData(testPatient, emptyRecords);
        assertTrue(alerts.isEmpty());
    }

    @Test
    @DisplayName("Should return empty alerts for insufficient data")
    void testInsufficientData() {
        List<PatientRecord> records = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        // Add only 10 records (need at least 11 for sliding window analysis)
        for (int i = 0; i < 10; i++) {
            records.add(new PatientRecord(789, 1.0, "ECG", currentTime + i * 1000));
        }

        List<Alert> alerts = strategy.evaluateData(testPatient, records);
        assertTrue(alerts.isEmpty());
    }

   /*@Test
    @DisplayName("Should trigger abnormal peak alert")
    void testAbnormalPeakAlert() {
        List<PatientRecord> records = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        // Add 10 normal readings with average around 1.0
        for (int i = 0; i < 10; i++) {
            records.add(new PatientRecord(789, 1.0, "ECG", currentTime + i * 1000));
        }

        // Add abnormal peak (2.5 * 1.0 = 2.5, so 3.0 should trigger)
        records.add(new PatientRecord(789, 3.0, "ECG", currentTime + 10 * 1000));

        List<Alert> alerts = strategy.evaluateData(testPatient, records);
        assertEquals(1, alerts.size());
        Alert alert = alerts.get(0);
        assertEquals("789", alert.getPatientId());
        assertTrue(alert.getCondition().contains("ECG Abnormal Peak"));
        assertTrue(alert.getCondition().contains("3.00"));
    }*/

    @Test
    @DisplayName("Should not trigger alert for normal peaks")
    void testNormalPeaks() {
        List<PatientRecord> records = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        // Add 10 normal readings
        for (int i = 0; i < 10; i++) {
            records.add(new PatientRecord(789, 1.0, "ECG", currentTime + i * 1000));
        }

        // Add peak that's less than 2.5x average
        records.add(new PatientRecord(789, 2.0, "ECG", currentTime + 10 * 1000));

        List<Alert> alerts = strategy.evaluateData(testPatient, records);
        assertTrue(alerts.isEmpty());
    }

   /* @Test
    @DisplayName("Should handle negative ECG values")
    void testNegativeECGValues() {
        List<PatientRecord> records = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        // Add 10 readings with mixed positive and negative values
        for (int i = 0; i < 10; i++) {
            double value = (i % 2 == 0) ? 1.0 : -1.0;
            records.add(new PatientRecord(789, value, "ECG", currentTime + i * 1000));
        }

        // Add large negative peak (absolute value should be used)
        records.add(new PatientRecord(789, -3.0, "ECG", currentTime + 10 * 1000));

        List<Alert> alerts = strategy.evaluateData(testPatient, records);

        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).getCondition().contains("-3.00"));
    }*/

    @Test
    @DisplayName("Should use sliding window correctly")
    void testSlidingWindow() {
        List<PatientRecord> records = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        // Add 5 high values followed by 5 low values
        for (int i = 0; i < 5; i++) {
            records.add(new PatientRecord(789, 5.0, "ECG", currentTime + i * 1000));
        }
        for (int i = 5; i < 10; i++) {
            records.add(new PatientRecord(789, 1.0, "ECG", currentTime + i * 1000));
        }

        // The sliding window should only consider the last 10 readings (5 high + 5 low, avg = 3.0)
        // Peak of 8.0 should trigger (8.0 > 3.0 * 2.5 = 7.5)
        records.add(new PatientRecord(789, 8.0, "ECG", currentTime + 10 * 1000));

        List<Alert> alerts = strategy.evaluateData(testPatient, records);

        assertEquals(1, alerts.size());
    }

    @Test
    @DisplayName("Should filter out non-ECG records")
    void testRecordTypeFiltering() {
        List<PatientRecord> records = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        // Mix different record types
        for (int i = 0; i < 5; i++) {
            records.add(new PatientRecord(789, 1.0, "ECG", currentTime + i * 1000));
            records.add(new PatientRecord(789, 100.0, "HeartRate", currentTime + i * 1000));
        }
        for (int i = 5; i < 10; i++) {
            records.add(new PatientRecord(789, 1.0, "ECG", currentTime + i * 1000));
        }

        // Add abnormal ECG peak
        records.add(new PatientRecord(789, 3.0, "ECG", currentTime + 10 * 1000));

        List<Alert> alerts = strategy.evaluateData(testPatient, records);
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).getCondition().contains("ECG Abnormal Peak"));
    }

    @Test
    @DisplayName("Should limit to one alert per evaluation")
    void testOneAlertLimit() {
        List<PatientRecord> records = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        // Add 10 normal readings
        for (int i = 0; i < 10; i++) {
            records.add(new PatientRecord(789, 1.0, "ECG", currentTime + i * 1000));
        }

        // Add multiple abnormal peaks
        records.add(new PatientRecord(789, 3.0, "ECG", currentTime + 10 * 1000));
        records.add(new PatientRecord(789, 4.0, "ECG", currentTime + 11 * 1000));
        records.add(new PatientRecord(789, 5.0, "ECG", currentTime + 12 * 1000));
        List<Alert> alerts = strategy.evaluateData(testPatient, records);

        // Should only generate one alert due to break statement
        assertEquals(1, alerts.size());
    }

    @Test
    @DisplayName("Should handle zero values in sliding window")
    void testZeroValuesInSlidingWindow() {
        List<PatientRecord> records = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        // Add 10 zero readings
        for (int i = 0; i < 10; i++) {
            records.add(new PatientRecord(789, 0.0, "ECG", currentTime + i * 1000));
        }

        // Add any non-zero value (should trigger since average is 0)
        records.add(new PatientRecord(789, 1.0, "ECG", currentTime + 10 * 1000));

        List<Alert> alerts = strategy.evaluateData(testPatient, records);

        // Should trigger alert since any value > 0 * 2.5 when average is 0
        assertEquals(1, alerts.size());
    }



    }
