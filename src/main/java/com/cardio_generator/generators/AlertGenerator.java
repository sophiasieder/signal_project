package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * class implements {@link PatientDataGenerator} and randomly turns on and off alerts for patients
 * alerts are randomly triggered by probability model
 */

public class AlertGenerator implements PatientDataGenerator {
    //final constants become upper snake case
    public static final Random RANDOM_GENERATOR = new Random();
    //variables become lower camel case
    private boolean[] alertStates; // false = resolved, true = pressed

    //adjust alertStates, add comment with param and function

    /**
    * constructor that creates an alert generator for number of patients
    *@param patientCount: count of patients to generate alarms for
    *
     */
    public AlertGenerator(int patientCount) {
        alertStates = new boolean[patientCount + 1];
    }

    /**
     * generates alert for a patient
     * if alert triggered, it can resolve (90% chance)
     * if no alert is active, a new can be triggered through probability
     * @param patientId takes the ID of the patient that is being looked at
     * @param outputStrategy how the data is being stored/where it goes (console, file etc.)
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (alertStates[patientId]) {
                if (RANDOM_GENERATOR.nextDouble() < 0.9) { // 90% chance to resolve
                    alertStates[patientId] = false;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(),
                            "Alert", "resolved");
                }
            } else {
                //variable lamba small l, lower camel case
                double lambda = 0.1; // Average rate (alerts per period), adjust based on desired frequency
                double p = -Math.expm1(-lambda); // Probability of at least one alert in the period
                boolean alertTriggered = RANDOM_GENERATOR.nextDouble() < p;

                if (alertTriggered) {
                    alertStates[patientId] = true;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(),
                            "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
