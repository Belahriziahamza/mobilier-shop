package com.mobilier.shop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilier.shop.entity.Tissu;


public interface TissuRepository
        extends JpaRepository<Tissu, Long> {


    List<Tissu>
    findAllByOrderByCreatedAtDesc();


    List<Tissu>
    findByActiveTrueOrderByNameAsc();


    long countByActiveTrue();


    Optional<Tissu>
    findByReferenceIgnoreCase(
            String reference
    );


    boolean existsByReferenceIgnoreCase(
            String reference
    );


    boolean existsByReferenceIgnoreCaseAndIdNot(
            String reference,
            Long id
    );

}