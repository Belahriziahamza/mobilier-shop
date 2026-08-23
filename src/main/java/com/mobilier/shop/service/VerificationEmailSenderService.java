package com.mobilier.shop.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.mobilier.shop.entity.Customer;


@Service
public class VerificationEmailSenderService {


    /* =====================================================
       JAVA MAIL SENDER
    ===================================================== */

    private final JavaMailSender mailSender;



    /* =====================================================
       URL DE L'APPLICATION

       En local :
       http://localhost:8080
    ===================================================== */

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;



    /* =====================================================
       EMAIL EXPEDITEUR

       Il viendra de spring.mail.username.
    ===================================================== */

    @Value("${spring.mail.username:}")
    private String senderEmail;



    /* =====================================================
       CONSTRUCTEUR
    ===================================================== */

    public VerificationEmailSenderService(
            JavaMailSender mailSender
    ) {

        this.mailSender =
                mailSender;
    }



    /* =====================================================
       ENVOYER EMAIL DE VERIFICATION
    ===================================================== */

    public void sendVerificationEmail(

            Customer customer,

            String rawToken
    ) {


        /* =================================================
           SECURITE PARAMETRES
        ================================================= */

        if (
                customer == null
                ||
                customer.getEmail() == null
                ||
                customer.getEmail().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Adresse email du client invalide."
            );
        }


        if (
                rawToken == null
                ||
                rawToken.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Token de vérification invalide."
            );
        }



        /* =================================================
           SMTP CONFIGURE ?
        ================================================= */

        if (
                senderEmail == null
                ||
                senderEmail.isBlank()
        ) {

            throw new IllegalStateException(
                    "L'adresse email d'envoi n'est pas configurée."
            );
        }



        /* =================================================
           LIEN DE VERIFICATION
        ================================================= */

        String verificationUrl =
                baseUrl
                        + "/compte/verifier-email?token="
                        + rawToken;



        /* =================================================
           MESSAGE
        ================================================= */

        SimpleMailMessage message =
                new SimpleMailMessage();


        message.setFrom(
                senderEmail
        );


        message.setTo(
                customer.getEmail()
        );


        message.setSubject(
                "Vérification de votre adresse email | Zineb Déco"
        );


        String firstName =
                customer.getFirstName() != null
                        ? customer.getFirstName()
                        : "";


        message.setText(

                "Bonjour "
                        + firstName
                        + ",\n\n"

                        + "Merci d'avoir créé votre compte Zineb Déco.\n\n"

                        + "Pour confirmer que cette adresse email vous appartient, "
                        + "cliquez sur le lien suivant :\n\n"

                        + verificationUrl
                        + "\n\n"

                        + "Ce lien est valable pendant 24 heures.\n\n"

                        + "Si vous n'êtes pas à l'origine de cette inscription, "
                        + "vous pouvez ignorer cet email.\n\n"

                        + "Zineb Déco"
        );



        /* =================================================
           ENVOI
        ================================================= */

        mailSender.send(
                message
        );
    }
}