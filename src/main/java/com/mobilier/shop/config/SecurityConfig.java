package com.mobilier.shop.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import com.mobilier.shop.service.CustomerUserDetailsService;
import com.mobilier.shop.service.GoogleOidcUserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        /*
         * =========================================================
         * 01. PASSWORD ENCODER
         * =========================================================
         */

        @Bean
        public PasswordEncoder passwordEncoder() {

                return new BCryptPasswordEncoder();
        }

        /*
         * =========================================================
         * 02. SECURITY CONTEXT ADMIN
         * =========================================================
         */

        @Bean("adminSecurityContextRepository")
        public SecurityContextRepository adminSecurityContextRepository() {

                HttpSessionSecurityContextRepository repository = new HttpSessionSecurityContextRepository();

                repository.setSpringSecurityContextKey(
                                "ZINEB_ADMIN_SECURITY_CONTEXT");

                return repository;
        }

        /*
         * =========================================================
         * 03. SECURITY CONTEXT CLIENT
         * =========================================================
         */

        @Bean("customerSecurityContextRepository")
        public SecurityContextRepository customerSecurityContextRepository() {

                HttpSessionSecurityContextRepository repository = new HttpSessionSecurityContextRepository();

                repository.setSpringSecurityContextKey(
                                "ZINEB_CUSTOMER_SECURITY_CONTEXT");

                return repository;
        }

        /*
         * =========================================================
         * 04. AUTHENTICATION PROVIDER CLIENT
         * =========================================================
         */

        @Bean("customerAuthenticationProvider")
        public AuthenticationProvider customerAuthenticationProvider(

                        CustomerUserDetailsService customerUserDetailsService,

                        PasswordEncoder passwordEncoder) {

                DaoAuthenticationProvider provider = new DaoAuthenticationProvider(
                                customerUserDetailsService);

                provider.setPasswordEncoder(
                                passwordEncoder);

                return provider;
        }

        /*
         * =========================================================
         * 05. AUTHENTICATION PROVIDER ADMIN
         * =========================================================
         */

        @Bean("adminAuthenticationProvider")
        public AuthenticationProvider adminAuthenticationProvider(

                        Environment environment,

                        PasswordEncoder passwordEncoder) {

                String username = environment.getProperty(
                                "spring.security.user.name",
                                "root");

                String rawPassword = environment.getProperty(
                                "spring.security.user.password");

                if (rawPassword == null ||
                                rawPassword.isBlank()) {

                        throw new IllegalStateException(
                                        "spring.security.user.password est obligatoire.");
                }

                UserDetails admin = User
                                .withUsername(
                                                username)
                                .password(
                                                passwordEncoder.encode(
                                                                rawPassword))
                                .roles(
                                                "ADMIN")
                                .build();

                UserDetailsService adminUsers = new InMemoryUserDetailsManager(
                                admin);

                DaoAuthenticationProvider provider = new DaoAuthenticationProvider(
                                adminUsers);

                provider.setPasswordEncoder(
                                passwordEncoder);

                return provider;
        }

        /*
         * =========================================================
         * 06. AUTHENTICATION MANAGER ADMIN
         * =========================================================
         */

        @Bean("adminAuthenticationManager")
        public AuthenticationManager adminAuthenticationManager(

                        @Qualifier("adminAuthenticationProvider") AuthenticationProvider adminProvider) {

                return new ProviderManager(
                                adminProvider);
        }

        /*
         * =========================================================
         * 07. AUTHENTICATION MANAGER CLIENT
         * =========================================================
         */

        @Bean("customerAuthenticationManager")
        public AuthenticationManager customerAuthenticationManager(

                        @Qualifier("customerAuthenticationProvider") AuthenticationProvider customerProvider) {

                return new ProviderManager(
                                customerProvider);
        }

        /*
         * =========================================================
         * 08. SECURITY ADMIN
         * =========================================================
         */

        @Bean
        @Order(1)
        public SecurityFilterChain adminSecurityFilterChain(

                        HttpSecurity http,

                        @Qualifier("adminAuthenticationManager") AuthenticationManager adminAuthenticationManager,

                        @Qualifier("adminSecurityContextRepository") SecurityContextRepository adminSecurityContextRepository

        ) throws Exception {

                /*
                 * Handler logout ADMIN.
                 *
                 * IMPORTANT :
                 * on ne détruit PAS toute la session,
                 * sinon le Client serait aussi déconnecté.
                 */

                SecurityContextLogoutHandler adminLogoutHandler = new SecurityContextLogoutHandler();

                adminLogoutHandler.setSecurityContextRepository(
                                adminSecurityContextRepository);

                adminLogoutHandler.setInvalidateHttpSession(
                                false);

                adminLogoutHandler.setClearAuthentication(
                                true);

                http

                                /*
                                 * ================================
                                 * ROUTES ADMIN
                                 * =================================
                                 */

                                .securityMatcher(
                                                "/admin/**")

                                /*
                                 * ================================
                                 * CONTEXTE ADMIN
                                 * =================================
                                 */

                                .securityContext(

                                                securityContext -> securityContext

                                                                .securityContextRepository(
                                                                                adminSecurityContextRepository))

                                /*
                                 * ================================
                                 * AUTHENTICATION MANAGER ADMIN
                                 * =================================
                                 */

                                .authenticationManager(
                                                adminAuthenticationManager)

                                /*
                                 * ================================
                                 * AUTORISATIONS
                                 * =================================
                                 */

                                .authorizeHttpRequests(

                                                auth -> auth

                                                                .requestMatchers(
                                                                                "/admin/login")
                                                                .permitAll()

                                                                .anyRequest()
                                                                .hasRole(
                                                                                "ADMIN"))

                                /*
                                 * ================================
                                 * LOGIN ADMIN
                                 * =================================
                                 */

                                .formLogin(

                                                form -> form

                                                                .loginPage(
                                                                                "/admin/login")

                                                                .loginProcessingUrl(
                                                                                "/admin/login")

                                                                .usernameParameter(
                                                                                "username")

                                                                .passwordParameter(
                                                                                "password")

                                                                .defaultSuccessUrl(
                                                                                "/admin",
                                                                                true)

                                                                .failureUrl(
                                                                                "/admin/login?error")

                                                                .permitAll())

                                /*
                                 * ================================
                                 * LOGOUT ADMIN
                                 * =================================
                                 */

                                .logout(

                                                logout -> logout

                                                                .logoutUrl(
                                                                                "/admin/logout")

                                                                .logoutSuccessUrl(
                                                                                "/admin/login?logout")

                                                                /*
                                                                 * Supprime uniquement
                                                                 * le contexte ADMIN.
                                                                 */
                                                                .addLogoutHandler(
                                                                                adminLogoutHandler)

                                                                /*
                                                                 * Ne pas détruire
                                                                 * toute la session.
                                                                 */
                                                                .invalidateHttpSession(
                                                                                false)

                                                                .clearAuthentication(
                                                                                true));

                return http.build();
        }

        /*
         * =========================================================
         * 09. SECURITY CLIENT LOCAL
         * =========================================================
         */

        @Bean
        @Order(2)
        public SecurityFilterChain customerSecurityFilterChain(

                        HttpSecurity http,

                        @Qualifier("customerAuthenticationManager") AuthenticationManager customerAuthenticationManager,

                        CustomerLoginSuccessHandler customerLoginSuccessHandler,

                        CustomerOnlineLogoutHandler customerOnlineLogoutHandler,

                        @Qualifier("customerSecurityContextRepository") SecurityContextRepository customerSecurityContextRepository

        ) throws Exception {

                /*
                 * Handler qui supprime uniquement
                 * le contexte de sécurité CLIENT.
                 */

                SecurityContextLogoutHandler customerLogoutHandler = new SecurityContextLogoutHandler();

                customerLogoutHandler.setSecurityContextRepository(
                                customerSecurityContextRepository);

                customerLogoutHandler.setInvalidateHttpSession(
                                false);

                customerLogoutHandler.setClearAuthentication(
                                true);

                http

                                /*
                                 * ================================
                                 * ROUTES CLIENT
                                 * =================================
                                 */

                                .securityMatcher(
                                                "/compte/**")

                                /*
                                 * ================================
                                 * CONTEXTE CLIENT
                                 * =================================
                                 */

                                .securityContext(

                                                securityContext -> securityContext

                                                                .securityContextRepository(
                                                                                customerSecurityContextRepository))

                                /*
                                 * ================================
                                 * AUTHENTICATION MANAGER CLIENT
                                 * =================================
                                 */

                                .authenticationManager(
                                                customerAuthenticationManager)

                                /*
                                 * ================================
                                 * AUTORISATIONS
                                 * =================================
                                 */

                                .authorizeHttpRequests(

                                                auth -> auth

                                                                /* Login */

                                                                .requestMatchers(
                                                                                "/compte/login")
                                                                .permitAll()

                                                                /* Inscription */

                                                                .requestMatchers(
                                                                                "/compte/inscription")
                                                                .permitAll()

                                                                /* Vérification Email */

                                                                .requestMatchers(
                                                                                "/compte/verifier-email")
                                                                .permitAll()

                                                                /* Espace client */

                                                                .anyRequest()
                                                                .hasRole(
                                                                                "CUSTOMER"))

                                /*
                                 * ================================
                                 * LOGIN CLIENT LOCAL
                                 * =================================
                                 */

                                .formLogin(

                                                form -> form

                                                                .loginPage(
                                                                                "/compte/login")

                                                                .loginProcessingUrl(
                                                                                "/compte/login")

                                                                .usernameParameter(
                                                                                "username")

                                                                .passwordParameter(
                                                                                "password")

                                                                /*
                                                                 * Met à jour last_login_at
                                                                 * et enregistre le client
                                                                 * comme EN LIGNE.
                                                                 */
                                                                .successHandler(
                                                                                customerLoginSuccessHandler)

                                                                .failureUrl(
                                                                                "/compte/login?error")

                                                                .permitAll())

                                /*
                                 * ================================
                                 * LOGOUT CLIENT
                                 * =================================
                                 */

                                .logout(

                                                logout -> logout

                                                                .logoutUrl(
                                                                                "/compte/logout")

                                                                .logoutSuccessUrl(
                                                                                "/compte/login?logout")

                                                                /*
                                                                 * 1.
                                                                 * Retire la session
                                                                 * de OnlineCustomerService.
                                                                 *
                                                                 * Client devient HORS LIGNE.
                                                                 */
                                                                .addLogoutHandler(
                                                                                customerOnlineLogoutHandler)

                                                                /*
                                                                 * 2.
                                                                 * Supprime uniquement
                                                                 * le contexte CLIENT.
                                                                 */
                                                                .addLogoutHandler(
                                                                                customerLogoutHandler)

                                                                /*
                                                                 * IMPORTANT :
                                                                 *
                                                                 * false = ne détruit pas
                                                                 * la session ADMIN.
                                                                 */
                                                                .invalidateHttpSession(
                                                                                false)

                                                                .clearAuthentication(
                                                                                true));

                return http.build();
        }

        /*
         * =========================================================
         * 10. SITE PUBLIC + GOOGLE OAUTH2
         * =========================================================
         */

        @Bean
        @Order(3)
        public SecurityFilterChain publicSecurityFilterChain(

                        HttpSecurity http,

                        GoogleOidcUserService googleOidcUserService,

                        GoogleLoginSuccessHandler googleLoginSuccessHandler,

                        @Qualifier("customerSecurityContextRepository") SecurityContextRepository customerSecurityContextRepository

        ) throws Exception {

                /*
                 * Google utilise le même contexte
                 * que le compte CLIENT.
                 *
                 * Donc :
                 *
                 * Google
                 * ↓
                 * ROLE_CUSTOMER
                 * ↓
                 * /compte
                 */

                http

                                /*
                                 * ================================
                                 * CONTEXTE CLIENT POUR GOOGLE
                                 * =================================
                                 */

                                .securityContext(

                                                securityContext -> securityContext

                                                                .securityContextRepository(
                                                                                customerSecurityContextRepository))

                                /*
                                 * ================================
                                 * AUTORISATIONS PUBLIQUES
                                 * =================================
                                 */

                                .authorizeHttpRequests(

                                                auth -> auth

                                                                /* Accueil */

                                                                .requestMatchers(
                                                                                "/")
                                                                .permitAll()

                                                                /*
                                                                 * =========================
                                                                 * GOOGLE OAUTH2
                                                                 * ==========================
                                                                 */

                                                                .requestMatchers(
                                                                                "/oauth2/**",
                                                                                "/login/oauth2/**")
                                                                .permitAll()

                                                                /*
                                                                 * =========================
                                                                 * FICHIERS STATIQUES
                                                                 * ==========================
                                                                 */

                                                                .requestMatchers(
                                                                                "/css/**",
                                                                                "/js/**",
                                                                                "/images/**",
                                                                                "/uploads/**",
                                                                                "/favicon.ico")
                                                                .permitAll()

                                                                /*
                                                                 * =========================
                                                                 * CATALOGUES
                                                                 * ==========================
                                                                 */

                                                                .requestMatchers(
                                                                                "/salons",
                                                                                "/canapes",
                                                                                "/chambres",
                                                                                "/lits",
                                                                                "/tetes-de-lit",
                                                                                "/chaises",
                                                                                "/fauteuils",
                                                                                "/tables",
                                                                                "/nouveautes",
                                                                                "/promotions")
                                                                .permitAll()

                                                                /*
                                                                 * =========================
                                                                 * PRODUITS
                                                                 * ==========================
                                                                 */

                                                                .requestMatchers(
                                                                                "/produit/**")
                                                                .permitAll()

                                                                /*
                                                                 * =========================
                                                                 * PAGES PUBLIQUES
                                                                 * ==========================
                                                                 */

                                                                .requestMatchers(
                                                                                "/sur-mesure",
                                                                                "/tissus",
                                                                                "/bois",
                                                                                "/mdf",
                                                                                "/contact",
                                                                                "/panier")
                                                                .permitAll()

                                                                /*
                                                                 * =========================
                                                                 * API
                                                                 * ==========================
                                                                 */

                                                                .requestMatchers(
                                                                                "/api/**")
                                                                .permitAll()

                                                                /*
                                                                 * =========================
                                                                 * RESTE DU SITE
                                                                 * ==========================
                                                                 */

                                                                .anyRequest()
                                                                .permitAll())

                                /*
                                 * ================================
                                 * GOOGLE OAUTH2 LOGIN
                                 * =================================
                                 */

                                .oauth2Login(

                                                oauth2 -> oauth2

                                                                /*
                                                                 * =========================
                                                                 * GOOGLE USER
                                                                 * ==========================
                                                                 */

                                                                .userInfoEndpoint(

                                                                                userInfo -> userInfo

                                                                                                .oidcUserService(
                                                                                                                googleOidcUserService::loadUser))

                                                                /*
                                                                 * GoogleLoginSuccessHandler :
                                                                 *
                                                                 * - crée/récupère le client
                                                                 * - auth_provider = GOOGLE
                                                                 * - email_verified = true
                                                                 * - last_login_at
                                                                 * - ajoute la session dans
                                                                 * OnlineCustomerService
                                                                 */

                                                                .successHandler(
                                                                                googleLoginSuccessHandler)

                                                                /*
                                                                 * =========================
                                                                 * ERREUR GOOGLE
                                                                 * ==========================
                                                                 */

                                                                .failureUrl(
                                                                                "/compte/login?googleError"));

                return http.build();
        }
}