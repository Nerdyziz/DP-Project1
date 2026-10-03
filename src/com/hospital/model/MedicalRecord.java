package com.hospital.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MedicalRecord {
    private String recordId;
    private String patientId;
    private String doctorId;
    private String diagnosis;
    private String prescription;
    private boolean confidential;
    private String timestamp;

    public MedicalRecord(String recordId, String patientId, String doctorId, String diagnosis, String prescription, boolean confidential) {
        this.recordId = recordId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.diagnosis = diagnosis;
        this.prescription = prescription;
        this.confidential = confidential;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getRecordId() { return recordId; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public String getDiagnosis() { return diagnosis; }
    public String getPrescription() { return prescription; }
    public boolean isConfidential() { return confidential; }
    public String getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "Record #" + recordId + " [Patient: " + patientId + ", Doctor: " + doctorId + ", Confidential: " + confidential + "]";
    }
}
