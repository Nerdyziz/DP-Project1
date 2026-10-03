package com.hospital.observer;

public class PatientObserver implements Observer {

    private String patientEmail;
    private String lastNotification;

    public PatientObserver(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    @Override
    public void update(String eventType, String message) {
        this.lastNotification = "SMS to " + patientEmail + ": " + message;
        System.out.println("    -> [PatientObserver] Alert sent to " + patientEmail + ": \"" + message + "\"");
    }

    public String getLastNotification() {
        return lastNotification;
    }

    @Override
    public String getObserverName() {
        return "Patient Notification Service (" + patientEmail + ")";
    }
}
