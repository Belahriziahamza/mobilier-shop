package com.mobilier.shop.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletResponse;

import com.mobilier.shop.entity.ShopSetting;
import com.mobilier.shop.repository.ShopSettingRepository;

@Controller
public class MaintenanceController {

    private final ShopSettingRepository shopSettingRepository;

    public MaintenanceController(
            ShopSettingRepository shopSettingRepository) {

        this.shopSettingRepository = shopSettingRepository;
    }

    /*
     * =====================================================
     * PAGE MAINTENANCE
     * =====================================================
     */

    @GetMapping("/maintenance")
    public String maintenance(
            Model model,
            HttpServletResponse response) {

        /*
         * HTTP 503 = indisponibilité temporaire.
         * C'est le bon statut pour une maintenance.
         */

        response.setStatus(
                HttpStatus.SERVICE_UNAVAILABLE.value());

        /*
         * Permet aux moteurs de recherche
         * de comprendre que le site reviendra.
         */

        response.setHeader(
                "Retry-After",
                "3600");

        ShopSetting shopSetting = shopSettingRepository
                .findById(1L)
                .orElse(null);

        model.addAttribute(
                "shopSetting",
                shopSetting);

        return "maintenance";
    }

}