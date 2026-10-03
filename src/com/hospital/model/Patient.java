package com.hospital.model;

public class Patient {
    private String id;
    private String name;
    private int age;
    private String bloodGroup;
    private String allergies;

    public Patient(String id, String name, int age, String bloodGroup, String allergies) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.bloodGroup = bloodGroup;
        this.allergies = allergies;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getBloodGroup() { return bloodGroup; }
    public String getAllergies() { return allergies; }

    @Override
    public String toString() {
        return name + " [MRN: " + id + ", Blood: " + bloodGroup + ", Allergies: " + allergies + "]";
    }
}
