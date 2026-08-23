package com.mobilier.shop.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mobilier.shop.service.CustomerFavoriteService;

@Controller
public class CustomerFavoriteController {

    private final CustomerFavoriteService favoriteService;


    public CustomerFavoriteController(
            CustomerFavoriteService favoriteService
    ) {

        this.favoriteService =
                favoriteService;
    }


    @PostMapping(
            "/compte/favoris/{productId}/toggle"
    )
    public String toggleFavorite(

            @PathVariable
            Long productId,

            Authentication authentication,

            RedirectAttributes redirectAttributes
    ) {

        try {

            boolean favorite =
                    favoriteService
                            .toggleFavorite(
                                    authentication.getName(),
                                    productId
                            );


            if (favorite) {

                redirectAttributes
                        .addFlashAttribute(
                                "favoriteSuccess",
                                "Produit ajouté aux favoris."
                        );

            } else {

                redirectAttributes
                        .addFlashAttribute(
                                "favoriteSuccess",
                                "Produit retiré des favoris."
                        );
            }


        } catch (
                IllegalArgumentException e
        ) {

            redirectAttributes
                    .addFlashAttribute(
                            "favoriteError",
                            e.getMessage()
                    );
        }


        return "redirect:/compte";
    }
}