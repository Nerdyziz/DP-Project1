package com.hospital.abstractfactory;

import java.util.List;

/**
 * Abstract Product C
 */
public interface ConsentForm {
    String getFormTitle();
    List<String> getRequiredDisclosures();
    boolean isConsentValid(boolean patientAgreed);
}
