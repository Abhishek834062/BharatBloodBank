package com.bharatbloodbank.enums;

public enum BloodGroup {
    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O-"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-");

    private final String displayName;

    BloodGroup(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static BloodGroup fromDisplayName(String displayName) {
        for (BloodGroup bg : values()) {
            if (bg.displayName.equalsIgnoreCase(displayName)) {
                return bg;
            }
        }
        throw new IllegalArgumentException("Unknown blood group: " + displayName);
    }
}
