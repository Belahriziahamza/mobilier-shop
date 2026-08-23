package com.mobilier.shop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilier.shop.entity.Customer;

public interface CustomerRepository
        extends JpaRepository<Customer, Long> {


    Optional<Customer>
    findByEmailIgnoreCase(
            String email
    );


    boolean existsByEmailIgnoreCase(
            String email
    );
}