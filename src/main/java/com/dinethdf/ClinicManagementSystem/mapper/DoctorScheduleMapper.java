package com.dinethdf.ClinicManagementSystem.mapper;

import com.dinethdf.ClinicManagementSystem.Model.DoctorShedule.DoctorSchedule;
import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class DoctorScheduleMapper {

    public DoctorScheduleResponseDTO toResponseDTO(DoctorSchedule schedule) {
        if (schedule == null) return null;

        DoctorScheduleResponseDTO dto = new DoctorScheduleResponseDTO();
        dto.setId(schedule.getId());
        dto.setScheduleDate(schedule.getScheduleDate());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setDoctorFee(schedule.getDoctorFee());
        dto.setClinicFee(schedule.getClinicFee());
        dto.setMaxPatientCount(schedule.getMaxPatientCount());
        dto.setRegisteredPatientCount(schedule.getRegisteredPatientCount());

        // Optional: remove isActive if you rely on status enum
//        dto.setIsActive(schedule.getIsActive());

        // Map enums
        dto.setStatus(schedule.getStatus());
        dto.setBookingStatus(schedule.getBookingStatus());
        dto.setPayoutStatus(schedule.getPayoutStatus());

        if (schedule.getDoctor() != null) {
            dto.setDoctorId(schedule.getDoctor().getId());
            dto.setDoctorName(schedule.getDoctor().getFirstName() + " " + schedule.getDoctor().getLastName());

            if (schedule.getDoctor().getSpecialization() != null) {
                dto.setSpecialization(schedule.getDoctor().getSpecialization().getName());
            }
        }

        return dto;
    }
}
