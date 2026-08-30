package com.mobilier.shop.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


@Entity
@Table(name = "pieces")
public class Piece {


    /* =========================================================
       ID
    ========================================================= */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    /* =========================================================
       CATALOGUE

       TAPISSERIE
       COUTURE
       MATIERES_TISSUS
    ========================================================= */

    @NotBlank(message = "Le catalogue est obligatoire.")
    @Column(nullable = false, length = 50)
    private String catalogue;



    /* =========================================================
       NOM
    ========================================================= */

    @NotBlank(message = "Le nom de la pièce est obligatoire.")
    @Size(
            max = 150,
            message = "Le nom ne doit pas dépasser 150 caractères."
    )
    @Column(nullable = false, length = 150)
    private String name;



    /* =========================================================
       TYPE

       Exemple :
       Pied bois noir
       Colle mousse
       Sfifa dorée
       Rouleau de fil
    ========================================================= */

    @NotBlank(message = "Le type de la pièce est obligatoire.")
    @Size(
            max = 150,
            message = "Le type ne doit pas dépasser 150 caractères."
    )
    @Column(nullable = false, length = 150)
    private String type;



    /* =========================================================
       PRIX
    ========================================================= */

    @NotNull(message = "Le prix est obligatoire.")
    @DecimalMin(
            value = "0.00",
            inclusive = true,
            message = "Le prix doit être supérieur ou égal à 0."
    )
    @Column(
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal price;



    /* =========================================================
       DESCRIPTION
    ========================================================= */

    @Size(
            max = 2000,
            message = "La description ne doit pas dépasser 2000 caractères."
    )
    @Column(
            length = 2000
    )
    private String description;



    /* =========================================================
       PHOTO
    ========================================================= */

    @Column(
            name = "image_path",
            length = 500
    )
    private String imagePath;



    /* =========================================================
       DISPONIBILITE

       true  = Disponible
       false = Non disponible
    ========================================================= */

    @Column(
            nullable = false
    )
    private boolean available = true;



    /* =========================================================
       DATE CREATION
    ========================================================= */

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;



    /* =========================================================
       DATE MODIFICATION
    ========================================================= */

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;



    /* =========================================================
       PRE PERSIST
    ========================================================= */

    @PrePersist
    public void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();


        this.createdAt =
                now;


        this.updatedAt =
                now;
    }



    /* =========================================================
       PRE UPDATE
    ========================================================= */

    @PreUpdate
    public void onUpdate() {

        this.updatedAt =
                LocalDateTime.now();
    }



    /* =========================================================
       CONSTRUCTEUR VIDE
    ========================================================= */

    public Piece() {
    }



    /* =========================================================
       GETTERS / SETTERS
    ========================================================= */

    public Long getId() {

        return id;
    }


    public void setId(
            Long id
    ) {

        this.id =
                id;
    }



    public String getCatalogue() {

        return catalogue;
    }


    public void setCatalogue(
            String catalogue
    ) {

        this.catalogue =
                catalogue;
    }



    public String getName() {

        return name;
    }


    public void setName(
            String name
    ) {

        this.name =
                name;
    }



    public String getType() {

        return type;
    }


    public void setType(
            String type
    ) {

        this.type =
                type;
    }



    public BigDecimal getPrice() {

        return price;
    }


    public void setPrice(
            BigDecimal price
    ) {

        this.price =
                price;
    }



    public String getDescription() {

        return description;
    }


    public void setDescription(
            String description
    ) {

        this.description =
                description;
    }



    public String getImagePath() {

        return imagePath;
    }


    public void setImagePath(
            String imagePath
    ) {

        this.imagePath =
                imagePath;
    }



    public boolean isAvailable() {

        return available;
    }


    public void setAvailable(
            boolean available
    ) {

        this.available =
                available;
    }



    public LocalDateTime getCreatedAt() {

        return createdAt;
    }


    public void setCreatedAt(
            LocalDateTime createdAt
    ) {

        this.createdAt =
                createdAt;
    }



    public LocalDateTime getUpdatedAt() {

        return updatedAt;
    }


    public void setUpdatedAt(
            LocalDateTime updatedAt
    ) {

        this.updatedAt =
                updatedAt;
    }

}