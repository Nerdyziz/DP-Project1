package com.hospital.singleton;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton Pattern: Hospital Central Configuration Registry
 */
public class HospitalConfig {

    private static HospitalConfig instance;
    private final Map<String, String> configs = new HashMap<>();

    private HospitalConfig() {
        configs.put("hospitalName", "City Central Hospital");
        configs.put("emergencyContact", "108 / 911");
        configs.put("standardConsultationFee", "$50.00");
        configs.put("maxDailyAppointments", "30");
    }

    public static synchronized HospitalConfig getInstance() {
        if (instance == null) {
            instance = new HospitalConfig();
        }
        return instance;
    }

    public String getConfig(String key) {
        return configs.getOrDefault(key, "N/A");
    }

    public void setConfig(String key, String value) {
        configs.put(key, value);
    }

    public Map<String, String> getAllConfigs() {
        return new HashMap<>(configs);
    }
}
