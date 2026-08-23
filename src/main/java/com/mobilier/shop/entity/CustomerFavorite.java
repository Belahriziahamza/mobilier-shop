package com.mobilier.shop.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "customer_favorites",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_customer_favorite",
                        columnNames = {
                                "customer_id",
                                "product_id"
                        }
                )
        }
)
public class CustomerFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "customer_id",
            nullable = false
    )
    private Customer customer;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "product_id",
            nullable = false
    )
    private Product product;


    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;


    @PrePersist
    public void prePersist() {

        createdAt =
                LocalDateTime.now();
    }


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


    public Product getProduct() {
        return product;
    }


    public void setProduct(
            Product product
    ) {
        this.product = product;
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