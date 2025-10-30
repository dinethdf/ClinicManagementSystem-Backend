package com.dinethdf.ClinicManagementSystem.Service.Appointment;

import com.dinethdf.ClinicManagementSystem.DTO.Appointment.AppointmentCreateDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Appointment.AppointmentResponseDTO;

public interface AppointmentService {

    AppointmentResponseDTO createAppointment(AppointmentCreateDTO createDTO, String createBy);

}