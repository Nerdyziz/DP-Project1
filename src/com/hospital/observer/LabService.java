package com.hospital.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete Subject: LabService
 * Maintains a list of observers and notifies them when lab reports are ready.
 */
public class LabService implements Subject {

    private final List<Observer> observers = new ArrayList<>();
    private final List<String> recentNotifications = new ArrayList<>();

    @Override
    public void attach(Observer observer) {
        observers.add(observer);
        System.out.println("  [Observer Pattern] Subscribed observer: " + observer.getObserverName());
    }

    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
        System.out.println("  [Observer Pattern] Unsubscribed observer: " + observer.getObserverName());
    }

    @Override
    public void notifyObservers(String eventType, String message) {
        System.out.println("  [Subject: LabService] Notifying " + observers.size() + " registered observers for event: " + eventType);
        for (Observer o : observers) {
            o.update(eventType, message);
        }
    }

    // Business method that triggers the observer notification
    public List<String> completeLabReport(String patientName, String reportId) {
        System.out.println("\n  [LabService] Laboratory analysis complete for " + patientName + " (Report #" + reportId + ")");
        String message = "Report #" + reportId + " for " + patientName + " is ready for review.";
        notifyObservers("LAB_REPORT_READY", message);

        List<String> dispatched = new ArrayList<>();
        for (Observer o : observers) {
            if (o instanceof PatientObserver po && po.getLastNotification() != null) {
                dispatched.add(po.getLastNotification());
            } else if (o instanceof DoctorObserver dobs && dobs.getLastNotification() != null) {
                dispatched.add(dobs.getLastNotification());
            }
        }
        recentNotifications.addAll(dispatched);
        return dispatched;
    }

    public List<String> getRecentNotifications() {
        return new ArrayList<>(recentNotifications);
    }
}
