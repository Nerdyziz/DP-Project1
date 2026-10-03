package com.hospital.abstractfactory;

public class XRayMachine implements DiagnosticTool {
    @Override
    public String getToolName() {
        return "Digital High-Definition X-Ray Scanner";
    }

    @Override
    public String runDiagnosticScan(String patientId) {
        return "Scan complete for patient " + patientId + ": Radiographic images captured at 120 kVp, ALARA compliant.";
    }
}
