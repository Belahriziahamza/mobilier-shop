package com.mobilier.shop.controller;

import com.mobilier.shop.dto.CreateOrderRequest;

import com.mobilier.shop.entity.CustomerOrder;

import com.mobilier.shop.service.OrderService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;


    public OrderController(
            OrderService orderService
    ) {

        this.orderService =
                orderService;
    }


    /* =====================================================
       CREER UNE COMMANDE
    ===================================================== */

    @PostMapping
    public ResponseEntity<?> createOrder(

            @Valid
            @RequestBody
            CreateOrderRequest request,

            Authentication authentication
    ) {

        try {

            String customerEmail =
                    null;


            /*
             * Si un client est connecté avec son compte,
             * on récupère son email directement depuis
             * Spring Security.
             */

            if (
                    authentication != null &&
                    authentication.isAuthenticated()
            ) {

                boolean isCustomer =
                        authentication
                                .getAuthorities()
                                .stream()
                                .anyMatch(
                                        authority ->
                                                "ROLE_CUSTOMER"
                                                        .equals(
                                                                authority
                                                                        .getAuthority()
                                                        )
                                );


                if (isCustomer) {

                    customerEmail =
                            authentication.getName();
                }
            }


            CustomerOrder order =
                    orderService.createOrder(
                            request,
                            customerEmail
                    );


            /* ===============================
               REPONSE JSON
            =============================== */

            Map<String, Object> response =
                    new LinkedHashMap<>();


            response.put(
                    "success",
                    true
            );


            response.put(
                    "orderId",
                    order.getId()
            );


            response.put(
                    "total",
                    order.getTotalAmount()
            );


            response.put(
                    "status",
                    order.getStatus()
            );


            response.put(
                    "customerEmail",
                    order.getCustomerEmail()
            );


            return ResponseEntity
                    .status(
                            HttpStatus.CREATED
                    )
                    .body(
                            response
                    );


        } catch (
                IllegalArgumentException e
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "success",
                                    false,
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }
}