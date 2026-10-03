package com.hospital.abstractfactory;

import java.util.List;

public class RadiologyConsentForm implements ConsentForm {
    @Override
    public String getFormTitle() {
        return "Radiation & Contrast Media Diagnostic Consent Form";
    }

    @Override
    public List<String> getRequiredDisclosures() {
        return List.of(
            "Disclosure of ionizing radiation risks",
            "Screening for iodine/gadolinium contrast allergies",
            "Negative pregnancy declaration"
        );
    }

    @Override
    public boolean isConsentValid(boolean patientAgreed) {
        return patientAgreed;
    }
}
