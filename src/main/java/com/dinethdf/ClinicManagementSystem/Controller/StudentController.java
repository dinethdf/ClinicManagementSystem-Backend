package com.dinethdf.ClinicManagementSystem.Controller;

import com.dinethdf.ClinicManagementSystem.Model.Student;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class StudentController {

    private List<Student> students = new ArrayList<>(
            List.of(
                    new Student(1,"Dineth", 60),
                    new Student(2,"Dilshan", 70),
                    new Student(3,"Fonseka", 80),
                    new Student(4,"Maheshika", 90)
            ));

    @GetMapping("/students")
    public List<Student> getStudent(){
        System.out.println("get Result");
        return students;
    }

    @GetMapping("/csrf-token")
    public CsrfToken getCrfToken(HttpServletRequest request){
        return (CsrfToken) request.getAttribute("_csrf");
    }

    @PostMapping("/students")
    public Student addStudent(@RequestBody Student student){
        students.add(student);
        return student;
    }
}
