package com.in28minutes.soap.webservices.soapcoursemanagement.soap.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class SoapUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if(!username.equalsIgnoreCase("Huy Le")) {
            throw new UsernameNotFoundException("User not found.");
        }

        User.UserBuilder builder =
                User.withUsername("Huy Le").password("password").roles("ADMIN");

        return builder.build();
    }
}
