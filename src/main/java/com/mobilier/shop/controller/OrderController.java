package com.mobilier.shop.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobilier.shop.dto.CreateOrderRequest;
import com.mobilier.shop.entity.CustomerOrder;
import com.mobilier.shop.service.OrderService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/orders")
public class OrderController {


    /*
     * Même clé utilisée pour la connexion client.
     */
    private static final String CUSTOMER_SECURITY_CONTEXT_KEY =
            "ZINEB_CUSTOMER_SECURITY_CONTEXT";


    private final OrderService orderService;


    public OrderController(
            OrderService orderService
    ) {

        this.orderService =
                orderService;
    }



    /* =========================================================
       CREER UNE COMMANDE
    ========================================================= */

    @PostMapping
    public ResponseEntity<?> createOrder(

            @Valid
            @RequestBody
            CreateOrderRequest request,

            HttpSession session,

            Authentication authentication
    ) {


        try {


            String customerEmail =
                    null;



            /* =================================================
               1. RECUPERER LE COMPTE CLIENT DEPUIS LA SESSION
            ================================================= */

            Object contextObject =
                    session.getAttribute(
                            CUSTOMER_SECURITY_CONTEXT_KEY
                    );


            if (contextObject instanceof SecurityContext securityContext) {


                Authentication customerAuthentication =
                        securityContext.getAuthentication();


                if (
                        customerAuthentication != null
                        &&
                        customerAuthentication.isAuthenticated()
                ) {


                    boolean isCustomer =
                            customerAuthentication
                                    .getAuthorities()
                                    .stream()
                                    .anyMatch(
                                            authority ->
                                                    "ROLE_CUSTOMER"
                                                            .equals(
                                                                    authority.getAuthority()
                                                            )
                                    );


                    if (isCustomer) {

                        customerEmail =
                                customerAuthentication.getName();

                    }

                }

            }



            /* =================================================
               2. FALLBACK AUTHENTICATION STANDARD
               Utile par exemple si une connexion OAuth
               utilise le contexte Spring Security classique.
            ================================================= */

            if (
                    customerEmail == null
                    &&
                    authentication != null
                    &&
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
                                                                authority.getAuthority()
                                                        )
                                );


                if (isCustomer) {

                    customerEmail =
                            authentication.getName();

                }

            }



            /* =================================================
               3. CREATION COMMANDE
            ================================================= */

            CustomerOrder order =
                    orderService.createOrder(
                            request,
                            customerEmail
                    );



            /* =================================================
               REPONSE JSON
            ================================================= */

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


        } catch (IllegalArgumentException e) {


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