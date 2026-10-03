package com.hospital.factory;

public class TechnicianFactory extends StaffFactory {
    @Override
    public Staff createStaff(String name, String specialization, String department) {
        return new Technician(name, specialization, department, "Certified MLT-Level 2");
    }
}
