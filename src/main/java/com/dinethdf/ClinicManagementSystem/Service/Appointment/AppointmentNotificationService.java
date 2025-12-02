package com.dinethdf.ClinicManagementSystem.Service.Appointment;

import com.dinethdf.ClinicManagementSystem.DTO.Appointment.AppointmentFullDetailsDTO;
import com.dinethdf.ClinicManagementSystem.Repo.Appointment.AppointmentRepo;
import com.dinethdf.ClinicManagementSystem.util.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AppointmentNotificationService {

    @Autowired
    private AppointmentRepo appointmentRepo;

    @Autowired
    private EmailService emailService;

    // This method runs in the background thread
    // We use Transactional(readOnly=true) because we are reading DB data
    @Transactional(readOnly = true)
    public void sendBookingConfirmation(Long appointmentId, String recipientEmail) {

        // 1. Fetch the full details using your existing DTO
        AppointmentFullDetailsDTO details = appointmentRepo.findAppointmentFullDetails(appointmentId)
                .orElse(null);

        if (details == null) {
            System.err.println("Error: Could not find appointment details for ID: " + appointmentId);
            return;
        }

        // 2. Calculate Total Fee (Doctor + Clinic)
        BigDecimal doctorFee = details.getDoctorFee() != null ? details.getDoctorFee() : BigDecimal.ZERO;
        BigDecimal clinicFee = details.getClinicFee() != null ? details.getClinicFee() : BigDecimal.ZERO;
        BigDecimal totalFee = doctorFee.add(clinicFee);

        // 3. Format the Email Body
        // Using Java Text Blocks (formatted string) for better readability
        String emailBody = String.format("""
            Dear %s,
            
            Your appointment has been successfully booked!
            
            --------------------------------------------------
            APPOINTMENT DETAILS
            --------------------------------------------------
            Appointment ID  : %d
            Doctor          : Dr. %s
            Specialization  : %s
            Date            : %s
            Time            : %s - %s
            Your Queue No   : %d
            --------------------------------------------------
            
            PAYMENT INFORMATION
            Doctor Fee      : %.2f
            Clinic Fee      : %.2f
            Total Amount    : %.2f
            
            Note: Please arrive 10 minutes before your scheduled time.
            
            Thank you for choosing CureGuard.
            """,
                details.getPatientName(),
                details.getId(),
                details.getDoctorName(),
                details.getSpecialization(),
                details.getScheduleDate(),
                details.getStartTime(),
                details.getEndTime(),
                details.getQueuePosition(),
                doctorFee,
                clinicFee,
                totalFee
        );

        // 4. Send the Email
        emailService.sendOtpEmail(recipientEmail, emailBody);
        System.out.println("Confirmation email sent to " + recipientEmail);
    }
}