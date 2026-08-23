package com.mobilier.shop.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "order_id",
            nullable = false
    )
    private CustomerOrder order;


    @Column(nullable = false)
    private Long productId;


    @Column(nullable = false, length = 150)
    private String productName;


    @Column(length = 500)
    private String imagePath;


    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;


    @Column(nullable = false)
    private Integer quantity;


    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public CustomerOrder getOrder() {
        return order;
    }


    public void setOrder(
            CustomerOrder order
    ) {
        this.order = order;
    }


    public Long getProductId() {
        return productId;
    }


    public void setProductId(
            Long productId
    ) {
        this.productId = productId;
    }


    public String getProductName() {
        return productName;
    }


    public void setProductName(
            String productName
    ) {
        this.productName = productName;
    }


    public String getImagePath() {
        return imagePath;
    }


    public void setImagePath(
            String imagePath
    ) {
        this.imagePath = imagePath;
    }


    public BigDecimal getUnitPrice() {
        return unitPrice;
    }


    public void setUnitPrice(
            BigDecimal unitPrice
    ) {
        this.unitPrice = unitPrice;
    }


    public Integer getQuantity() {
        return quantity;
    }


    public void setQuantity(
            Integer quantity
    ) {
        this.quantity = quantity;
    }


    public BigDecimal getLineTotal() {
        return lineTotal;
    }


    public void setLineTotal(
            BigDecimal lineTotal
    ) {
        this.lineTotal = lineTotal;
    }
}