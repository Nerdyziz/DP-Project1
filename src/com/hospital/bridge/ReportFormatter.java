package com.hospital.bridge;

/**
 * =====================================================================
 * PATTERN 4: BRIDGE PATTERN (Structural)
 * =====================================================================
 * Meaningful Role: Decouples Report Abstractions (Lab Report,
 * Discharge Summary, Prescription) from Output Formats (PDF, HTML, JSON).
 * Without Bridge: 3 reports * 3 formats = 9 subclasses.
 * With Bridge: 3 report classes + 3 formatter classes = 6 classes total.
 * =====================================================================
 * Implementor Interface: ReportFormatter
 */
public interface ReportFormatter {
    String format(String title, String patientName, String date, String content);
    String getFormatType();
}
