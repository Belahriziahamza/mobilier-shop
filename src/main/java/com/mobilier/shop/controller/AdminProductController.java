package com.mobilier.shop.controller;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mobilier.shop.entity.Product;
import com.mobilier.shop.service.ProductService;

import jakarta.validation.Valid;


@Controller
@RequestMapping({
        "/admin/produits",
        "/admin/products"
})
public class AdminProductController {


    private final ProductService productService;


    public AdminProductController(
            ProductService productService
    ) {

        this.productService = productService;
    }



    /* =========================================================
       LISTE + RECHERCHE + FILTRES
    ========================================================= */

    @GetMapping
    public String products(

            @RequestParam(
                    value = "search",
                    required = false,
                    defaultValue = ""
            )
            String search,

            @RequestParam(
                    value = "category",
                    required = false,
                    defaultValue = ""
            )
            String category,

            @RequestParam(
                    value = "state",
                    required = false,
                    defaultValue = ""
            )
            String state,

            @RequestParam(
                    value = "special",
                    required = false,
                    defaultValue = ""
            )
            String special,

            Model model
    ) {


        /* =====================================================
           FORMULAIRE PRODUIT
        ===================================================== */

        if (!model.containsAttribute("formProduct")) {

            Product product = new Product();

            product.setActive(true);
            product.setAvailability("custom");
            product.setFeatured(false);
            product.setNewProduct(false);
            product.setPromotion(false);

            model.addAttribute(
                    "formProduct",
                    product
            );
        }



        /* =====================================================
           TOUS LES PRODUITS
        ===================================================== */

        List<Product> allProducts =
                productService.findAll();



        /* =====================================================
           NORMALISATION
        ===================================================== */

        String normalizedSearch =
                normalize(search);


        String normalizedCategory =
                normalize(category);


        String normalizedState =
                state == null
                        ? ""
                        : state.trim().toUpperCase(Locale.ROOT);


        String normalizedSpecial =
                special == null
                        ? ""
                        : special.trim().toUpperCase(Locale.ROOT);



        /* =====================================================
           FILTRAGE
        ===================================================== */

        List<Product> filteredProducts =
                allProducts
                        .stream()

                        /* -------------------------------------
                           RECHERCHE
                        ------------------------------------- */

                        .filter(product -> {

                            if (normalizedSearch.isBlank()) {

                                return true;
                            }


                            String name =
                                    normalize(
                                            product.getName()
                                    );


                            String type =
                                    normalize(
                                            product.getType()
                                    );


                            String productCategory =
                                    normalize(
                                            product.getCategory()
                                    );


                            String color =
                                    normalize(
                                            product.getColor()
                                    );


                            String description =
                                    normalize(
                                            product.getDescription()
                                    );


                            String id =
                                    product.getId() != null
                                            ? String.valueOf(
                                                    product.getId()
                                            )
                                            : "";


                            return
                                    name.contains(normalizedSearch)
                                    ||
                                    type.contains(normalizedSearch)
                                    ||
                                    productCategory.contains(normalizedSearch)
                                    ||
                                    color.contains(normalizedSearch)
                                    ||
                                    description.contains(normalizedSearch)
                                    ||
                                    id.contains(normalizedSearch);
                        })


                        /* -------------------------------------
                           CATEGORIE
                        ------------------------------------- */

                        .filter(product -> {

                            if (normalizedCategory.isBlank()) {

                                return true;
                            }


                            return normalizedCategory.equals(
                                    normalize(
                                            product.getCategory()
                                    )
                            );
                        })


                        /* -------------------------------------
                           ACTIF / INACTIF
                        ------------------------------------- */

                        .filter(product -> {

                            if (normalizedState.isBlank()) {

                                return true;
                            }


                            if ("ACTIVE".equals(normalizedState)) {

                                return product.isActive();
                            }


                            if ("INACTIVE".equals(normalizedState)) {

                                return !product.isActive();
                            }


                            return true;
                        })


                        /* -------------------------------------
                           SPECIAL
                        ------------------------------------- */

                        .filter(product -> {

                            if (normalizedSpecial.isBlank()) {

                                return true;
                            }


                            return switch (normalizedSpecial) {

                                case "NEW" ->
                                        product.isNewProduct();

                                case "PROMOTION" ->
                                        product.isPromotion();

                                case "FEATURED" ->
                                        product.isFeatured();

                                default ->
                                        true;
                            };
                        })


                        .toList();



        /* =====================================================
           STATISTIQUES
        ===================================================== */

        long totalProducts =
                allProducts.size();


        long activeProducts =
                allProducts
                        .stream()
                        .filter(Product::isActive)
                        .count();


        long inactiveProducts =
                allProducts
                        .stream()
                        .filter(product -> !product.isActive())
                        .count();


        long newProducts =
                allProducts
                        .stream()
                        .filter(Product::isNewProduct)
                        .count();


        long promotionProducts =
                allProducts
                        .stream()
                        .filter(Product::isPromotion)
                        .count();


        long featuredProducts =
                allProducts
                        .stream()
                        .filter(Product::isFeatured)
                        .count();



        /* =====================================================
           MODEL
        ===================================================== */

        model.addAttribute(
                "products",
                filteredProducts
        );


        model.addAttribute(
                "editing",
                false
        );


        model.addAttribute(
                "totalProducts",
                totalProducts
        );


        model.addAttribute(
                "resultCount",
                filteredProducts.size()
        );


        model.addAttribute(
                "activeProducts",
                activeProducts
        );


        model.addAttribute(
                "inactiveProducts",
                inactiveProducts
        );


        model.addAttribute(
                "newProductsCount",
                newProducts
        );


        model.addAttribute(
                "promotionProductsCount",
                promotionProducts
        );


        model.addAttribute(
                "featuredProductsCount",
                featuredProducts
        );


        model.addAttribute(
                "search",
                search
        );


        model.addAttribute(
                "selectedCategory",
                category
        );


        model.addAttribute(
                "selectedState",
                normalizedState
        );


        model.addAttribute(
                "selectedSpecial",
                normalizedSpecial
        );


        return "admin/products";
    }



    /* =========================================================
       NOUVEAU PRODUIT
    ========================================================= */

    @GetMapping("/nouveau")
    public String newProduct(
            Model model
    ) {


        Product product =
                new Product();


        product.setActive(true);
        product.setAvailability("custom");
        product.setFeatured(false);
        product.setNewProduct(false);
        product.setPromotion(false);


        model.addAttribute(
                "formProduct",
                product
        );


        model.addAttribute(
                "products",
                productService.findAll()
        );


        model.addAttribute(
                "editing",
                false
        );


        return "admin/products";
    }



    /* =========================================================
       MODIFIER
    ========================================================= */

    @GetMapping("/edit/{id}")
    public String edit(

            @PathVariable("id")
            Long id,

            Model model,

            RedirectAttributes redirectAttributes
    ) {


        try {


            Product product =
                    productService.findById(id);


            model.addAttribute(
                    "formProduct",
                    product
            );


            model.addAttribute(
                    "products",
                    productService.findAll()
            );


            model.addAttribute(
                    "editing",
                    true
            );


            /*
             * Valeurs nécessaires au template
             * même en mode modification.
             */

            model.addAttribute(
                    "totalProducts",
                    productService.findAll().size()
            );


            model.addAttribute(
                    "resultCount",
                    productService.findAll().size()
            );


            model.addAttribute(
                    "search",
                    ""
            );


            model.addAttribute(
                    "selectedCategory",
                    ""
            );


            model.addAttribute(
                    "selectedState",
                    ""
            );


            model.addAttribute(
                    "selectedSpecial",
                    ""
            );


            return "admin/products";


        } catch (IllegalArgumentException e) {


            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            "Produit introuvable."
                    );


            return "redirect:/admin/produits";
        }
    }



    /* =========================================================
       SAVE
    ========================================================= */

    @PostMapping("/save")
    public String save(

            @Valid
            @ModelAttribute("formProduct")
            Product formProduct,

            BindingResult bindingResult,

            @RequestParam(
                    value = "image",
                    required = false
            )
            MultipartFile image,

            Model model,

            RedirectAttributes redirectAttributes
    ) {


        boolean editing =
                formProduct.getId() != null;



        /* =====================================================
           IMAGE OBLIGATOIRE CREATION
        ===================================================== */

        if (
                !editing
                &&
                (
                        image == null
                        ||
                        image.isEmpty()
                )
        ) {


            bindingResult.reject(
                    "image.required",
                    "Une photo du produit est obligatoire."
            );
        }



        /* =====================================================
           ERREURS VALIDATION
        ===================================================== */

        if (bindingResult.hasErrors()) {


            prepareFormModel(
                    model,
                    editing
            );


            model.addAttribute(
                    "error",
                    "Vérifiez les informations du produit."
            );


            return "admin/products";
        }



        /* =====================================================
           ENREGISTREMENT
        ===================================================== */

        try {


            if (editing) {


                productService.update(
                        formProduct.getId(),
                        formProduct,
                        image
                );


                redirectAttributes
                        .addFlashAttribute(
                                "success",
                                "Produit modifié avec succès."
                        );


            } else {


                productService.create(
                        formProduct,
                        image
                );


                redirectAttributes
                        .addFlashAttribute(
                                "success",
                                "Produit ajouté avec succès."
                        );
            }


        } catch (IOException e) {


            prepareFormModel(
                    model,
                    editing
            );


            model.addAttribute(
                    "error",
                    "Erreur lors de l'enregistrement de l'image."
            );


            return "admin/products";


        } catch (IllegalArgumentException e) {


            prepareFormModel(
                    model,
                    editing
            );


            model.addAttribute(
                    "error",
                    e.getMessage()
            );


            return "admin/products";
        }


        return "redirect:/admin/produits";
    }



    /* =========================================================
       DELETE
    ========================================================= */

    @PostMapping("/delete/{id}")
    public String delete(

            @PathVariable("id")
            Long id,

            RedirectAttributes redirectAttributes
    ) {


        try {


            productService.delete(id);


            redirectAttributes
                    .addFlashAttribute(
                            "success",
                            "Produit supprimé avec succès."
                    );


        } catch (IOException e) {


            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            "Impossible de supprimer l'image du produit."
                    );


        } catch (IllegalArgumentException e) {


            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            e.getMessage()
                    );
        }


        return "redirect:/admin/produits";
    }



    /* =========================================================
       PREPARER LE MODEL EN CAS D'ERREUR
    ========================================================= */

    private void prepareFormModel(

            Model model,

            boolean editing
    ) {


        List<Product> products =
                productService.findAll();


        model.addAttribute(
                "products",
                products
        );


        model.addAttribute(
                "editing",
                editing
        );


        model.addAttribute(
                "totalProducts",
                products.size()
        );


        model.addAttribute(
                "resultCount",
                products.size()
        );


        model.addAttribute(
                "search",
                ""
        );


        model.addAttribute(
                "selectedCategory",
                ""
        );


        model.addAttribute(
                "selectedState",
                ""
        );


        model.addAttribute(
                "selectedSpecial",
                ""
        );
    }



    /* =========================================================
       NORMALISER TEXTE
    ========================================================= */

    private String normalize(
            String value
    ) {


        if (value == null) {

            return "";
        }


        return value
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }

}