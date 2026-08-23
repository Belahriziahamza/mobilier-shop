package com.mobilier.shop.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobilier.shop.entity.CustomerFavorite;
import com.mobilier.shop.service.CustomerFavoriteService;

@RestController
@RequestMapping("/api/favorites")
public class CustomerFavoriteApiController {

    private final CustomerFavoriteService favoriteService;


    public CustomerFavoriteApiController(
            CustomerFavoriteService favoriteService
    ) {

        this.favoriteService = favoriteService;
    }


    /* =====================================================
       LISTE IDS FAVORIS DU CLIENT
    ===================================================== */

    @GetMapping
    public ResponseEntity<?> getFavorites(
            Authentication authentication
    ) {

        if (!isCustomer(authentication)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "authenticated",
                                    false
                            )
                    );
        }


        List<Long> productIds =
                favoriteService
                        .findCustomerFavorites(
                                authentication.getName()
                        )
                        .stream()
                        .map(CustomerFavorite::getProduct)
                        .map(product -> product.getId())
                        .toList();


        return ResponseEntity.ok(
                Map.of(
                        "authenticated",
                        true,
                        "productIds",
                        productIds
                )
        );
    }


    /* =====================================================
       AJOUTER / RETIRER FAVORI
    ===================================================== */

    @PostMapping("/{productId}/toggle")
    public ResponseEntity<?> toggleFavorite(

            @PathVariable
            Long productId,

            Authentication authentication
    ) {

        if (!isCustomer(authentication)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "authenticated",
                                    false,
                                    "message",
                                    "Connexion requise."
                            )
                    );
        }


        try {

            boolean favorite =
                    favoriteService.toggleFavorite(
                            authentication.getName(),
                            productId
                    );


            return ResponseEntity.ok(
                    Map.of(
                            "success",
                            true,
                            "favorite",
                            favorite,
                            "productId",
                            productId
                    )
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


    /* =====================================================
       VERIFICATION ROLE CLIENT
    ===================================================== */

    private boolean isCustomer(
            Authentication authentication
    ) {

        if (
                authentication == null ||
                !authentication.isAuthenticated()
        ) {

            return false;
        }


        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(
                        authority ->
                                "ROLE_CUSTOMER"
                                        .equals(
                                                authority.getAuthority()
                                        )
                );
    }
}