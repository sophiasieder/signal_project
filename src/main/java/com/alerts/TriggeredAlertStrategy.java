package com.alerts;

import com.data_management.*;
import com.data_management.PatientRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This one handles manually triggered alerts
 * for example nurse call buttons, patient alerts :)
 */
public class TriggeredAlertStrategy implements AlertStrategy {

    @Override
    public List<Alert> evaluateData(Patient patient, List<PatientRecord> records) {
        List<Alert> alerts = new ArrayList<>();

        // Filter alert records
        List<PatientRecord> alertRecords = records.stream()
                .filter(r -> "Alert".equals(r.getRecordType()))
                .sorted((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()))
                .collect(Collectors.toList());

        for (PatientRecord record : alertRecords) {
            // Check if this is a new triggered alert (not resolved)
            String recordData = record.getRecordData();
            if ("triggered".equals(recordData)) {
                alerts.add(new Alert(
                        String.valueOf(patient.getPatientId()),
                        "Manual Alert: Triggered by patient or medical staff",
                        System.currentTimeMillis()
                ));
            }
        }

        return alerts;
    }

    @Override
    public String getStrategyName() {
        return "TriggeredAlertStrategy";
    }
}
