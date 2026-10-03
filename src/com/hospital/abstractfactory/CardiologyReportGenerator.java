package com.hospital.abstractfactory;

public class CardiologyReportGenerator implements ReportGenerator {
    @Override
    public String getReportStandard() {
        return "AHA/ACC Cardiovascular Clinical Standard";
    }

    @Override
    public String generateReport(String patientName, String findings) {
        return "[CARDIOLOGY REPORT - " + getReportStandard() + "]\n" +
               "Patient: " + patientName + "\n" +
               "Cardiac Rhythm / Hemodynamics: " + findings + "\n" +
               "Verified by Attending Cardiologist.";
    }
}
