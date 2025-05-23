package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

    /**
     * Handles ECG data alerts:
     * - Abnormal peaks based on sliding window average
     */
    public class ECGAlertStrategy implements AlertStrategy {

        private static final int SLIDING_WINDOW_SIZE = 10;
        private static final double ABNORMAL_PEAK_MULTIPLIER = 2.5; // Peak must be 2.5x the average

        @Override
        public List<Alert> evaluateData(Patient patient, List<PatientRecord> records) {
            List<Alert> alerts = new ArrayList<>();

            // Filter ECG records
            List<PatientRecord> ecgRecords = records.stream()
                    .filter(r -> "ECG".equals(r.getRecordType()))
                    .sorted((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()))
                    .collect(Collectors.toList());

            if (ecgRecords.size() < SLIDING_WINDOW_SIZE + 1) {
                return alerts; // Not enough data for analysis
            }

            // Check for abnormal peaks using sliding window
            for (int i = SLIDING_WINDOW_SIZE; i < ecgRecords.size(); i++) {
                double currentValue = ecgRecords.get(i).getMeasurementValue();

                // Calculate average of previous readings in sliding window
                double sum = 0;
                for (int j = i - SLIDING_WINDOW_SIZE; j < i; j++) {
                    sum += Math.abs(ecgRecords.get(j).getMeasurementValue());
                }
                double average = sum / SLIDING_WINDOW_SIZE;

                // Check if current value is an abnormal peak
                if (Math.abs(currentValue) > average * ABNORMAL_PEAK_MULTIPLIER) {
                    alerts.add(new Alert(
                            String.valueOf(patient.getPatientId()),
                            String.format("ECG Abnormal Peak: %.2f (%.1fx above recent average of %.2f)",
                                    currentValue, Math.abs(currentValue) / average, average),
                            System.currentTimeMillis()
                    ));

                    // Limit to one alert per evaluation
                    // --> to avoid spam
                    break;
                }
            }

            return alerts;
        }

        @Override
        public String getStrategyName() {
            return "ECGAlertStrategy";
        }
    }

