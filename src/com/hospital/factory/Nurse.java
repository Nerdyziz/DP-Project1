package com.hospital.factory;

public class Nurse extends Staff {
    private String wardAssignment;

    public Nurse(String name, String specialization, String department, String wardAssignment) {
        super(name, specialization, department);
        this.wardAssignment = wardAssignment;
    }

    @Override
    public String getRole() { return "NURSE"; }

    @Override
    public String getDuty() {
        return "Monitors patient vitals, administers medication, and assists in ward care.";
    }

    @Override
    public double calculateSalary() {
        return 4500.00;
    }

    public String getWardAssignment() { return wardAssignment; }
}
