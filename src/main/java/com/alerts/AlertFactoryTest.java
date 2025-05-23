package com.alerts;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class AlertFactoryTest {
    @Test
    public void testFactoryCreatesAlert() {
        AlertFactory factory = new BloodPressureAlertFactory();
        Alert alert = factory.createAlert("P1", "Too High", 123);
        assertEquals("P1", alert.getPatientId());
    }

}
