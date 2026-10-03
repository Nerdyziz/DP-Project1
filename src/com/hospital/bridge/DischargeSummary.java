package com.hospital.bridge;

public class DischargeSummary extends Report {
    private String admissionDiagnosis;
    private String dischargeDiagnosis;
    private String instructions;

    public DischargeSummary(ReportFormatter formatter, String patientName, String admissionDiagnosis, String dischargeDiagnosis, String instructions) {
        super(formatter, patientName);
        this.admissionDiagnosis = admissionDiagnosis;
        this.dischargeDiagnosis = dischargeDiagnosis;
        this.instructions = instructions;
    }

    @Override
    public String getTitle() {
        return "Patient Inpatient Discharge Summary";
    }

    @Override
    public String buildContent() {
        return "Admitting Diagnosis: " + admissionDiagnosis + "\n" +
               "Discharge Diagnosis: " + dischargeDiagnosis + "\n" +
               "Discharge Directives: " + instructions + "\n" +
               "Follow-up: In-person clinic visit in 10 days.";
    }
}
