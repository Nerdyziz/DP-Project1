package com.hospital.bridge;

public class PdfFormatter implements ReportFormatter {
    @Override
    public String getFormatType() {
        return "PDF";
    }

    @Override
    public String format(String title, String patientName, String date, String content) {
        return "%PDF-1.7 [Hospital Medical Document]\n" +
               "==============================================================\n" +
               "                     " + title.toUpperCase() + "\n" +
               "==============================================================\n" +
               "Patient: " + patientName + " | Date: " + date + "\n" +
               "--------------------------------------------------------------\n" +
               content + "\n" +
               "--------------------------------------------------------------\n" +
               "%%EOF [Confidential Healthcare Document - Verified]";
    }
}
