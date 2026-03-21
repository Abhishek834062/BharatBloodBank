package com.bharatbloodbank.enums;

public enum BloodComponent {
    RBC("Red Blood Cells", 42),
    PLASMA("Fresh Frozen Plasma", 365),
    PLATELETS("Platelets", 5);

    private final String displayName;
    private final int expiryDays;

    BloodComponent(String displayName, int expiryDays) {
        this.displayName = displayName;
        this.expiryDays = expiryDays;
    }

    public String getDisplayName() { return displayName; }
    public int getExpiryDays() { return expiryDays; }
}
