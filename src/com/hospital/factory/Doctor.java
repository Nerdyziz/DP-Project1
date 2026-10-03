package com.hospital.factory;

public class Doctor extends Staff {
    private String medicalLicense;

    public Doctor(String name, String specialization, String department, String medicalLicense) {
        super(name, specialization, department);
        this.medicalLicense = medicalLicense;
    }

    @Override
    public String getRole() { return "DOCTOR"; }

    @Override
    public String getDuty() {
        return "Examines patients, performs diagnoses, and prescribes treatments.";
    }

    @Override
    public double calculateSalary() {
        return 8500.00;
    }

    public String getMedicalLicense() { return medicalLicense; }
}
