package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles combined hypotensive hypoxemia alerts:
 * it Triggers when both systolic BP are less that
 * 90 mmHg AND blood saturation is less than 92%
 */
public class HypotensiveHypoxemiaAlertStrategy implements AlertStrategy {

    private static final double SYSTOLIC_THRESHOLD = 90.0;
    private static final double SATURATION_THRESHOLD = 92.0;
    private static final long TIME_CORRELATION_WINDOW = 5 * 60 * 1000; // 5 minutes

    @Override
    public List<Alert> evaluateData(Patient patient, List<PatientRecord> records) {
        List<Alert> alerts = new ArrayList<>();

        // Filter relevant records
        List<PatientRecord> systolicRecords = records.stream()
                .filter(r -> "SystolicPressure".equals(r.getRecordType()))
                .sorted((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()))
                .collect(Collectors.toList());

        List<PatientRecord> saturationRecords = records.stream()
                .filter(r -> "Saturation".equals(r.getRecordType()) || "BloodSaturation".equals(r.getRecordType()))
                .sorted((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()))
                .collect(Collectors.toList());

        if (systolicRecords.isEmpty() || saturationRecords.isEmpty()) {
            return alerts;
        }

        // check for concurrent low systolic BP and for low saturation
        PatientRecord latestSystolic = systolicRecords.get(systolicRecords.size() - 1);
        PatientRecord latestSaturation = saturationRecords.get(saturationRecords.size() - 1);

        // Check if readings are within thee correlation time window
        long timeDiff = Math.abs(latestSystolic.getTimestamp() - latestSaturation.getTimestamp());

        if (timeDiff <= TIME_CORRELATION_WINDOW &&
                latestSystolic.getMeasurementValue() < SYSTOLIC_THRESHOLD &&
                latestSaturation.getMeasurementValue() < SATURATION_THRESHOLD) {

            alerts.add(new Alert(
                    String.valueOf(patient.getPatientId()),
                    String.format("CRITICAL: Hypotensive Hypoxemia - Low BP (%.1f mmHg) AND Low O2 (%.1f%%)",
                            latestSystolic.getMeasurementValue(), latestSaturation.getMeasurementValue()),
                    System.currentTimeMillis()
            ));
        }

        return alerts;
    }

    @Override
    public String getStrategyName() {
        return "HypotensiveHypoxemiaAlertStrategy";
    }
}