package com.alerts;

public class HeartRateStrategy implements AlertStrategy2 {
        @Override
        public boolean checkAlert(double[] data) {
            // Beispiel: Herzrate außerhalb 60–100 bpm
            double heartRate = data[0];
            return heartRate < 60 || heartRate > 100;
        }
    }


