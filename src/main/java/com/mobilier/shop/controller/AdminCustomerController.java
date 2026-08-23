package com.mobilier.shop.controller;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mobilier.shop.entity.Customer;
import com.mobilier.shop.entity.CustomerOrder;
import com.mobilier.shop.repository.CustomerOrderRepository;
import com.mobilier.shop.repository.CustomerRepository;
import com.mobilier.shop.service.OnlineCustomerService;

@Controller
@RequestMapping("/admin/clients")
public class AdminCustomerController {

        private final CustomerRepository customerRepository;

        private final CustomerOrderRepository orderRepository;

        private final OnlineCustomerService onlineCustomerService;

        public AdminCustomerController(

                        CustomerRepository customerRepository,

                        CustomerOrderRepository orderRepository,

                        OnlineCustomerService onlineCustomerService) {

                this.customerRepository = customerRepository;

                this.orderRepository = orderRepository;

                this.onlineCustomerService = onlineCustomerService;
        }

        /*
         * =========================================================
         * LISTE + RECHERCHE + FILTRES CLIENTS
         * =========================================================
         */

        @GetMapping
        public String customers(

                        @RequestParam(value = "search", required = false, defaultValue = "") String search,

                        @RequestParam(value = "provider", required = false, defaultValue = "") String provider,

                        @RequestParam(value = "verified", required = false, defaultValue = "") String verified,

                        Model model) {

                /*
                 * =====================================================
                 * TOUS LES CLIENTS MYSQL
                 * =====================================================
                 */

                List<Customer> allCustomers = customerRepository
                                .findAll()
                                .stream()
                                .sorted(
                                                Comparator.comparing(
                                                                Customer::getId).reversed())
                                .toList();

                String normalizedSearch = normalize(search);

                String normalizedProvider = normalize(provider);

                /*
                 * =====================================================
                 * FILTRES
                 * =====================================================
                 */

                List<Customer> filteredCustomers = allCustomers
                                .stream()

                                /*
                                 * ==============================
                                 * RECHERCHE
                                 * ==============================
                                 */

                                .filter(customer -> {

                                        if (normalizedSearch
                                                        .isBlank()) {

                                                return true;
                                        }

                                        String firstName = normalize(
                                                        customer
                                                                        .getFirstName());

                                        String lastName = normalize(
                                                        customer
                                                                        .getLastName());

                                        String fullName = firstName
                                                        + " "
                                                        + lastName;

                                        String email = normalize(
                                                        customer
                                                                        .getEmail());

                                        String phone = normalize(
                                                        customer
                                                                        .getPhone());

                                        String city = normalize(
                                                        customer
                                                                        .getCity());

                                        String address = normalize(
                                                        customer
                                                                        .getAddress());

                                        String id = customer.getId() != null

                                                        ? String.valueOf(
                                                                        customer
                                                                                        .getId())

                                                        : "";

                                        return

                                firstName.contains(
                                                normalizedSearch)

                                                ||

                                                lastName.contains(
                                                                normalizedSearch)

                                                ||

                                                fullName.contains(
                                                                normalizedSearch)

                                                ||

                                                email.contains(
                                                                normalizedSearch)

                                                ||

                                                phone.contains(
                                                                normalizedSearch)

                                                ||

                                                city.contains(
                                                                normalizedSearch)

                                                ||

                                                address.contains(
                                                                normalizedSearch)

                                                ||

                                                id.contains(
                                                                normalizedSearch);
                                })

                                /*
                                 * ==============================
                                 * PROVIDER LOCAL / GOOGLE
                                 * ==============================
                                 */

                                .filter(customer -> {

                                        if (normalizedProvider
                                                        .isBlank()) {

                                                return true;
                                        }

                                        return normalize(
                                                        customer
                                                                        .getAuthProvider())
                                                        .equals(
                                                                        normalizedProvider);
                                })

                                /*
                                 * ==============================
                                 * EMAIL VERIFIE
                                 * ==============================
                                 */

                                .filter(customer -> {

                                        if (verified == null
                                                        ||
                                                        verified.isBlank()) {

                                                return true;
                                        }

                                        if ("true".equalsIgnoreCase(
                                                        verified)) {

                                                return customer
                                                                .isEmailVerified();
                                        }

                                        if ("false".equalsIgnoreCase(
                                                        verified)) {

                                                return !customer
                                                                .isEmailVerified();
                                        }

                                        return true;
                                })

                                .toList();

                /*
                 * =====================================================
                 * STATISTIQUES GENERALES
                 * =====================================================
                 */

                long localCustomers = allCustomers
                                .stream()
                                .filter(
                                                customer -> "LOCAL"
                                                                .equalsIgnoreCase(
                                                                                customer
                                                                                                .getAuthProvider()))
                                .count();

                long googleCustomers = allCustomers
                                .stream()
                                .filter(
                                                customer -> "GOOGLE"
                                                                .equalsIgnoreCase(
                                                                                customer
                                                                                                .getAuthProvider()))
                                .count();

                long verifiedCustomers = allCustomers
                                .stream()
                                .filter(
                                                Customer::isEmailVerified)
                                .count();

                long unverifiedCustomers = allCustomers.size()
                                - verifiedCustomers;

                /*
                 * Déjà connecté au moins une fois.
                 *
                 * Ce compteur est différent
                 * des clients actuellement en ligne.
                 */

                long customersWithLogin = allCustomers
                                .stream()
                                .filter(
                                                customer -> customer
                                                                .getLastLoginAt() != null)
                                .count();

                /*
                 * =====================================================
                 * CLIENTS ACTUELLEMENT EN LIGNE
                 * =====================================================
                 */

                long onlineCustomers = onlineCustomerService
                                .countOnlineCustomers();

                /*
                 * Liste des IDs actuellement en ligne.
                 *
                 * Le HTML pourra faire :
                 *
                 * #lists.contains(
                 * onlineCustomerIds,
                 * customer.id
                 * )
                 */

                List<Long> onlineCustomerIds = allCustomers
                                .stream()
                                .filter(
                                                customer -> onlineCustomerService
                                                                .isOnline(
                                                                                customer
                                                                                                .getEmail()))
                                .map(
                                                Customer::getId)
                                .toList();

                /*
                 * =====================================================
                 * MODEL
                 * =====================================================
                 */

                model.addAttribute(
                                "customers",
                                filteredCustomers);

                model.addAttribute(
                                "totalCustomers",
                                allCustomers.size());

                model.addAttribute(
                                "resultCount",
                                filteredCustomers.size());

                model.addAttribute(
                                "localCustomers",
                                localCustomers);

                model.addAttribute(
                                "googleCustomers",
                                googleCustomers);

                model.addAttribute(
                                "verifiedCustomers",
                                verifiedCustomers);

                model.addAttribute(
                                "unverifiedCustomers",
                                unverifiedCustomers);

                model.addAttribute(
                                "customersWithLogin",
                                customersWithLogin);

                /*
                 * NOUVEAU :
                 * nombre de clients en ligne maintenant.
                 */

                model.addAttribute(
                                "onlineCustomers",
                                onlineCustomers);

                /*
                 * NOUVEAU :
                 * permet d'afficher
                 * 🟢 En ligne / ⚫ Hors ligne.
                 */

                model.addAttribute(
                                "onlineCustomerIds",
                                onlineCustomerIds);

                model.addAttribute(
                                "search",
                                search);

                model.addAttribute(
                                "provider",
                                provider);

                model.addAttribute(
                                "verified",
                                verified);

                return "admin/customers";
        }

        /*
         * =========================================================
         * DETAIL CLIENT
         * =========================================================
         */

        @GetMapping("/{id}")
        public String customerDetails(

                        @PathVariable("id") Long id,

                        Model model,

                        RedirectAttributes redirectAttributes) {

                Customer customer = customerRepository
                                .findById(id)
                                .orElse(null);

                if (customer == null) {

                        redirectAttributes
                                        .addFlashAttribute(
                                                        "error",
                                                        "Client introuvable.");

                        return "redirect:/admin/clients";
                }

                /*
                 * =====================================================
                 * COMMANDES DU CLIENT
                 * =====================================================
                 */

                List<CustomerOrder> orders = orderRepository
                                .findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(
                                                customer.getEmail());

                long totalOrders = orders.size();

                /*
                 * =====================================================
                 * TOTAL DEPENSE
                 * =====================================================
                 */

                BigDecimal totalSpent = orders
                                .stream()

                                .filter(
                                                order -> !"ANNULEE"
                                                                .equals(
                                                                                order
                                                                                                .getStatus()))

                                .map(
                                                CustomerOrder::getTotalAmount)

                                .filter(
                                                amount -> amount != null)

                                .reduce(
                                                BigDecimal.ZERO,
                                                BigDecimal::add);

                /*
                 * =====================================================
                 * COMMANDES LIVREES
                 * =====================================================
                 */

                long deliveredOrders = orders
                                .stream()

                                .filter(
                                                order -> "LIVREE"
                                                                .equals(
                                                                                order
                                                                                                .getStatus()))

                                .count();

                /*
                 * =====================================================
                 * COMMANDES ACTIVES
                 * =====================================================
                 */

                long activeOrders = orders
                                .stream()

                                .filter(
                                                order ->

                                                order
                                                                .getStatus() != null

                                                                &&

                                                                !"LIVREE"
                                                                                .equals(
                                                                                                order
                                                                                                                .getStatus())

                                                                &&

                                                                !"ANNULEE"
                                                                                .equals(
                                                                                                order
                                                                                                                .getStatus()))

                                .count();

                /*
                 * =====================================================
                 * ETAT EN LIGNE DU CLIENT
                 * =====================================================
                 */

                boolean customerOnline = onlineCustomerService
                                .isOnline(
                                                customer.getEmail());

                /*
                 * =====================================================
                 * MODEL
                 * =====================================================
                 */

                model.addAttribute(
                                "customer",
                                customer);

                model.addAttribute(
                                "orders",
                                orders);

                model.addAttribute(
                                "totalOrders",
                                totalOrders);

                model.addAttribute(
                                "totalSpent",
                                totalSpent);

                model.addAttribute(
                                "deliveredOrders",
                                deliveredOrders);

                model.addAttribute(
                                "activeOrders",
                                activeOrders);

                model.addAttribute(
                                "customerOnline",
                                customerOnline);

                return "admin/customer-detail";
        }

        /*
         * =========================================================
         * NORMALISATION
         * =========================================================
         */

        private String normalize(
                        String value) {

                if (value == null) {

                        return "";
                }

                return value
                                .trim()
                                .toLowerCase(
                                                Locale.ROOT);
        }
}