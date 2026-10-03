package com.hospital.observer;

/**
 * Subject (Observable) Interface in Observer Pattern.
 */
public interface Subject {
    void attach(Observer observer);
    void detach(Observer observer);
    void notifyObservers(String eventType, String message);
}
