package com.hospital.chain;

import com.hospital.model.PatientRequest;

public class DoctorHandler extends RequestHandler {

    @Override
    public void handle(PatientRequest request) {
        if (request.getType() == PatientRequest.RequestType.PRIMARY_CARE) {
            System.out.println("    [General Doctor] RESOLVED: Diagnosed routine condition, prescribed oral medications.");
            request.setResolved(true);
            request.setResolvedBy("General Doctor");
        } else {
            passToNext(request, "General Doctor");
        }
    }
}
