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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScheduleReportService {

    @Autowired
    private DataSource dataSource;

    public ByteArrayOutputStream generateScheduleFinancialReport(Integer clinicId, Integer scheduleId, String lang) throws JRException, IOException {

        // 1. Define File Paths (Relative to src/main/resources)
        // Ensure you create this folder structure
        final String reportPath = "/reports/ScheduleReport/ScheduleFinancialsReport.jrxml";

//        final String jsonPath = "/languages/reports/ScheduleReport/" + lang + ".json";

        InputStream reportStream = getClass().getResourceAsStream(reportPath);

//        InputStream jsonStream = getClass().getResourceAsStream(jsonPath);

        if (reportStream == null) {
            throw new FileNotFoundException("Report file not found in resources: " + reportPath);
        }

        // 3. Prepare Parameters
        Map<String, Object> parameters = new HashMap<>();

//
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

        // 4. Add SP Parameters
        parameters.put("p_clinicId", clinicId);
        parameters.put("p_scheduleId", scheduleId);

        // Default title fallback
        parameters.putIfAbsent("mainTitle", "Schedule Financial Report");
        parameters.putIfAbsent("companyName", "Cure Guard Clinic");

        // 5. Generate Report
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