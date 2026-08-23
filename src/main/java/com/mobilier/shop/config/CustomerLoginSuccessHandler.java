package com.mobilier.shop.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.mobilier.shop.service.CustomerService;
import com.mobilier.shop.service.OnlineCustomerService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class CustomerLoginSuccessHandler
                implements AuthenticationSuccessHandler {

        private final CustomerService customerService;
        private final OnlineCustomerService onlineCustomerService;

        public CustomerLoginSuccessHandler(
                        CustomerService customerService,
                        OnlineCustomerService onlineCustomerService) {
                this.customerService = customerService;
                this.onlineCustomerService = onlineCustomerService;
        }

        @Override
        public void onAuthenticationSuccess(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        Authentication authentication) throws IOException, ServletException {

                String email = authentication.getName();

                /* Dernière connexion dans MySQL */
                customerService.updateLastLogin(email);

                /* Session actuellement en ligne */
                HttpSession session = request.getSession(true);

                onlineCustomerService.customerConnected(
                                session.getId(),
                                email);

                response.sendRedirect(
                                request.getContextPath() + "/compte");
        }
}