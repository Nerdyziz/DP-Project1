package com.hospital.singleton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * =====================================================================
 * PATTERN 1: SINGLETON PATTERN (Creational)
 * =====================================================================
 * Meaningful Role: Centralized, thread-safe audit logging for hospital security.
 * Every sensitive operation (medical record read, security check, access denial)
 * is recorded through this single global instance.
 *
 * Key Elements:
 * 1. Private static volatile instance (Double-Checked Locking)
 * 2. Private constructor (prevents instantiation from other classes)
 * 3. Public static synchronized / DCL getInstance() method
 * =====================================================================
 */
public class AuditLogger {

    // 1. Volatile variable ensures changes are immediately visible to all threads
    private static volatile AuditLogger instance;

    // In-memory list to store audit log entries
    private final List<String> logEntries = new ArrayList<>();

    // 2. Private constructor prevents 'new AuditLogger()'
    private AuditLogger() {
        System.out.println("  [Singleton] Initializing AuditLogger instance (HashCode: " + System.identityHashCode(this) + ")");
    }

    // 3. Double-Checked Locking (DCL) for high performance and thread safety
    public static AuditLogger getInstance() {
        if (instance == null) {
            synchronized (AuditLogger.class) {
                if (instance == null) {
                    instance = new AuditLogger();
                }
            }
        }
        return instance;
    }

    public synchronized void log(String userId, String action, String resource) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String entry = String.format("[%s] USER: %-15s | ACTION: %-15s | RESOURCE: %s", timestamp, userId, action, resource);
        logEntries.add(entry);
        System.out.println("  [AuditLog] " + entry);
    }

    public List<String> getLogEntries() {
        return Collections.unmodifiableList(logEntries);
    }
}
