package com.mobilier.shop.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;


@Entity
@Table(
        name = "customers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_customer_email",
                        columnNames = "email"
                )
        }
)
public class Customer {


    /* =====================================================
       ID
    ===================================================== */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    /* =====================================================
       INFORMATIONS PERSONNELLES
    ===================================================== */

    @Column(
            nullable = false,
            length = 100
    )
    private String firstName;


    @Column(
            nullable = false,
            length = 100
    )
    private String lastName;


    @Column(
            nullable = false,
            unique = true,
            length = 180
    )
    private String email;


    @Column(
            nullable = false,
            length = 30
    )
    private String phone;


    @Column(
            name = "password_hash",
            nullable = false,
            length = 255
    )
    private String passwordHash;


    @Column(length = 100)
    private String city;


    @Column(length = 500)
    private String address;



    /* =====================================================
       ETAT DU COMPTE
    ===================================================== */

    @Column(nullable = false)
    private boolean active = true;



    /* =====================================================
       TYPE DE CONNEXION

       LOCAL  = inscription classique
       GOOGLE = connexion Google
    ===================================================== */

    @Column(
            name = "auth_provider",
            nullable = false,
            length = 20,
            columnDefinition = "varchar(20) default 'LOCAL'"
    )
    private String authProvider = "LOCAL";



    /* =====================================================
       EMAIL VERIFIE

       false = email non vérifié
       true  = email vérifié
    ===================================================== */

    @Column(
            name = "email_verified",
            nullable = false,
            columnDefinition = "boolean default false"
    )
    private boolean emailVerified = false;



    /* =====================================================
       DERNIERE CONNEXION
    ===================================================== */

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;



    /* =====================================================
       DATES
    ===================================================== */

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;


    private LocalDateTime updatedAt;



    /* =====================================================
       PRE PERSIST
    ===================================================== */

    @PrePersist
    public void prePersist() {

        LocalDateTime now =
                LocalDateTime.now();


        createdAt = now;

        updatedAt = now;


        /*
         * Sécurité :
         * tout nouveau compte classique est LOCAL
         * si aucun provider n'a été défini.
         */

        if (
                authProvider == null ||
                authProvider.isBlank()
        ) {

            authProvider = "LOCAL";
        }
    }



    /* =====================================================
       PRE UPDATE
    ===================================================== */

    @PreUpdate
    public void preUpdate() {

        updatedAt =
                LocalDateTime.now();
    }



    /* =====================================================
       GETTERS / SETTERS
    ===================================================== */

    public Long getId() {

        return id;
    }


    public void setId(
            Long id
    ) {

        this.id = id;
    }



    public String getFirstName() {

        return firstName;
    }


    public void setFirstName(
            String firstName
    ) {

        this.firstName = firstName;
    }



    public String getLastName() {

        return lastName;
    }


    public void setLastName(
            String lastName
    ) {

        this.lastName = lastName;
    }



    public String getEmail() {

        return email;
    }


    public void setEmail(
            String email
    ) {

        this.email = email;
    }



    public String getPhone() {

        return phone;
    }


    public void setPhone(
            String phone
    ) {

        this.phone = phone;
    }



    public String getPasswordHash() {

        return passwordHash;
    }


    public void setPasswordHash(
            String passwordHash
    ) {

        this.passwordHash = passwordHash;
    }



    public String getCity() {

        return city;
    }


    public void setCity(
            String city
    ) {

        this.city = city;
    }



    public String getAddress() {

        return address;
    }


    public void setAddress(
            String address
    ) {

        this.address = address;
    }



    public boolean isActive() {

        return active;
    }


    public void setActive(
            boolean active
    ) {

        this.active = active;
    }



    /* =====================================================
       AUTH PROVIDER
    ===================================================== */

    public String getAuthProvider() {

        /*
         * Compatibilité avec les anciens clients
         * déjà présents dans MySQL.
         */

        if (
                authProvider == null ||
                authProvider.isBlank()
        ) {

            return "LOCAL";
        }


        return authProvider;
    }


    public void setAuthProvider(
            String authProvider
    ) {

        this.authProvider = authProvider;
    }



    /* =====================================================
       EMAIL VERIFIED
    ===================================================== */

    public boolean isEmailVerified() {

        return emailVerified;
    }


    public void setEmailVerified(
            boolean emailVerified
    ) {

        this.emailVerified = emailVerified;
    }



    /* =====================================================
       LAST LOGIN
    ===================================================== */

    public LocalDateTime getLastLoginAt() {

        return lastLoginAt;
    }


    public void setLastLoginAt(
            LocalDateTime lastLoginAt
    ) {

        this.lastLoginAt = lastLoginAt;
    }



    /* =====================================================
       CREATED AT
    ===================================================== */

    public LocalDateTime getCreatedAt() {

        return createdAt;
    }


    public void setCreatedAt(
            LocalDateTime createdAt
    ) {

        this.createdAt = createdAt;
    }



    /* =====================================================
       UPDATED AT
    ===================================================== */

    public LocalDateTime getUpdatedAt() {

        return updatedAt;
    }


    public void setUpdatedAt(
            LocalDateTime updatedAt
    ) {

        this.updatedAt = updatedAt;
    }
}