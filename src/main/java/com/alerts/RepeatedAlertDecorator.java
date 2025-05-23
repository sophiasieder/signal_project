package com.alerts;

    public class RepeatedAlertDecorator extends AlertDecorator {
        public RepeatedAlertDecorator(AlertComponent alert) {
            super(alert);
        }

        @Override
        public String getCondition() {
            return wrappedAlert.getCondition() + " [REPEATED CHECK]";
        }
    }



