package com.mobilier.shop.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mobilier.shop.entity.CustomerOrder;


public interface CustomerOrderRepository
        extends JpaRepository<CustomerOrder, Long> {


    /* =====================================================
       ADMIN - TOUTES LES COMMANDES
    ===================================================== */

    @EntityGraph(attributePaths = "items")
    List<CustomerOrder>
    findAllByOrderByCreatedAtDesc();



    /* =====================================================
       ADMIN - DETAIL COMMANDE
    ===================================================== */

    @Override
    @EntityGraph(attributePaths = "items")
    Optional<CustomerOrder>
    findById(Long id);



    /* =====================================================
       COMMANDES PAR STATUT
    ===================================================== */

    List<CustomerOrder>
    findByStatusOrderByCreatedAtDesc(
            String status
    );



    /* =====================================================
       COMMANDES DU CLIENT CONNECTE
    ===================================================== */

    @EntityGraph(attributePaths = "items")
    List<CustomerOrder>
    findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(
            String customerEmail
    );



    /* =====================================================
       DETAIL SECURISE D'UNE COMMANDE CLIENT

       ID + EMAIL doivent correspondre.
    ===================================================== */

    @EntityGraph(attributePaths = "items")
    Optional<CustomerOrder>
    findByIdAndCustomerEmailIgnoreCase(
            Long id,
            String customerEmail
    );



    /* =====================================================
       STATISTIQUES ADMIN
    ===================================================== */

    long countByStatus(
            String status
    );



    /* =====================================================
       CHIFFRE TOTAL
       commandes annulées exclues
    ===================================================== */

    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0)
        FROM CustomerOrder o
        WHERE o.status <> 'ANNULEE'
    """)
    BigDecimal calculateTotalOrdersAmount();

}