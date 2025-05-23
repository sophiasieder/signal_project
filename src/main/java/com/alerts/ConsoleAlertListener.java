package com.alerts;
/**
 * Simple alert listener that logs alerts to console
 */
public class ConsoleAlertListener implements AlertListener {

    @Override
    public void onAlertTriggered(Alert alert) {
        System.out.println("=== MEDICAL ALERT ===");
        System.out.println("Patient ID: " + alert.getPatientId());
        System.out.println("Condition: " + alert.getCondition());
        System.out.println("Timestamp: " + new java.util.Date(alert.getTimestamp()));
        System.out.println("=====================");
    }
}