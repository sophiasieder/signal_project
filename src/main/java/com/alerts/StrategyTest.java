package com.alerts;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StrategyTest { @Test
public void testHeartRateStrategyHigh() {
    AlertStrategy2 strategy = new HeartRateStrategy();
    assertTrue(strategy.checkAlert(new double[]{120}));
}
}


