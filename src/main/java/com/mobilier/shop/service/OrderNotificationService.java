package com.mobilier.shop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.mobilier.shop.entity.CustomerOrder;


@Service
public class OrderNotificationService {


    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    OrderNotificationService.class
            );


    private final JavaMailSender mailSender;

    private final String fromAddress;



    public OrderNotificationService(

            JavaMailSender mailSender,

            @Value("${app.mail.from:}")
            String configuredFrom,

            @Value("${spring.mail.username:}")
            String mailUsername
    ) {

        this.mailSender =
                mailSender;


        if (
                configuredFrom != null
                &&
                !configuredFrom.isBlank()
        ) {

            this.fromAddress =
                    configuredFrom.trim();

        } else {

            this.fromAddress =
                    mailUsername == null
                            ? ""
                            : mailUsername.trim();
        }
    }



    /* =========================================================
       ENVOYER NOTIFICATION STATUT
    ========================================================= */

    public boolean sendStatusNotification(
            CustomerOrder order
    ) {


        if (order == null) {

            return false;
        }


        if (
                order.getId() == null
        ) {

            return false;
        }


        if (
                order.getCustomerEmail() == null
                ||
                order.getCustomerEmail().isBlank()
        ) {

            LOGGER.warn(
                    "Email non envoyé pour la commande {} : aucun email client.",
                    order.getId()
            );

            return false;
        }


        if (
                fromAddress == null
                ||
                fromAddress.isBlank()
        ) {

            LOGGER.warn(
                    "Email non envoyé pour la commande {} : adresse expéditeur non configurée.",
                    order.getId()
            );

            return false;
        }


        String messageStatus =
                buildStatusMessage(
                        order.getId(),
                        order.getStatus()
                );


        if (messageStatus == null) {

            return false;
        }


        String customerName =
                order.getCustomerName() == null
                        ||
                        order.getCustomerName().isBlank()
                        ? "Client"
                        : order.getCustomerName().trim();



        String body =
                "Bonjour "
                +
                customerName
                +
                ",\n\n"
                +
                messageStatus
                +
                "\n\n"
                +
                "Vous pouvez consulter le statut de votre commande "
                +
                "depuis votre espace client Zineb Déco."
                +
                "\n\n"
                +
                "Merci pour votre confiance."
                +
                "\n\n"
                +
                "Zineb Déco";



        SimpleMailMessage message =
                new SimpleMailMessage();


        message.setFrom(
                fromAddress
        );


        message.setTo(
                order
                        .getCustomerEmail()
                        .trim()
        );


        message.setSubject(
                "Zineb Déco - Mise à jour commande #"
                +
                order.getId()
        );


        message.setText(
                body
        );



        try {


            mailSender.send(
                    message
            );


            LOGGER.info(
                    "Notification envoyée pour la commande {}.",
                    order.getId()
            );


            return true;


        } catch (MailException e) {


            /*
             * IMPORTANT :
             *
             * Une erreur SMTP ne doit jamais
             * annuler la modification du statut
             * enregistrée dans MySQL.
             */

            LOGGER.error(
                    "Impossible d'envoyer la notification de la commande {}.",
                    order.getId(),
                    e
            );


            return false;
        }

    }



    /* =========================================================
       MESSAGE SELON STATUT
    ========================================================= */

    private String buildStatusMessage(

            Long orderId,

            String status
    ) {


        if (
                status == null
                ||
                status.isBlank()
        ) {

            return null;
        }


        return switch (
                status
                        .trim()
                        .toUpperCase()
        ) {


            case "CONFIRMEE" ->

                    "Votre commande #"
                    +
                    orderId
                    +
                    " a été confirmée.";



            case "EN_FABRICATION" ->

                    "Votre commande #"
                    +
                    orderId
                    +
                    " est maintenant en fabrication.";



            case "PRETE" ->

                    "Votre commande #"
                    +
                    orderId
                    +
                    " est prête.";



            case "LIVREE" ->

                    "Votre commande #"
                    +
                    orderId
                    +
                    " a été livrée.";



            case "ANNULEE" ->

                    "Votre commande #"
                    +
                    orderId
                    +
                    " a été annulée.";



            case "NOUVELLE" ->

                    "Votre commande #"
                    +
                    orderId
                    +
                    " a été enregistrée.";



            default ->
                    null;

        };

    }

}