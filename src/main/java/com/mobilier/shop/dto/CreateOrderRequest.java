package com.mobilier.shop.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;


public class CreateOrderRequest {


    /* =========================================================
       CLIENT
    ========================================================= */

    @NotBlank(message = "Le nom du client est obligatoire.")
    @Size(
            max = 150,
            message = "Le nom ne doit pas dépasser 150 caractères."
    )
    private String customerName;


    @NotBlank(message = "Le téléphone est obligatoire.")
    @Size(
            max = 30,
            message = "Le téléphone est trop long."
    )
    private String phone;


    @Size(
            max = 100,
            message = "La ville est trop longue."
    )
    private String city;


    @Size(
            max = 500,
            message = "L'adresse est trop longue."
    )
    private String address;


    @Size(
            max = 1000,
            message = "Les notes sont trop longues."
    )
    private String notes;



    /* =========================================================
       PRODUITS
    ========================================================= */

    @Valid
    @NotEmpty(
            message = "La commande doit contenir au moins un produit."
    )
    private List<OrderItemRequest> items =
            new ArrayList<>();



    /* =========================================================
       CONSTRUCTEUR VIDE
       nécessaire pour JSON / Spring
    ========================================================= */

    public CreateOrderRequest() {
    }



    /* =========================================================
       CONSTRUCTEUR COMPLET
    ========================================================= */

    public CreateOrderRequest(
            String customerName,
            String phone,
            String city,
            String address,
            String notes,
            List<OrderItemRequest> items
    ) {

        this.customerName =
                customerName;

        this.phone =
                phone;

        this.city =
                city;

        this.address =
                address;

        this.notes =
                notes;

        this.items =
                items != null
                        ? items
                        : new ArrayList<>();
    }



    /* =========================================================
       GETTERS / SETTERS
    ========================================================= */

    public String getCustomerName() {

        return customerName;
    }


    public void setCustomerName(
            String customerName
    ) {

        this.customerName =
                customerName;
    }



    public String getPhone() {

        return phone;
    }


    public void setPhone(
            String phone
    ) {

        this.phone =
                phone;
    }



    public String getCity() {

        return city;
    }


    public void setCity(
            String city
    ) {

        this.city =
                city;
    }



    public String getAddress() {

        return address;
    }


    public void setAddress(
            String address
    ) {

        this.address =
                address;
    }



    public String getNotes() {

        return notes;
    }


    public void setNotes(
            String notes
    ) {

        this.notes =
                notes;
    }



    public List<OrderItemRequest> getItems() {

        return items;
    }


    public void setItems(
            List<OrderItemRequest> items
    ) {

        this.items =
                items != null
                        ? items
                        : new ArrayList<>();
    }

}