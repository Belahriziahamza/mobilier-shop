package com.mobilier.shop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobilier.shop.entity.CustomerCartItem;


@Repository
public interface CustomerCartItemRepository
        extends JpaRepository<CustomerCartItem, Long> {


    /* =========================================================
       PANIER COMPLET D'UN CLIENT
    ========================================================= */

    List<CustomerCartItem>
    findByCustomerEmailIgnoreCaseOrderByCreatedAtAsc(
            String customerEmail
    );



    /* =========================================================
       TROUVER UN ARTICLE PRECIS DANS LE PANIER
    ========================================================= */

    Optional<CustomerCartItem>
    findByCustomerEmailIgnoreCaseAndItemTypeAndItemId(
            String customerEmail,
            String itemType,
            Long itemId
    );



    /* =========================================================
       TROUVER UNE LIGNE PAR ID + CLIENT
       SECURITE : impossible de modifier le panier d'un autre Gmail
    ========================================================= */

    Optional<CustomerCartItem>
    findByIdAndCustomerEmailIgnoreCase(
            Long id,
            String customerEmail
    );



    /* =========================================================
       COMPTER LES LIGNES DU PANIER
    ========================================================= */

    long countByCustomerEmailIgnoreCase(
            String customerEmail
    );



    /* =========================================================
       SUPPRIMER TOUT LE PANIER DU CLIENT
    ========================================================= */

    void deleteByCustomerEmailIgnoreCase(
            String customerEmail
    );



    /* =========================================================
       VERIFIER SI UN ARTICLE EST DEJA DANS LE PANIER
    ========================================================= */

    boolean existsByCustomerEmailIgnoreCaseAndItemTypeAndItemId(
            String customerEmail,
            String itemType,
            Long itemId
    );

}