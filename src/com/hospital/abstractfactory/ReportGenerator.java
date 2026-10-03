package com.hospital.abstractfactory;

/**
 * Abstract Product B
 */
public interface ReportGenerator {
    String getReportStandard();
    String generateReport(String patientName, String findings);
}
