package com.cardio_generator.outputs;

/**
 * interface that gives the structure for an output strategy, how and where the health care data will be outputcd
 */
public interface OutputStrategy {
    /**
     * method that needs to be overridden when interface is implemented
     * @param patientId initializes the specific patient by the ID
     * @param timestamp when the data was generated
     * @param label describes the type of data
     * @param data actual data
     */
    void output(int patientId, long timestamp, String label, String data);
}
