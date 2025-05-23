package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * handles blood pressure related alerts:
 * -trend alerts (3 consecutive readings with >10mmHg change)
 * -critical threshold alerts (>180/120 or <90/60)
 */
public class BloodPressureAlertStrategy implements AlertStrategy{

    private static final double TREND_THRESHOLD = 10.0;
    private static final int TREND_READINGS_COUNT = 3;

    //critical thresholds
    private static final double SYSTOLIC_HIGH = 180.0;
    private static final double SYSTOLIC_LOW = 90.0;
    private static final double DIASTOLIC_HIGH = 120.0;
    private static final double DIASTOLIC_LOW = 60.0;

    @Override
    public List<Alert> evaluateData(Patient patient, List<PatientRecord> records) {
        List<Alert> alerts = new ArrayList<>();

        //filter blood pressure records
        List<PatientRecord> systolicRecords = records.stream()
                .filter(r -> "SystolicPressure".equals(r.getRecordType()))
                .sorted((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()))
                .collect(Collectors.toList());

        List<PatientRecord> diastolicRecords = records.stream()
                .filter(r -> "DiastolicPressure".equals(r.getRecordType()))
                .sorted((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()))
                .collect(Collectors.toList());

        //check systolic pressure alerts
        alerts.addAll(checkBloodPressureAlerts(patient, systolicRecords, "Systolic", SYSTOLIC_HIGH, SYSTOLIC_LOW));
        // Check diastolic pressure alerts
        alerts.addAll(checkBloodPressureAlerts(patient, diastolicRecords, "Diastolic", DIASTOLIC_HIGH, DIASTOLIC_LOW));

        return alerts;
    }

    private List<Alert> checkBloodPressureAlerts(Patient patient, List<PatientRecord> records,
                                                 String pressureType, double highThreshold, double lowThreshold) {
        List<Alert> alerts = new ArrayList<>();

        if (records.size() < 1) return alerts;

        //check critical thresholds for latest reading
        PatientRecord latestRecord = records.get(records.size() - 1);
        double latestValue = latestRecord.getMeasurementValue();

        if (latestValue > highThreshold) {
            alerts.add(new Alert(
                    String.valueOf(patient.getPatientId()),
                    String.format("Critical High %s: %.1f mmHg (threshold: %.1f)",
                            pressureType, latestValue, highThreshold),
                    System.currentTimeMillis()
            ));
        } else if (latestValue < lowThreshold) {
            alerts.add(new Alert(
                            String.valueOf(patient.getPatientId()),
                            String.format("Critical Low %s: %.1f mmHg (threshold: %.1f)",
                                    pressureType, latestValue, lowThreshold),
                            System.currentTimeMillis()
                    ));
        }

        // Check trend alerts if we have enough readings
        if (records.size() >= TREND_READINGS_COUNT) {
            Alert trendAlert = checkTrendAlert(patient, records, pressureType);
            if (trendAlert != null) {
                alerts.add(trendAlert);
            }
        }

        return alerts;
    }

    private Alert checkTrendAlert(Patient patient, List<PatientRecord> records, String pressureType) {
        if (records.size() < TREND_READINGS_COUNT) return null;

        //get last 3 readings
        List<PatientRecord> lastThree = records.subList(records.size() - TREND_READINGS_COUNT, records.size());

        // Check for increasing trend
        boolean increasingTrend = true;
        boolean decreasingTrend = true;

        for (int i = 1; i < lastThree.size(); i++) {
            double current = lastThree.get(i).getMeasurementValue();
            double previous = lastThree.get(i-1).getMeasurementValue();
            double change = current - previous;

            if (Math.abs(change) <= TREND_THRESHOLD) {
                increasingTrend = false;
                decreasingTrend = false;
                break;
            }

            if (change <= 0) increasingTrend = false;
            if (change >= 0) decreasingTrend = false;
        }

        if (increasingTrend) {
            return new Alert(
                    String.valueOf(patient.getPatientId()),
                    String.format("%s pressure increasing trend: 3 consecutive readings with >10mmHg increase", pressureType),
                    System.currentTimeMillis()
            );
        } else if (decreasingTrend) {
            return new Alert(
                    String.valueOf(patient.getPatientId()),
                    String.format("%s pressure decreasing trend: 3 consecutive readings with >10mmHg decrease", pressureType),
                    System.currentTimeMillis()
            );
        }

        return null;
    }

    @Override
    public String getStrategyName() {
        return "BloodPressureAlertStrategy";
}}
