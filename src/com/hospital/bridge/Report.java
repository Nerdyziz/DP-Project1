package com.hospital.bridge;

import java.time.LocalDate;

/**
 * The Abstraction in the Bridge Pattern.
 * Holds a reference to the Implementor (ReportFormatter).
 */
public abstract class Report {

    // The Bridge: reference to the implementor
    protected ReportFormatter formatter;
    protected String patientName;
    protected String date;

    public Report(ReportFormatter formatter, String patientName) {
        this.formatter = formatter;
        this.patientName = patientName;
        this.date = LocalDate.now().toString();
    }

    // Dynamic switching of formatters via Bridge
    public void setFormatter(ReportFormatter formatter) {
        this.formatter = formatter;
    }

    public ReportFormatter getFormatter() {
        return formatter;
    }

    public abstract String getTitle();
    public abstract String buildContent();

    // Delegates to the implementor
    public String generate() {
        return formatter.format(getTitle(), patientName, date, buildContent());
    }
}
