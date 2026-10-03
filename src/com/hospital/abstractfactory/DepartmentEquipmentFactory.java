package com.hospital.abstractfactory;

/**
 * =====================================================================
 * PATTERN 3: ABSTRACT FACTORY PATTERN (Creational)
 * =====================================================================
 * Meaningful Role: Each department needs an entire coherent FAMILY
 * of objects that change together:
 * - Diagnostic Tool
 * - Department Report Generator
 * - Legal Consent Form
 * Radiology cannot mix an MRI scan with a Cardiology consent form.
 * =====================================================================
 */
public interface DepartmentEquipmentFactory {
    DiagnosticTool createDiagnosticTool();
    ReportGenerator createReportGenerator();
    ConsentForm createConsentForm();
    String getDepartmentName();
}
