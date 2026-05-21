package com.dinethdf.ClinicManagementSystem.Service.Appointment;

import com.dinethdf.ClinicManagementSystem.DTO.Appointment.AppointmentFullDetailsDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Clinic.ClinicResponseDTO;
import com.dinethdf.ClinicManagementSystem.Repo.Appointment.AppointmentRepo;
import com.dinethdf.ClinicManagementSystem.Service.Clinic.ClinicService;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import com.dinethdf.ClinicManagementSystem.util.EmailService;
import com.dinethdf.ClinicManagementSystem.util.SmsService;
import com.dinethdf.ClinicManagementSystem.Model.Appointment.Appointment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AppointmentNotificationService {

    @Autowired
    private AppointmentRepo appointmentRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private ClinicService clinicService;

    @Async("emailTaskExecutor")
    @Transactional(readOnly = true)
    public void sendBookingConfirmation(Long appointmentId, String recipientEmail, Integer clinicId) {

        // Set tenant inside this thread — safe because clinicId passed as parameter
        TenantContext.setCurrentTenant(clinicId);

        try {
            // 1. Fetch details
            AppointmentFullDetailsDTO details = appointmentRepo
                    .findAppointmentFullDetails(appointmentId, clinicId)
                    .orElse(null);

            if (details == null) {
                System.err.println("Error: Could not find appointment details for: " + appointmentId);
                return;
            }

            // 2. Calculate Fees
            BigDecimal doctorFee = details.getDoctorFee() != null ? details.getDoctorFee() : BigDecimal.ZERO;
            BigDecimal clinicFee = details.getClinicFee() != null ? details.getClinicFee() : BigDecimal.ZERO;
            BigDecimal totalFee = doctorFee.add(clinicFee);

            // 3. Format Date/Time
            String formattedDate = details.getScheduleDate().toString();
            String formattedTime = details.getStartTime() + " - " + details.getEndTime();

            // 4. Generate HTML
            String emailBody = generateHtmlTemplate(details, formattedDate, formattedTime, doctorFee, clinicFee, totalFee);

            // 5. Send — already in background thread, call sync version directly
            emailService.sendHtmlEmailSync(recipientEmail, "Booking Confirmation - CureGuard", emailBody);

            System.out.println("Confirmation email sent to " + recipientEmail);

        } finally {
            TenantContext.clear(); // ✅ Always clear tenant after done
        }
    }

    @Async("emailTaskExecutor")
    @Transactional(readOnly = true)
    public void sendBookingSms(Long appointmentId, String mobileNo, Integer clinicId) {
        if (mobileNo == null || mobileNo.isEmpty()) return;

        TenantContext.setCurrentTenant(clinicId);
        try {
            // 1. Fetch cached clinic details — reads from clinicDetailsCache, no DB hit
            ClinicResponseDTO clinic = clinicService.getCurrentClinicDetails();
            if (clinic == null) return;

            String gateway = clinic.getAppointmentBookedSmsGateway() != null
                    ? clinic.getAppointmentBookedSmsGateway()
                    : SmsService.SMS_GATEWAY;

            if (SmsService.SMS_NONE.equalsIgnoreCase(gateway)) {
                return;
            }

            // 2. Fetch appointment details
            AppointmentFullDetailsDTO details = appointmentRepo
                    .findAppointmentFullDetails(appointmentId, clinicId)
                    .orElse(null);

            if (details == null) {
                System.err.println("Error: Could not find appointment details for SMS: " + appointmentId);
                return;
            }

            // 3. Format SMS message
            String message = String.format(
                    "CureGuard: Your appointment is booked. Ref #%d. Dr. %s on %s at %s. Queue #%d.",
                    details.getId(),
                    details.getDoctorName(),
                    details.getScheduleDate().toString(),
                    details.getStartTime(),
                    details.getQueuePosition()
            );

            // 4. Send SMS via dynamic gateway
            smsService.sendDynamicSms(mobileNo, message, gateway);
            System.out.println("Booking SMS sent to " + mobileNo);

        } finally {
            TenantContext.clear();
        }
    }

    @Async("emailTaskExecutor")
    @Transactional(readOnly = true)
    public void sendCancellationNotification(Long appointmentId, String recipientEmail, Integer clinicId) {

        TenantContext.setCurrentTenant(clinicId);
        try {
            AppointmentFullDetailsDTO details = appointmentRepo
                    .findAppointmentFullDetails(appointmentId, clinicId)
                    .orElse(null);

            if (details == null) {
                System.err.println("Cancellation email error: appointment not found: " + appointmentId);
                return;
            }

            String emailBody = generateCancellationHtml(details);
            emailService.sendHtmlEmailSync(recipientEmail, "Appointment Cancelled - CureGuard", emailBody);
            System.out.println("Cancellation email sent to " + recipientEmail);

        } finally {
            TenantContext.clear();
        }
    }

    @Async("emailTaskExecutor")
    @Transactional(readOnly = true)
    public void sendCancellationSms(Long appointmentId, String mobileNo, Integer clinicId) {
        if (mobileNo == null || mobileNo.isEmpty()) return;

        TenantContext.setCurrentTenant(clinicId);
        try {
            ClinicResponseDTO clinic = clinicService.getCurrentClinicDetails();
            if (clinic == null) return;

            // Re-use appointmentConfirmSmsGateway for cancellation notices
            String gateway = clinic.getAppointmentConfirmSmsGateway() != null
                    ? clinic.getAppointmentConfirmSmsGateway()
                    : SmsService.SMS_GATEWAY;

            if (SmsService.SMS_NONE.equalsIgnoreCase(gateway)) return;

            AppointmentFullDetailsDTO details = appointmentRepo
                    .findAppointmentFullDetails(appointmentId, clinicId)
                    .orElse(null);

            if (details == null) {
                System.err.println("Cancellation SMS error: appointment not found: " + appointmentId);
                return;
            }

            String message = String.format(
                    "CureGuard: Your appointment Ref #%d with Dr. %s on %s has been CANCELLED. Contact us for rebooking.",
                    details.getId(),
                    details.getDoctorName(),
                    details.getScheduleDate().toString()
            );

            smsService.sendDynamicSms(mobileNo, message, gateway);
            System.out.println("Cancellation SMS sent to " + mobileNo);

        } finally {
            TenantContext.clear();
        }
    }

    private String generateCancellationHtml(AppointmentFullDetailsDTO details) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }
                    .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; padding: 40px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .header { text-align: center; margin-bottom: 30px; }
                    .header h1 { color: #003366; margin: 0; font-size: 28px; }
                    .contact-info { text-align: center; color: #666; font-size: 12px; margin-top: 10px; }
                    .cancelled-badge { text-align: center; margin: 20px 0; }
                    .cancelled-badge span { background-color: #ffe0e0; color: #c0392b; font-size: 18px; font-weight: bold; padding: 10px 24px; border-radius: 20px; display: inline-block; }
                    .details-box { background-color: #f9f9f9; border-left: 4px solid #c0392b; padding: 20px; border-radius: 4px; margin: 20px 0; }
                    .details-box p { margin: 6px 0; color: #333; font-size: 14px; }
                    .details-box strong { color: #003366; }
                    .footer { margin-top: 40px; font-size: 12px; color: #888; text-align: center; line-height: 1.6; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <img src="https://cdn-icons-png.flaticon.com/512/3063/3063176.png" width="50" alt="Logo" style="margin-bottom: 10px;">
                        <h1>CureGuard Medical Center</h1>
                        <div class="contact-info">
                            \ud83d\udccd 123 Pelmadulla, Rathnapura City &nbsp;|&nbsp; \ud83d\udcde +94 452 274 222 &nbsp;|&nbsp; \u2709\ufe0f support@cureguard.com
                        </div>
                        <hr style="border: 0; border-top: 2px solid #aecfea; margin-top: 20px;">
                    </div>
                    <div class="cancelled-badge">
                        <span>\u274c Appointment Cancelled</span>
                    </div>
                    <p style="text-align:center; color:#555; font-size:15px;">
                        We're sorry to inform you that the following appointment has been <strong>cancelled</strong>.
                    </p>
                    <div class="details-box">
                        <p><strong>Reference ID:</strong> #%d</p>
                        <p><strong>Patient:</strong> %s</p>
                        <p><strong>Doctor:</strong> %s</p>
                        <p><strong>Specialization:</strong> %s</p>
                        <p><strong>Date:</strong> %s</p>
                        <p><strong>Time:</strong> %s - %s</p>
                        <p><strong>Queue No:</strong> #%d</p>
                    </div>
                    <p style="text-align:center; color:#555; font-size:14px;">
                        If you believe this is an error or would like to rebook, please contact us directly.
                    </p>
                    <div class="footer">
                        <p>Thank you for trusting CureGuard with your health.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                details.getId(),
                details.getPatientName(),
                details.getDoctorName(),
                details.getSpecialization(),
                details.getScheduleDate().toString(),
                details.getStartTime(),
                details.getEndTime(),
                details.getQueuePosition()
        );
    }

    private String generateHtmlTemplate(AppointmentFullDetailsDTO details, String date, String time,
                                        BigDecimal doctorFee, BigDecimal clinicFee, BigDecimal totalFee) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }
                    .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; padding: 40px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .header { text-align: center; margin-bottom: 30px; }
                    .header h1 { color: #003366; margin: 0; font-size: 28px; letter-spacing: 1px; }
                    .contact-info { text-align: center; color: #666; font-size: 12px; margin-top: 10px; }
                    .details-grid { width: 100%%; margin-bottom: 30px; }
                    .details-grid td { vertical-align: top; padding-bottom: 10px; }
                    .label { font-weight: bold; color: #003366; font-size: 14px; margin-bottom: 5px; display: block;}
                    .value { color: #333; font-size: 14px; line-height: 1.5; }
                    .section-title { color: #003366; font-size: 18px; font-weight: bold; text-transform: uppercase; text-align: right; margin-bottom: 15px;}
                    .fee-table { width: 100%%; border-collapse: collapse; margin-top: 20px; }
                    .fee-table th { background-color: #0f4c81; color: #ffffff; padding: 12px; text-align: left; font-size: 14px; }
                    .fee-table td { padding: 12px; border-bottom: 1px solid #eee; color: #333; font-size: 14px; }
                    .total-row td { background-color: #eef6ff; font-weight: bold; color: #003366; border-top: 2px solid #0f4c81; }
                    .footer { margin-top: 40px; font-size: 12px; color: #888; text-align: center; line-height: 1.6; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <img src="https://cdn-icons-png.flaticon.com/512/3063/3063176.png" width="50" alt="Logo" style="margin-bottom: 10px;">
                        <h1>CureGuard Medical Center</h1>
                        <div class="contact-info">
                            📍 123 Pelmadulla, Rathnapura City &nbsp;|&nbsp; 📞 +94 452 274 222 &nbsp;|&nbsp; ✉️ support@cureguard.com
                        </div>
                        <hr style="border: 0; border-top: 2px solid #aecfea; margin-top: 20px;">
                    </div>
                    <table class="details-grid">
                        <tr>
                            <td width="60%%">
                                <span class="label">APPOINTMENT DETAILS</span>
                                <div class="value">
                                    <strong>Date:</strong> %s<br>
                                    <strong>Time:</strong> %s<br>
                                    <strong>Queue No:</strong> <span style="background-color: #eef6ff; padding: 2px 6px; border-radius: 4px; color: #003366; font-weight:bold;">#%d</span><br>
                                    <strong>Doctor:</strong> %s<br>
                                    <strong>Specialization:</strong> %s
                                </div>
                            </td>
                            <td width="40%%" style="text-align: right;">
                                <div class="section-title">BOOKING CONFIRMED</div>
                                <div class="value">
                                    <strong>Ref ID:</strong> #%d<br>
                                    <strong>Patient:</strong> %s<br>
                                    <strong>Status:</strong> <span style="color: green;">Confirmed</span>
                                </div>
                            </td>
                        </tr>
                    </table>
                    <table class="fee-table">
                        <thead>
                            <tr>
                                <th width="60%%">Description</th>
                                <th width="20%%" style="text-align: right;">Fee Type</th>
                                <th width="20%%" style="text-align: right;">Amount</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>Professional Doctor Charge</td>
                                <td style="text-align: right;">Service</td>
                                <td style="text-align: right;">%.2f</td>
                            </tr>
                            <tr>
                                <td>Hospital / Clinic Channeling Fee</td>
                                <td style="text-align: right;">Facility</td>
                                <td style="text-align: right;">%.2f</td>
                            </tr>
                            <tr class="total-row">
                                <td colspan="2" style="text-align: right; text-transform: uppercase;">Total Payable</td>
                                <td style="text-align: right; font-size: 16px;">%.2f</td>
                            </tr>
                        </tbody>
                    </table>
                    <div class="footer">
                        <p><strong>Important Note:</strong> Please arrive 15 minutes prior to your scheduled time.<br>
                        Bring this email or your reference ID #%d to the reception desk.</p>
                        <p>Thank you for trusting CureGuard with your health.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                date, time,
                details.getQueuePosition(),
                details.getDoctorName(),
                details.getSpecialization(),
                details.getId(),
                details.getPatientName(),
                doctorFee, clinicFee, totalFee,
                details.getId()
        );
    }

    @Async("emailTaskExecutor")
    @Transactional(readOnly = true)
    public void sendDelayNotification(Long appointmentId, String customMessage, Integer delayMinutes, Integer clinicId) {
        TenantContext.setCurrentTenant(clinicId);
        try {
            AppointmentFullDetailsDTO details = appointmentRepo
                    .findAppointmentFullDetails(appointmentId, clinicId)
                    .orElse(null);

            if (details == null) return;

            // 1. Send SMS
            String patientMobile = details.getPatientMobile();
            if (patientMobile != null && !patientMobile.isEmpty()) {
                ClinicResponseDTO clinic = clinicService.getCurrentClinicDetails();
                String gateway = (clinic != null && clinic.getAppointmentConfirmSmsGateway() != null)
                        ? clinic.getAppointmentConfirmSmsGateway()
                        : SmsService.SMS_GATEWAY;

                if (!SmsService.SMS_NONE.equalsIgnoreCase(gateway)) {
                    String smsMsg = (customMessage != null && !customMessage.isEmpty()) ? customMessage :
                            String.format("CureGuard Alert: Dr. %s is running late by %d minutes for schedule on %s. Your appointment ID #%d (Queue #%d) is updated.",
                                    details.getDoctorName(), delayMinutes, details.getScheduleDate(), details.getId(), details.getQueuePosition());
                    smsService.sendDynamicSms(patientMobile, smsMsg, gateway);
                }
            }

            // 2. Send Email
            Appointment appt = appointmentRepo.findById(appointmentId).orElse(null);
            if (appt != null && appt.getPatient() != null && appt.getPatient().getEmail() != null && !appt.getPatient().getEmail().isEmpty()) {
                String recipientEmail = appt.getPatient().getEmail();
                String emailBody = String.format("""
                    <h2>Schedule Delay Notification</h2>
                    <p>Dear %s,</p>
                    <p>Please note that Dr. %s is currently running late by about <strong>%d minutes</strong> for their session on %s.</p>
                    <p>Your Appointment Reference is <strong>#%d</strong> (Queue Position: #%d).</p>
                    <p><em>Message from Clinic: %s</em></p>
                    <p>We apologize for any inconvenience caused.</p>
                    <p>Thank you,<br>CureGuard Medical Team</p>
                    """, details.getPatientName(), details.getDoctorName(), delayMinutes, details.getScheduleDate(), details.getId(), details.getQueuePosition(), 
                    (customMessage != null && !customMessage.isEmpty()) ? customMessage : "Doctor is delayed due to clinical reasons.");
                emailService.sendHtmlEmailSync(recipientEmail, "Schedule Delay Notice - CureGuard", emailBody);
            }
        } catch (Exception e) {
            System.err.println("Failed to send delay notification for appointment " + appointmentId + ": " + e.getMessage());
        } finally {
            TenantContext.clear();
        }
    }
}