package com.dinethdf.ClinicManagementSystem.Service.Appointment;

import com.dinethdf.ClinicManagementSystem.DTO.Appointment.*;
import com.dinethdf.ClinicManagementSystem.DTO.Invoice.AppointmentInvoiceViewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    AppointmentResponseDTO createAppointment(AppointmentCreateDTO createDTO, String createBy);

     List<AppointmentInvoiceViewDTO> getInvoicesByAppointmentId(Long appointmentId) ;

    Page<AppointmentByScheduleListDTO> getAppointmentsBySchedule(Long scheduleId, Pageable pageable);

    List<PatientAppointmentDTO> getAppointmentsByPatient(Long patientId);


    Page<AppointmentAdminListDTO> getFilteredAppointments(Long patientId, Long scheduleId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    AppointmentFullDetailsDTO getAppointmentDetails(Long appointmentId);

}