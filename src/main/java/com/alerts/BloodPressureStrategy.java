package com.alerts;

public class BloodPressureStrategy implements AlertStrategy2 {
    @Override
    public boolean checkAlert(double[] data) {
        //example: systolic > 140 or diastolic > 90 (easier)
        double systolic = data[0];
        double diastolic = data[1];
        return systolic > 140 || diastolic > 90;
    }

}
