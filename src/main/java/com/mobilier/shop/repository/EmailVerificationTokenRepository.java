package com.mobilier.shop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilier.shop.entity.Customer;
import com.mobilier.shop.entity.EmailVerificationToken;


public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, Long> {


    /* =====================================================
       TROUVER PAR HASH DU TOKEN
    ===================================================== */

    Optional<EmailVerificationToken> findByTokenHash(
            String tokenHash
    );


    /* =====================================================
       TROUVER LE TOKEN D'UN CLIENT
    ===================================================== */

    Optional<EmailVerificationToken> findByCustomer(
            Customer customer
    );


    /* =====================================================
       SUPPRIMER ANCIEN TOKEN DU CLIENT
    ===================================================== */

    void deleteByCustomer(
            Customer customer
    );
}