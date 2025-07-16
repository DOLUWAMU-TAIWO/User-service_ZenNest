package dev.dolu.userservice.models;

/**
 * Comprehensive visit duration options for property viewings
 * Covers different viewing scenarios and property types
 */
public enum VisitDuration {
    FIFTEEN_MINUTES(15, "15 minutes", "Quick walkthrough for studios/small apartments"),
    THIRTY_MINUTES(30, "30 minutes", "Standard viewing for most properties"),
    FORTY_FIVE_MINUTES(45, "45 minutes", "Extended viewing for larger properties"),
    ONE_HOUR(60, "1 hour", "Comprehensive viewing for luxury/commercial properties"),
    NINETY_MINUTES(90, "1.5 hours", "Detailed viewing for multi-unit/estate properties"),
    TWO_HOURS(120, "2 hours", "Extended viewing for commercial/investment properties");

    private final int minutes;
    private final String displayName;
    private final String description;

    VisitDuration(int minutes, String displayName, String description) {
        this.minutes = minutes;
        this.displayName = displayName;
        this.description = description;
    }

    public int getMinutes() {
        return minutes;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static VisitDuration getDefault() {
        return THIRTY_MINUTES;
    }
}

