package com.hospital.bridge;

public class PrescriptionReport extends Report {
    private String doctorName;
    private String medication;
    private String dosage;

    public PrescriptionReport(ReportFormatter formatter, String patientName, String doctorName, String medication, String dosage) {
        super(formatter, patientName);
        this.doctorName = doctorName;
        this.medication = medication;
        this.dosage = dosage;
    }

    @Override
    public String getTitle() {
        return "Official Outpatient Pharmacy Prescription";
    }

    @Override
    public String buildContent() {
        return "Prescribing Physician: Dr. " + doctorName + "\n" +
               "Medication: " + medication + "\n" +
               "Dosage & Instructions: " + dosage + "\n" +
               "Pharmacy Signature: Electronically authenticated.";
    }
}
