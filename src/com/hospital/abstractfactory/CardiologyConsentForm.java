package com.hospital.abstractfactory;

import java.util.List;

public class CardiologyConsentForm implements ConsentForm {
    @Override
    public String getFormTitle() {
        return "Cardiac Stress Testing & Defibrillation Protocol Consent Form";
    }

    @Override
    public List<String> getRequiredDisclosures() {
        return List.of(
            "Acknowledgment of exertion-induced arrhythmia risks",
            "Emergency defibrillation standby protocol confirmed",
            "Caffeine withholding verification (12 hours prior)"
        );
    }

    @Override
    public boolean isConsentValid(boolean patientAgreed) {
        return patientAgreed;
    }
}
