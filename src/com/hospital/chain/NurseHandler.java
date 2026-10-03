package com.hospital.chain;

import com.hospital.model.PatientRequest;

public class NurseHandler extends RequestHandler {

    @Override
    public void handle(PatientRequest request) {
        if (request.getType() == PatientRequest.RequestType.VITALS_TRIAGE) {
            System.out.println("    [Nurse] RESOLVED: Vital signs taken (BP, Pulse, Temp), wound dressing completed.");
            request.setResolved(true);
            request.setResolvedBy("Nurse");
        } else {
            passToNext(request, "Nurse");
        }
    }
}
