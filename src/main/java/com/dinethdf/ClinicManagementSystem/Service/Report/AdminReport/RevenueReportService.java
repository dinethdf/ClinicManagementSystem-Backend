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
public class RevenueReportService {

    @Autowired
    private DataSource dataSource;

    public ByteArrayOutputStream generateRevenueReport(Integer clinicId, String startDateStr, String endDateStr, String revenueType, String lang) throws JRException, IOException {

        // 1. Define File Paths (Relative to src/main/resources)
        // Ensure your .jrxml file is inside resources/reports/RevenueReport/
        final String reportPath = "/reports/RevenueReport/RevenueReport.jrxml";
        // Ensure your .json file is inside resources/languages/reports/RevenueReport/
//        final String jsonPath = "/languages/reports/RevenueReport/" + lang + ".json";

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
//        } else {
//            System.out.println("Warning: Language JSON file not found: " + jsonPath);
//        }


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            // Check if dates are not "0" or null
            if (startDateStr != null && !startDateStr.equals("0")) {
                Date startDate = dateFormat.parse(startDateStr);
                parameters.put("p_startDate", startDate);
            }
            if (endDateStr != null && !endDateStr.equals("0")) {
                Date endDate = dateFormat.parse(endDateStr);
                parameters.put("p_endDate", endDate);
            }
        } catch (Exception e) {
            throw new JRException("Invalid Date Format. Expected yyyy-MM-dd", e);
        }

        // 5. Add other SP parameters
        parameters.put("p_clinicId", clinicId);
        parameters.put("p_revenueType", revenueType);

        // Default title if JSON failed
        parameters.putIfAbsent("mainTitle", "Revenue Report");

        // 6. Generate Report
        try (Connection connection = dataSource.getConnection()) {

            // Compile the report
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // Fill the report
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

            // Export to PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);

            return outputStream;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new JRException("Database connection error", e);
        }
    }

}