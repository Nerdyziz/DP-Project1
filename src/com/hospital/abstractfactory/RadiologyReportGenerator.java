package com.hospital.abstractfactory;

public class RadiologyReportGenerator implements ReportGenerator {
    @Override
    public String getReportStandard() {
        return "ACR-DICOM Imaging Standard";
    }

    @Override
    public String generateReport(String patientName, String findings) {
        return "[RADIOLOGY REPORT - " + getReportStandard() + "]\n" +
               "Patient: " + patientName + "\n" +
               "Imaging Findings: " + findings + "\n" +
               "Verified by Radiologist.";
    }
}
