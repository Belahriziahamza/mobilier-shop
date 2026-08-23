package com.mobilier.shop.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mobilier.shop.entity.Customer;
import com.mobilier.shop.entity.CustomerOrder;
import com.mobilier.shop.repository.CustomerOrderRepository;
import com.mobilier.shop.repository.CustomerRepository;
import com.mobilier.shop.repository.ProductRepository;


@Controller
public class AdminDashboardController {


    /* =========================================================
       REPOSITORIES
    ========================================================= */

    private final ProductRepository productRepository;

    private final CustomerOrderRepository orderRepository;

    private final CustomerRepository customerRepository;



    /* =========================================================
       CONSTRUCTEUR
    ========================================================= */

    public AdminDashboardController(

            ProductRepository productRepository,

            CustomerOrderRepository orderRepository,

            CustomerRepository customerRepository
    ) {

        this.productRepository =
                productRepository;

        this.orderRepository =
                orderRepository;

        this.customerRepository =
                customerRepository;
    }



    /* =========================================================
       DASHBOARD ADMIN

       URL :
       /admin
    ========================================================= */

    @GetMapping("/admin")
    public String dashboard(
            Model model
    ) {


        /* =====================================================
           PRODUITS
        ===================================================== */

        long activeProducts =
                productRepository
                        .countByActiveTrue();



        /* =====================================================
           CLIENTS
        ===================================================== */

        List<Customer> customers =
                customerRepository
                        .findAll();



        long totalCustomers =
                customers.size();



        /* =====================================================
           CLIENTS LOCAL
        ===================================================== */

        long localCustomers =
                customers
                        .stream()

                        .filter(
                                customer ->
                                        "LOCAL"
                                                .equalsIgnoreCase(
                                                        customer.getAuthProvider()
                                                )
                        )

                        .count();



        /* =====================================================
           CLIENTS GOOGLE
        ===================================================== */

        long googleCustomers =
                customers
                        .stream()

                        .filter(
                                customer ->
                                        "GOOGLE"
                                                .equalsIgnoreCase(
                                                        customer.getAuthProvider()
                                                )
                        )

                        .count();



        /* =====================================================
           EMAILS VERIFIES
        ===================================================== */

        long verifiedCustomers =
                customers
                        .stream()

                        .filter(
                                Customer::isEmailVerified
                        )

                        .count();



        /* =====================================================
           EMAILS NON VERIFIES
        ===================================================== */

        long unverifiedCustomers =
                totalCustomers
                        - verifiedCustomers;



        /* =====================================================
           CLIENTS AYANT DEJA REUSSI UNE CONNEXION

           IMPORTANT :
           ce compteur ne signifie pas
           "connectés actuellement".
        ===================================================== */

        long customersWithLogin =
                customers
                        .stream()

                        .filter(
                                customer ->
                                        customer.getLastLoginAt()
                                                != null
                        )

                        .count();



        /* =====================================================
           COMMANDES
        ===================================================== */

        long totalOrders =
                orderRepository
                        .count();


        long newOrders =
                orderRepository
                        .countByStatus(
                                "NOUVELLE"
                        );


        long confirmedOrders =
                orderRepository
                        .countByStatus(
                                "CONFIRMEE"
                        );


        long manufacturingOrders =
                orderRepository
                        .countByStatus(
                                "EN_FABRICATION"
                        );


        long readyOrders =
                orderRepository
                        .countByStatus(
                                "PRETE"
                        );


        long deliveredOrders =
                orderRepository
                        .countByStatus(
                                "LIVREE"
                        );


        long cancelledOrders =
                orderRepository
                        .countByStatus(
                                "ANNULEE"
                        );



        /* =====================================================
           CHIFFRE TOTAL
        ===================================================== */

        BigDecimal totalAmount =
                orderRepository
                        .calculateTotalOrdersAmount();


        if (
                totalAmount == null
        ) {

            totalAmount =
                    BigDecimal.ZERO;
        }



        /* =====================================================
           5 DERNIERES COMMANDES
        ===================================================== */

        List<CustomerOrder> recentOrders =
                orderRepository
                        .findAllByOrderByCreatedAtDesc()
                        .stream()
                        .limit(5)
                        .toList();



        /* =====================================================
           MODEL - PRODUITS
        ===================================================== */

        model.addAttribute(
                "activeProducts",
                activeProducts
        );



        /* =====================================================
           MODEL - CLIENTS
        ===================================================== */

        model.addAttribute(
                "totalCustomers",
                totalCustomers
        );


        model.addAttribute(
                "localCustomers",
                localCustomers
        );


        model.addAttribute(
                "googleCustomers",
                googleCustomers
        );


        model.addAttribute(
                "verifiedCustomers",
                verifiedCustomers
        );


        model.addAttribute(
                "unverifiedCustomers",
                unverifiedCustomers
        );


        model.addAttribute(
                "customersWithLogin",
                customersWithLogin
        );



        /* =====================================================
           MODEL - COMMANDES
        ===================================================== */

        model.addAttribute(
                "totalOrders",
                totalOrders
        );


        model.addAttribute(
                "newOrders",
                newOrders
        );


        model.addAttribute(
                "confirmedOrders",
                confirmedOrders
        );


        model.addAttribute(
                "manufacturingOrders",
                manufacturingOrders
        );


        model.addAttribute(
                "readyOrders",
                readyOrders
        );


        model.addAttribute(
                "deliveredOrders",
                deliveredOrders
        );


        model.addAttribute(
                "cancelledOrders",
                cancelledOrders
        );



        /* =====================================================
           MODEL - CHIFFRE
        ===================================================== */

        model.addAttribute(
                "totalAmount",
                totalAmount
        );



        /* =====================================================
           MODEL - COMMANDES RECENTES
        ===================================================== */

        model.addAttribute(
                "recentOrders",
                recentOrders
        );


        return "admin/dashboard";
    }

}