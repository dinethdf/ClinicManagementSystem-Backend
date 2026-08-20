package com.dinethdf.ClinicManagementSystem.Service.Appointment;

import com.dinethdf.ClinicManagementSystem.DTO.Appointment.*;
import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.ScheduleAnalyticsDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Invoice.AppointmentInvoiceViewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    AppointmentResponseDTO createAppointment(AppointmentCreateDTO createDTO, String createBy);

     List<AppointmentInvoiceViewDTO> getInvoicesByAppointmentId(Long appointmentId) ;

    @Transactional(readOnly = true)
    AppointmentFullDetailsDTO getAppointmentDetailsByAppoinemtnPatientId(Long appointmentId, Long patientId);

    Page<AppointmentByScheduleListDTO> getAppointmentsBySchedule(Long scheduleId, Pageable pageable);

    ScheduleAnalyticsDTO getAnalyticsByScheduleId(Long scheduleId);

    List<PatientAppointmentDTO> getAppointmentsByPatient(Long patientId);

    Page<AppointmentAdminListDTO> getFilteredAppointments(
            Long patientId, Long doctorId, Long scheduleId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    AppointmentFullDetailsDTO getAppointmentDetails(Long appointmentId);

    AppointmentCheckDTO checkPatientAppointment(Long scheduleId, Long patientId);

    AppointmentCancelResponseDTO cancelAppointment(Long appointmentId, AppointmentCancelDTO cancelDTO, Long staffId);

    void sendManualCancellationSms(Long appointmentId);
}