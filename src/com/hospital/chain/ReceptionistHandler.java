package com.hospital.chain;

import com.hospital.model.PatientRequest;

public class ReceptionistHandler extends RequestHandler {

    @Override
    public void handle(PatientRequest request) {
        if (request.getType() == PatientRequest.RequestType.GENERAL_QUERY) {
            System.out.println("    [Receptionist] RESOLVED: Handled appointment scheduling, visiting hours, and billing inquiry.");
            request.setResolved(true);
            request.setResolvedBy("Receptionist");
        } else {
            passToNext(request, "Receptionist");
        }
    }
}
