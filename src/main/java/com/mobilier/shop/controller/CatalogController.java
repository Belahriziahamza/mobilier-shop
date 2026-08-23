package com.mobilier.shop.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.mobilier.shop.entity.Product;
import com.mobilier.shop.service.ProductService;


@Controller
public class CatalogController {


    /* =========================================================
       SERVICE
    ========================================================= */

    private final ProductService productService;


    public CatalogController(
            ProductService productService
    ) {

        this.productService =
                productService;
    }



    /* =========================================================
       SALONS
    ========================================================= */

    @GetMapping("/salons")
    public String salons(
            Model model
    ) {

        model.addAttribute(
                "products",
                activeOnly(
                        productService.findByCategory(
                                "salon"
                        )
                )
        );


        model.addAttribute(
                "pageTitle",
                "Salons"
        );


        model.addAttribute(
                "pageDescription",
                "Découvrez notre collection de salons Zineb Déco, disponibles et personnalisables."
        );


        return "salons";
    }



    /* =========================================================
       CANAPÉS
    ========================================================= */

    @GetMapping("/canapes")
    public String canapes(
            Model model
    ) {

        model.addAttribute(
                "products",
                activeOnly(
                        productService.findByCategory(
                                "canape"
                        )
                )
        );


        model.addAttribute(
                "pageTitle",
                "Canapés"
        );


        model.addAttribute(
                "pageDescription",
                "Découvrez les canapés Zineb Déco disponibles et personnalisables."
        );


        return "canapes";
    }



    /* =========================================================
       CHAMBRES
    ========================================================= */

    @GetMapping("/chambres")
    public String chambres(
            Model model
    ) {

        model.addAttribute(
                "products",
                activeOnly(
                        productService.findByCategory(
                                "chambre"
                        )
                )
        );


        model.addAttribute(
                "pageTitle",
                "Chambres"
        );


        model.addAttribute(
                "pageDescription",
                "Découvrez nos chambres et ensembles personnalisables."
        );


        return "chambres";
    }



    /* =========================================================
       LITS
    ========================================================= */

    @GetMapping("/lits")
    public String lits(
            Model model
    ) {

        model.addAttribute(
                "products",
                activeOnly(
                        productService.findByCategory(
                                "lit"
                        )
                )
        );


        model.addAttribute(
                "pageTitle",
                "Lits"
        );


        model.addAttribute(
                "pageDescription",
                "Découvrez nos lits Zineb Déco, disponibles dans plusieurs styles et finitions."
        );


        return "lits";
    }



    /* =========================================================
       TÊTES DE LIT

       On réutilise le template lits.html.
    ========================================================= */

    @GetMapping("/tetes-de-lit")
    public String headboards(
            Model model
    ) {

        model.addAttribute(
                "products",
                activeOnly(
                        productService.findByCategory(
                                "tete-de-lit"
                        )
                )
        );


        model.addAttribute(
                "pageTitle",
                "Têtes de lit"
        );


        model.addAttribute(
                "pageDescription",
                "Découvrez les têtes de lit Zineb Déco et personnalisez votre chambre."
        );


        return "lits";
    }



    /* =========================================================
       CHAISES + FAUTEUILS
    ========================================================= */

    @GetMapping("/chaises")
    public String chairs(
            Model model
    ) {

        model.addAttribute(
                "products",
                activeOnly(
                        productService.findByCategories(
                                List.of(
                                        "chaise",
                                        "fauteuil"
                                )
                        )
                )
        );


        model.addAttribute(
                "pageTitle",
                "Chaises & fauteuils"
        );


        model.addAttribute(
                "pageDescription",
                "Découvrez notre collection de chaises et fauteuils Zineb Déco."
        );


        return "chaises";
    }



    /* =========================================================
       FAUTEUILS
    ========================================================= */

    @GetMapping("/fauteuils")
    public String armchairs(
            Model model
    ) {

        model.addAttribute(
                "products",
                activeOnly(
                        productService.findByCategory(
                                "fauteuil"
                        )
                )
        );


        model.addAttribute(
                "pageTitle",
                "Fauteuils"
        );


        model.addAttribute(
                "pageDescription",
                "Découvrez notre collection de fauteuils Zineb Déco."
        );


        /*
         * On utilise le même template
         * que les chaises.
         */

        return "chaises";
    }



    /* =========================================================
       TABLES
    ========================================================= */

    @GetMapping("/tables")
    public String tables(
            Model model
    ) {

        model.addAttribute(
                "products",
                activeOnly(
                        productService.findByCategory(
                                "table"
                        )
                )
        );


        model.addAttribute(
                "pageTitle",
                "Tables"
        );


        model.addAttribute(
                "pageDescription",
                "Découvrez les tables Zineb Déco."
        );


        return "tables";
    }



    /* =========================================================
       NOUVEAUTÉS
    ========================================================= */

    @GetMapping("/nouveautes")
    public String newProducts(
            Model model
    ) {

        model.addAttribute(
                "products",
                activeOnly(
                        productService.findNewProducts()
                )
        );


        model.addAttribute(
                "pageTitle",
                "Nouveautés"
        );


        model.addAttribute(
                "pageDescription",
                "Découvrez les dernières nouveautés Zineb Déco."
        );


        return "nouveautes";
    }



    /* =========================================================
       PROMOTIONS
    ========================================================= */

    @GetMapping("/promotions")
    public String promotions(
            Model model
    ) {

        model.addAttribute(
                "products",
                activeOnly(
                        productService.findPromotions()
                )
        );


        model.addAttribute(
                "pageTitle",
                "Promotions"
        );


        model.addAttribute(
                "pageDescription",
                "Découvrez les offres et promotions Zineb Déco."
        );


        return "promotions";
    }



    /* =========================================================
       FICHE PRODUIT

       Exemple :
       /produit/5
    ========================================================= */

    @GetMapping("/produit/{id}")
    public String productDetail(

            @PathVariable("id")
            Long id,

            Model model
    ) {

        Product product =
                productService.findById(
                        id
                );


        /*
         * Un produit désactivé par l'Admin
         * ne doit pas être accessible depuis
         * la boutique publique.
         */

        if (
                product == null ||
                !product.isActive()
        ) {

            return "redirect:/";
        }


        model.addAttribute(
                "product",
                product
        );


        return "product-detail";
    }



    /* =========================================================
       FILTRE PRODUITS ACTIFS

       Sécurité supplémentaire :
       même si ProductService renvoie tous les produits,
       le catalogue public n'affiche jamais un produit inactif.
    ========================================================= */

    private List<Product> activeOnly(
            List<Product> products
    ) {

        if (products == null) {

            return List.of();
        }


        return products
                .stream()

                .filter(
                        Product::isActive
                )

                .toList();
    }

}