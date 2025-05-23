package com.alerts;
import java.util.List;
import com.data_management.*;

//for different types of alert evaluations
public interface AlertStrategy {
    /**
     * evaluates patient data, returns alert
     */
    List <Alert> evaluateData(Patient patient, List<PatientRecord> records);

    //returns name of alert strategy
    String getStrategyName();
}
