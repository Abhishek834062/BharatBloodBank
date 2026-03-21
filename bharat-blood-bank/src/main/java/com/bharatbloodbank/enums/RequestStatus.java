package com.bharatbloodbank.enums;

public enum RequestStatus {
    PENDING,                     // Doctor submitted, bank not yet reviewed
    ACCEPTED,                    // Bank accepted the request
    EMERGENCY_PENDING_VERIFY,    // Bank needs to verify emergency claim
    EMERGENCY_VERIFIED,          // Bank confirmed it's a real emergency
    DONOR_CONTACT_PENDING,       // Bank is arranging fresh donation (non-emergency)
    FULFILLED,                   // Blood dispatched, request complete
    REJECTED,                    // Bank rejected the request
    CANCELLED                    // Doctor cancelled the request
}
