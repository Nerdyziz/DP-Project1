package com.hospital.factory;

/**
 * Abstract Creator in Factory Method Pattern.
 * Declares the factory method that returns a Staff object.
 */
public abstract class StaffFactory {

    // The Factory Method
    public abstract Staff createStaff(String name, String specialization, String department);

    // Common template operation
    public Staff registerStaff(String name, String specialization, String department) {
        Staff staff = createStaff(name, specialization, department);
        System.out.println("  [Factory Method] Successfully onboarded: " + staff);
        return staff;
    }
}
