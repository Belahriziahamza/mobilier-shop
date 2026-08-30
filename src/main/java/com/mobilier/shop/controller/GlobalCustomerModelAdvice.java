package com.mobilier.shop.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.mobilier.shop.entity.Customer;
import com.mobilier.shop.service.CustomerService;

import jakarta.servlet.http.HttpSession;


@ControllerAdvice
public class GlobalCustomerModelAdvice {


    /*
     * Même clé utilisée dans SecurityConfig.
     */
    private static final String CUSTOMER_SECURITY_CONTEXT_KEY =
            "ZINEB_CUSTOMER_SECURITY_CONTEXT";


    private final CustomerService customerService;


    public GlobalCustomerModelAdvice(
            CustomerService customerService
    ) {

        this.customerService =
                customerService;
    }


    /*
     * =========================================================
     * CLIENT CONNECTE DISPONIBLE SUR TOUTES LES PAGES
     * =========================================================
     *
     * Thymeleaf pourra utiliser :
     *
     * ${headerCustomer}
     *
     * sur :
     *
     * /
     * /salons
     * /canapes
     * /chambres
     * /produit/...
     * /panier
     * /contact
     * etc.
     */

    @ModelAttribute("headerCustomer")
    public Customer getHeaderCustomer(
            HttpSession session
    ) {


        /*
         * Récupérer le contexte client enregistré
         * dans la session.
         */

        Object contextObject =
                session.getAttribute(
                        CUSTOMER_SECURITY_CONTEXT_KEY
                );


        if (!(contextObject instanceof SecurityContext)) {

            return null;
        }


        SecurityContext securityContext =
                (SecurityContext) contextObject;


        Authentication authentication =
                securityContext.getAuthentication();


        /*
         * Pas connecté.
         */

        if (authentication == null) {

            return null;
        }


        if (!authentication.isAuthenticated()) {

            return null;
        }


        if (authentication instanceof AnonymousAuthenticationToken) {

            return null;
        }


        String email =
                authentication.getName();


        if (
                email == null
                ||
                email.isBlank()
                ||
                "anonymousUser".equalsIgnoreCase(email)
        ) {

            return null;
        }


        /*
         * Charger le vrai client MySQL.
         */

        try {

            return customerService.findByEmail(
                    email
            );

        } catch (RuntimeException exception) {

            /*
             * Le header ne doit jamais provoquer
             * une erreur 500 si le client n'existe plus.
             */

            return null;
        }
    }
}