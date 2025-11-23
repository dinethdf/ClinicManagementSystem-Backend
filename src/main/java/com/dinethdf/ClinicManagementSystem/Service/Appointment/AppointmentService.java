package com.dinethdf.ClinicManagementSystem.Service.Appointment;

import com.dinethdf.ClinicManagementSystem.DTO.Appointment.AppointmentCreateDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Appointment.AppointmentResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Appointment.PatientAppointmentDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Invoice.AppointmentInvoiceViewDTO;

import java.util.List;

public interface AppointmentService {

    AppointmentResponseDTO createAppointment(AppointmentCreateDTO createDTO, String createBy);

     List<AppointmentInvoiceViewDTO> getInvoicesByAppointmentId(Long appointmentId) ;

    List<PatientAppointmentDTO> getAppointmentsByPatient(Long patientId);

}