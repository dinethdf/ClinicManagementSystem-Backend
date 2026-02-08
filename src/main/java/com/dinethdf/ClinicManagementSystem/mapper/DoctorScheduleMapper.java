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

        // --- UPDATED MAPPING ---

        // 1. Map Status Entity -> StatusDTO
        if (schedule.getStatus() != null) {
            dto.setStatus(new DoctorScheduleResponseDTO.StatusDTO(
                    schedule.getStatus().getCode(),
                    schedule.getStatus().getDisplayName(),
                    schedule.getStatus().getColorCode()
            ));
        }

        // 2. Map Payout Entity -> StatusDTO
        if (schedule.getPayoutStatus() != null) {
            dto.setPayoutStatus(new DoctorScheduleResponseDTO.StatusDTO(
                    schedule.getPayoutStatus().getCode(),
                    schedule.getPayoutStatus().getDisplayName(),
                    schedule.getPayoutStatus().getColorCode()
            ));
        }

        // 3. Map Boolean
        dto.setIsBookingOpen(schedule.getIsBookingOpen());

        // 4. Map Doctor Details
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