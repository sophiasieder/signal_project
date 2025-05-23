package com.alerts;

public class HeartStrategyTest implements AlertStrategy2 {
    @Override
    public boolean checkAlert(double[] data) {
        //example:heartrate outside 60–100 bpm
        double heartRate = data[0];
        return heartRate < 60 || heartRate > 100;
    }
}
