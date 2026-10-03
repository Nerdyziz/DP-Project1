package com.hospital.factory;

public class NurseFactory extends StaffFactory {
    @Override
    public Staff createStaff(String name, String specialization, String department) {
        return new Nurse(name, specialization, department, "Ward 3B");
    }
}
