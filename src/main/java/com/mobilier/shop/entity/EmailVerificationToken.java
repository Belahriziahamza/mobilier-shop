package com.mobilier.shop.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;


@Entity
@Table(name = "email_verification_tokens")
public class EmailVerificationToken {


    /* =====================================================
       ID
    ===================================================== */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    /* =====================================================
       CLIENT
    ===================================================== */

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "customer_id",
            nullable = false,
            unique = true
    )
    private Customer customer;



    /* =====================================================
       HASH DU TOKEN

       Le vrai token n'est PAS enregistré en base.
       On garde uniquement son SHA-256.
    ===================================================== */

    @Column(
            name = "token_hash",
            nullable = false,
            unique = true,
            length = 64
    )
    private String tokenHash;



    /* =====================================================
       EXPIRATION
    ===================================================== */

    @Column(
            name = "expires_at",
            nullable = false
    )
    private LocalDateTime expiresAt;



    /* =====================================================
       DATE D'UTILISATION

       null = pas encore utilisé
    ===================================================== */

    @Column(name = "used_at")
    private LocalDateTime usedAt;



    /* =====================================================
       CREATION
    ===================================================== */

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;



    /* =====================================================
       PRE PERSIST
    ===================================================== */

    @PrePersist
    public void prePersist() {

        if (createdAt == null) {

            createdAt =
                    LocalDateTime.now();
        }
    }



    /* =====================================================
       UTILITAIRES
    ===================================================== */

    public boolean isExpired() {

        return expiresAt != null
                &&
                LocalDateTime.now()
                        .isAfter(
                                expiresAt
                        );
    }


    public boolean isUsed() {

        return usedAt != null;
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



    public Customer getCustomer() {

        return customer;
    }


    public void setCustomer(
            Customer customer
    ) {

        this.customer = customer;
    }



    public String getTokenHash() {

        return tokenHash;
    }


    public void setTokenHash(
            String tokenHash
    ) {

        this.tokenHash = tokenHash;
    }



    public LocalDateTime getExpiresAt() {

        return expiresAt;
    }


    public void setExpiresAt(
            LocalDateTime expiresAt
    ) {

        this.expiresAt = expiresAt;
    }



    public LocalDateTime getUsedAt() {

        return usedAt;
    }


    public void setUsedAt(
            LocalDateTime usedAt
    ) {

        this.usedAt = usedAt;
    }



    public LocalDateTime getCreatedAt() {

        return createdAt;
    }


    public void setCreatedAt(
            LocalDateTime createdAt
    ) {

        this.createdAt = createdAt;
    }
}