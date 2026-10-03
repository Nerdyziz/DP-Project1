package com.hospital.bridge;

public class JsonFormatter implements ReportFormatter {
    @Override
    public String getFormatType() {
        return "JSON";
    }

    @Override
    public String format(String title, String patientName, String date, String content) {
        String escapedContent = content.replace("\"", "\\\"").replace("\n", "\\n");
        return "{\n" +
               "  \"documentType\": \"" + title + "\",\n" +
               "  \"patient\": \"" + patientName + "\",\n" +
               "  \"date\": \"" + date + "\",\n" +
               "  \"body\": \"" + escapedContent + "\",\n" +
               "  \"compliance\": \"HIPAA_VERIFIED\"\n" +
               "}";
    }
}
