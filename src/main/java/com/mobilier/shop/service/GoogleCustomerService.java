package com.mobilier.shop.service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilier.shop.entity.Customer;
import com.mobilier.shop.repository.CustomerRepository;

@Service
public class GoogleCustomerService {

    private final CustomerRepository customerRepository;

    private final PasswordEncoder passwordEncoder;

    /*
     * =====================================================
     * CONSTRUCTEUR
     * =====================================================
     */

    public GoogleCustomerService(

            CustomerRepository customerRepository,

            PasswordEncoder passwordEncoder) {

        this.customerRepository = customerRepository;

        this.passwordEncoder = passwordEncoder;
    }

    /*
     * =====================================================
     * CONNEXION / CREATION CLIENT GOOGLE
     * =====================================================
     */

    @Transactional
    public Customer loginOrCreateGoogleCustomer(
            OidcUser googleUser) {

        if (googleUser == null) {

            throw new IllegalArgumentException(
                    "Compte Google invalide.");
        }

        /*
         * =================================================
         * EMAIL FOURNI PAR GOOGLE
         * =================================================
         */

        String email = normalize(
                googleUser.getClaimAsString(
                        "email"));

        if (email.isBlank()) {

            throw new IllegalArgumentException(
                    "Google n'a pas fourni d'adresse email.");
        }

        /*
         * =================================================
         * VERIFICATION REELLE DE L'EMAIL PAR GOOGLE
         * =================================================
         */

        Object verifiedClaim = googleUser
                .getClaims()
                .get(
                        "email_verified");

        boolean googleEmailVerified =

                Boolean.TRUE.equals(
                        verifiedClaim)

                        ||

                        "true".equalsIgnoreCase(
                                String.valueOf(
                                        verifiedClaim));

        if (!googleEmailVerified) {

            throw new IllegalArgumentException(
                    "Cette adresse email n'a pas été vérifiée par Google.");
        }

        /*
         * =================================================
         * INFORMATIONS GOOGLE
         * =================================================
         */

        String firstName = clean(
                googleUser.getClaimAsString(
                        "given_name"));

        String lastName = clean(
                googleUser.getClaimAsString(
                        "family_name"));

        String fullName = clean(
                googleUser.getClaimAsString(
                        "name"));

        /*
         * =================================================
         * FALLBACK PRENOM
         * =================================================
         */

        if (firstName.isBlank()) {

            if (!fullName.isBlank()) {

                String[] parts = fullName.split(
                        "\\s+",
                        2);

                firstName = parts[0];

                if (lastName.isBlank()
                        &&
                        parts.length > 1) {

                    lastName = parts[1];
                }

            } else {

                firstName = "Client";
            }
        }

        /*
         * =================================================
         * NOM OBLIGATOIRE DANS CUSTOMER
         * =================================================
         */

        if (lastName.isBlank()) {

            lastName = "Google";
        }

        /*
         * =================================================
         * CLIENT EXISTANT
         * =================================================
         */

        Customer existingCustomer = customerRepository
                .findByEmailIgnoreCase(
                        email)
                .orElse(
                        null);

        if (existingCustomer != null) {

            /*
             * Le compte Google a confirmé
             * la propriété de cette adresse email.
             */

            existingCustomer.setAuthProvider(
                    "GOOGLE");

            existingCustomer.setEmailVerified(
                    true);

            existingCustomer.setLastLoginAt(
                    LocalDateTime.now());

            existingCustomer.setActive(
                    true);

            return customerRepository.save(
                    existingCustomer);
        }

        /*
         * =================================================
         * NOUVEAU CLIENT GOOGLE
         * =================================================
         */

        Customer customer = new Customer();

        customer.setFirstName(
                limit(
                        firstName,
                        100));

        customer.setLastName(
                limit(
                        lastName,
                        100));

        customer.setEmail(
                email);

        /*
         * Google ne fournit pas le téléphone
         * dans cette connexion.
         *
         * Le client pourra l'ajouter
         * ensuite dans son profil.
         */

        customer.setPhone(
                "");

        /*
         * Un client Google ne connaît pas
         * ce mot de passe.
         *
         * On génère une valeur aléatoire
         * uniquement parce que password_hash
         * est obligatoire dans la table.
         */

        String randomPassword = UUID.randomUUID().toString()
                +
                UUID.randomUUID().toString();

        customer.setPasswordHash(

                passwordEncoder.encode(
                        randomPassword));

        customer.setActive(
                true);

        customer.setAuthProvider(
                "GOOGLE");

        customer.setEmailVerified(
                true);

        customer.setLastLoginAt(
                LocalDateTime.now());

        return customerRepository.save(
                customer);
    }

    /*
     * =====================================================
     * NORMALISER EMAIL
     * =====================================================
     */

    private String normalize(
            String value) {

        if (value == null) {

            return "";
        }

        return value
                .trim()
                .toLowerCase(
                        Locale.ROOT);
    }

    /*
     * =====================================================
     * NETTOYAGE TEXTE
     * =====================================================
     */

    private String clean(
            String value) {

        if (value == null) {

            return "";
        }

        return value.trim();
    }

    /*
     * =====================================================
     * LIMITER LONGUEUR
     * =====================================================
     */

    private String limit(

            String value,

            int maxLength) {

        if (value == null) {

            return "";
        }

        String cleaned = value.trim();

        if (cleaned.length() <= maxLength) {

            return cleaned;
        }

        return cleaned.substring(
                0,
                maxLength);
    }
}