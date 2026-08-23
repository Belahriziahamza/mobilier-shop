package com.mobilier.shop.config;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mobilier.shop.entity.ShopSetting;
import com.mobilier.shop.repository.ShopSettingRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class MaintenanceFilter extends OncePerRequestFilter {

    private final ShopSettingRepository shopSettingRepository;

    public MaintenanceFilter(
            ShopSettingRepository shopSettingRepository) {

        this.shopSettingRepository = shopSettingRepository;
    }

    /*
     * =====================================================
     * FILTRE GLOBAL
     * =====================================================
     */

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        /*
         * =================================================
         * ROUTES TOUJOURS AUTORISÉES
         * ==================================================
         */

        if (isAlwaysAllowed(uri)) {

            filterChain.doFilter(
                    request,
                    response);

            return;
        }

        /*
         * =================================================
         * CONFIGURATION BOUTIQUE
         * ==================================================
         */

        ShopSetting shopSetting = shopSettingRepository
                .findById(1L)
                .orElse(null);

        /*
         * S'il n'existe aucune configuration,
         * on ne bloque pas le site.
         */

        if (shopSetting == null) {

            filterChain.doFilter(
                    request,
                    response);

            return;
        }

        /*
         * =================================================
         * MODE MAINTENANCE
         * ==================================================
         */

        if (shopSetting.isMaintenanceMode()) {

            /*
             * On ne fait PAS une redirection HTTP classique.
             *
             * On fait un forward interne afin que :
             *
             * /produits
             * /
             * /contact
             *
             * retournent directement HTTP 503.
             *
             * C'est mieux pour Google.
             */

            request
                    .getRequestDispatcher(
                            "/maintenance")
                    .forward(
                            request,
                            response);

            return;
        }

        /*
         * =================================================
         * SITE NORMAL
         * ==================================================
         */

        filterChain.doFilter(
                request,
                response);
    }

    /*
     * =====================================================
     * ROUTES ACCESSIBLES PENDANT MAINTENANCE
     * =====================================================
     */

    private boolean isAlwaysAllowed(
            String uri) {

        /* ADMIN */

        if (uri.equals("/admin")
                ||
                uri.startsWith("/admin/")) {

            return true;
        }

        /* PAGE MAINTENANCE */

        if (uri.equals("/maintenance")) {

            return true;
        }

        /* CSS */

        if (uri.startsWith("/css/")) {

            return true;
        }

        /* JAVASCRIPT */

        if (uri.startsWith("/js/")) {

            return true;
        }

        /* IMAGES */

        if (uri.startsWith("/images/")) {

            return true;
        }

        /* WEBJARS */

        if (uri.startsWith("/webjars/")) {

            return true;
        }

        /* FAVICON */

        if (uri.equals("/favicon.ico")) {

            return true;
        }

        /* PAGE ERREUR SPRING */

        if (uri.equals("/error")) {

            return true;
        }

        return false;
    }

}