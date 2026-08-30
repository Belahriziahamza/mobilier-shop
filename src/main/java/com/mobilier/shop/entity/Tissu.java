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
import jakarta.validation.constraints.NotBlank;


@Entity
@Table(name = "tissus")
public class Tissu {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotBlank
    @Column(
            nullable = false,
            length = 150
    )
    private String name;


    @Column(
            unique = true,
            length = 80
    )
    private String reference;


    @Column(length = 80)
    private String type;


    @Column(length = 2000)
    private String description;


    @Column(
            name = "image_path",
            length = 500
    )
    private String imagePath;


    @Column(nullable = false)
    private boolean available = true;


    @Column(nullable = false)
    private boolean active = true;


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

        this.createdAt = now;
        this.updatedAt = now;
    }



    /* =====================================================
       PRE UPDATE
    ===================================================== */

    @PreUpdate
    public void preUpdate() {

        this.updatedAt =
                LocalDateTime.now();
    }



    /* =====================================================
       ID
    ===================================================== */

    public Long getId() {

        return this.id;
    }


    public void setId(Long id) {

        this.id = id;
    }



    /* =====================================================
       NAME
    ===================================================== */

    public String getName() {

        return this.name;
    }


    public void setName(String name) {

        this.name = name;
    }



    /* =====================================================
       REFERENCE
    ===================================================== */

    public String getReference() {

        return this.reference;
    }


    public void setReference(
            String reference
    ) {

        this.reference = reference;
    }



    /* =====================================================
       TYPE
    ===================================================== */

    public String getType() {

        return this.type;
    }


    public void setType(String type) {

        this.type = type;
    }



    /* =====================================================
       DESCRIPTION
    ===================================================== */

    public String getDescription() {

        return this.description;
    }


    public void setDescription(
            String description
    ) {

        this.description = description;
    }



    /* =====================================================
       IMAGE PATH
    ===================================================== */

    public String getImagePath() {

        return this.imagePath;
    }


    public void setImagePath(
            String imagePath
    ) {

        this.imagePath = imagePath;
    }



    /* =====================================================
       AVAILABLE
    ===================================================== */

    public boolean isAvailable() {

        return this.available;
    }


    public void setAvailable(
            boolean available
    ) {

        this.available = available;
    }



    /* =====================================================
       ACTIVE
    ===================================================== */

    public boolean isActive() {

        return this.active;
    }


    public void setActive(
            boolean active
    ) {

        this.active = active;
    }



    /* =====================================================
       CREATED AT
    ===================================================== */

    public LocalDateTime getCreatedAt() {

        return this.createdAt;
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

        return this.updatedAt;
    }


    public void setUpdatedAt(
            LocalDateTime updatedAt
    ) {

        this.updatedAt = updatedAt;
    }

}