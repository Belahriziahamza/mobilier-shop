package com.mobilier.shop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterCustomerRequest(

        @NotBlank(message = "Le prénom est obligatoire.")
        @Size(max = 100)
        String firstName,


        @NotBlank(message = "Le nom est obligatoire.")
        @Size(max = 100)
        String lastName,


        @NotBlank(message = "L'email est obligatoire.")
        @Email(message = "Adresse email invalide.")
        @Size(max = 180)
        String email,


        @NotBlank(message = "Le téléphone est obligatoire.")
        @Size(max = 30)
        String phone,


        @NotBlank(message = "Le mot de passe est obligatoire.")
        @Size(
                min = 8,
                max = 100,
                message = "Le mot de passe doit contenir au moins 8 caractères."
        )
        String password,


        @NotBlank(message = "Confirmez le mot de passe.")
        String confirmPassword

) {
}