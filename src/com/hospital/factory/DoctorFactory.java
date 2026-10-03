package com.hospital.factory;

public class DoctorFactory extends StaffFactory {
    @Override
    public Staff createStaff(String name, String specialization, String department) {
        String license = "MD-LIC-" + (int)(Math.random() * 90000 + 10000);
        return new Doctor(name, specialization, department, license);
    }
}
