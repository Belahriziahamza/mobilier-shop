package com.mobilier.shop.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer_orders")
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /* =====================================================
       CLIENT
    ===================================================== */

    @Column(
            nullable = false,
            length = 120
    )
    private String customerName;


    @Column(
            nullable = false,
            length = 30
    )
    private String phone;


    /*
     * Email du compte client connecté.
     *
     * Il permet d'afficher uniquement
     * les commandes appartenant au client.
     */
    @Column(
            name = "customer_email",
            length = 180
    )
    private String customerEmail;


    @Column(length = 100)
    private String city;


    @Column(length = 500)
    private String address;


    @Column(length = 1000)
    private String notes;


    /* =====================================================
       COMMANDE
    ===================================================== */

    @Column(
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal totalAmount;


    @Column(
            nullable = false,
            length = 30
    )
    private String status = "NOUVELLE";


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
       ARTICLES
    ===================================================== */

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> items =
            new ArrayList<>();


    /* =====================================================
       LIFECYCLE
    ===================================================== */

    @PrePersist
    public void prePersist() {

        LocalDateTime now =
                LocalDateTime.now();


        createdAt =
                now;


        updatedAt =
                now;


        if (
                status == null ||
                status.isBlank()
        ) {

            status =
                    "NOUVELLE";
        }
    }


    @PreUpdate
    public void preUpdate() {

        updatedAt =
                LocalDateTime.now();
    }


    /* =====================================================
       ITEMS
    ===================================================== */

    public void addItem(
            OrderItem item
    ) {

        if (item == null) {
            return;
        }


        items.add(
                item
        );


        item.setOrder(
                this
        );
    }


    public void removeItem(
            OrderItem item
    ) {

        if (item == null) {
            return;
        }


        items.remove(
                item
        );


        item.setOrder(
                null
        );
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

        this.id =
                id;
    }


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


    /* =====================================================
       CUSTOMER EMAIL
    ===================================================== */

    public String getCustomerEmail() {

        return customerEmail;
    }


    public void setCustomerEmail(
            String customerEmail
    ) {

        this.customerEmail =
                customerEmail;
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


    public BigDecimal getTotalAmount() {

        return totalAmount;
    }


    public void setTotalAmount(
            BigDecimal totalAmount
    ) {

        this.totalAmount =
                totalAmount;
    }


    public String getStatus() {

        return status;
    }


    public void setStatus(
            String status
    ) {

        this.status =
                status;
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


    public List<OrderItem> getItems() {

        return items;
    }


    public void setItems(
            List<OrderItem> items
    ) {

        this.items =
                items != null
                        ? items
                        : new ArrayList<>();


        for (
                OrderItem item
                : this.items
        ) {

            item.setOrder(
                    this
            );
        }
    }
}