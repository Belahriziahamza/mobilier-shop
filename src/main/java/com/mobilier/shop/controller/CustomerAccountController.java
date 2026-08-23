package com.mobilier.shop.controller;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mobilier.shop.dto.RegisterCustomerRequest;
import com.mobilier.shop.entity.Customer;
import com.mobilier.shop.entity.CustomerOrder;
import com.mobilier.shop.service.CustomerFavoriteService;
import com.mobilier.shop.service.CustomerService;
import com.mobilier.shop.service.OrderService;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;


@Controller
public class CustomerAccountController {


    /* =====================================================
       SERVICES
    ===================================================== */

    private final CustomerService customerService;

    private final Validator validator;

    private final OrderService orderService;

    private final CustomerFavoriteService favoriteService;



    /* =====================================================
       CONSTRUCTEUR
    ===================================================== */

    public CustomerAccountController(

            CustomerService customerService,

            Validator validator,

            OrderService orderService,

            CustomerFavoriteService favoriteService
    ) {

        this.customerService =
                customerService;

        this.validator =
                validator;

        this.orderService =
                orderService;

        this.favoriteService =
                favoriteService;
    }



    /* =====================================================
       LOGIN CLIENT
    ===================================================== */

    @GetMapping("/compte/login")
    public String login() {

        return "compte/login";
    }



    /* =====================================================
       PAGE INSCRIPTION
    ===================================================== */

    @GetMapping("/compte/inscription")
    public String registerPage() {

        return "compte/inscription";
    }



    /* =====================================================
       INSCRIPTION CLIENT
    ===================================================== */

    @PostMapping("/compte/inscription")
    public String register(

            @RequestParam
            String firstName,

            @RequestParam
            String lastName,

            @RequestParam
            String email,

            @RequestParam
            String phone,

            @RequestParam
            String password,

            @RequestParam
            String confirmPassword,

            Model model
    ) {


        RegisterCustomerRequest request =
                new RegisterCustomerRequest(

                        firstName,

                        lastName,

                        email,

                        phone,

                        password,

                        confirmPassword
                );


        /* ===============================================
           VALIDATION
        =============================================== */

        Set<ConstraintViolation<RegisterCustomerRequest>>
                violations =
                validator.validate(
                        request
                );


        if (!violations.isEmpty()) {

            String errorMessage =
                    violations
                            .iterator()
                            .next()
                            .getMessage();


            /*
             * On garde les deux noms pour compatibilité
             * avec les différents templates.
             */

            model.addAttribute(
                    "error",
                    errorMessage
            );


            model.addAttribute(
                    "registrationError",
                    errorMessage
            );


            keepFormValues(

                    model,

                    firstName,

                    lastName,

                    email,

                    phone
            );


            return "compte/inscription";
        }



        /* ===============================================
           CREATION COMPTE
        =============================================== */

        try {

            customerService.register(
                    request
            );


            return "redirect:/compte/login?registered";


        } catch (
                IllegalArgumentException e
        ) {


            model.addAttribute(
                    "error",
                    e.getMessage()
            );


            model.addAttribute(
                    "registrationError",
                    e.getMessage()
            );


            keepFormValues(

                    model,

                    firstName,

                    lastName,

                    email,

                    phone
            );


            return "compte/inscription";
        }
    }



    /* =====================================================
       MON COMPTE
    ===================================================== */

    @GetMapping("/compte")
    public String account(

            Authentication authentication,

            Model model
    ) {


        /*
         * L'utilisateur est déjà protégé
         * par Spring Security.
         */

        if (
                authentication == null ||
                !authentication.isAuthenticated()
        ) {

            return "redirect:/compte/login";
        }


        String email =
                authentication.getName();



        /* ===============================================
           INFORMATIONS CLIENT
        =============================================== */

        Customer customer =
                customerService.findByEmail(
                        email
                );


        model.addAttribute(
                "customer",
                customer
        );



        /* ===============================================
           COMMANDES CLIENT
        =============================================== */

        model.addAttribute(
                "orders",
                orderService.findCustomerOrders(
                        email
                )
        );



        /* ===============================================
           FAVORIS CLIENT
        =============================================== */

        model.addAttribute(
                "favorites",
                favoriteService.findCustomerFavorites(
                        email
                )
        );


        return "compte/index";
    }



    /* =====================================================
       DETAIL SECURISE D'UNE COMMANDE CLIENT
    ===================================================== */

    @GetMapping("/compte/commandes/{id}")
    public String orderDetails(

            @PathVariable("id")
            Long id,

            Authentication authentication,

            Model model
    ) {


        /*
         * Sécurité supplémentaire :
         * si aucun utilisateur n'est connecté.
         */

        if (
                authentication == null ||
                !authentication.isAuthenticated()
        ) {

            return "redirect:/compte/login";
        }


        String customerEmail =
                authentication.getName();


        try {


            /*
             * IMPORTANT :
             *
             * La recherche utilise :
             *
             * ID commande
             * +
             * email du client connecté
             *
             * Un client ne peut donc pas consulter
             * la commande d'un autre client.
             */

            CustomerOrder order =
                    orderService
                            .findCustomerOrderById(
                                    id,
                                    customerEmail
                            );


            model.addAttribute(
                    "order",
                    order
            );


            return "compte/commande-detail";


        } catch (
                IllegalArgumentException e
        ) {


            /*
             * On renvoie 404 plutôt que 403.
             *
             * Cela évite de révéler qu'une commande
             * existe mais appartient à un autre client.
             */

            throw new ResponseStatusException(

                    HttpStatus.NOT_FOUND,

                    "Commande introuvable."
            );
        }
    }



    /* =====================================================
       MODIFIER PROFIL
    ===================================================== */

    @PostMapping("/compte/profil")
    public String updateProfile(

            Authentication authentication,

            @RequestParam
            String firstName,

            @RequestParam
            String lastName,

            @RequestParam
            String phone,

            @RequestParam(required = false)
            String city,

            @RequestParam(required = false)
            String address,

            RedirectAttributes redirectAttributes
    ) {


        try {


            String email =
                    authentication.getName();


            customerService.updateProfile(

                    email,

                    firstName,

                    lastName,

                    phone,

                    city,

                    address
            );


            redirectAttributes
                    .addFlashAttribute(

                            "profileSuccess",

                            "Vos informations ont été mises à jour avec succès."
                    );


        } catch (
                IllegalArgumentException e
        ) {


            redirectAttributes
                    .addFlashAttribute(

                            "profileError",

                            e.getMessage()
                    );
        }


        /*
         * L'id de la section dans compte/index.html
         * est maintenant "profil".
         */

        return "redirect:/compte#profil";
    }



    /* =====================================================
       CHANGER MOT DE PASSE
    ===================================================== */

    @PostMapping("/compte/mot-de-passe")
    public String changePassword(

            Authentication authentication,

            @RequestParam
            String currentPassword,

            @RequestParam
            String newPassword,

            @RequestParam
            String confirmNewPassword,

            RedirectAttributes redirectAttributes
    ) {


        try {


            String email =
                    authentication.getName();


            customerService.changePassword(

                    email,

                    currentPassword,

                    newPassword,

                    confirmNewPassword
            );


            redirectAttributes
                    .addFlashAttribute(

                            "passwordSuccess",

                            "Votre mot de passe a été modifié avec succès."
                    );


        } catch (
                IllegalArgumentException e
        ) {


            redirectAttributes
                    .addFlashAttribute(

                            "passwordError",

                            e.getMessage()
                    );
        }


        return "redirect:/compte#securite";
    }



    /* =====================================================
       UTILITAIRE FORMULAIRE INSCRIPTION
    ===================================================== */

    private void keepFormValues(

            Model model,

            String firstName,

            String lastName,

            String email,

            String phone
    ) {


        model.addAttribute(
                "firstName",
                firstName
        );


        model.addAttribute(
                "lastName",
                lastName
        );


        model.addAttribute(
                "email",
                email
        );


        model.addAttribute(
                "phone",
                phone
        );
    }

}