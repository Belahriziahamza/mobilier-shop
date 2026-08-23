package com.mobilier.shop.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class GoogleOidcUserService {

    /*
     * =====================================================
     * SERVICE OIDC STANDARD SPRING
     * =====================================================
     */

    private final OidcUserService delegate = new OidcUserService();

    /*
     * =====================================================
     * CHARGER UTILISATEUR GOOGLE
     * =====================================================
     */

    public OidcUser loadUser(
            OidcUserRequest userRequest) throws OAuth2AuthenticationException {

        /*
         * =================================================
         * GOOGLE VERIFIE LE COMPTE
         * =================================================
         */

        OidcUser googleUser = delegate.loadUser(
                userRequest);

        /*
         * =================================================
         * CONSERVER LES AUTORITES GOOGLE
         * =================================================
         */

        Set<GrantedAuthority> authorities = new HashSet<>(
                googleUser.getAuthorities());

        /*
         * =================================================
         * AJOUTER ROLE CLIENT ZINEB DECO
         * =================================================
         */

        authorities.add(
                new SimpleGrantedAuthority(
                        "ROLE_CUSTOMER"));

        /*
         * =================================================
         * CREER UTILISATEUR OIDC FINAL
         * =================================================
         */

        if (googleUser.getUserInfo() != null) {

            return new DefaultOidcUser(

                    authorities,

                    googleUser.getIdToken(),

                    googleUser.getUserInfo(),

                    "email");
        }

        return new DefaultOidcUser(

                authorities,

                googleUser.getIdToken(),

                "email");
    }
}