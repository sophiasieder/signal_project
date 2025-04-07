package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
*interface to identify the structure for generating patient-specific health data
 */

public interface PatientDataGenerator {
    /**
     * generates and specifies health data for a specific patient
     * @param patientId takes the ID of the patient that is being looked at
     * @param outputStrategy how the data is being stored/where it goes (console, file etc.)
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
