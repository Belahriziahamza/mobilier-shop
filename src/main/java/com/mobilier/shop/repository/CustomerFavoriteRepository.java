package com.mobilier.shop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilier.shop.entity.CustomerFavorite;

public interface CustomerFavoriteRepository
        extends JpaRepository<CustomerFavorite, Long> {


    @EntityGraph(attributePaths = "product")
    List<CustomerFavorite>
    findByCustomer_IdOrderByCreatedAtDesc(
            Long customerId
    );


    Optional<CustomerFavorite>
    findByCustomer_IdAndProduct_Id(
            Long customerId,
            Long productId
    );


    boolean existsByCustomer_IdAndProduct_Id(
            Long customerId,
            Long productId
    );
}