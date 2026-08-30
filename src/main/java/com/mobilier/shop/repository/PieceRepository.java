package com.mobilier.shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobilier.shop.entity.Piece;


@Repository
public interface PieceRepository
        extends JpaRepository<Piece, Long> {


    /* =========================================================
       TOUTES LES PIECES PAR CATALOGUE
    ========================================================= */

    List<Piece> findByCatalogueOrderByCreatedAtDesc(
            String catalogue
    );



    /* =========================================================
       PIECES DISPONIBLES PAR CATALOGUE
    ========================================================= */

    List<Piece> findByCatalogueAndAvailableTrueOrderByCreatedAtDesc(
            String catalogue
    );



    /* =========================================================
       TOUTES LES PIECES DISPONIBLES
    ========================================================= */

    List<Piece> findByAvailableTrueOrderByCreatedAtDesc();



    /* =========================================================
       RECHERCHE PAR NOM
    ========================================================= */

    List<Piece> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(
            String name
    );



    /* =========================================================
       RECHERCHE PAR TYPE
    ========================================================= */

    List<Piece> findByTypeContainingIgnoreCaseOrderByCreatedAtDesc(
            String type
    );



    /* =========================================================
       COMPTER PAR CATALOGUE
    ========================================================= */

    long countByCatalogue(
            String catalogue
    );



    /* =========================================================
       COMPTER DISPONIBLES
    ========================================================= */

    long countByAvailableTrue();



    /* =========================================================
       COMPTER DISPONIBLES PAR CATALOGUE
    ========================================================= */

    long countByCatalogueAndAvailableTrue(
            String catalogue
    );

}