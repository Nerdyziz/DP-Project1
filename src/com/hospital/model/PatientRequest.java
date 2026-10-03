package com.hospital.model;

public class PatientRequest {

    public enum RequestType {
        GENERAL_QUERY,
        VITALS_TRIAGE,
        PRIMARY_CARE,
        SPECIALIZED_CARE
    }

    private String id;
    private String patientName;
    private RequestType type;
    private String description;
    private String resolvedBy;
    private boolean resolved;

    public PatientRequest(String id, String patientName, RequestType type, String description) {
        this.id = id;
        this.patientName = patientName;
        this.type = type;
        this.description = description;
        this.resolved = false;
        this.resolvedBy = null;
    }

    public String getId() { return id; }
    public String getPatientName() { return patientName; }
    public RequestType getType() { return type; }
    public String getDescription() { return description; }
    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }
    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }

    @Override
    public String toString() {
        return "Request #" + id + " [" + type + "] from " + patientName + ": \"" + description + "\"";
    }
}
