package com.hospital.factory;

/**
 * =====================================================================
 * PATTERN 2: FACTORY METHOD PATTERN (Creational)
 * =====================================================================
 * Meaningful Role: Hospital staff onboarding involves polymorphic
 * creation. A Doctor has a medical license & diagnosis duties;
 * a Nurse has a ward assignment; a Technician has equipment certification.
 * =====================================================================
 * Abstract Product: Staff
 */
public abstract class Staff {
    protected String name;
    protected String specialization;
    protected String department;

    public Staff(String name, String specialization, String department) {
        this.name = name;
        this.specialization = specialization;
        this.department = department;
    }

    public abstract String getRole();
    public abstract String getDuty();
    public abstract double calculateSalary();

    public String getName() { return name; }
    public String getSpecialization() { return specialization; }
    public String getDepartment() { return department; }

    @Override
    public String toString() {
        return getRole() + ": " + name + " | Dept: " + department + " | Specialization: " + specialization + " | Monthly Salary: $" + calculateSalary();
    }
}
