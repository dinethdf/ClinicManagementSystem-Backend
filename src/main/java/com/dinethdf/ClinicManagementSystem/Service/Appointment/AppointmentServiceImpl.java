package com.dinethdf.ClinicManagementSystem.Service.Appointment;

import com.dinethdf.ClinicManagementSystem.DTO.Appointment.*;
import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.ScheduleAnalyticsDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Invoice.AppointmentInvoiceViewDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Invoice.InvoiceItemViewDTO;
import com.dinethdf.ClinicManagementSystem.Model.Appointment.Appointment;
import com.dinethdf.ClinicManagementSystem.Repo.Appointment.AppointmentRepo;
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

    private final QueueService queueService;

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

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentAdminListDTO> getFilteredAppointments(Long patientId, Long scheduleId, LocalDate startDate, LocalDate endDate, Pageable pageable) {

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ValidationException("Start date cannot be after end date", "INVALID_DATE_RANGE", HttpStatus.BAD_REQUEST);
        }

        return appointmentRepo.findFilteredAppointments(
                patientId, scheduleId, startDate, endDate, TenantContext.getCurrentTenant(), pageable
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
    public String cancelAppointment(Long appointmentId, AppointmentCancelDTO cancelDTO, String cancelledBy) {

        // 1. Execute SQL Cancellation
        String statusMessage = appointmentRepo.sp_CancelAppointment(
                TenantContext.getCurrentTenant(),
                appointmentId.intValue(),
                cancelDTO.getReason(),
                cancelledBy
        );

        // 2. Handle Logic based on Status
        return switch (statusMessage) {
            case "CANCEL_SUCCESS", "REFUND_SUCCESS" -> {
                // --- FIREBASE SYNC START ---
                try {
                    Appointment appt = appointmentRepo.findById(appointmentId).orElse(null);

                    if (appt != null) {
                        queueService.cancelAppointment(
                                Long.valueOf(TenantContext.getCurrentTenant()),
                                appt.getDoctorSchedule().getId(),
                                appt.getQueuePosition()
                        );
                        System.out.println("Firebase updated: Appointment " + appointmentId + " cancelled.");
                    }
                } catch (Exception e) {
                    System.err.println("Failed to update Firebase cancellation: " + e.getMessage());
                }
                // --- FIREBASE SYNC END ---

                yield "CANCEL_SUCCESS".equals(statusMessage)
                        ? "Appointment Cancelled Successfully."
                        : "Appointment Cancelled & Refund Recorded.";
            }
            case "ERROR_NOT_FOUND" ->
                    throw new ValidationException("Appointment not found", "NOT_FOUND", HttpStatus.NOT_FOUND);
            case "ERROR_ALREADY_CANCELLED" ->
                    throw new ValidationException("Appointment is already cancelled", "ALREADY_CANCELLED", HttpStatus.BAD_REQUEST);
            case "ERROR_CANNOT_CANCEL_COMPLETED" ->
                    throw new ValidationException("Cannot cancel a completed appointment", "INVALID_STATUS", HttpStatus.BAD_REQUEST);
            default ->
                    throw new ValidationException("Database Error: " + statusMessage, "DB_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        };
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