package com.mobilier.shop.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;


@Entity
@Table(name = "order_items")
public class OrderItem {


    /* =========================================================
       TYPES
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
       COMMANDE
    ========================================================= */

    @JsonIgnore
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "order_id",
            nullable = false
    )
    private CustomerOrder order;



    /* =========================================================
       TYPE ARTICLE

       PRODUCT
       PIECE

       nullable volontairement pour rester compatible
       avec les anciennes commandes déjà enregistrées.
    ========================================================= */

    @Column(
            name = "item_type",
            length = 20
    )
    private String itemType =
            TYPE_PRODUCT;



    /* =========================================================
       ID ARTICLE

       IMPORTANT :

       On garde le nom product_id dans MySQL pour ne pas casser
       les anciennes commandes.

       Si itemType = PRODUCT
       → productId contient Product.id

       Si itemType = PIECE
       → productId contient Piece.id
    ========================================================= */

    @Column(
            name = "product_id",
            nullable = false
    )
    private Long productId;



    /* =========================================================
       NOM ARTICLE

       On garde product_name pour compatibilité avec
       les anciennes pages Thymeleaf.

       Pour une pièce, ce champ contient simplement
       le nom de la pièce.
    ========================================================= */

    @Column(
            name = "product_name",
            nullable = false,
            length = 255
    )
    private String productName;



    /* =========================================================
       TYPE / DETAIL ARTICLE

       Exemple :

       Produit :
       "Canapé d'angle"

       Pièce :
       "Pied Aluminium"
    ========================================================= */

    @Column(
            name = "item_detail",
            length = 255
    )
    private String itemDetail;



    /* =========================================================
       IMAGE
    ========================================================= */

    @Column(
            name = "image_path",
            length = 500
    )
    private String imagePath;



    /* =========================================================
       PRIX UNITAIRE
    ========================================================= */

    @Column(
            name = "unit_price",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal unitPrice =
            BigDecimal.ZERO;



    /* =========================================================
       QUANTITE
    ========================================================= */

    @Column(
            name = "quantity",
            nullable = false
    )
    private Integer quantity =
            1;



    /* =========================================================
       TOTAL LIGNE
    ========================================================= */

    @Column(
            name = "line_total",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal lineTotal =
            BigDecimal.ZERO;



    /* =========================================================
       CONSTRUCTEUR
    ========================================================= */

    public OrderItem() {
    }



    /* =========================================================
       PRE PERSIST
    ========================================================= */

    @PrePersist
    public void onCreate() {

        normalizeValues();
    }



    /* =========================================================
       PRE UPDATE
    ========================================================= */

    @PreUpdate
    public void onUpdate() {

        normalizeValues();
    }



    /* =========================================================
       NORMALISATION
    ========================================================= */

    private void normalizeValues() {


        /* =========================
           TYPE
        ========================= */

        if (
                itemType == null
                ||
                itemType.isBlank()
        ) {

            itemType =
                    TYPE_PRODUCT;
        }


        itemType =
                itemType
                        .trim()
                        .toUpperCase();


        if (
                !TYPE_PRODUCT.equals(
                        itemType
                )
                &&
                !TYPE_PIECE.equals(
                        itemType
                )
        ) {

            itemType =
                    TYPE_PRODUCT;
        }



        /* =========================
           QUANTITE
        ========================= */

        if (
                quantity == null
                ||
                quantity < 1
        ) {

            quantity =
                    1;
        }



        /* =========================
           PRIX
        ========================= */

        if (unitPrice == null) {

            unitPrice =
                    BigDecimal.ZERO;
        }



        /* =========================
           TOTAL
        ========================= */

        lineTotal =
                unitPrice.multiply(
                        BigDecimal.valueOf(
                                quantity
                        )
                );
    }



    /* =========================================================
       GET ID
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



    /* =========================================================
       ORDER
    ========================================================= */

    public CustomerOrder getOrder() {

        return order;
    }


    public void setOrder(
            CustomerOrder order
    ) {

        this.order =
                order;
    }



    /* =========================================================
       ITEM TYPE
    ========================================================= */

    public String getItemType() {

        if (
                itemType == null
                ||
                itemType.isBlank()
        ) {

            return TYPE_PRODUCT;
        }


        return itemType;
    }


    public void setItemType(
            String itemType
    ) {

        this.itemType =
                itemType;
    }



    /* =========================================================
       PRODUCT ID
       ANCIENNE COMPATIBILITE
    ========================================================= */

    public Long getProductId() {

        return productId;
    }


    public void setProductId(
            Long productId
    ) {

        this.productId =
                productId;
    }



    /* =========================================================
       ITEM ID
       NOUVELLE API GENERIQUE

       PRODUCT ou PIECE
    ========================================================= */

    public Long getItemId() {

        return productId;
    }


    public void setItemId(
            Long itemId
    ) {

        this.productId =
                itemId;
    }



    /* =========================================================
       PRODUCT NAME
       ANCIENNE COMPATIBILITE
    ========================================================= */

    public String getProductName() {

        return productName;
    }


    public void setProductName(
            String productName
    ) {

        this.productName =
                productName;
    }



    /* =========================================================
       ITEM NAME
       NOUVELLE API GENERIQUE
    ========================================================= */

    public String getItemName() {

        return productName;
    }


    public void setItemName(
            String itemName
    ) {

        this.productName =
                itemName;
    }



    /* =========================================================
       DETAIL
    ========================================================= */

    public String getItemDetail() {

        return itemDetail;
    }


    public void setItemDetail(
            String itemDetail
    ) {

        this.itemDetail =
                itemDetail;
    }



    /* =========================================================
       IMAGE
    ========================================================= */

    public String getImagePath() {

        return imagePath;
    }


    public void setImagePath(
            String imagePath
    ) {

        this.imagePath =
                imagePath;
    }



    /* =========================================================
       UNIT PRICE
    ========================================================= */

    public BigDecimal getUnitPrice() {

        return unitPrice;
    }


    public void setUnitPrice(
            BigDecimal unitPrice
    ) {

        this.unitPrice =
                unitPrice;
    }



    /* =========================================================
       QUANTITY
    ========================================================= */

    public Integer getQuantity() {

        return quantity;
    }


    public void setQuantity(
            Integer quantity
    ) {

        this.quantity =
                quantity;
    }



    /* =========================================================
       LINE TOTAL
    ========================================================= */

    public BigDecimal getLineTotal() {

        return lineTotal;
    }


    public void setLineTotal(
            BigDecimal lineTotal
    ) {

        this.lineTotal =
                lineTotal;
    }



    /* =========================================================
       HELPERS
    ========================================================= */

    public boolean isProduct() {

        return TYPE_PRODUCT.equals(
                getItemType()
        );
    }


    public boolean isPiece() {

        return TYPE_PIECE.equals(
                getItemType()
        );
    }

}