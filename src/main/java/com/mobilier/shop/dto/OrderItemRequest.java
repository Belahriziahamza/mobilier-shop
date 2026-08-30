package com.mobilier.shop.dto;


public class OrderItemRequest {


    /* =========================================================
       TYPE ARTICLE

       PRODUCT = meuble / produit normal
       PIECE   = pièce / tissu / couture
    ========================================================= */

    private String itemType;



    /* =========================================================
       ID ARTICLE

       PRODUCT -> Product.id
       PIECE   -> Piece.id
    ========================================================= */

    private Long itemId;



    /* =========================================================
       QUANTITE
    ========================================================= */

    private int quantity = 1;



    /* =========================================================
       CONSTRUCTEUR
    ========================================================= */

    public OrderItemRequest() {
    }



    /* =========================================================
       ITEM TYPE
    ========================================================= */

    public String getItemType() {

        /*
         * Compatibilité :
         * si aucun type n'est envoyé,
         * on considère qu'il s'agit d'un PRODUCT.
         */

        if (
                itemType == null
                ||
                itemType.isBlank()
        ) {

            return "PRODUCT";
        }


        return itemType
                .trim()
                .toUpperCase();
    }


    public void setItemType(
            String itemType
    ) {

        this.itemType =
                itemType;
    }



    /* =========================================================
       ITEM ID
    ========================================================= */

    public Long getItemId() {

        return itemId;
    }


    public void setItemId(
            Long itemId
    ) {

        this.itemId =
                itemId;
    }



    /* =========================================================
       QUANTITE
    ========================================================= */

    public int getQuantity() {

        return quantity;
    }


    public void setQuantity(
            int quantity
    ) {

        this.quantity =
                quantity;
    }



    /* =========================================================
       COMPATIBILITE ANCIEN SYSTEME

       Ton OrderService actuel utilise encore :

       itemRequest.getProductId()

       On garde temporairement cette méthode
       pour éviter une erreur de compilation.

       Elle sera supprimable après modification
       complète de OrderService.
    ========================================================= */

    public Long getProductId() {

        if (
                !"PRODUCT".equalsIgnoreCase(
                        getItemType()
                )
        ) {

            return null;
        }


        return itemId;
    }


    public void setProductId(
            Long productId
    ) {

        /*
         * Permet également à l'ancien JavaScript :
         *
         * {
         *    "productId": 5,
         *    "quantity": 1
         * }
         *
         * de continuer à fonctionner temporairement.
         */

        this.itemId =
                productId;


        if (
                this.itemType == null
                ||
                this.itemType.isBlank()
        ) {

            this.itemType =
                    "PRODUCT";
        }
    }

}