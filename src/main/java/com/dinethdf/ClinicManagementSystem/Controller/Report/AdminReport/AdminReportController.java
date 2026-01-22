package com.dinethdf.ClinicManagementSystem.Controller.Report.AdminReport;

import com.dinethdf.ClinicManagementSystem.Service.Report.AdminReport.RevenueReportService;
import com.dinethdf.ClinicManagementSystem.Service.Report.AdminReport.ScheduleReportService;
import com.dinethdf.ClinicManagementSystem.Service.Report.AdminReport.SessionSummaryService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("api/report/admin")
public class AdminReportController {

    @Autowired
    private RevenueReportService revenueReportService;

    @Autowired
    private ScheduleReportService scheduleReportService;

    @Autowired
    private SessionSummaryService sessionSummaryService;

    @GetMapping("/v1.0/revenue")
    public ResponseEntity<byte[]> getRevenueReport(
            @RequestParam(name = "clinicId") Integer clinicId,
            @RequestParam(name = "startDate", defaultValue = "0") String startDate,
            @RequestParam(name = "endDate", defaultValue = "0") String endDate,
            @RequestParam(name = "type", defaultValue = "ALL") String revenueType,
            @RequestParam(name = "lang", defaultValue = "en") String lang
    ) {
        try {
            ByteArrayOutputStream outputStream = revenueReportService.generateRevenueReport(clinicId, startDate, endDate, revenueType, lang);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add("Content-Disposition", "inline; filename=RevenueReport.pdf");

            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);

        } catch (JRException | IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/v1.0/reports/schedule-financials")
    public ResponseEntity<byte[]> getScheduleFinancialReport(
            @RequestParam(name = "clinicId") Integer clinicId,
            @RequestParam(name = "scheduleId") Integer scheduleId,
            @RequestParam(name = "lang", defaultValue = "en") String lang
    ) {
        try {
            ByteArrayOutputStream outputStream = scheduleReportService.generateScheduleFinancialReport(clinicId, scheduleId, lang);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);

            // Filename includes schedule ID for easy identification
            String filename = "Schedule_Financials_" + scheduleId + ".pdf";
            headers.add("Content-Disposition", "inline; filename=" + filename);

            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);

        } catch (JRException | IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

        @GetMapping("/v1.0/reports/session-summary")
        public ResponseEntity<byte[]> getSessionSummaryReport(
                @RequestParam(name = "clinicId") Integer clinicId,
                @RequestParam(name = "startDate") String startDate,
                @RequestParam(name = "endDate") String endDate,
                @RequestParam(name = "lang", defaultValue = "en") String lang
        ) {
            try {
                // Call the service
                ByteArrayOutputStream outputStream = sessionSummaryService.generateSessionSummaryReport(clinicId, startDate, endDate, lang);

                // Prepare Response
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);

                // Filename for the browser
                String filename = "Doctor_Session_Summary_" + startDate + "_to_" + endDate + ".pdf";
                headers.add("Content-Disposition", "inline; filename=" + filename);

                return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);

            } catch (JRException | IOException e) {
                e.printStackTrace();
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }


    @GetMapping("/v1.0/reports/doctor-wise-summary")
    public ResponseEntity<byte[]> getDoctorWiseSummaryReport(
            @RequestParam(name = "clinicId") Integer clinicId,
            @RequestParam(name = "doctorId", required = false) Long doctorId,
            @RequestParam(name = "startDate") String startDate,
            @RequestParam(name = "endDate") String endDate,
            @RequestParam(name = "lang", defaultValue = "en") String lang
    ) {
        try {
            // Call the service
            ByteArrayOutputStream outputStream = sessionSummaryService.generateDoctorWiseSummaryReport(clinicId, doctorId, startDate, endDate, lang);

            // Prepare Response
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);

            // Filename for the browser
            String filename = "Doctor_Wise_Summary_" + startDate + "_to_" + endDate + ".pdf";
            headers.add("Content-Disposition", "inline; filename=" + filename);

            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);

        } catch (JRException | IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}