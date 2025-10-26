package com.dinethdf.ClinicManagementSystem.mapper;

import com.dinethdf.ClinicManagementSystem.Model.Appoinment.DoctorSchedule;
import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleResponseDTO;
import org.springframework.stereotype.Component;

@Component // Or use MapStruct
public class DoctorScheduleMapper {

    public DoctorScheduleResponseDTO toResponseDTO(DoctorSchedule schedule) {
        DoctorScheduleResponseDTO dto = new DoctorScheduleResponseDTO();
        dto.setId(schedule.getId());
        dto.setScheduleDate(schedule.getScheduleDate());
        dto.setScheduleTime(schedule.getScheduleTime());
        dto.setPayment(schedule.getPayment());
        dto.setMaxPatientCount(schedule.getMaxPatientCount());
        dto.setIsActive(schedule.getIsActive());

        if (schedule.getDoctor() != null) {
            dto.setDoctorId(schedule.getDoctor().getId());
            // Assuming Doctor entity has getName()
            dto.setDoctorName(schedule.getDoctor().getFirstName() + " " + schedule.getDoctor().getLastName());
        }
        return dto;
    }
}