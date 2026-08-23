package com.mobilier.shop.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import com.mobilier.shop.service.OnlineCustomerService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class CustomerOnlineLogoutHandler implements LogoutHandler {

    private final OnlineCustomerService onlineCustomerService;

    public CustomerOnlineLogoutHandler(
            OnlineCustomerService onlineCustomerService) {
        this.onlineCustomerService = onlineCustomerService;
    }

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return;
        }

        onlineCustomerService.customerDisconnected(
                session.getId());
    }
}