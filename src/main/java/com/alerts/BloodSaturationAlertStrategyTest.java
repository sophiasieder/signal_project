package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;


public class BloodSaturationAlertStrategyTest {
    private BloodSaturationAlertStrategy strategy;
    private Patient testPatient;

    @BeforeEach
    void setUp() {
        strategy = new BloodSaturationAlertStrategy();
        testPatient = new Patient(123);
    }

    @Test
    @DisplayName("Should return strategy name")
    void testGetStrategyName() {
        assertEquals("BloodSaturationAlertStrategy", strategy.getStrategyName());
    }

    @Test
    @DisplayName("Should return empty alerts for empty records")
    void testEmptyRecords() {
        List<PatientRecord> emptyRecords = new ArrayList<>();
        List<Alert> alerts = strategy.evaluateData(testPatient, emptyRecords);
        assertTrue(alerts.isEmpty());
    }

    @Test
    @DisplayName("Should trigger low saturation alert when below threshold")
    void testLowSaturationAlert() {
        List<PatientRecord> records = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        // Add record with saturation below 92%
        records.add(new PatientRecord(123, 91.0, "Saturation", currentTime));

        List<Alert> alerts = strategy.evaluateData(testPatient, records);


        assertEquals(1, alerts.size());
        Alert alert = alerts.get(0);
        assertEquals("123", alert.getPatientId());
        assertTrue(alert.getCondition().contains("Low Blood Saturation"));
        assertTrue(alert.getCondition().contains("91.0%"));
    }

    @Test
    @DisplayName("Should not trigger low saturation alert when above threshold")
    void testNormalSaturation() {
        List<PatientRecord> records = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        // Add record with saturation above 92%
        records.add(new PatientRecord(123, 95.0, "Saturation", currentTime));

        List<Alert> alerts = strategy.evaluateData(testPatient, records);

        assertTrue(alerts.isEmpty());
    }
    @Test
    @DisplayName("Should trigger low saturation alert at exact threshold")
    void testSaturationAtThreshold() {
        List<PatientRecord> records = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        // Add record with saturation exactly at 92%
        records.add(new PatientRecord(123, 92.0, "Saturation", currentTime));

        List<Alert> alerts = strategy.evaluateData(testPatient, records);

        assertTrue(alerts.isEmpty()); // Should not trigger as it's not below threshold
    }

    @Test
    @DisplayName("Should trigger rapid drop alert")
    void testRapidDropAlert() {
        List<PatientRecord> records = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        long fiveMinutesAgo = currentTime - (5 * 60 * 1000);

        // Add earlier record with higher saturation
        records.add(new PatientRecord(123, 98.0, "Saturation", fiveMinutesAgo));
        // Add current record with 5% drop
        records.add(new PatientRecord(123, 93.0, "Saturation", currentTime));

        List<Alert> alerts = strategy.evaluateData(testPatient, records);

        assertEquals(1, alerts.size());
        Alert alert = alerts.get(0);
        assertEquals("123", alert.getPatientId());
        assertTrue(alert.getCondition().contains("Rapid Saturation Drop"));
        assertTrue(alert.getCondition().contains("5.0%"));
    }

    @Test
    @DisplayName("Should not trigger rapid drop alert when drop is less than 5%")
    void testSmallDropAlert() {
        List<PatientRecord> records = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        long fiveMinutesAgo = currentTime - (5 * 60 * 1000);
        // Add earlier record with higher saturation (only 4% drop)
        records.add(new PatientRecord(123, 97.0, "Saturation", fiveMinutesAgo));
        records.add(new PatientRecord(123, 93.0, "Saturation", currentTime));

        List<Alert> alerts = strategy.evaluateData(testPatient, records);

        assertTrue(alerts.isEmpty());
    }

    @Test
    @DisplayName("Should not trigger rapid drop alert when time window exceeded")
    void testRapidDropOutsideTimeWindow() {
        List<PatientRecord> records = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        long fifteenMinutesAgo = currentTime - (15 * 60 * 1000); // Outside 10-minute window

        records.add(new PatientRecord(123, 98.0, "Saturation", fifteenMinutesAgo));
        records.add(new PatientRecord(123, 93.0, "Saturation", currentTime));

        List<Alert> alerts = strategy.evaluateData(testPatient, records);

        assertTrue(alerts.isEmpty());
    }
    @Test
    @DisplayName("Should handle BloodSaturation record type")
    void testBloodSaturationType() {
        List<PatientRecord> records = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        records.add(new PatientRecord(123, 90.0, "BloodSaturation", currentTime));

        List<Alert> alerts = strategy.evaluateData(testPatient, records);

        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).getCondition().contains("Low Blood Saturation"));
    }
    @Test
    @DisplayName("Should trigger both low saturation and rapid drop alerts")
    void testMultipleAlerts() {
        List<PatientRecord> records = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        long fiveMinutesAgo = currentTime - (5 * 60 * 1000);

        records.add(new PatientRecord(123, 96.0, "Saturation", fiveMinutesAgo));
        records.add(new PatientRecord(123, 90.0, "Saturation", currentTime)); // Both low and rapid drop

        List<Alert> alerts = strategy.evaluateData(testPatient, records);

        assertEquals(2, alerts.size());
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().contains("Low Blood Saturation")));
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().contains("Rapid Saturation Drop")));
    }
    @Test
    @DisplayName("Should filter out non-saturation records")
    void testRecordTypeFiltering() {
        List<PatientRecord> records = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        // Mix of different record types
        records.add(new PatientRecord(123, 90.0, "SystolicPressure", currentTime));
        records.add(new PatientRecord(123, 85.0, "Saturation", currentTime));
        records.add(new PatientRecord(123, 70.0, "HeartRate", currentTime));

        List<Alert> alerts = strategy.evaluateData(testPatient, records);

        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).getCondition().contains("Low Blood Saturation"));
    }
}



