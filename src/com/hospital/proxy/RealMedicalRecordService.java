package com.hospital.proxy;

import com.hospital.model.MedicalRecord;
import com.hospital.model.User;
import java.util.HashMap;
import java.util.Map;

/**
 * Real Subject in Proxy Pattern.
 * Fetches actual records from storage.
 */
public class RealMedicalRecordService implements MedicalRecordAccess {

    private final Map<String, MedicalRecord> recordsDb = new HashMap<>();

    public RealMedicalRecordService() {
        // Pre-populate sample patient records
        recordsDb.put("REC-101", new MedicalRecord(
            "REC-101", "P-1 (Alice)", "DOC-1 (Dr. Smith)",
            "Acute Coronary Syndrome - Controlled. Normal sinus rhythm.",
            "Aspirin 81mg daily, Metoprolol 25mg BID.",
            true
        ));
        recordsDb.put("REC-102", new MedicalRecord(
            "REC-102", "P-2 (Bob)", "DOC-2 (Dr. Jones)",
            "Lumbar spine disc herniation at L4-L5 with mild nerve compression.",
            "Ibuprofen 400mg TID with food, Physical therapy 2x/week.",
            true
        ));
    }

    @Override
    public MedicalRecord getRecord(String recordId, User requester) {
        System.out.println("    [RealMedicalRecordService] Fetching record " + recordId + " from internal database...");
        return recordsDb.get(recordId);
    }
}
