package com.mobilier.shop.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mobilier.shop.entity.Customer;
import com.mobilier.shop.repository.CustomerRepository;

@Service
public class CustomerUserDetailsService
        implements UserDetailsService {

    private final CustomerRepository customerRepository;


    public CustomerUserDetailsService(
            CustomerRepository customerRepository
    ) {

        this.customerRepository =
                customerRepository;
    }


    @Override
    public UserDetails loadUserByUsername(
            String email
    ) throws UsernameNotFoundException {

        Customer customer =
                customerRepository
                        .findByEmailIgnoreCase(email)
                        .orElseThrow(
                                () ->
                                        new UsernameNotFoundException(
                                                "Compte client introuvable."
                                        )
                        );


        return User
                .withUsername(
                        customer.getEmail()
                )
                .password(
                        customer.getPasswordHash()
                )
                .roles(
                        "CUSTOMER"
                )
                .disabled(
                        !customer.isActive()
                )
                .build();
    }
}