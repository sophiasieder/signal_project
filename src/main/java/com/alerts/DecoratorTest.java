package com.alerts;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

    public class DecoratorTest {
        @Test
        public void testPriorityDecoratorAddsTag() {
            Alert alert = new Alert("P1", "Problem", 123);
            AlertComponent decorated = new PriorityAlertDecorator(new BaseAlert(alert));
            assertTrue(decorated.getCondition().startsWith("[PRIORITY]"));
        }
    }



