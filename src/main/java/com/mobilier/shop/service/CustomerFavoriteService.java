package com.mobilier.shop.service;

import com.mobilier.shop.entity.Customer;
import com.mobilier.shop.entity.CustomerFavorite;
import com.mobilier.shop.entity.Product;

import com.mobilier.shop.repository.CustomerFavoriteRepository;
import com.mobilier.shop.repository.CustomerRepository;
import com.mobilier.shop.repository.ProductRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerFavoriteService {

    private final CustomerFavoriteRepository favoriteRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;


    public CustomerFavoriteService(
            CustomerFavoriteRepository favoriteRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository
    ) {

        this.favoriteRepository = favoriteRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }


    /* =====================================================
       LISTE DES FAVORIS
    ===================================================== */

    @Transactional(readOnly = true)
    public List<CustomerFavorite> findCustomerFavorites(
            String email
    ) {

        Customer customer =
                findCustomerByEmail(email);

        return favoriteRepository
                .findByCustomer_IdOrderByCreatedAtDesc(
                        customer.getId()
                );
    }


    /* =====================================================
       VERIFIER SI PRODUIT FAVORI
    ===================================================== */

    @Transactional(readOnly = true)
    public boolean isFavorite(
            String email,
            Long productId
    ) {

        Customer customer =
                findCustomerByEmail(email);

        return favoriteRepository
                .existsByCustomer_IdAndProduct_Id(
                        customer.getId(),
                        productId
                );
    }


    /* =====================================================
       AJOUTER / RETIRER FAVORI
    ===================================================== */

    @Transactional
    public boolean toggleFavorite(
            String email,
            Long productId
    ) {

        Customer customer =
                findCustomerByEmail(email);


        Product product =
                productRepository
                        .findById(productId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Produit introuvable."
                                        )
                        );


        if (!product.isActive()) {

            throw new IllegalArgumentException(
                    "Ce produit n'est plus disponible."
            );
        }


        Optional<CustomerFavorite> existing =
                favoriteRepository
                        .findByCustomer_IdAndProduct_Id(
                                customer.getId(),
                                product.getId()
                        );


        /* PRODUIT DEJA FAVORI -> RETIRER */

        if (existing.isPresent()) {

            favoriteRepository.delete(
                    existing.get()
            );

            return false;
        }


        /* SINON -> AJOUTER */

        CustomerFavorite favorite =
                new CustomerFavorite();


        favorite.setCustomer(
                customer
        );


        favorite.setProduct(
                product
        );


        favoriteRepository.save(
                favorite
        );


        return true;
    }


    /* =====================================================
       TROUVER CLIENT PAR EMAIL
    ===================================================== */

    private Customer findCustomerByEmail(
            String email
    ) {

        if (
                email == null ||
                email.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Client non connecté."
            );
        }


        return customerRepository
                .findByEmailIgnoreCase(
                        email.trim()
                )
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Compte client introuvable."
                                )
                );
    }
}