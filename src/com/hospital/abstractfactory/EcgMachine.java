package com.hospital.abstractfactory;

public class EcgMachine implements DiagnosticTool {
    @Override
    public String getToolName() {
        return "12-Lead Diagnostic Electrocardiograph (ECG)";
    }

    @Override
    public String runDiagnosticScan(String patientId) {
        return "Scan complete for patient " + patientId + ": Normal Sinus Rhythm, HR 72 bpm, QTc 410 ms.";
    }
}
