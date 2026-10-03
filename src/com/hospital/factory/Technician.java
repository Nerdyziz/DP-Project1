package com.hospital.factory;

public class Technician extends Staff {
    private String equipmentCertification;

    public Technician(String name, String specialization, String department, String equipmentCertification) {
        super(name, specialization, department);
        this.equipmentCertification = equipmentCertification;
    }

    @Override
    public String getRole() { return "TECHNICIAN"; }

    @Override
    public String getDuty() {
        return "Calibrates and operates lab and imaging equipment (MRI, X-Ray, Blood Analyzers).";
    }

    @Override
    public double calculateSalary() {
        return 4000.00;
    }

    public String getEquipmentCertification() { return equipmentCertification; }
}
