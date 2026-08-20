package com.dinethdf.ClinicManagementSystem.Service.Appointment;

import com.dinethdf.ClinicManagementSystem.DTO.Appointment.*;
import com.dinethdf.ClinicManagementSystem.DTO.ClinicStaff.ClinicStaffResponseDto;
import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.ScheduleAnalyticsDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Invoice.AppointmentInvoiceViewDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Invoice.InvoiceItemViewDTO;
import com.dinethdf.ClinicManagementSystem.Model.Appointment.Appointment;
import com.dinethdf.ClinicManagementSystem.Repo.Appointment.AppointmentRepo;
import com.dinethdf.ClinicManagementSystem.Service.ClinicStaff.ClinicStaffService;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.mapper.AppointmentMapper;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepo appointmentRepo;
    private final AppointmentMapper appointmentMapper;
    private final MessageSourceUtil messageSourceUtil;
    private final ClinicStaffService clinicStaffService;
    private final QueueService queueService;
    private final AppointmentNotificationService notificationService;

    @Override
    public AppointmentResponseDTO createAppointment(AppointmentCreateDTO createDTO, String createBy) {

        validateAppointmentCreateDTO(createDTO);

        Map<String, Object> spResult = appointmentRepo.sp_BookAppointment(
                TenantContext.getCurrentTenant(),
                createBy,
                createDTO.getDoctorScheduleId(),
                createDTO.getPatientId(),
                createDTO.getNote()
        );

        String statusMessage = (String) spResult.get("o_statusMessage");

        if (!"BOOKED_SUCCESS".equals(statusMessage)) {
            handleSpError(statusMessage);
        }

        Object newIdObj = spResult.get("o_newAppointmentId");
        if (newIdObj == null) {
            throw new ValidationException("Database failed to return a valid Appointment ID", "DB_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Long newAppointmentNumber = ((Number) newIdObj).longValue();

        Appointment newAppointment = appointmentRepo.findById(newAppointmentNumber)
                .orElseThrow(() -> new ValidationException(
                        "Failed to retrieve new appointment",
                        "APPOINTMENT_NOT_FOUND",
                        HttpStatus.INTERNAL_SERVER_ERROR
                ));

        try {
            String patientFullName = newAppointment.getPatient().getFirstName() + " " + newAppointment.getPatient().getLastName();

            int queueNo = newAppointment.getQueuePosition();

            queueService.addPatientToQueue(
                    Long.valueOf(TenantContext.getCurrentTenant()),
                    newAppointment.getDoctorSchedule().getId(),
                    queueNo,
                    patientFullName
            );

            System.out.println("Firebase queue updated for Appointment ID: {}" +  newAppointment.getId());

        } catch (Exception e) {
            // Log error but allow transaction to complete
            System.out.println("Failed to update Firebase Queue: {}" +  e.getMessage());
        }

        return appointmentMapper.toResponseDTO(newAppointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentInvoiceViewDTO> getInvoicesByAppointmentId(Long appointmentId) {

        if (appointmentId == null || appointmentId <= 0) {
            throw new ValidationException("Invalid appointment ID.", "INVALID_INPUT", HttpStatus.BAD_REQUEST);
        }

        // --- CHANGE: Handle Status as String (Code) from Lookup Table ---
        String statusCode = appointmentRepo.findStatusById(appointmentId, TenantContext.getCurrentTenant())
                .orElseThrow(() -> new ValidationException("Appointment not found.", "APPOINTMENT_NOT_FOUND", HttpStatus.NOT_FOUND));

        if ("CANCELLED".equals(statusCode)) {
            throw new ValidationException("Appointment is CANCELLED.", "INVALID_STATUS", HttpStatus.BAD_REQUEST);
        }

        // 1. Fetch Raw Flat Data
        List<InvoiceAppointmentFlatDTO> rawData = appointmentRepo.findRawInvoicesByAppointmentId(appointmentId, TenantContext.getCurrentTenant());

        if (rawData.isEmpty()) {
            throw new ValidationException("No invoices generated for this appointment yet.", "NO_INVOICES", HttpStatus.NOT_FOUND);
        }

        // 2. Group by Invoice ID (One Invoice -> Many Items)
        Map<Long, List<InvoiceAppointmentFlatDTO>> groupedByInvoice = rawData.stream()
                .collect(Collectors.groupingBy(InvoiceAppointmentFlatDTO::getInvoiceId));

        List<AppointmentInvoiceViewDTO> resultList = new ArrayList<>();

        // 3. Map to View DTO
        for (Map.Entry<Long, List<InvoiceAppointmentFlatDTO>> entry : groupedByInvoice.entrySet()) {
            List<InvoiceAppointmentFlatDTO> invoiceRows = entry.getValue();

            if (invoiceRows.isEmpty()) continue;

            // Use the first row to populate common headers
            InvoiceAppointmentFlatDTO header = invoiceRows.get(0);

            AppointmentInvoiceViewDTO dto = new AppointmentInvoiceViewDTO();

            // --- Invoice Header ---
            dto.setInvoiceId(header.getInvoiceId());
            dto.setDateIssued(header.getInvoiceDate());
            dto.setDatePaidDate(header.getPaidDate());

            // --- Status Object Mapping ---
            dto.setStatus(new AppointmentInvoiceViewDTO.StatusDTO(
                    header.getInvoiceStatusCode(),
                    header.getInvoiceStatusDisplay(),
                    header.getInvoiceStatusColor()
            ));

            dto.setSubtotal(header.getSubtotal());
            dto.setTotalDiscount(header.getTotalDiscount());
            dto.setFinalAmount(header.getFinalAmount());

            // --- Payment Details ---
            dto.setPaymentMethod(header.getPaymentMethod());
            dto.setAmountPaid(header.getAmountPaid());
            dto.setTransactionId(header.getTransactionId());

            // --- Patient Info ---
            dto.setPatientId(header.getPatientId());
            dto.setPatientNo(header.getPatientNo());
            dto.setPatientName(header.getPatientFirstName() + " " + header.getPatientLastName());
            dto.setPatientAddress((header.getPatientAddress() != null ? header.getPatientAddress() : "") +
                    (header.getPatientCity() != null ? ", " + header.getPatientCity() : ""));
            dto.setPatientDob(header.getPatientDob());
            dto.setPatientContactNo(header.getPatientMobile());

            // --- Appointment Info ---
            dto.setAppointmentId(header.getApptId());
            // dto.setAppointmentNumber(header.getApptNumber()); // Commented out in Repo, keeping consistant
            dto.setAppointmentNote(header.getApptNote());
            dto.setAppointmentDate(header.getScheduleDate());
            dto.setAppointmentTime(header.getScheduleStartTime());
            dto.setQueuePosition(header.getQueuePosition());
            dto.setScheduleId(header.getScheduleId());

            if (header.getDoctorFirstName() != null) {
                dto.setDoctorName("Dr. " + header.getDoctorFirstName() + " " + header.getDoctorLastName());
            }
            dto.setDoctorSpecialization(header.getDoctorSpec());

            // --- Map Items ---
            List<InvoiceItemViewDTO> items = invoiceRows.stream().map(row -> {
                InvoiceItemViewDTO item = new InvoiceItemViewDTO();
                item.setDescription(row.getItemDescription());
                item.setTypeName(row.getItemType());
                item.setUnitPrice(row.getItemAmount());
                item.setQuantity(1); // Default logic
                item.setTotalAmount(row.getItemAmount());
                return item;
            }).collect(Collectors.toList());

            dto.setItems(items);
            resultList.add(dto);
        }

        return resultList;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<AppointmentAdminListDTO> getFilteredAppointments(
            Long patientId,
            Long doctorId,
            Long scheduleId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ValidationException("Start date cannot be after end date", "INVALID_DATE_RANGE", HttpStatus.BAD_REQUEST);
        }

        return appointmentRepo.findFilteredAppointments(
                patientId,
                doctorId,       // NEW
                scheduleId,
                startDate,
                endDate,
                TenantContext.getCurrentTenant(),
                pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentFullDetailsDTO getAppointmentDetails(Long appointmentId) {
        return appointmentRepo.findAppointmentFullDetails(appointmentId, TenantContext.getCurrentTenant())
                .orElseThrow(() -> new ValidationException(
                        messageSourceUtil.getMessage("appointment.not.found"),
                        "APPOINTMENT_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));
    }

    @Transactional(readOnly = true)
    @Override
    public AppointmentFullDetailsDTO getAppointmentDetailsByAppoinemtnPatientId(Long appointmentId, Long patientId) {
        return appointmentRepo.findAppointmentFullDetailsByPatientIdANDAppointmentId(appointmentId, patientId, TenantContext.getCurrentTenant())
                .orElseThrow(() -> new ValidationException(
                        messageSourceUtil.getMessage("appointment.not.found"),
                        "APPOINTMENT_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentByScheduleListDTO> getAppointmentsBySchedule(Long scheduleId, Pageable pageable) {
        return appointmentRepo.findAppointmentsByScheduleId(scheduleId, TenantContext.getCurrentTenant(), pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public ScheduleAnalyticsDTO getAnalyticsByScheduleId(Long scheduleId) {
        return appointmentRepo.getScheduleAnalytics(scheduleId, TenantContext.getCurrentTenant());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientAppointmentDTO> getAppointmentsByPatient(Long patientId) {
        if (patientId == null || patientId <= 0) {
            throw new ValidationException("Invalid Patient ID", "INVALID_INPUT", HttpStatus.BAD_REQUEST);
        }
        return appointmentRepo.findAppointmentsByPatientId(patientId, TenantContext.getCurrentTenant());
    }

    @Override
    public AppointmentCheckDTO checkPatientAppointment(Long scheduleId, Long patientId) {
        return appointmentRepo.findDetailsByScheduleAndPatient(scheduleId, patientId, TenantContext.getCurrentTenant())
                .orElseThrow(() -> new ValidationException(
                        "No appointment found for this patient in the selected schedule.",
                        "APPOINTMENT_NOT_FOUND"
                ));
    }

    @Override
    public AppointmentCancelResponseDTO cancelAppointment(Long appointmentId, AppointmentCancelDTO cancelDTO, Long staffId) {

        ClinicStaffResponseDto staff = clinicStaffService.getStaffById(staffId);

        String refundMethod = (cancelDTO.getRefundMethod() != null) ? cancelDTO.getRefundMethod() : "CASH";

        Map<String, Object> result = appointmentRepo.sp_CancelAppointment(
                TenantContext.getCurrentTenant(),
                appointmentId.intValue(),
                cancelDTO.getReason(),
                staff.getFirstName() + staff.getLastName(),
                staff.getId(),
                null,
                refundMethod
        );

        // 2. Extract Outputs
        String message = (String) result.get("o_status_message");
        BigDecimal refundAmount = (BigDecimal) result.get("o_refund_amount");
        Boolean isSuccess = (Boolean) result.get("o_is_success");

        // 3. Handle Firebase Sync + Patient Notifications (Only if Success)
        if (Boolean.TRUE.equals(isSuccess)) {
            Integer clinicId = TenantContext.getCurrentTenant();

            try {
                Appointment appt = appointmentRepo.findById(appointmentId).orElse(null);
                if (appt != null) {
                    // Firebase queue sync
                    queueService.cancelAppointment(
                            Long.valueOf(clinicId),
                            appt.getDoctorSchedule().getId(),
                            appt.getQueuePosition()
                    );

                    // Async Email notification to patient
                    String patientEmail = appt.getPatient().getEmail();
                    if (patientEmail != null && !patientEmail.isEmpty()) {
                        notificationService.sendCancellationNotification(appointmentId, patientEmail, clinicId);
                    }

                    // Async SMS notification to patient (Now manual via separate API)
                    // String patientMobile = appt.getPatient().getMobileNo();
                    // if (patientMobile != null && !patientMobile.isEmpty()) {
                    //     notificationService.sendCancellationSms(appointmentId, patientMobile, clinicId);
                    // }
                }
            } catch (Exception e) {
                System.err.println("Post-cancellation notification failed: " + e.getMessage());
            }
        }

        // 4. Return DTO
        return new AppointmentCancelResponseDTO(
                Boolean.TRUE.equals(isSuccess),
                message,
                refundAmount != null ? refundAmount : BigDecimal.ZERO
        );
    }

    @Override
    public void sendManualCancellationSms(Long appointmentId) {
        Integer clinicId = TenantContext.getCurrentTenant();

        // 1. Verify status is actually CANCELLED
        String statusCode = appointmentRepo.findStatusById(appointmentId, clinicId)
                .orElseThrow(() -> new ValidationException("Appointment not found", "APPOINTMENT_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (!"CANCELLED".equals(statusCode)) {
            throw new ValidationException("Cannot send cancellation SMS. Appointment is not cancelled.", "INVALID_STATUS", HttpStatus.BAD_REQUEST);
        }

        Appointment appt = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new ValidationException("Appointment not found", "APPOINTMENT_NOT_FOUND", HttpStatus.NOT_FOUND));

        String patientMobile = appt.getPatient().getMobileNo();
        if (patientMobile != null && !patientMobile.isEmpty()) {
            boolean success = notificationService.sendCancellationSmsSync(appointmentId, patientMobile, clinicId);
            if (!success) {
                throw new ValidationException("Failed to send SMS through the gateway.", "SMS_GATEWAY_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            throw new ValidationException("Patient does not have a registered mobile number.", "NO_MOBILE_NUMBER", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateAppointmentCreateDTO(AppointmentCreateDTO createDTO) {
        if (createDTO.getDoctorScheduleId() == null) {
            throw new ValidationException(
                    messageSourceUtil.getMessage("appointment.error.no_schedule_id"),
                    "INVALID_SCHEDULE_ID",
                    HttpStatus.BAD_REQUEST
            );
        }
        if (createDTO.getPatientId() == null) {
            throw new ValidationException(
                    messageSourceUtil.getMessage("appointment.error.no_patient_id"),
                    "INVALID_PATIENT_ID",
                    HttpStatus.BAD_REQUEST
            );
        }
        if (createDTO.getNote() != null && createDTO.getNote().length() > 500) {
            throw new ValidationException(
                    messageSourceUtil.getMessage("appointment.error.note_too_long"),
                    "NOTE_TOO_LONG",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void handleSpError(String statusMessage) {
        switch (statusMessage) {
            case "ERROR_SCHEDULE_FULL":
                throw new ValidationException(messageSourceUtil.getMessage("appointment.error.full"), "SCHEDULE_FULL", HttpStatus.CONFLICT);
            case "ERROR_DUPLICATE_PATIENT":
                throw new ValidationException(messageSourceUtil.getMessage("appointment.error.duplicate"), "DUPLICATE_PATIENT", HttpStatus.CONFLICT);
            case "ERROR_INVALID_SCHEDULE":
                throw new ValidationException(messageSourceUtil.getMessage("appointment.error.invalid_schedule"), "INVALID_SCHEDULE", HttpStatus.NOT_FOUND);
            case "ERROR_SCHEDULE_INACTIVE":
                throw new ValidationException(messageSourceUtil.getMessage("appointment.error.inactive_schedule"), "INACTIVE_SCHEDULE", HttpStatus.BAD_REQUEST);
            case "ERROR_SQL_EXCEPTION":
            default:
                throw new ValidationException(messageSourceUtil.getMessage("appointment.error.database"), "DATABASE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}