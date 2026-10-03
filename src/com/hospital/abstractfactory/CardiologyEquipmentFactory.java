package com.hospital.abstractfactory;

public class CardiologyEquipmentFactory implements DepartmentEquipmentFactory {
    @Override
    public DiagnosticTool createDiagnosticTool() {
        return new EcgMachine();
    }

    @Override
    public ReportGenerator createReportGenerator() {
        return new CardiologyReportGenerator();
    }

    @Override
    public ConsentForm createConsentForm() {
        return new CardiologyConsentForm();
    }

    @Override
    public String getDepartmentName() {
        return "CARDIOLOGY";
    }
}
