package com.dinethdf.ClinicManagementSystem.Service.UsersAuth;

import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.User;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserPrincipal;
import com.dinethdf.ClinicManagementSystem.Repo.UsersAuth.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    @Autowired
    private UserRepo repo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = repo.findByEmail(email);

        if (user == null){
            System.out.println("User Not Found");
            throw new UsernameNotFoundException("user Not Found");
        }

        return new UserPrincipal(user);
    }

    public User getUserByEmail(String email) throws UsernameNotFoundException {
        User user = repo.findByEmail(email);

        if (user == null){
            System.out.println("User Not Found");
            throw new UsernameNotFoundException("user Not Found");
        }
        return user;
    }
}
