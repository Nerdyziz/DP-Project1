package com.hospital.bridge;

public class LabReport extends Report {
    private String testName;
    private String resultValue;
    private String normalRange;

    public LabReport(ReportFormatter formatter, String patientName, String testName, String resultValue, String normalRange) {
        super(formatter, patientName);
        this.testName = testName;
        this.resultValue = resultValue;
        this.normalRange = normalRange;
    }

    @Override
    public String getTitle() {
        return "Clinical Laboratory Analysis Report";
    }

    @Override
    public String buildContent() {
        return "Test: " + testName + "\n" +
               "Result: " + resultValue + "\n" +
               "Reference Range: " + normalRange + "\n" +
               "Status: Completed by Central Pathology Lab.";
    }
}
