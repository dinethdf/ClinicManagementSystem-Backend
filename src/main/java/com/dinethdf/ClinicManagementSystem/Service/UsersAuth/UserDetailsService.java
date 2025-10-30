package com.dinethdf.ClinicManagementSystem.Service.UsersAuth;

import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserAuth;
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
        UserAuth userAuth = repo.findByEmail(email);

        if (userAuth == null){
            System.out.println("User Not Found");
            throw new UsernameNotFoundException("user Not Found");
        }

        return new UserPrincipal(userAuth);
    }

    public UserAuth getUserByEmail(String email) throws UsernameNotFoundException {
        UserAuth userAuth = repo.findByEmail(email);

        if (userAuth == null){
            System.out.println("User Not Found");
            throw new UsernameNotFoundException("user Not Found");
        }
        return userAuth;
    }
}
