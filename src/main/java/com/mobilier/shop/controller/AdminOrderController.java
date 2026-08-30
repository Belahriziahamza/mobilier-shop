package com.mobilier.shop.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mobilier.shop.entity.CustomerOrder;
import com.mobilier.shop.service.OrderNotificationService;
import com.mobilier.shop.service.OrderService;


@Controller
@RequestMapping("/admin/commandes")
public class AdminOrderController {


    /* =========================================================
       SERVICES
    ========================================================= */

    private final OrderService orderService;

    private final OrderNotificationService orderNotificationService;



    /* =========================================================
       CONSTRUCTEUR
    ========================================================= */

    public AdminOrderController(

            OrderService orderService,

            OrderNotificationService orderNotificationService
    ) {

        this.orderService =
                orderService;


        this.orderNotificationService =
                orderNotificationService;
    }



    /* =========================================================
       LISTE + RECHERCHE + FILTRE
    ========================================================= */

    @GetMapping
    public String orders(

            @RequestParam(
                    value = "search",
                    required = false,
                    defaultValue = ""
            )
            String search,

            @RequestParam(
                    value = "status",
                    required = false,
                    defaultValue = ""
            )
            String status,

            Model model
    ) {


        /* =====================================================
           TOUTES LES COMMANDES
        ===================================================== */

        List<CustomerOrder> allOrders =
                orderService.findAll();



        /* =====================================================
           NORMALISER RECHERCHE
        ===================================================== */

        String normalizedSearch =
                search == null
                        ? ""
                        : search
                                .trim()
                                .toLowerCase(
                                        Locale.ROOT
                                );


        String normalizedStatus =
                status == null
                        ? ""
                        : status
                                .trim()
                                .toUpperCase(
                                        Locale.ROOT
                                );



        /* =====================================================
           FILTRAGE
        ===================================================== */

        List<CustomerOrder> filteredOrders =
                allOrders
                        .stream()


                        /* =====================================
                           RECHERCHE CLIENT
                        ====================================== */

                        .filter(
                                order -> {


                                    if (
                                            normalizedSearch.isBlank()
                                    ) {

                                        return true;
                                    }


                                    String name =
                                            safe(
                                                    order.getCustomerName()
                                            );


                                    String email =
                                            safe(
                                                    order.getCustomerEmail()
                                            );


                                    String phone =
                                            safe(
                                                    order.getPhone()
                                            );


                                    String city =
                                            safe(
                                                    order.getCity()
                                            );


                                    String orderId =
                                            order.getId() != null
                                                    ? String.valueOf(
                                                            order.getId()
                                                    )
                                                    : "";


                                    return
                                            name.contains(
                                                    normalizedSearch
                                            )
                                            ||
                                            email.contains(
                                                    normalizedSearch
                                            )
                                            ||
                                            phone.contains(
                                                    normalizedSearch
                                            )
                                            ||
                                            city.contains(
                                                    normalizedSearch
                                            )
                                            ||
                                            orderId.contains(
                                                    normalizedSearch
                                            );
                                }
                        )


                        /* =====================================
                           FILTRE STATUT
                        ====================================== */

                        .filter(
                                order -> {


                                    if (
                                            normalizedStatus.isBlank()
                                    ) {

                                        return true;
                                    }


                                    return
                                            normalizedStatus.equals(
                                                    order.getStatus()
                                            );
                                }
                        )


                        .toList();



        /* =====================================================
           STATISTIQUES
        ===================================================== */

        long totalOrders =
                allOrders.size();


        long newOrders =
                countStatus(
                        allOrders,
                        "NOUVELLE"
                );


        long confirmedOrders =
                countStatus(
                        allOrders,
                        "CONFIRMEE"
                );


        long manufacturingOrders =
                countStatus(
                        allOrders,
                        "EN_FABRICATION"
                );


        long readyOrders =
                countStatus(
                        allOrders,
                        "PRETE"
                );


        long deliveredOrders =
                countStatus(
                        allOrders,
                        "LIVREE"
                );


        long cancelledOrders =
                countStatus(
                        allOrders,
                        "ANNULEE"
                );



        /* =====================================================
           MODEL
        ===================================================== */

        model.addAttribute(
                "orders",
                filteredOrders
        );


        model.addAttribute(
                "totalOrders",
                totalOrders
        );


        model.addAttribute(
                "resultCount",
                filteredOrders.size()
        );


        model.addAttribute(
                "newOrders",
                newOrders
        );


        model.addAttribute(
                "confirmedOrders",
                confirmedOrders
        );


        model.addAttribute(
                "manufacturingOrders",
                manufacturingOrders
        );


        model.addAttribute(
                "readyOrders",
                readyOrders
        );


        model.addAttribute(
                "deliveredOrders",
                deliveredOrders
        );


        model.addAttribute(
                "cancelledOrders",
                cancelledOrders
        );


        /*
         * Conserver les valeurs des filtres.
         */

        model.addAttribute(
                "search",
                search
        );


        model.addAttribute(
                "selectedStatus",
                normalizedStatus
        );


        return "admin/orders";
    }



    /* =========================================================
       DETAIL COMMANDE
    ========================================================= */

    @GetMapping("/{id}")
    public String orderDetails(

            @PathVariable("id")
            Long id,

            Model model,

            RedirectAttributes redirectAttributes
    ) {


        try {


            CustomerOrder order =
                    orderService.findById(
                            id
                    );


            model.addAttribute(
                    "order",
                    order
            );


            return "admin/order-detail";


        } catch (
                IllegalArgumentException e
        ) {


            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            e.getMessage()
                    );


            return "redirect:/admin/commandes";
        }
    }



    /* =========================================================
       MODIFIER STATUT + NOTIFIER CLIENT
    ========================================================= */

    @PostMapping("/{id}/statut")
    public String updateStatus(

            @PathVariable("id")
            Long id,

            @RequestParam("status")
            String status,

            RedirectAttributes redirectAttributes
    ) {


        try {


            /* =================================================
               COMMANDE AVANT MODIFICATION
            ================================================= */

            CustomerOrder currentOrder =
                    orderService.findById(
                            id
                    );


            String previousStatus =
                    currentOrder.getStatus();



            /* =================================================
               MODIFIER ET ENREGISTRER LE STATUT
            ================================================= */

            CustomerOrder updatedOrder =
                    orderService.updateStatus(
                            id,
                            status
                    );



            /* =================================================
               VERIFIER SI LE STATUT A CHANGE
            ================================================= */

            boolean statusChanged =
                    previousStatus == null
                    ||
                    !previousStatus.equalsIgnoreCase(
                            updatedOrder.getStatus()
                    );



            /* =================================================
               SI STATUT INCHANGE
            ================================================= */

            if (!statusChanged) {


                redirectAttributes
                        .addFlashAttribute(
                                "success",
                                "Le statut de la commande est déjà "
                                        + formatStatus(updatedOrder.getStatus())
                                        + "."
                        );


                return "redirect:/admin/commandes";
            }



            /* =================================================
               ENVOYER EMAIL AU CLIENT
            ================================================= */

            boolean emailSent =
                    orderNotificationService
                            .sendStatusNotification(
                                    updatedOrder
                            );



            /* =================================================
               MESSAGE ADMIN
            ================================================= */

            if (emailSent) {


                redirectAttributes
                        .addFlashAttribute(
                                "success",
                                "Commande #"
                                        + updatedOrder.getId()
                                        + " mise à jour : "
                                        + formatStatus(
                                                updatedOrder.getStatus()
                                        )
                                        + ". Le client a été notifié par email."
                        );


            } else {


                /*
                 * Le statut reste enregistré même si
                 * l'email n'a pas pu être envoyé.
                 */

                redirectAttributes
                        .addFlashAttribute(
                                "success",
                                "Commande #"
                                        + updatedOrder.getId()
                                        + " mise à jour : "
                                        + formatStatus(
                                                updatedOrder.getStatus()
                                        )
                                        + ". L'email client n'a pas été envoyé."
                        );

            }


        } catch (
                IllegalArgumentException e
        ) {


            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            e.getMessage()
                    );

        }



        /* =====================================================
           IMPORTANT :
           RETOUR VERS MES COMMANDES ADMIN
        ===================================================== */

        return "redirect:/admin/commandes";
    }



    /* =========================================================
       COMPTER UN STATUT
    ========================================================= */

    private long countStatus(

            List<CustomerOrder> orders,

            String status
    ) {


        return orders
                .stream()

                .filter(
                        order ->
                                status.equals(
                                        order.getStatus()
                                )
                )

                .count();
    }



    /* =========================================================
       FORMATTER STATUT
    ========================================================= */

    private String formatStatus(
            String status
    ) {


        if (
                status == null
                ||
                status.isBlank()
        ) {

            return "";
        }


        return switch (
                status
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        )
        ) {

            case "NOUVELLE" ->
                    "Nouvelle";

            case "CONFIRMEE" ->
                    "Confirmée";

            case "EN_FABRICATION" ->
                    "En fabrication";

            case "PRETE" ->
                    "Prête";

            case "LIVREE" ->
                    "Livrée";

            case "ANNULEE" ->
                    "Annulée";

            default ->
                    status;
        };
    }



    /* =========================================================
       STRING SECURISEE
    ========================================================= */

    private String safe(
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