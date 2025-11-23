package com.dinethdf.ClinicManagementSystem.Service.Appointment;

import com.dinethdf.ClinicManagementSystem.DTO.Appointment.AppointmentCreateDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Appointment.AppointmentResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Appointment.PatientAppointmentDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Invoice.AppointmentInvoiceViewDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Invoice.InvoiceItemViewDTO;
import com.dinethdf.ClinicManagementSystem.Model.Appointment.Appointment;
import com.dinethdf.ClinicManagementSystem.Repo.Appointment.AppointmentRepo;
import com.dinethdf.ClinicManagementSystem.DTO.Appointment.InvoiceAppointmentFlatDTO;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.mapper.AppointmentMapper;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// import org.springframework.security.core.context.SecurityContextHolder;

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

    @Override
    public AppointmentResponseDTO createAppointment(AppointmentCreateDTO createDTO, String createBy) {

        validateAppointmentCreateDTO(createDTO);

        Map<String, Object> spResult = appointmentRepo.sp_BookAppointment(
                createBy,
                createDTO.getDoctorScheduleId(),
                createDTO.getPatientId(),
                createDTO.getNote()
        );


        String statusMessage = (String) spResult.get("o_statusMessage");

        if (!"BOOKED_SUCCESS".equals(statusMessage)) {
            handleSpError(statusMessage);
        }


        Long newAppointmentNumber = (Long) spResult.get("o_newAppointmentId");


        Appointment newAppointment = appointmentRepo.findById(newAppointmentNumber)
                .orElseThrow(() -> new ValidationException(
                        "Failed to retrieve new appointment",
                        "APPOINTMENT_NOT_FOUND",
                        HttpStatus.INTERNAL_SERVER_ERROR
                ));


        return appointmentMapper.toResponseDTO(newAppointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentInvoiceViewDTO> getInvoicesByAppointmentId(Long appointmentId) {


        if (appointmentId == null || appointmentId <= 0) {
            throw new ValidationException("Invalid appointment ID.", "INVALID_INPUT", HttpStatus.BAD_REQUEST);
        }

        // 2. Fast Existence & Status Check (Combined)
        // We fetch JUST the status. If it's empty, the appointment doesn't exist.
        Appointment.AppointmentStatus status = appointmentRepo.findStatusById(appointmentId)
                .orElseThrow(() -> new ValidationException("Appointment not found.", "APPOINTMENT_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (status == Appointment.AppointmentStatus.CANCELLED || status == Appointment.AppointmentStatus.Booked) {
            throw new ValidationException("Appointment is already " + status + ".", "INVALID_STATUS", HttpStatus.BAD_REQUEST);
        }

        List<InvoiceAppointmentFlatDTO> rawData = appointmentRepo.findRawInvoicesByAppointmentId(appointmentId);

        if (rawData.isEmpty()) {
            throw new ValidationException("No invoices generated for this appointment yet.", "NO_INVOICES", HttpStatus.NOT_FOUND);
        }

        Map<Long, List<InvoiceAppointmentFlatDTO>> groupedByInvoice = rawData.stream()
                .collect(Collectors.groupingBy(InvoiceAppointmentFlatDTO::getInvoiceId));

        List<AppointmentInvoiceViewDTO> resultList = new ArrayList<>();

        for (Map.Entry<Long, List<InvoiceAppointmentFlatDTO>> entry : groupedByInvoice.entrySet()) {
            List<InvoiceAppointmentFlatDTO> invoiceRows = entry.getValue();
            InvoiceAppointmentFlatDTO header = invoiceRows.getFirst();

            // Map Header
            AppointmentInvoiceViewDTO dto = new AppointmentInvoiceViewDTO();
            dto.setInvoiceId(header.getInvoiceId());
            dto.setDateIssued(header.getInvoiceDate().toLocalDate());
            dto.setStatus(header.getInvoiceStatus());
            dto.setSubtotal(header.getSubtotal());
            dto.setTotalDiscount(header.getTotalDiscount());
            dto.setFinalAmount(header.getFinalAmount());

            // Map Patient
            dto.setPatientNo(header.getPatientNo());
            dto.setPatientName(header.getPatientFirstName() + " " + header.getPatientLastName());
            dto.setPatientAddress((header.getPatientAddress() != null ? header.getPatientAddress() : "") +
                    (header.getPatientCity() != null ? ", " + header.getPatientCity() : ""));
            dto.setPatientDob(header.getPatientDob());
            dto.setPatientContactNo(header.getPatientMobile());

            // Map Appointment (they all share the same appt info in this case)
            dto.setAppointmentId(header.getApptId());
            dto.setAppointmentNumber(header.getApptNumber());
            dto.setAppointmentNote(header.getApptNote());
            dto.setDoctorName("Dr. " + header.getDoctorFirstName() + " " + header.getDoctorLastName());
            dto.setDoctorSpecialization(header.getDoctorSpec());

            // Map Items for THIS specific invoice
            List<InvoiceItemViewDTO> items = invoiceRows.stream().map(row -> {
                InvoiceItemViewDTO item = new InvoiceItemViewDTO();
                item.setDescription(row.getItemDescription());
                item.setTypeName(row.getItemType());
                item.setUnitPrice(row.getItemAmount());
                item.setQuantity(1);
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
    public List<PatientAppointmentDTO> getAppointmentsByPatient(Long patientId) {
        if (patientId == null || patientId <= 0) {

            throw new ValidationException("Invalid Patient ID", "INVALID_INPUT", HttpStatus.BAD_REQUEST);
        }

        List<PatientAppointmentDTO> appointments = appointmentRepo.findAppointmentsByPatientId(patientId);


        return appointments;
    }
    private void validateAppointmentCreateDTO(AppointmentCreateDTO createDTO) {
        if (createDTO.getDoctorScheduleId() == null) {
            throw new ValidationException(
                    messageSourceUtil.getMessage("appointment.error.no_schedule_id"), // You need to add this key
                    "INVALID_SCHEDULE_ID",
                    HttpStatus.BAD_REQUEST
            );
        }
        if (createDTO.getPatientId() == null) {
            throw new ValidationException(
                    messageSourceUtil.getMessage("appointment.error.no_patient_id"), // You need to add this key
                    "INVALID_PATIENT_ID",
                    HttpStatus.BAD_REQUEST
            );
        }
        if (createDTO.getNote() != null && createDTO.getNote().length() > 500) {
            throw new ValidationException(
                    messageSourceUtil.getMessage("appointment.error.note_too_long"), // You need to add this key
                    "NOTE_TOO_LONG",
                    HttpStatus.BAD_REQUEST
            );
        }
    }


    private void handleSpError(String statusMessage) {
        switch (statusMessage) {

            case "ERROR_SCHEDULE_FULL":
                throw new ValidationException(
                        messageSourceUtil.getMessage("appointment.error.full"),
                        "SCHEDULE_FULL",
                        HttpStatus.CONFLICT
                );

            case "ERROR_DUPLICATE_PATIENT":
                throw new ValidationException(
                        messageSourceUtil.getMessage("appointment.error.duplicate"),
                        "DUPLICATE_PATIENT",
                        HttpStatus.CONFLICT
                );

            case "ERROR_INVALID_SCHEDULE":
                throw new ValidationException(
                        messageSourceUtil.getMessage("appointment.error.invalid_schedule"),
                        "INVALID_SCHEDULE",
                        HttpStatus.NOT_FOUND
                );

            case "ERROR_SCHEDULE_INACTIVE":
                throw new ValidationException(
                        messageSourceUtil.getMessage("appointment.error.inactive_schedule"),
                        "INACTIVE_SCHEDULE",
                        HttpStatus.BAD_REQUEST
                );

            case "ERROR_SQL_EXCEPTION":
            default:
                throw new ValidationException(
                        messageSourceUtil.getMessage("appointment.error.database"), // A generic error message
                        "DATABASE_ERROR",
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
        }
    }
}