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
import com.mobilier.shop.service.OrderService;


@Controller
@RequestMapping("/admin/commandes")
public class AdminOrderController {


    /* =========================================================
       SERVICE
    ========================================================= */

    private final OrderService orderService;



    /* =========================================================
       CONSTRUCTEUR
    ========================================================= */

    public AdminOrderController(
            OrderService orderService
    ) {

        this.orderService =
                orderService;
    }



    /* =========================================================
       LISTE + RECHERCHE + FILTRE

       Exemples :

       /admin/commandes

       /admin/commandes?search=hamza

       /admin/commandes?status=NOUVELLE

       /admin/commandes?search=hamza&status=CONFIRMEE
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
           STATISTIQUES SUR TOUTES LES COMMANDES
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
         * Permet au formulaire HTML
         * de conserver les filtres sélectionnés.
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
       MODIFIER STATUT
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


            orderService.updateStatus(
                    id,
                    status
            );


            redirectAttributes
                    .addFlashAttribute(
                            "success",
                            "Le statut de la commande a été mis à jour."
                    );


        } catch (
                IllegalArgumentException e
        ) {


            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            e.getMessage()
                    );
        }


        return "redirect:/admin/commandes/" + id;
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