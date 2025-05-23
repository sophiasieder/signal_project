package com.alerts;

public class OxygenSaturationStrategy implements AlertStrategy2 {
    @Override
    public boolean checkAlert(double[] data) {
        //example: oxygen level < 92%
        double oxygenLevel = data[0];
        return oxygenLevel < 92;
    }
}

