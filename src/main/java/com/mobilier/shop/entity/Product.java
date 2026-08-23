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

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotBlank
    @Column(nullable = false, length = 150)
    private String name;


    /*
     * salon
     * canape
     * chambre
     * lit
     * tete-de-lit
     * chaise
     * fauteuil
     * table
     */
    @NotBlank
    @Column(nullable = false, length = 50)
    private String category;


    /*
     * moderne
     * marocain
     * angle
     * etc.
     */
    @Column(length = 80)
    private String type;


    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;


    @DecimalMin("0.0")
    @Column(precision = 12, scale = 2)
    private BigDecimal oldPrice;


    @Column(length = 50)
    private String color;


    @Column(length = 50)
    private String availability;


    @Column(length = 3000)
    private String description;


    @Column(length = 500)
    private String imagePath;


    @Column(length = 1000)
    private String sourceUrl;


    @Column(nullable = false)
    private boolean active = true;


    private boolean featured = false;


    private boolean newProduct = false;


    private boolean promotion = false;


    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


    private LocalDateTime updatedAt;


    @PrePersist
    public void prePersist() {

        LocalDateTime now =
                LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }


    @PreUpdate
    public void preUpdate() {

        updatedAt =
                LocalDateTime.now();
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getCategory() {
        return category;
    }


    public void setCategory(String category) {
        this.category = category;
    }


    public String getType() {
        return type;
    }


    public void setType(String type) {
        this.type = type;
    }


    public BigDecimal getPrice() {
        return price;
    }


    public void setPrice(BigDecimal price) {
        this.price = price;
    }


    public BigDecimal getOldPrice() {
        return oldPrice;
    }


    public void setOldPrice(BigDecimal oldPrice) {
        this.oldPrice = oldPrice;
    }


    public String getColor() {
        return color;
    }


    public void setColor(String color) {
        this.color = color;
    }


    public String getAvailability() {
        return availability;
    }


    public void setAvailability(String availability) {
        this.availability = availability;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public String getImagePath() {
        return imagePath;
    }


    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }


    public String getSourceUrl() {
        return sourceUrl;
    }


    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }


    public boolean isActive() {
        return active;
    }


    public void setActive(boolean active) {
        this.active = active;
    }


    public boolean isFeatured() {
        return featured;
    }


    public void setFeatured(boolean featured) {
        this.featured = featured;
    }


    public boolean isNewProduct() {
        return newProduct;
    }


    public void setNewProduct(boolean newProduct) {
        this.newProduct = newProduct;
    }


    public boolean isPromotion() {
        return promotion;
    }


    public void setPromotion(boolean promotion) {
        this.promotion = promotion;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }


    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}