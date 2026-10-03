package com.hospital.factory;

public class StaffFactoryProvider {

    public static StaffFactory getFactory(String staffType) {
        if (staffType == null) {
            throw new IllegalArgumentException("Staff type cannot be null");
        }
        return switch (staffType.trim().toUpperCase()) {
            case "DOCTOR" -> new DoctorFactory();
            case "NURSE" -> new NurseFactory();
            case "TECHNICIAN" -> new TechnicianFactory();
            default -> throw new IllegalArgumentException("Unknown staff type: " + staffType);
        };
    }
}
