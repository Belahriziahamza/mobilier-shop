package com.mobilier.shop.service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class OnlineCustomerService {

    /*
     * sessionId -> email du client
     *
     * Exemple :
     *
     * ABC123 -> client@gmail.com
     * XYZ789 -> autre@gmail.com
     *
     * ConcurrentHashMap permet plusieurs connexions
     * simultanées sans problème.
     */
    private final Map<String, String> onlineSessions = new ConcurrentHashMap<>();

    /*
     * =========================================================
     * ENREGISTRER UNE SESSION CLIENT
     * =========================================================
     */

    public void customerConnected(
            String sessionId,
            String email) {

        if (sessionId == null ||
                sessionId.isBlank() ||
                email == null ||
                email.isBlank()) {
            return;
        }

        onlineSessions.put(
                sessionId,
                email.trim().toLowerCase());
    }

    /*
     * =========================================================
     * SUPPRIMER UNE SESSION CLIENT
     * =========================================================
     */

    public void customerDisconnected(
            String sessionId) {

        if (sessionId == null ||
                sessionId.isBlank()) {
            return;
        }

        onlineSessions.remove(
                sessionId);
    }

    /*
     * =========================================================
     * SAVOIR SI UN CLIENT EST EN LIGNE
     * =========================================================
     */

    public boolean isOnline(
            String email) {

        if (email == null ||
                email.isBlank()) {
            return false;
        }

        String normalizedEmail = email.trim().toLowerCase();

        return onlineSessions
                .values()
                .stream()
                .anyMatch(
                        value -> value.equals(
                                normalizedEmail));
    }

    /*
     * =========================================================
     * NOMBRE DE CLIENTS ACTUELLEMENT EN LIGNE
     * 
     * IMPORTANT :
     * un client connecté sur plusieurs onglets/sessions
     * n'est compté qu'une seule fois.
     * =========================================================
     */

    public long countOnlineCustomers() {

        return onlineSessions
                .values()
                .stream()
                .distinct()
                .count();
    }

    /*
     * =========================================================
     * LISTE DES EMAILS EN LIGNE
     * =========================================================
     */

    public Set<String> getOnlineCustomerEmails() {

        return onlineSessions
                .values()
                .stream()
                .collect(
                        Collectors.toSet());
    }

    /*
     * =========================================================
     * NOMBRE DE SESSIONS CLIENT ACTIVES
     * 
     * Exemple :
     * même client connecté sur Chrome + Edge
     * = 2 sessions
     * =========================================================
     */

    public int countOnlineSessions() {

        return onlineSessions.size();
    }
}