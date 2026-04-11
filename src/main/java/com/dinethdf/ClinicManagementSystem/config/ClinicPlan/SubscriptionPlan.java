//package com.dinethdf.ClinicManagementSystem.config.ClinicPlan;
//
//import lombok.Getter;
//import java.util.Set;
//
//// ============================================
//// PLAN ENUM (Single source of truth)
//// ============================================
//@Getter
//public enum SubscriptionPlan {
//
//    PLAN_BASIC(
//            "Basic Plan",
//            // Features
//            Set.of(
//                    "FEAT_APPOINTMENTS",
//                    "FEAT_PATIENT_RECORDS",
//                    "FEAT_PRESCRIPTION",
//                    "FEAT_REPORTS_BASIC",
//                    "FEAT_REPORTS_EXPORT_PDF",
//                    "FEAT_EMAIL_NOTIFICATIONS",
//                    "FEAT_SMS_NOTIFICATIONS"
//            ),
//            // Limits
//            5,      // maxDoctors
//            1000,   // maxPatients
//            50,     // maxSmsMonthly
//            500     // maxEmailMonthly
//    ),
//
//    PLAN_PRO(
//            "Professional Plan",
//            Set.of(
//                    // All BASIC features
//                    "FEAT_APPOINTMENTS",
//                    "FEAT_PATIENT_RECORDS",
//                    "FEAT_PRESCRIPTION",
//                    "FEAT_REPORTS_BASIC",
//                    "FEAT_REPORTS_EXPORT_PDF",
//                    "FEAT_EMAIL_NOTIFICATIONS",
//                    "FEAT_SMS_NOTIFICATIONS",
//
//                    // PRO additions
//                    "FEAT_BILLING",
//                    "FEAT_INVENTORY",
//                    "FEAT_REPORTS_FINANCIAL",
//                    "FEAT_REPORTS_ADVANCED",
//                    "FEAT_REPORTS_EXPORT_EXCEL",
//                    "FEAT_REPORTS_DASHBOARD",
//                    "FEAT_WHATSAPP_INTEGRATION",
//                    "FEAT_APPOINTMENT_REMINDERS",
//                    "FEAT_PATIENT_PORTAL",
//                    "FEAT_PAYMENT_GATEWAY"
//            ),
//            20,     // maxDoctors
//            10000,  // maxPatients
//            500,    // maxSmsMonthly
//            2000    // maxEmailMonthly
//    ),
//
//    PLAN_ENTERPRISE(
//            "Enterprise Plan",
//            Set.of(
//                    // All features
//                    "FEAT_APPOINTMENTS",
//                    "FEAT_PATIENT_RECORDS",
//                    "FEAT_PRESCRIPTION",
//                    "FEAT_BILLING",
//                    "FEAT_INVENTORY",
//                    "FEAT_REPORTS_BASIC",
//                    "FEAT_REPORTS_FINANCIAL",
//                    "FEAT_REPORTS_ADVANCED",
//                    "FEAT_REPORTS_CUSTOM",
//                    "FEAT_REPORTS_EXPORT_PDF",
//                    "FEAT_REPORTS_EXPORT_EXCEL",
//                    "FEAT_REPORTS_SCHEDULED",
//                    "FEAT_REPORTS_DASHBOARD",
//                    "FEAT_EMAIL_NOTIFICATIONS",
//                    "FEAT_SMS_NOTIFICATIONS",
//                    "FEAT_WHATSAPP_INTEGRATION",
//                    "FEAT_APPOINTMENT_REMINDERS",
//                    "FEAT_PATIENT_PORTAL",
//                    "FEAT_API_ACCESS",
//                    "FEAT_THIRD_PARTY_LAB",
//                    "FEAT_PAYMENT_GATEWAY",
//                    "FEAT_MULTI_LOCATION",
//                    "FEAT_TELEMEDICINE",
//                    "FEAT_AI_ASSISTANCE"
//            ),
//            100,    // maxDoctors
//            100000, // maxPatients
//            5000,   // maxSmsMonthly
//            10000   // maxEmailMonthly
//    );
//
//    private final String displayName;
//    private final Set<String> features;
//    private final int maxDoctors;
//    private final int maxPatients;
//    private final int maxSmsMonthly;
//    private final int maxEmailMonthly;
//
//    SubscriptionPlan(String displayName, Set<String> features,
//                     int maxDoctors, int maxPatients,
//                     int maxSmsMonthly, int maxEmailMonthly) {
//        this.displayName = displayName;
//        this.features = features;
//        this.maxDoctors = maxDoctors;
//        this.maxPatients = maxPatients;
//        this.maxSmsMonthly = maxSmsMonthly;
//        this.maxEmailMonthly = maxEmailMonthly;
//    }
//
//    public boolean hasFeature(String featureCode) {
//        return features.contains(featureCode);
//    }
//
//    public static SubscriptionPlan fromCode(String code) {
//        try {
//            return SubscriptionPlan.valueOf(code);
//        } catch (IllegalArgumentException e) {
//            return PLAN_BASIC; // Default fallback
//        }
//    }
//}