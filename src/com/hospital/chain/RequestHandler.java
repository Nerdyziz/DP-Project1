package com.hospital.chain;

import com.hospital.model.PatientRequest;

/**
 * =====================================================================
 * PATTERN 5: CHAIN OF RESPONSIBILITY PATTERN (Behavioral)
 * =====================================================================
 * Meaningful Role: Patient triage and request escalation pipeline.
 * Requests are passed along a chain of handlers:
 * Receptionist -> Nurse -> General Doctor -> Specialist.
 * Each handler processes what it is authorized for, or escalates.
 * =====================================================================
 * Handler Interface / Abstract Class
 */
public abstract class RequestHandler {

    protected RequestHandler nextHandler;

    public RequestHandler setNext(RequestHandler nextHandler) {
        this.nextHandler = nextHandler;
        return nextHandler; // Allows fluent chaining: h1.setNext(h2).setNext(h3)...
    }

    public abstract void handle(PatientRequest request);

    protected void passToNext(PatientRequest request, String currentHandlerName) {
        if (nextHandler != null) {
            System.out.println("    [" + currentHandlerName + "] Cannot resolve. Escalating request to -> " + nextHandler.getClass().getSimpleName());
            nextHandler.handle(request);
        } else {
            System.out.println("    [Chain End] No handler could resolve this request.");
            request.setResolved(false);
            request.setResolvedBy("UNRESOLVED");
        }
    }
}
