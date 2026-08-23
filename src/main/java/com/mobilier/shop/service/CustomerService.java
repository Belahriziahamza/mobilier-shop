package com.mobilier.shop.service;

import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilier.shop.dto.RegisterCustomerRequest;
import com.mobilier.shop.entity.Customer;
import com.mobilier.shop.repository.CustomerRepository;


@Service
public class CustomerService {


    /* =====================================================
       REPOSITORY
    ===================================================== */

    private final CustomerRepository customerRepository;


    /* =====================================================
       PASSWORD ENCODER
    ===================================================== */

    private final PasswordEncoder passwordEncoder;



    /* =====================================================
       CONSTRUCTEUR
    ===================================================== */

    public CustomerService(

            CustomerRepository customerRepository,

            PasswordEncoder passwordEncoder
    ) {

        this.customerRepository =
                customerRepository;

        this.passwordEncoder =
                passwordEncoder;
    }



    /* =====================================================
       INSCRIPTION CLIENT
    ===================================================== */

    @Transactional
    public Customer register(
            RegisterCustomerRequest request
    ) {


        String email =
                normalizeEmail(
                        request.email()
                );


        /* ===============================================
           VERIFIER EMAIL
        =============================================== */

        if (
                email.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "L'adresse email est obligatoire."
            );
        }


        if (
                customerRepository
                        .existsByEmailIgnoreCase(
                                email
                        )
        ) {

            throw new IllegalArgumentException(
                    "Un compte existe déjà avec cette adresse email."
            );
        }



        /* ===============================================
           VERIFIER MOT DE PASSE
        =============================================== */

        if (
                request.password() == null ||
                request.password().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Le mot de passe est obligatoire."
            );
        }


        if (
                request.password().length() < 8
        ) {

            throw new IllegalArgumentException(
                    "Le mot de passe doit contenir au moins 8 caractères."
            );
        }


        if (
                !request.password()
                        .equals(
                                request.confirmPassword()
                        )
        ) {

            throw new IllegalArgumentException(
                    "Les deux mots de passe ne correspondent pas."
            );
        }



        /* ===============================================
           CREATION CLIENT
        =============================================== */

        Customer customer =
                new Customer();



        customer.setFirstName(
                request.firstName()
                        .trim()
        );


        customer.setLastName(
                request.lastName()
                        .trim()
        );


        customer.setEmail(
                email
        );


        customer.setPhone(
                request.phone()
                        .trim()
        );



        /* ===============================================
           BCRYPT
        =============================================== */

        customer.setPasswordHash(

                passwordEncoder.encode(
                        request.password()
                )
        );



        /* ===============================================
           COMPTE ACTIF
        =============================================== */

        customer.setActive(
                true
        );



        /* ===============================================
           TYPE DE CONNEXION

           Une inscription classique est LOCAL.
        =============================================== */

        customer.setAuthProvider(
                "LOCAL"
        );



        /* ===============================================
           EMAIL NON VERIFIE AU DEPART

           Plus tard :
           email de confirmation
           puis passage à true.
        =============================================== */

        customer.setEmailVerified(
                false
        );



        /* ===============================================
           PAS ENCORE CONNECTE
        =============================================== */

        customer.setLastLoginAt(
                null
        );



        return customerRepository.save(
                customer
        );
    }



    /* =====================================================
       TROUVER CLIENT PAR EMAIL
    ===================================================== */

    @Transactional(readOnly = true)
    public Customer findByEmail(
            String email
    ) {

        return customerRepository
                .findByEmailIgnoreCase(

                        normalizeEmail(
                                email
                        )
                )
                .orElseThrow(

                        () ->
                                new IllegalArgumentException(
                                        "Compte client introuvable."
                                )
                );
    }



    /* =====================================================
       ENREGISTRER DERNIERE CONNEXION

       Cette méthode devra être appelée uniquement
       après une authentification réussie.
    ===================================================== */

    @Transactional
    public void updateLastLogin(
            String email
    ) {


        Customer customer =
                findByEmail(
                        email
                );


        customer.setLastLoginAt(
                LocalDateTime.now()
        );


        customerRepository.save(
                customer
        );
    }



    /* =====================================================
       MODIFIER PROFIL CLIENT
    ===================================================== */

    @Transactional
    public Customer updateProfile(

            String email,

            String firstName,

            String lastName,

            String phone,

            String city,

            String address
    ) {


        Customer customer =
                findByEmail(
                        email
                );



        /* ===============================================
           PRENOM
        =============================================== */

        if (
                firstName == null ||
                firstName.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Le prénom est obligatoire."
            );
        }


        if (
                firstName.trim().length() > 100
        ) {

            throw new IllegalArgumentException(
                    "Le prénom est trop long."
            );
        }



        /* ===============================================
           NOM
        =============================================== */

        if (
                lastName == null ||
                lastName.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Le nom est obligatoire."
            );
        }


        if (
                lastName.trim().length() > 100
        ) {

            throw new IllegalArgumentException(
                    "Le nom est trop long."
            );
        }



        /* ===============================================
           TELEPHONE
        =============================================== */

        if (
                phone == null ||
                phone.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Le téléphone est obligatoire."
            );
        }


        if (
                phone.trim().length() > 30
        ) {

            throw new IllegalArgumentException(
                    "Le numéro de téléphone est trop long."
            );
        }



        /* ===============================================
           VILLE
        =============================================== */

        if (
                city != null &&
                city.trim().length() > 100
        ) {

            throw new IllegalArgumentException(
                    "Le nom de la ville est trop long."
            );
        }



        /* ===============================================
           ADRESSE
        =============================================== */

        if (
                address != null &&
                address.trim().length() > 500
        ) {

            throw new IllegalArgumentException(
                    "L'adresse est trop longue."
            );
        }



        /* ===============================================
           MISE A JOUR
        =============================================== */

        customer.setFirstName(
                firstName.trim()
        );


        customer.setLastName(
                lastName.trim()
        );


        customer.setPhone(
                phone.trim()
        );


        customer.setCity(
                cleanOptional(
                        city
                )
        );


        customer.setAddress(
                cleanOptional(
                        address
                )
        );


        /*
         * L'email et le mot de passe
         * ne sont pas modifiés ici.
         */

        return customerRepository.save(
                customer
        );
    }



    /* =====================================================
       CHANGER MOT DE PASSE CLIENT
    ===================================================== */

    @Transactional
    public void changePassword(

            String email,

            String currentPassword,

            String newPassword,

            String confirmNewPassword
    ) {


        Customer customer =
                findByEmail(
                        email
                );



        /* ===============================================
           MOT DE PASSE ACTUEL OBLIGATOIRE
        =============================================== */

        if (
                currentPassword == null ||
                currentPassword.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Le mot de passe actuel est obligatoire."
            );
        }



        /* ===============================================
           VERIFIER ANCIEN MOT DE PASSE
        =============================================== */

        boolean currentPasswordCorrect =
                passwordEncoder.matches(

                        currentPassword,

                        customer.getPasswordHash()
                );


        if (!currentPasswordCorrect) {

            throw new IllegalArgumentException(
                    "Le mot de passe actuel est incorrect."
            );
        }



        /* ===============================================
           NOUVEAU MOT DE PASSE
        =============================================== */

        if (
                newPassword == null ||
                newPassword.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Le nouveau mot de passe est obligatoire."
            );
        }


        if (
                newPassword.length() < 8
        ) {

            throw new IllegalArgumentException(
                    "Le nouveau mot de passe doit contenir au moins 8 caractères."
            );
        }


        if (
                newPassword.length() > 100
        ) {

            throw new IllegalArgumentException(
                    "Le nouveau mot de passe est trop long."
            );
        }



        /* ===============================================
           CONFIRMATION
        =============================================== */

        if (
                confirmNewPassword == null ||
                confirmNewPassword.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Vous devez confirmer le nouveau mot de passe."
            );
        }


        if (
                !newPassword.equals(
                        confirmNewPassword
                )
        ) {

            throw new IllegalArgumentException(
                    "La confirmation du nouveau mot de passe ne correspond pas."
            );
        }



        /* ===============================================
           INTERDIRE MEME MOT DE PASSE
        =============================================== */

        boolean samePassword =
                passwordEncoder.matches(

                        newPassword,

                        customer.getPasswordHash()
                );


        if (samePassword) {

            throw new IllegalArgumentException(
                    "Le nouveau mot de passe doit être différent de l'ancien."
            );
        }



        /* ===============================================
           GENERER NOUVEAU HASH BCRYPT
        =============================================== */

        String newPasswordHash =
                passwordEncoder.encode(
                        newPassword
                );



        /* ===============================================
           MYSQL
        =============================================== */

        customer.setPasswordHash(
                newPasswordHash
        );


        customerRepository.save(
                customer
        );
    }



    /* =====================================================
       NORMALISER EMAIL
    ===================================================== */

    private String normalizeEmail(
            String email
    ) {

        if (email == null) {

            return "";
        }


        return email
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }



    /* =====================================================
       NETTOYER CHAMP OPTIONNEL
    ===================================================== */

    private String cleanOptional(
            String value
    ) {

        if (
                value == null ||
                value.isBlank()
        ) {

            return null;
        }


        return value.trim();
    }
}