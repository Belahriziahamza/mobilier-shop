package com.mobilier.shop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilier.shop.entity.Couleur;


public interface CouleurRepository
        extends JpaRepository<Couleur, Long> {


    List<Couleur>
    findAllByOrderByCreatedAtDesc();


    List<Couleur>
    findByActiveTrueOrderByNameAsc();


    long countByActiveTrue();


    Optional<Couleur>
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