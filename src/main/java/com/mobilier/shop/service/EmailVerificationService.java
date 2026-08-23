package com.mobilier.shop.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilier.shop.entity.Customer;
import com.mobilier.shop.entity.EmailVerificationToken;
import com.mobilier.shop.repository.CustomerRepository;
import com.mobilier.shop.repository.EmailVerificationTokenRepository;


@Service
public class EmailVerificationService {


    private final EmailVerificationTokenRepository tokenRepository;

    private final CustomerRepository customerRepository;

    private final SecureRandom secureRandom =
            new SecureRandom();



    public EmailVerificationService(

            EmailVerificationTokenRepository tokenRepository,

            CustomerRepository customerRepository
    ) {

        this.tokenRepository =
                tokenRepository;

        this.customerRepository =
                customerRepository;
    }



    /* =====================================================
       GENERER TOKEN POUR UN CLIENT
    ===================================================== */

    @Transactional
    public String createVerificationToken(
            Customer customer
    ) {


        /*
         * Supprimer l'ancien token du client
         * s'il existe.
         */

        tokenRepository
                .findByCustomer(customer)
                .ifPresent(
                        tokenRepository::delete
                );


        /*
         * Générer 32 octets aléatoires.
         */

        byte[] randomBytes =
                new byte[32];


        secureRandom.nextBytes(
                randomBytes
        );


        /*
         * Token envoyé au client.
         */

        String rawToken =
                Base64
                        .getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(
                                randomBytes
                        );


        /*
         * Hash stocké en base.
         */

        String tokenHash =
                hashToken(
                        rawToken
                );


        EmailVerificationToken verificationToken =
                new EmailVerificationToken();


        verificationToken.setCustomer(
                customer
        );


        verificationToken.setTokenHash(
                tokenHash
        );


        /*
         * Expiration : 24 heures.
         */

        verificationToken.setExpiresAt(
                LocalDateTime.now()
                        .plusHours(24)
        );


        verificationToken.setUsedAt(
                null
        );


        tokenRepository.save(
                verificationToken
        );


        return rawToken;
    }



    /* =====================================================
       VERIFIER TOKEN
    ===================================================== */

    @Transactional
    public Customer verifyEmail(
            String rawToken
    ) {


        if (
                rawToken == null ||
                rawToken.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Token de vérification invalide."
            );
        }


        String tokenHash =
                hashToken(
                        rawToken
                );


        EmailVerificationToken verificationToken =
                tokenRepository
                        .findByTokenHash(
                                tokenHash
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Lien de vérification invalide."
                                        )
                        );


        if (
                verificationToken.isUsed()
        ) {

            throw new IllegalArgumentException(
                    "Ce lien de vérification a déjà été utilisé."
            );
        }


        if (
                verificationToken.isExpired()
        ) {

            throw new IllegalArgumentException(
                    "Ce lien de vérification a expiré."
            );
        }


        Customer customer =
                verificationToken
                        .getCustomer();


        /*
         * Valider email.
         */

        customer.setEmailVerified(
                true
        );


        customerRepository.save(
                customer
        );


        /*
         * Marquer token utilisé.
         */

        verificationToken.setUsedAt(
                LocalDateTime.now()
        );


        tokenRepository.save(
                verificationToken
        );


        return customer;
    }



    /* =====================================================
       HASH SHA-256
    ===================================================== */

    private String hashToken(
            String rawToken
    ) {


        try {


            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );


            byte[] hash =
                    digest.digest(

                            rawToken
                                    .getBytes(
                                            StandardCharsets.UTF_8
                                    )
                    );


            return HexFormat
                    .of()
                    .formatHex(
                            hash
                    );


        } catch (
                NoSuchAlgorithmException e
        ) {


            throw new IllegalStateException(
                    "SHA-256 indisponible.",
                    e
            );
        }
    }
}