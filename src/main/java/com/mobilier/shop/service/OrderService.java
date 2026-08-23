package com.mobilier.shop.service;

import com.mobilier.shop.dto.CreateOrderRequest;
import com.mobilier.shop.dto.OrderItemRequest;

import com.mobilier.shop.entity.CustomerOrder;
import com.mobilier.shop.entity.OrderItem;
import com.mobilier.shop.entity.Product;

import com.mobilier.shop.repository.CustomerOrderRepository;
import com.mobilier.shop.repository.ProductRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;


@Service
@Transactional
public class OrderService {


    /* =========================================================
       STATUTS AUTORISES
    ========================================================= */

    private static final Set<String> ALLOWED_STATUSES =
            Set.of(
                    "NOUVELLE",
                    "CONFIRMEE",
                    "EN_FABRICATION",
                    "PRETE",
                    "LIVREE",
                    "ANNULEE"
            );



    /* =========================================================
       DEPENDANCES
    ========================================================= */

    private final CustomerOrderRepository customerOrderRepository;

    private final ProductRepository productRepository;



    /* =========================================================
       CONSTRUCTEUR
    ========================================================= */

    public OrderService(
            CustomerOrderRepository customerOrderRepository,
            ProductRepository productRepository
    ) {

        this.customerOrderRepository =
                customerOrderRepository;

        this.productRepository =
                productRepository;
    }



    /* =========================================================
       CREER UNE COMMANDE
    ========================================================= */

    public CustomerOrder createOrder(
            CreateOrderRequest request,
            String customerEmail
    ) {


        /* =========================
           VALIDATION REQUEST
        ========================= */

        if (request == null) {

            throw new IllegalArgumentException(
                    "Commande invalide."
            );
        }


        if (
                request.getCustomerName() == null ||
                request.getCustomerName().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Le nom du client est obligatoire."
            );
        }


        if (
                request.getPhone() == null ||
                request.getPhone().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Le téléphone est obligatoire."
            );
        }


        if (
                request.getItems() == null ||
                request.getItems().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "La commande ne contient aucun produit."
            );
        }



        /* =========================
           COMMANDE
        ========================= */

        CustomerOrder order =
                new CustomerOrder();


        order.setCustomerName(
                request
                        .getCustomerName()
                        .trim()
        );


        order.setPhone(
                request
                        .getPhone()
                        .trim()
        );


        order.setCity(
                cleanOptional(
                        request.getCity()
                )
        );


        order.setAddress(
                cleanOptional(
                        request.getAddress()
                )
        );


        order.setNotes(
                cleanOptional(
                        request.getNotes()
                )
        );


        /*
         * IMPORTANT :
         *
         * L'email ne vient jamais du navigateur.
         *
         * Il vient de Spring Security.
         */

        order.setCustomerEmail(
                cleanOptional(
                        customerEmail
                )
        );


        order.setStatus(
                "NOUVELLE"
        );



        /* =========================
           TOTAL
        ========================= */

        BigDecimal orderTotal =
                BigDecimal.ZERO;



        /* =========================
           PRODUITS
        ========================= */

        for (
                OrderItemRequest itemRequest
                : request.getItems()
        ) {


            if (
                    itemRequest == null ||
                    itemRequest.getProductId() == null
            ) {

                throw new IllegalArgumentException(
                        "Produit invalide."
                );
            }


            int quantity =
                    itemRequest.getQuantity();


            if (quantity <= 0) {

                throw new IllegalArgumentException(
                        "La quantité doit être supérieure à zéro."
                );
            }



            /* =====================
               PRODUIT MYSQL
            ====================== */

            Product product =
                    productRepository
                            .findById(
                                    itemRequest
                                            .getProductId()
                            )
                            .orElseThrow(
                                    () ->
                                            new IllegalArgumentException(
                                                    "Produit introuvable : "
                                                            +
                                                            itemRequest
                                                                    .getProductId()
                                            )
                            );



            /* =====================
               PRIX SERVEUR
            ====================== */

            BigDecimal unitPrice =
                    product.getPrice();


            if (unitPrice == null) {

                unitPrice =
                        BigDecimal.ZERO;
            }



            BigDecimal lineTotal =
                    unitPrice.multiply(
                            BigDecimal.valueOf(
                                    quantity
                            )
                    );



            /* =====================
               ITEM
            ====================== */

            OrderItem orderItem =
                    new OrderItem();


            orderItem.setProductId(
                    product.getId()
            );


            orderItem.setProductName(
                    product.getName()
            );


            orderItem.setImagePath(
                    product.getImagePath()
            );


            orderItem.setUnitPrice(
                    unitPrice
            );


            orderItem.setQuantity(
                    quantity
            );


            orderItem.setLineTotal(
                    lineTotal
            );



            /* =====================
               ASSOCIATION
            ====================== */

            order.addItem(
                    orderItem
            );



            /* =====================
               TOTAL COMMANDE
            ====================== */

            orderTotal =
                    orderTotal.add(
                            lineTotal
                    );
        }



        order.setTotalAmount(
                orderTotal
        );



        /* =========================
           SAVE
        ========================= */

        return customerOrderRepository
                .save(
                        order
                );
    }



    /* =========================================================
       ADMIN - TOUTES LES COMMANDES
    ========================================================= */

    @Transactional(readOnly = true)
    public List<CustomerOrder> findAll() {

        return customerOrderRepository
                .findAllByOrderByCreatedAtDesc();
    }



    /* =========================================================
       ADMIN - DETAIL COMMANDE
    ========================================================= */

    @Transactional(readOnly = true)
    public CustomerOrder findById(
            Long orderId
    ) {

        if (orderId == null) {

            throw new IllegalArgumentException(
                    "Commande invalide."
            );
        }


        return customerOrderRepository
                .findById(
                        orderId
                )
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Commande introuvable."
                                )
                );
    }



    /* =========================================================
       COMMANDES DU CLIENT
    ========================================================= */

    @Transactional(readOnly = true)
    public List<CustomerOrder> findCustomerOrders(
            String customerEmail
    ) {


        if (
                customerEmail == null ||
                customerEmail.isBlank()
        ) {

            return List.of();
        }


        return customerOrderRepository
                .findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(
                        customerEmail.trim()
                );
    }



    /* =========================================================
       DETAIL SECURISE COMMANDE CLIENT

       L'ID + L'EMAIL doivent correspondre.
    ========================================================= */

    @Transactional(readOnly = true)
    public CustomerOrder findCustomerOrderById(
            Long orderId,
            String customerEmail
    ) {


        if (orderId == null) {

            throw new IllegalArgumentException(
                    "Commande invalide."
            );
        }


        if (
                customerEmail == null ||
                customerEmail.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Client non connecté."
            );
        }


        return customerOrderRepository
                .findByIdAndCustomerEmailIgnoreCase(
                        orderId,
                        customerEmail.trim()
                )
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Commande introuvable ou accès non autorisé."
                                )
                );
    }



    /* =========================================================
       ADMIN - CHANGER STATUT
    ========================================================= */

    public CustomerOrder updateStatus(
            Long orderId,
            String status
    ) {


        if (orderId == null) {

            throw new IllegalArgumentException(
                    "Commande invalide."
            );
        }


        if (
                status == null ||
                status.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Statut obligatoire."
            );
        }


        String normalizedStatus =
                status
                        .trim()
                        .toUpperCase();


        if (
                !ALLOWED_STATUSES.contains(
                        normalizedStatus
                )
        ) {

            throw new IllegalArgumentException(
                    "Statut de commande invalide."
            );
        }



        CustomerOrder order =
                findById(
                        orderId
                );


        order.setStatus(
                normalizedStatus
        );


        return customerOrderRepository
                .save(
                        order
                );
    }



    /* =========================================================
       HELPER
    ========================================================= */

    private String cleanOptional(
            String value
    ) {

        if (value == null) {

            return null;
        }


        String cleaned =
                value.trim();


        if (cleaned.isEmpty()) {

            return null;
        }


        return cleaned;
    }

}