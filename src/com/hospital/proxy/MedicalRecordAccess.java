package com.hospital.proxy;

import com.hospital.model.MedicalRecord;
import com.hospital.model.User;

/**
 * =====================================================================
 * PATTERN 7: PROXY PATTERN (Structural - Protection Proxy)
 * =====================================================================
 * Meaningful Role: Protection Proxy for electronic medical records (EHR).
 * Directly accessing RealMedicalRecordService is prevented.
 * The MedicalRecordProxy transparently verifies the caller's role,
 * blocks unauthorized access, and records all attempts in the AuditLogger.
 * =====================================================================
 * Subject Interface
 */
public interface MedicalRecordAccess {
    MedicalRecord getRecord(String recordId, User requester) throws SecurityException;
}
