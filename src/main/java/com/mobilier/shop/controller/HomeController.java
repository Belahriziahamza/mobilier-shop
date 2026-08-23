package com.mobilier.shop.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mobilier.shop.entity.Customer;
import com.mobilier.shop.entity.Product;
import com.mobilier.shop.repository.CustomColorRepository;
import com.mobilier.shop.repository.FabricRepository;
import com.mobilier.shop.service.CustomerService;
import com.mobilier.shop.service.ProductService;

@Controller
public class HomeController {

        private final ProductService productService;

        private final CustomerService customerService;

        private final FabricRepository fabricRepository;

        private final CustomColorRepository customColorRepository;

        public HomeController(
                        ProductService productService,
                        CustomerService customerService,
                        FabricRepository fabricRepository,
                        CustomColorRepository customColorRepository) {

                this.productService = productService;

                this.customerService = customerService;

                this.fabricRepository = fabricRepository;

                this.customColorRepository = customColorRepository;
        }

        /*
         * =====================================================
         * ACCUEIL
         * =====================================================
         */

        @GetMapping("/")
        public String home(
                        Model model) {

                /* NOUVEAUTÉS */

                List<Product> newProducts = productService
                                .findNewProducts()
                                .stream()
                                .limit(4)
                                .toList();

                /* PRODUITS EN VEDETTE */

                List<Product> featuredProducts = productService
                                .findFeatured()
                                .stream()
                                .limit(4)
                                .toList();

                /* PROMOTIONS */

                List<Product> promotionProducts = productService
                                .findPromotions()
                                .stream()
                                .limit(4)
                                .toList();

                model.addAttribute(
                                "newProducts",
                                newProducts);

                model.addAttribute(
                                "featuredProducts",
                                featuredProducts);

                model.addAttribute(
                                "promotionProducts",
                                promotionProducts);

                return "index";
        }

        /*
         * =====================================================
         * TISSUS
         * =====================================================
         */

        @GetMapping("/tissus")
        public String tissus(
                        Model model) {

                /*
                 * On récupère uniquement
                 * les tissus ACTIFS depuis MySQL.
                 */

                model.addAttribute(
                                "fabrics",
                                fabricRepository
                                                .findByActiveTrueOrderByNameAsc());

                return "tissus";
        }

        /*
         * =====================================================
         * BOIS
         * =====================================================
         */

        @GetMapping("/bois")
        public String bois() {

                return "bois";
        }

        /*
         * =====================================================
         * MDF
         * =====================================================
         */

        @GetMapping("/mdf")
        public String mdf() {

                return "mdf";
        }

        /*
         * =====================================================
         * SUR MESURE
         * =====================================================
         */

        @GetMapping("/sur-mesure")
        public String surMesure(
                        Model model) {

                /*
                 * Tissus actifs
                 */

                model.addAttribute(
                                "fabrics",
                                fabricRepository
                                                .findByActiveTrueOrderByNameAsc());

                /*
                 * Couleurs actives
                 */

                model.addAttribute(
                                "customColors",
                                customColorRepository
                                                .findByActiveTrueOrderByNameAsc());

                return "sur-mesure";
        }

        /*
         * =====================================================
         * CONTACT
         * =====================================================
         */

        @GetMapping("/contact")
        public String contact() {

                return "contact";
        }

        /*
         * =====================================================
         * PANIER
         * =====================================================
         */

        @GetMapping("/panier")
        public String panier(

                        Authentication authentication,

                        Model model) {

                Customer customer = null;

                /*
                 * On récupère les informations
                 * du client seulement s'il est
                 * connecté avec ROLE_CUSTOMER.
                 */

                if (authentication != null
                                &&
                                authentication.isAuthenticated()) {

                        boolean isCustomer = authentication
                                        .getAuthorities()
                                        .stream()
                                        .anyMatch(
                                                        authority -> "ROLE_CUSTOMER"
                                                                        .equals(
                                                                                        authority
                                                                                                        .getAuthority()));

                        if (isCustomer) {

                                customer = customerService
                                                .findByEmail(
                                                                authentication
                                                                                .getName());

                        }

                }

                model.addAttribute(
                                "customer",
                                customer);

                return "panier";
        }

}