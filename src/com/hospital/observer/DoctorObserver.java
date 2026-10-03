package com.hospital.observer;

public class DoctorObserver implements Observer {

    private String doctorName;
    private String lastNotification;

    public DoctorObserver(String doctorName) {
        this.doctorName = doctorName;
    }

    @Override
    public void update(String eventType, String message) {
        this.lastNotification = "Beeper Alert to Dr. " + doctorName + ": " + message;
        System.out.println("    -> [DoctorObserver] Urgent Beeper/Console alert to Dr. " + doctorName + ": \"" + message + "\"");
    }

    public String getLastNotification() {
        return lastNotification;
    }

    @Override
    public String getObserverName() {
        return "Doctor Clinical Alert Console (Dr. " + doctorName + ")";
    }
}
