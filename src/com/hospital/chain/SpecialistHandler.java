package com.hospital.chain;

import com.hospital.model.PatientRequest;

public class SpecialistHandler extends RequestHandler {

    @Override
    public void handle(PatientRequest request) {
        if (request.getType() == PatientRequest.RequestType.SPECIALIZED_CARE) {
            System.out.println("    [Specialist] RESOLVED: Complex cardiology/surgical assessment performed; specialized intervention scheduled.");
            request.setResolved(true);
            request.setResolvedBy("Specialist Doctor");
        } else {
            passToNext(request, "Specialist Doctor");
        }
    }
}
