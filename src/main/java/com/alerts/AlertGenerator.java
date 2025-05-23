package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.util.ArrayList;
import java.util.List;

/**
 * The AlertGenerator class checks patient data and creates alerts
 * when certain conditions are met. It uses a DataStorage instance
 * to access (+ evaluate) the patients health information.
 */
public class AlertGenerator {
    private DataStorage dataStorage;
    private List<AlertStrategy> alertStrategies;
    private List<AlertListener> alertListeners;

    /**
     * Creates an AlertGenerator using the given DataStorage.
     * The DataStorage provides access to the patient data
     * that this class will check and monitor.
     *
     * @param dataStorage the data storage system that provides access to patient data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
        this.alertStrategies = new ArrayList<>();
        this.alertListeners = new ArrayList<>();

        // Initialize all alert strategies
        initializeAlertStrategies();
    }

    /**
     * Initialize all alert strategies
     */
    private void initializeAlertStrategies() {
        alertStrategies.add(new BloodPressureAlertStrategy());
        alertStrategies.add(new BloodSaturationAlertStrategy());
        alertStrategies.add(new HypotensiveHypoxemiaAlertStrategy());
        alertStrategies.add(new ECGAlertStrategy());
        alertStrategies.add(new TriggeredAlertStrategy());
    }

    /**
     * Add an alert listener to be notified when alerts are triggered
     * @param listener the alert listener to add
     */
    public void addAlertListener(AlertListener listener) {
        alertListeners.add(listener);
    }

    /**
     * Remove an alert listener
     * @param listener the alert listener to remove
     */
    public void removeAlertListener(AlertListener listener) {
        alertListeners.remove(listener);
    }

    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered via the
     * {@link #triggerAlert} method. This method defines the specific conditions
     * under which an alert will be triggered.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        try {
            // Get recent patient records (last 24 hours for comprehensive analysis)
            long currentTime = System.currentTimeMillis();
            long timeWindow = 24 * 60 * 60 * 1000; // 24 hours
            long startTime = currentTime - timeWindow;

            List<PatientRecord> recentRecords = dataStorage.getRecords(patient.getPatientId(), startTime, currentTime);

            if (recentRecords.isEmpty()) {
                return; // No data to evaluate
            }

            // Apply each alert strategy
            for (AlertStrategy strategy : alertStrategies) {
                try {
                    List<Alert> strategyAlerts = strategy.evaluateData(patient, recentRecords);
                    for (Alert alert : strategyAlerts) {
                        triggerAlert(alert);
                    }
                } catch (Exception e) {
                    System.err.println("Error in alert strategy " + strategy.getStrategyName() +
                            " for patient " + patient.getPatientId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            System.err.println("Error evaluating data for patient " + patient.getPatientId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Triggers an alert for the monitoring system. This method notifies all
     * registered alert listeners and logs the alert.
     *
     * @param alert the alert object containing details about the alert condition
     */
    private void triggerAlert(Alert alert) {
        try {
            // Log the alert
            System.out.println("ALERT TRIGGERED: Patient " + alert.getPatientId() +
                    " - " + alert.getCondition() + " at " + new java.util.Date(alert.getTimestamp()));

            // Notify all alert listeners
            for (AlertListener listener : alertListeners) {
                try {
                    listener.onAlertTriggered(alert);
                } catch (Exception e) {
                    System.err.println("Error notifying alert listener: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Error triggering alert: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Evaluate all patients in the data storage system
     */
    public void evaluateAllPatients() {
        try {
            List<Patient> patients = dataStorage.getAllPatients();
            for (Patient patient : patients) {
                evaluateData(patient);
            }
        } catch (Exception e) {
            System.err.println("Error evaluating all patients: " + e.getMessage());
            e.printStackTrace();
        }
    }
}