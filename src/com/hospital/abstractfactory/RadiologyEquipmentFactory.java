package com.hospital.abstractfactory;

public class RadiologyEquipmentFactory implements DepartmentEquipmentFactory {
    @Override
    public DiagnosticTool createDiagnosticTool() {
        return new XRayMachine();
    }

    @Override
    public ReportGenerator createReportGenerator() {
        return new RadiologyReportGenerator();
    }

    @Override
    public ConsentForm createConsentForm() {
        return new RadiologyConsentForm();
    }

    @Override
    public String getDepartmentName() {
        return "RADIOLOGY";
    }
}
