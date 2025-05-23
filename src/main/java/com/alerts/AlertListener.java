package com.alerts;
/**
 * Interface for components that need to be notified when alerts are triggered
 */
public interface AlertListener {
    /**
     * Called when an alert is triggered
     * @param alert the alert that was triggered
     */
    void onAlertTriggered(Alert alert);
}

