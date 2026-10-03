package com.hospital.abstractfactory;

/**
 * Abstract Product A
 */
public interface DiagnosticTool {
    String getToolName();
    String runDiagnosticScan(String patientId);
}
