package com.hospital.bridge;

public class HtmlFormatter implements ReportFormatter {
    @Override
    public String getFormatType() {
        return "HTML";
    }

    @Override
    public String format(String title, String patientName, String date, String content) {
        return "<div class=\"hospital-report\">\n" +
               "  <h2>" + title + "</h2>\n" +
               "  <p><strong>Patient:</strong> " + patientName + " | <strong>Date:</strong> " + date + "</p>\n" +
               "  <div class=\"report-content\">\n    " + content.replace("\n", "<br/>\n    ") + "\n  </div>\n" +
               "  <footer>Official Medical Record</footer>\n" +
               "</div>";
    }
}
