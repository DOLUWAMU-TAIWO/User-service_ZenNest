package dev.dolu.userservice.models;

/**
 * Comprehensive onboarding features enum for ZenNest platform
 * Covers all aspects from dashboard to visit booking and shortlet management
 */
public enum OnboardingFeature {

    // ===== CORE DASHBOARD ONBOARDING =====
    WELCOME_MODAL_SEEN,              // User has seen the welcome modal
    BUSINESS_TYPE_SELECTED,          // User has selected their business type (shortlet/longterm/both)
    DASHBOARD_TOUR_COMPLETED,        // User has completed the main dashboard tour
    QUICK_START_COMPLETED,           // User has completed the quick start guide

    // ===== ACCOUNT SETUP =====
    VERIFY_EMAIL,
    VERIFY_PHONE,
    COMPLETE_PROFILE,
    SET_PREFERRED_LANGUAGE,
    SET_PREFERRED_CURRENCY,
    SET_NOTIFICATION_PREFERENCES,
    ADD_PAYMENT_METHOD,
    COMPLETE_PAYMENT_SETUP,
    FEEDBACK_SURVEY,

    // ===== PROPERTY MANAGEMENT =====
    CREATE_FIRST_LISTING,            // First property created
    CREATE_FIRST_LEASE,              // First lease agreement created

    // ===== VISIT BOOKING FEATURES (Desktop & Mobile) =====
    VISIT_BOOKING_INTRO,             // Seen visit booking introduction
    VISIT_BOOKING_TOUR_COMPLETED,    // User has completed the booking system tour
    VISIT_TOUR_COMPLETED,            // User has completed the visit management tour
    REVENUE_TOUR_COMPLETED,          // User has completed the revenue/earnings tour
    DIGITAL_CONTRACT_TOUR_COMPLETED, // User has completed the digital contract tour
    CREATE_FIRST_VISIT_SLOT,         // Created first availability slot
    VIEW_VISIT_CALENDAR,             // Navigated the calendar view
    MANAGE_MULTIPLE_SLOTS,           // Created multiple slots in one session
    HANDLE_VISIT_BOOKING,            // Managed a visit booking request
    SET_AVAILABLE_SLOTS,             // Set availability slots
    SET_AVAILABLE_DATES,             // Set available dates

    // ===== SHORTLET BOOKING MANAGEMENT =====
    SHORTLET_BOOKING_INTRO,          // Seen shortlet booking management intro
    CREATE_FIRST_AVAILABILITY,       // Created first availability range
    USE_CALENDAR_RANGE_SELECT,       // Used calendar range selection
    SET_MONTH_AVAILABILITY,          // Used month availability feature
    HANDLE_FIRST_BOOKING,            // Handled first booking request
    USE_BOOKING_FILTERS,             // Used search and filtering tools
    EXPORT_BOOKING_DATA,             // Used export functionality
    USE_BULK_BOOKING_ACTIONS,        // Used bulk actions on bookings
    VIEW_BOOKING_STATISTICS,         // Viewed booking statistics dashboard
    MANAGE_AVAILABILITY_RANGES       // Managed availability ranges (edit/delete)
}