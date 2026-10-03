package com.hospital.model;

public class User {
    private String id;
    private String name;
    private String email;
    private Role role;

    public User(String id, String name, String email, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }

    @Override
    public String toString() {
        return name + " (" + role + ", ID: " + id + ")";
    }
}
