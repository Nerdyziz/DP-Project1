package com.hospital.observer;

/**
 * =====================================================================
 * PATTERN 6: OBSERVER PATTERN (Behavioral)
 * =====================================================================
 * Meaningful Role: Decoupled event notification system.
 * When a critical event occurs (e.g. Lab Report Ready, Emergency Vitals Alert),
 * multiple interested parties (Patient SMS gateway, Doctor Alert console,
 * Audit logger) are automatically notified without tight coupling.
 * =====================================================================
 * Observer Interface
 */
public interface Observer {
    void update(String eventType, String message);
    String getObserverName();
}
