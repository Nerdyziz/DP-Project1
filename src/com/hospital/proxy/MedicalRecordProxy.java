package com.hospital.proxy;

import com.hospital.model.MedicalRecord;
import com.hospital.model.Role;
import com.hospital.model.User;
import com.hospital.singleton.AuditLogger;

/**
 * Protection Proxy in Proxy Pattern.
 * Controls access to RealMedicalRecordService and audits all attempts.
 */
public class MedicalRecordProxy implements MedicalRecordAccess {

    // Reference to the Real Subject
    private final RealMedicalRecordService realService;

    public MedicalRecordProxy() {
        this.realService = new RealMedicalRecordService();
    }

    @Override
    public MedicalRecord getRecord(String recordId, User requester) throws SecurityException {
        System.out.println("  [MedicalRecordProxy] Intercepting access request for Record: " + recordId + " by " + requester);

        // Security Policy Check
        boolean isAuthorized = checkAccessPolicy(recordId, requester);

        if (!isAuthorized) {
            // Log security denial to Singleton AuditLogger
            AuditLogger.getInstance().log(
                requester.getName(),
                "ACCESS_DENIED",
                recordId + " (Insufficient clearance: " + requester.getRole() + ")"
            );
            throw new SecurityException("ACCESS DENIED: User " + requester.getName() + 
                                       " (" + requester.getRole() + ") is NOT authorized to view Medical Record " + recordId + "!");
        }

        // Log authorized access to Singleton AuditLogger
        AuditLogger.getInstance().log(
            requester.getName(),
            "ACCESS_GRANTED",
            recordId + " (Verified: " + requester.getRole() + ")"
        );

        // Delegate to the Real Subject
        return realService.getRecord(recordId, requester);
    }

    private boolean checkAccessPolicy(String recordId, User requester) {
        if (requester == null) return false;

        Role role = requester.getRole();

        // 1. ADMIN has complete compliance access
        if (role == Role.ADMIN) return true;

        // 2. RECEPTIONIST is strictly barred from clinical medical records
        if (role == Role.RECEPTIONIST) return false;

        // 3. DOCTOR can access their assigned patient's records
        if (role == Role.DOCTOR) {
            if ("REC-101".equals(recordId) && requester.getId().equals("DOC-1")) return true;
            if ("REC-102".equals(recordId) && requester.getId().equals("DOC-2")) return true;
            return false; // Cross-patient unauthorized doctor access blocked
        }

        // 4. PATIENT can only access their own record
        if (role == Role.PATIENT) {
            if ("REC-101".equals(recordId) && requester.getId().equals("P-1")) return true;
            if ("REC-102".equals(recordId) && requester.getId().equals("P-2")) return true;
            return false;
        }

        return false;
    }
}
