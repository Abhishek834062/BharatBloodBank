package com.bharatbloodbank.enums;

public enum BloodUnitStatus {
    AVAILABLE,  // In inventory, ready to use
    USED,       // Dispatched/given to a patient
    EXPIRED     // Past expiry date (auto-flagged by scheduler then removed)
}
