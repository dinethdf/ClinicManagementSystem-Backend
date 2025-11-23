package com.dinethdf.ClinicManagementSystem.mapper;

import com.dinethdf.ClinicManagementSystem.DTO.Appointment.AppointmentResponseDTO;
import com.dinethdf.ClinicManagementSystem.Model.Appointment.Appointment;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {


    public AppointmentResponseDTO toResponseDTO(Appointment appointment) {

        if (appointment == null) {
            return null;
        }


        AppointmentResponseDTO dto = new AppointmentResponseDTO();


        dto.setId(appointment.getId());

        dto.setQueuePosition(appointment.getQueuePosition());
        dto.setStatus(appointment.getStatus().toString());
        dto.setNote(appointment.getNote());
        dto.setCreatedDate(appointment.getCreatedDate());

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