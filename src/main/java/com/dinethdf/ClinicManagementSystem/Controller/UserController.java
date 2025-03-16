package com.dinethdf.ClinicManagementSystem.Controller;

import com.dinethdf.ClinicManagementSystem.Model.User;
import com.dinethdf.ClinicManagementSystem.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("register")
    public User registerUser(@RequestBody User user){
        return userService.register(user);
    }

    @PostMapping("login")
    public String login(@RequestBody User user){
      return userService.login(user);
    }

}
