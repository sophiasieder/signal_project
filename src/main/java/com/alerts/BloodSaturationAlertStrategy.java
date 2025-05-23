package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * handles blood oxygen saturation alerts:
 * - We have <92% => Low saturation alert
 * - 5% drop within 10 minutes => rapid drop alert
 */
public class BloodSaturationAlertStrategy implements AlertStrategy {

    private static final double LOW_SATURATION_THRESHOLD = 92.0;
    private static final double RAPID_DROP_THRESHOLD = 5.0;
    private static final long RAPID_DROP_TIME_WINDOW = 10 * 60 * 1000; // 10 minutes in milliseconds

    @Override
    public List<Alert> evaluateData(Patient patient, List<PatientRecord> records) {
        List<Alert> alerts = new ArrayList<>();

        //filter saturation records
        List<PatientRecord> saturationRecords = records.stream()
                .filter(r -> "Saturation".equals(r.getRecordType()) || "BloodSaturation".equals(r.getRecordType()))
                .sorted((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()))
                .collect(Collectors.toList());

        if (saturationRecords.isEmpty()) return alerts;

        //check low saturation alert
        PatientRecord latestRecord = saturationRecords.get(saturationRecords.size() - 1);
        if (latestRecord.getMeasurementValue() < LOW_SATURATION_THRESHOLD) {
            alerts.add(new Alert(
                    String.valueOf(patient.getPatientId()),
                    String.format("Low Blood Saturation: %.1f%% (threshold: %.1f%%)",
                            latestRecord.getMeasurementValue(), LOW_SATURATION_THRESHOLD),
                    System.currentTimeMillis()
            ));
        }

        // Check rapid drop alert
        Alert rapidDropAlert = checkRapidDropAlert(patient, saturationRecords);
        if (rapidDropAlert != null) {
            alerts.add(rapidDropAlert);
        }

        return alerts;
    }

    private Alert checkRapidDropAlert(Patient patient, List<PatientRecord> records) {
        if (records.size() < 2) return null;

        PatientRecord latestRecord = records.get(records.size() - 1);
        long currentTime = latestRecord.getTimestamp();

        // Find readings within the 10-minute window
        for (int i = records.size() - 2; i >= 0; i--) {
            PatientRecord earlierRecord = records.get(i);
            long timeDiff = currentTime - earlierRecord.getTimestamp();

            if (timeDiff > RAPID_DROP_TIME_WINDOW) {
                break; // Outside the time window
            }

            double saturationDrop = earlierRecord.getMeasurementValue() - latestRecord.getMeasurementValue();

            if (saturationDrop >= RAPID_DROP_THRESHOLD) {
                return new Alert(
                        String.valueOf(patient.getPatientId()),
                        String.format("Rapid Saturation Drop: %.1f%% within 10 minutes (from %.1f%% to %.1f%%)",
                                saturationDrop, earlierRecord.getMeasurementValue(), latestRecord.getMeasurementValue()),
                        System.currentTimeMillis()
                );
            }
        }

        return null;
    }

    @Override
    public String getStrategyName() {
        return "BloodSaturationAlertStrategy";
    }
}