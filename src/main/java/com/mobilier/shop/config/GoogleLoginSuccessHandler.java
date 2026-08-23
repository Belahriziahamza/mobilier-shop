package com.mobilier.shop.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.mobilier.shop.entity.Customer;
import com.mobilier.shop.service.GoogleCustomerService;
import com.mobilier.shop.service.OnlineCustomerService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class GoogleLoginSuccessHandler
        implements AuthenticationSuccessHandler {

    private final GoogleCustomerService googleCustomerService;
    private final OnlineCustomerService onlineCustomerService;

    public GoogleLoginSuccessHandler(
            GoogleCustomerService googleCustomerService,
            OnlineCustomerService onlineCustomerService) {
        this.googleCustomerService = googleCustomerService;
        this.onlineCustomerService = onlineCustomerService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        if (!(authentication.getPrincipal() instanceof OidcUser googleUser)) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/compte/login?googleError");

            return;
        }

        try {

            /* Création / mise à jour du client Google */
            Customer customer = googleCustomerService
                    .loginOrCreateGoogleCustomer(
                            googleUser);

            /* Session actuellement en ligne */
            HttpSession session = request.getSession(true);

            onlineCustomerService.customerConnected(
                    session.getId(),
                    customer.getEmail());

            response.sendRedirect(
                    request.getContextPath()
                            + "/compte");

        } catch (IllegalArgumentException exception) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/compte/login?googleError");
        }
    }
}