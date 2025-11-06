package com.dinethdf.ClinicManagementSystem.mapper;

import com.dinethdf.ClinicManagementSystem.DTO.Appointment.AppointmentResponseDTO;
import com.dinethdf.ClinicManagementSystem.Model.Appointment.Appointment;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {


    public AppointmentResponseDTO toResponseDTO(Appointment appointment) {
        // 1. Handle null input
        if (appointment == null) {
            return null;
        }

        // 2. Create the new DTO
        AppointmentResponseDTO dto = new AppointmentResponseDTO();

        // 3. Manually set all the fields
        dto.setId(appointment.getId());
//        dto.setAppointmentId(appointment.getId());
        dto.setQueuePosition(appointment.getQueuePosition());
        dto.setStatus(appointment.getStatus().toString()); // Convert Enum to String
        dto.setNote(appointment.getNote());
        dto.setCreatedDate(appointment.getCreatedDate());

        // 4. Manually map the IDs from the related objects
        if (appointment.getPatient() != null) {
            dto.setPatientId(appointment.getPatient().getId());
        }
        if (appointment.getDoctorSchedule() != null) {
            dto.setDoctorScheduleId(appointment.getDoctorSchedule().getId());
        }

        // 5. Return the finished DTO
        return dto;
    }
}