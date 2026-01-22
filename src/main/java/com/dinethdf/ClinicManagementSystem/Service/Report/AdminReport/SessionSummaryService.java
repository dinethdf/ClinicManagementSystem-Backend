package com.dinethdf.ClinicManagementSystem.Service.Report.AdminReport;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.jasperreports.engine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SessionSummaryService {

    @Autowired
    private DataSource dataSource;

    public ByteArrayOutputStream generateSessionSummaryReport(Integer clinicId, String startDateStr, String endDateStr, String lang) throws JRException, IOException {

        // 1. Define File Paths (Relative to src/main/resources)
        // Ensure the folder 'SessionReport' exists inside 'reports' and 'languages/reports'
        final String reportPath = "/reports/SessionReport/DoctorSessionSummary.jrxml";
//        final String jsonPath = "/languages/reports/SessionReport/" + lang + ".json";


        InputStream reportStream = getClass().getResourceAsStream(reportPath);
//        InputStream jsonStream = getClass().getResourceAsStream(jsonPath);

        if (reportStream == null) {
            throw new FileNotFoundException("Report file not found in resources: " + reportPath);
        }


        Map<String, Object> parameters = new HashMap<>();


//        if (jsonStream != null) {
//            try {
//                ObjectMapper objectMapper = new ObjectMapper();
//                List<Map<String, Object>> jsonDataList = objectMapper.readValue(jsonStream, new TypeReference<List<Map<String, Object>>>() {});
//
//                if (!jsonDataList.isEmpty()) {
//                    parameters.putAll(jsonDataList.get(0));
//                }
//            } catch (Exception e) {
//                System.out.println("Warning: Could not load language JSON file: " + e.getMessage());
//            }
//        }

        // 4. Process Date Parameters
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if (startDateStr != null && !startDateStr.equals("0")) {
                parameters.put("p_startDate", dateFormat.parse(startDateStr));
            }
            if (endDateStr != null && !endDateStr.equals("0")) {
                parameters.put("p_endDate", dateFormat.parse(endDateStr));
            }
        } catch (Exception e) {
            throw new JRException("Invalid Date Format. Expected yyyy-MM-dd", e);
        }

        // 5. Add other SP parameters
        parameters.put("p_clinicId", clinicId);

        // Default Title Fallback
        parameters.putIfAbsent("mainTitle", "Doctor Session Summary");
        parameters.putIfAbsent("companyName", "Cure Guard Clinic");

        // 6. Generate Report
        try (Connection connection = dataSource.getConnection()) {

            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);

            return outputStream;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new JRException("Database connection error", e);
        }
    }

    public ByteArrayOutputStream generateDoctorWiseSummaryReport(Integer clinicId, Long doctorId, String startDateStr, String endDateStr, String lang) throws JRException, IOException {

        final String reportPath = "/reports/SessionReport/DoctorWiseSummaryReport.jrxml";

        InputStream reportStream = getClass().getResourceAsStream(reportPath);
        if (reportStream == null) {
            throw new FileNotFoundException("Report file not found in resources: " + reportPath);
        }
        Map<String, Object> parameters = new HashMap<>();


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if (startDateStr != null && !startDateStr.equals("0")) {
                parameters.put("p_startDate", dateFormat.parse(startDateStr));
            }
            if (endDateStr != null && !endDateStr.equals("0")) {
                parameters.put("p_endDate", dateFormat.parse(endDateStr));
            }
        } catch (Exception e) {
            throw new JRException("Invalid Date Format. Expected yyyy-MM-dd", e);
        }

        parameters.put("p_clinicId", clinicId);
        parameters.put("p_doctor_id", null);

                parameters.putIfAbsent("mainTitle", "Doctor Session Summary");
        parameters.putIfAbsent("companyName", "Cure Guard Clinic");

        // 6. Generate Report
        try (Connection connection = dataSource.getConnection()) {

            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);

            return outputStream;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new JRException("Database connection error", e);
        }
    }
}