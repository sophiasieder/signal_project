package com.alerts;

    public class PriorityAlertDecorator extends AlertDecorator {
        public PriorityAlertDecorator(AlertComponent alert) {
            super(alert);
        }

        @Override
        public String getCondition() {
            return "[PRIORITY] " + wrappedAlert.getCondition();
        }
    }

