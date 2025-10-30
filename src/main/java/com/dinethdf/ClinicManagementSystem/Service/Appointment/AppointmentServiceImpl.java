package com.dinethdf.ClinicManagementSystem.Service.Appointment;

import com.dinethdf.ClinicManagementSystem.DTO.Appointment.AppointmentCreateDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Appointment.AppointmentResponseDTO;
import com.dinethdf.ClinicManagementSystem.Model.Appointment.Appointment;
import com.dinethdf.ClinicManagementSystem.Repo.Appointment.AppointmentRepo;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.mapper.AppointmentMapper;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

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

        // 3. Check the result message from the SP
        String statusMessage = (String) spResult.get("o_statusMessage");

        if (!"BOOKED_SUCCESS".equals(statusMessage)) {
            handleSpError(statusMessage);
        }

        // 4. On success, get the new appointment number
        String newAppointmentNumber = (String) spResult.get("o_appointmentNumber");

        // 5. Fetch the new appointment from the DB
        Appointment newAppointment = appointmentRepo.findByAppointmentNumber(newAppointmentNumber)
                .orElseThrow(() -> new ValidationException(
                        "Failed to retrieve new appointment",
                        "APPOINTMENT_NOT_FOUND",
                        HttpStatus.INTERNAL_SERVER_ERROR
                ));

        // 6. Map to DTO and return
        return appointmentMapper.toResponseDTO(newAppointment);
    }

    /**
     * --- NEW HELPER METHOD ---
     * Manually validates the DTO.
     * This replaces the @Valid and @NotNull annotations.
     */
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

    /**
     * Helper method to convert database errors into user-friendly exceptions.
     */
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