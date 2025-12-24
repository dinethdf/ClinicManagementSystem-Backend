package com.dinethdf.ClinicManagementSystem.Service.Appointment;

import com.dinethdf.ClinicManagementSystem.DTO.Appointment.AppointmentFullDetailsDTO;
import com.dinethdf.ClinicManagementSystem.Repo.Appointment.AppointmentRepo;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import com.dinethdf.ClinicManagementSystem.util.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class AppointmentNotificationService {

    @Autowired
    private AppointmentRepo appointmentRepo;

    @Autowired
    private EmailService emailService;

    @Transactional(readOnly = true)
    public void sendBookingConfirmation(Long appointmentId, String recipientEmail) {

        // 1. Fetch details
        AppointmentFullDetailsDTO details = appointmentRepo.findAppointmentFullDetails(appointmentId, TenantContext.getCurrentTenant())
                .orElse(null);

        if (details == null) {
            System.err.println("Error: Could not find appointment details for  : " + appointmentId);
            return;
        }

        // 2. Calculate Fees
        BigDecimal doctorFee = details.getDoctorFee() != null ? details.getDoctorFee() : BigDecimal.ZERO;
        BigDecimal clinicFee = details.getClinicFee() != null ? details.getClinicFee() : BigDecimal.ZERO;
        BigDecimal totalFee = doctorFee.add(clinicFee);

        // 3. Format Dates/Times for display
        // Assuming details.getScheduleDate() is a LocalDate or Date.
        // Adjust formatting pattern as needed.
        String formattedDate = details.getScheduleDate().toString();
        String formattedTime = details.getStartTime() + " - " + details.getEndTime();

        // 4. Generate HTML Body
        String emailBody = generateHtmlTemplate(details, formattedDate, formattedTime, doctorFee, clinicFee, totalFee);

        // 5. Send Email
        // NOTE: Ensure your emailService.sendOtpEmail implementation sets "isHtml" to true!
        emailService.sendHtmlEmail(
                recipientEmail,
                "Booking Confirmation - CureGuard",
                emailBody
        );

        System.out.println("Confirmation email sent to " + recipientEmail);
    }

    private String generateHtmlTemplate(AppointmentFullDetailsDTO details, String date, String time,
                                        BigDecimal doctorFee, BigDecimal clinicFee, BigDecimal totalFee) {

        // Colors extracted from your image:
        // Dark Blue: #003366 (Header text)
        // Table Header Blue: #0f4c81
        // Light Blue Background: #eef6ff (Totals section)

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
                    /* Grid for Details */
                    .details-grid { width: 100%%; margin-bottom: 30px; }
                    .details-grid td { vertical-align: top; padding-bottom: 10px; }
                    .label { font-weight: bold; color: #003366; font-size: 14px; margin-bottom: 5px; display: block;}
                    .value { color: #333; font-size: 14px; line-height: 1.5; }

                    .section-title { color: #003366; font-size: 18px; font-weight: bold; text-transform: uppercase; text-align: right; margin-bottom: 15px;}

                    /* Fee Table */
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
                date,
                time,
                details.getQueuePosition(),
                details.getDoctorName(),
                details.getSpecialization(),
                details.getId(),
                details.getPatientName(),
                doctorFee,
                clinicFee,
                totalFee,
                details.getId()
        );
    }
}