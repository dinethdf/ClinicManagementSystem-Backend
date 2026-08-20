package com.dinethdf.ClinicManagementSystem.Controller.Sampletest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/")
public class TestController {

    // GET endpoint
    @GetMapping("/hello")
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hello from GET endpoint!");
    }

    @GetMapping("/hello/a")
    public ResponseEntity<String> sayHello2() {
        return ResponseEntity.ok("Hello from GET 2 endpoint!");
    }

    @GetMapping("/hello/b")
    public ResponseEntity<String> sayHello3() {
        return ResponseEntity.ok("Hello from GET 3 endpoint!");
    }

    // GET endpoint
    @GetMapping("/good")
    public ResponseEntity<String> sayGood() {
        return ResponseEntity.ok("good from this is test 2 hewck endpoint!");
    }

    @GetMapping("/good/a")
    public ResponseEntity<String> sayGood2() {
        return ResponseEntity.ok("good from GET 2 endpoint!");
    }

    @GetMapping("/good/b")
    public ResponseEntity<String> sayGood3() {
        return ResponseEntity.ok("good from GET 3 endpoint!");
    }

    // POST endpoint
    @PostMapping("/submit")
    public ResponseEntity<String> submitData(@RequestBody String data) {
        return ResponseEntity.ok("Received via POST: " + data);
    }

    // PUT endpoint
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateData(@PathVariable("id") int id, @RequestBody String data) {
        return ResponseEntity.ok("Updated ID " + id + " with: " + data);
    }

    // DELETE endpoint
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteData(@PathVariable("id") int id) {
        return ResponseEntity.ok("Deleted item with ID: " + id);
    }

    // OPTIONS test (CORS pre-flight requests usually use this)
    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @GetMapping("/run-sql")
    public org.springframework.http.ResponseEntity<String> runSql() {
        try {
            String sql = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("DB/7_sp_CancelAppointment")));
            // split by DELIMITER $$ is tricky in jdbc, it's better to just extract the CREATE PROCEDURE part
            // Actually, we can just execute the raw query if we remove DELIMITER $$
            sql = sql.replace("DELIMITER $$", "").replace("DELIMITER ;", "").replace("$$", ";");
            jdbcTemplate.execute("DROP PROCEDURE IF EXISTS sp_CancelAppointment");
            jdbcTemplate.execute(sql.substring(sql.indexOf("CREATE PROCEDURE")));
            return org.springframework.http.ResponseEntity.ok("SQL Executed Successfully");
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @RequestMapping(method = RequestMethod.OPTIONS, value = "/options")
    public ResponseEntity<?> optionsTest() {
        return ResponseEntity.ok().build();
    }
}
