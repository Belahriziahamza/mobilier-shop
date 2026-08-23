package com.mobilier.shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilier.shop.entity.Product;

public interface ProductRepository
        extends JpaRepository<Product, Long> {


    List<Product>
    findByCategoryAndActiveTrueOrderByCreatedAtDesc(
            String category
    );


    List<Product>
    findByCategoryInAndActiveTrueOrderByCreatedAtDesc(
            List<String> categories
    );


    List<Product>
    findByActiveTrueOrderByCreatedAtDesc();


    List<Product>
    findByNewProductTrueAndActiveTrueOrderByCreatedAtDesc();


    List<Product>
    findByPromotionTrueAndActiveTrueOrderByCreatedAtDesc();


    List<Product>
    findByFeaturedTrueAndActiveTrueOrderByCreatedAtDesc();


    long countByActiveTrue();
}