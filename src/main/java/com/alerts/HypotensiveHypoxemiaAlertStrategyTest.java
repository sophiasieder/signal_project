package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;


public class HypotensiveHypoxemiaAlertStrategyTest {
    private HypotensiveHypoxemiaAlertStrategy strategy;
    private Patient testPatient;

    @BeforeEach
    void setUp() {
        strategy = new HypotensiveHypoxemiaAlertStrategy();
        testPatient = new Patient(456);
    }

    @Test
    @DisplayName("Should return strategy name")
    void testGetStrategyName() {
        assertEquals("HypotensiveHypoxemiaAlertStrategy", strategy.getStrategyName());
    }

    @Test
    @DisplayName("Should return empty alerts for empty records")
    void testEmptyRecords() {
        List<PatientRecord> emptyRecords = new ArrayList<>();
        List<Alert> alerts = strategy.evaluateData(testPatient, emptyRecords);
        assertTrue(alerts.isEmpty());
    }
    @Test
    @DisplayName("Should return empty alerts when missing systolic records")
    void testMissingSystolicRecords() {
        List<PatientRecord> records = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        records.add(new PatientRecord(456, 90.0, "Saturation", currentTime));

        List<Alert> alerts = strategy.evaluateData(testPatient, records);
        assertTrue(alerts.isEmpty());
    }

    @Test
    @DisplayName("Should return empty alerts when missing saturation records")
    void testMissingSaturationRecords() {
        List<PatientRecord> records = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        records.add(new PatientRecord(456, 85.0, "SystolicPressure", currentTime));

        List<Alert> alerts = strategy.evaluateData(testPatient, records);
        assertTrue(alerts.isEmpty());
    }
    /*@Test
    @DisplayName("Should trigger hypotensive hypoxemia alert")
    void testHypotensiveHypoxemiaAlert() {
        List<PatientRecord> records = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        // Both conditions met within time window
        records.add(new PatientRecord(456, 85.0, "SystolicPressure", currentTime));
        records.add(new PatientRecord(456, 90.0, "Saturation", currentTime + 1000)); // 1 second later

        List<Alert> alerts = strategy.evaluateData(testPatient, records);

        assertEquals(1, alerts.size());
        Alert alert = alerts.get(0);
        assertEquals("456", alert.getPatientId());
        assertTrue(alert.getCondition().contains("CRITICAL: Hypotensive Hypoxemia"));
        System.out.println("ALERTS RETURNED: " + alerts.size());
        for (Alert a : alerts) System.out.println(a.getCondition());
        assertTrue(alert.getCondition().contains("85.0 mmHg"));
        assertTrue(alert.getCondition().contains("90.0%"));
    }*/

    @Test
    @DisplayName("Should not trigger alert when systolic pressure is normal")
    void testNormalSystolicPressure() {
        List<PatientRecord> records = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        records.add(new PatientRecord(456, 95.0, "SystolicPressure", currentTime));
        records.add(new PatientRecord(456, 90.0, "Saturation", currentTime));

        List<Alert> alerts = strategy.evaluateData(testPatient, records);
        assertTrue(alerts.isEmpty());
    }

    @Test
    @DisplayName("Should not trigger alert when saturation is normal")
    void testNormalSaturation() {
        List<PatientRecord> records = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        records.add(new PatientRecord(456, 85.0, "SystolicPressure", currentTime));
        records.add(new PatientRecord(456, 95.0, "Saturation", currentTime));

        List<Alert> alerts = strategy.evaluateData(testPatient, records);
        assertTrue(alerts.isEmpty());
    }

    @Test
    @DisplayName("Should not trigger alert when readings are outside time correlation window")
    void testOutsideTimeWindow() {
        List<PatientRecord> records = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        long sixMinutesAgo = currentTime - (6 * 60 * 1000); // Outside 5-minute window

        records.add(new PatientRecord(456, 85.0, "SystolicPressure", sixMinutesAgo));
        records.add(new PatientRecord(456, 90.0, "Saturation", currentTime));
        List<Alert> alerts = strategy.evaluateData(testPatient, records);
        assertTrue(alerts.isEmpty());
    }

    @Test
    @DisplayName("Should trigger alert at exact threshold values")
    void testThresholdValues() {
        List<PatientRecord> records = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        // Exactly at thresholds (below threshold values)
        records.add(new PatientRecord(456, 89.0, "SystolicPressure", currentTime));
        records.add(new PatientRecord(456, 91.0, "Saturation", currentTime));

        List<Alert> alerts = strategy.evaluateData(testPatient, records);

        assertEquals(1, alerts.size());
    }
    @Test
    @DisplayName("Should handle BloodSaturation record type")
    void testBloodSaturationType() {
        List<PatientRecord> records = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        records.add(new PatientRecord(456, 85.0, "SystolicPressure", currentTime));
        records.add(new PatientRecord(456, 90.0, "BloodSaturation", currentTime));

        List<Alert> alerts = strategy.evaluateData(testPatient, records);

        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).getCondition().contains("CRITICAL: Hypotensive Hypoxemia"));
    }

   /* @Test
    @DisplayName("Should use most recent readings")
    void testMostRecentReadings() {
        List<PatientRecord> records = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        // Add older normal readings
        records.add(new PatientRecord(456, 120.0, "SystolicPressure", currentTime - 10000));
        records.add(new PatientRecord(456, 98.0, "Saturation", currentTime - 10000));

        // Add recent critical readings
        records.add(new PatientRecord(456, 85.0, "SystolicPressure", currentTime));
        records.add(new PatientRecord(456, 90.0, "Saturation", currentTime));

        List<Alert> alerts = strategy.evaluateData(testPatient, records);

        assertEquals(1, alerts.size());
        System.out.println("ALERTS RETURNED: " + alerts.size());
        for (Alert a : alerts) System.out.println(a.getCondition());
        assertTrue(alerts.get(0).getCondition().contains("85.0 mmHg"));
        assertTrue(alerts.get(0).getCondition().contains("90.0%"));
    }*/
}


