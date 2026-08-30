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
        name = "customer_cart_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_customer_cart_item",
                        columnNames = {
                                "customer_email",
                                "item_type",
                                "item_id"
                        }
                )
        }
)
public class CustomerCartItem {


    /* =========================================================
       TYPES D'ARTICLES
    ========================================================= */

    public static final String TYPE_PRODUCT =
            "PRODUCT";

    public static final String TYPE_PIECE =
            "PIECE";



    /* =========================================================
       ID
    ========================================================= */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    /* =========================================================
       CLIENT

       Chaque Gmail possède son propre panier.
    ========================================================= */

    @Column(
            name = "customer_email",
            nullable = false,
            length = 190
    )
    private String customerEmail;



    /* =========================================================
       TYPE

       PRODUCT = meuble normal
       PIECE   = pièce tapissier / couture / tissu
    ========================================================= */

    @Column(
            name = "item_type",
            nullable = false,
            length = 20
    )
    private String itemType;



    /* =========================================================
       ID DE L'ARTICLE

       Si PRODUCT :
       → Product.id

       Si PIECE :
       → Piece.id
    ========================================================= */

    @Column(
            name = "item_id",
            nullable = false
    )
    private Long itemId;



    /* =========================================================
       QUANTITE
    ========================================================= */

    @Column(
            name = "quantity",
            nullable = false
    )
    private Integer quantity = 1;



    /* =========================================================
       DATES
    ========================================================= */

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;


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


        if (quantity == null || quantity < 1) {

            quantity = 1;
        }


        createdAt =
                now;


        updatedAt =
                now;
    }



    /* =========================================================
       PRE UPDATE
    ========================================================= */

    @PreUpdate
    public void onUpdate() {

        if (quantity == null || quantity < 1) {

            quantity = 1;
        }


        updatedAt =
                LocalDateTime.now();
    }



    /* =========================================================
       CONSTRUCTEUR VIDE
    ========================================================= */

    public CustomerCartItem() {
    }



    /* =========================================================
       GETTERS / SETTERS
    ========================================================= */

    public Long getId() {

        return id;
    }


    public void setId(Long id) {

        this.id = id;
    }



    public String getCustomerEmail() {

        return customerEmail;
    }


    public void setCustomerEmail(
            String customerEmail
    ) {

        this.customerEmail =
                customerEmail;
    }



    public String getItemType() {

        return itemType;
    }


    public void setItemType(
            String itemType
    ) {

        this.itemType =
                itemType;
    }



    public Long getItemId() {

        return itemId;
    }


    public void setItemId(
            Long itemId
    ) {

        this.itemId =
                itemId;
    }



    public Integer getQuantity() {

        return quantity;
    }


    public void setQuantity(
            Integer quantity
    ) {

        this.quantity =
                quantity;
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