package com.dinethdf.ClinicManagementSystem.Controller.Sampletest;

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
    @RequestMapping(method = RequestMethod.OPTIONS, value = "/options")
    public ResponseEntity<?> optionsTest() {
        return ResponseEntity.ok().build();
    }
}
