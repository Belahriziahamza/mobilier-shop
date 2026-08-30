package com.mobilier.shop.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobilier.shop.entity.CustomerCartItem;
import com.mobilier.shop.repository.CustomerCartItemRepository;
import com.mobilier.shop.repository.PieceRepository;
import com.mobilier.shop.repository.ProductRepository;


@Service
public class CustomerCartService {


    /* =========================================================
       REPOSITORIES
    ========================================================= */

    private final CustomerCartItemRepository cartRepository;

    private final ProductRepository productRepository;

    private final PieceRepository pieceRepository;



    /* =========================================================
       CONSTRUCTEUR
    ========================================================= */

    public CustomerCartService(

            CustomerCartItemRepository cartRepository,

            ProductRepository productRepository,

            PieceRepository pieceRepository
    ) {

        this.cartRepository =
                cartRepository;

        this.productRepository =
                productRepository;

        this.pieceRepository =
                pieceRepository;
    }



    /* =========================================================
       PANIER DU CLIENT
    ========================================================= */

    @Transactional(readOnly = true)
    public List<CustomerCartItem> getCart(
            String customerEmail
    ) {

        String email =
                normalizeEmail(
                        customerEmail
                );


        return cartRepository
                .findByCustomerEmailIgnoreCaseOrderByCreatedAtAsc(
                        email
                );
    }



    /* =========================================================
       AJOUTER UN ARTICLE

       Si déjà présent :
       quantité +1
    ========================================================= */

    @Transactional
    public CustomerCartItem addItem(

            String customerEmail,

            String itemType,

            Long itemId
    ) {

        String email =
                normalizeEmail(
                        customerEmail
                );


        String type =
                normalizeItemType(
                        itemType
                );


        validateItemExists(
                type,
                itemId
        );


        CustomerCartItem existing =
                cartRepository
                        .findByCustomerEmailIgnoreCaseAndItemTypeAndItemId(
                                email,
                                type,
                                itemId
                        )
                        .orElse(null);



        /* =====================================================
           ARTICLE DEJA DANS LE PANIER
        ===================================================== */

        if (existing != null) {

            int currentQuantity =
                    existing.getQuantity() == null
                            ? 1
                            : existing.getQuantity();


            existing.setQuantity(
                    currentQuantity + 1
            );


            return cartRepository.save(
                    existing
            );
        }



        /* =====================================================
           NOUVEL ARTICLE
        ===================================================== */

        CustomerCartItem item =
                new CustomerCartItem();


        item.setCustomerEmail(
                email
        );


        item.setItemType(
                type
        );


        item.setItemId(
                itemId
        );


        item.setQuantity(
                1
        );


        return cartRepository.save(
                item
        );
    }



    /* =========================================================
       DEFINIR UNE QUANTITE PRECISE
    ========================================================= */

    @Transactional
    public CustomerCartItem setQuantity(

            String customerEmail,

            Long cartItemId,

            Integer quantity
    ) {

        String email =
                normalizeEmail(
                        customerEmail
                );


        if (cartItemId == null) {

            throw new IllegalArgumentException(
                    "Identifiant du panier invalide."
            );
        }


        if (
                quantity == null
                ||
                quantity < 1
        ) {

            throw new IllegalArgumentException(
                    "La quantité doit être supérieure ou égale à 1."
            );
        }


        if (quantity > 99) {

            throw new IllegalArgumentException(
                    "La quantité maximale est 99."
            );
        }


        CustomerCartItem item =
                findOwnedCartItem(
                        cartItemId,
                        email
                );


        item.setQuantity(
                quantity
        );


        return cartRepository.save(
                item
        );
    }



    /* =========================================================
       +1
    ========================================================= */

    @Transactional
    public CustomerCartItem increment(

            String customerEmail,

            Long cartItemId
    ) {

        String email =
                normalizeEmail(
                        customerEmail
                );


        CustomerCartItem item =
                findOwnedCartItem(
                        cartItemId,
                        email
                );


        int quantity =
                item.getQuantity() == null
                        ? 1
                        : item.getQuantity();


        if (quantity >= 99) {

            throw new IllegalArgumentException(
                    "La quantité maximale est 99."
            );
        }


        item.setQuantity(
                quantity + 1
        );


        return cartRepository.save(
                item
        );
    }



    /* =========================================================
       -1

       Si quantité = 1 :
       supprimer la ligne.
    ========================================================= */

    @Transactional
    public void decrement(

            String customerEmail,

            Long cartItemId
    ) {

        String email =
                normalizeEmail(
                        customerEmail
                );


        CustomerCartItem item =
                findOwnedCartItem(
                        cartItemId,
                        email
                );


        int quantity =
                item.getQuantity() == null
                        ? 1
                        : item.getQuantity();


        if (quantity <= 1) {

            cartRepository.delete(
                    item
            );

            return;
        }


        item.setQuantity(
                quantity - 1
        );


        cartRepository.save(
                item
        );
    }



    /* =========================================================
       SUPPRIMER UNE LIGNE
    ========================================================= */

    @Transactional
    public void removeItem(

            String customerEmail,

            Long cartItemId
    ) {

        String email =
                normalizeEmail(
                        customerEmail
                );


        CustomerCartItem item =
                findOwnedCartItem(
                        cartItemId,
                        email
                );


        cartRepository.delete(
                item
        );
    }



    /* =========================================================
       VIDER LE PANIER DU CLIENT CONNECTE
    ========================================================= */

    @Transactional
    public void clearCart(
            String customerEmail
    ) {

        String email =
                normalizeEmail(
                        customerEmail
                );


        cartRepository
                .deleteByCustomerEmailIgnoreCase(
                        email
                );
    }



    /* =========================================================
       NOMBRE TOTAL D'ARTICLES

       Exemple :
       Canapé x1
       Pied x3

       résultat = 4
    ========================================================= */

    @Transactional(readOnly = true)
    public int getTotalQuantity(
            String customerEmail
    ) {

        return getCart(
                customerEmail
        )
        .stream()
        .mapToInt(
                item -> {

                    if (
                            item.getQuantity() == null
                            ||
                            item.getQuantity() < 1
                    ) {

                        return 1;
                    }


                    return item.getQuantity();
                }
        )
        .sum();
    }



    /* =========================================================
       PANIER VIDE ?
    ========================================================= */

    @Transactional(readOnly = true)
    public boolean isEmpty(
            String customerEmail
    ) {

        return getCart(
                customerEmail
        )
        .isEmpty();
    }



    /* =========================================================
       TROUVER UNE LIGNE APPARTENANT AU CLIENT

       IMPORTANT :
       Gmail A ne peut jamais modifier
       la ligne du Gmail B.
    ========================================================= */

    private CustomerCartItem findOwnedCartItem(

            Long cartItemId,

            String customerEmail
    ) {

        if (cartItemId == null) {

            throw new IllegalArgumentException(
                    "Identifiant du panier invalide."
            );
        }


        return cartRepository
                .findByIdAndCustomerEmailIgnoreCase(
                        cartItemId,
                        customerEmail
                )
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Article du panier introuvable."
                                )
                );
    }



    /* =========================================================
       VERIFIER EXISTENCE ARTICLE
    ========================================================= */

    private void validateItemExists(

            String itemType,

            Long itemId
    ) {

        if (
                itemId == null
                ||
                itemId <= 0
        ) {

            throw new IllegalArgumentException(
                    "Identifiant de l'article invalide."
            );
        }



        /* =====================================================
           PRODUCT
        ===================================================== */

        if (
                CustomerCartItem.TYPE_PRODUCT.equals(
                        itemType
                )
        ) {

            if (
                    !productRepository.existsById(
                            itemId
                    )
            ) {

                throw new IllegalArgumentException(
                        "Produit introuvable."
                );
            }


            return;
        }



        /* =====================================================
           PIECE
        ===================================================== */

        if (
                CustomerCartItem.TYPE_PIECE.equals(
                        itemType
                )
        ) {

            if (
                    !pieceRepository.existsById(
                            itemId
                    )
            ) {

                throw new IllegalArgumentException(
                        "Pièce introuvable."
                );
            }


            return;
        }



        throw new IllegalArgumentException(
                "Type d'article invalide."
        );
    }



    /* =========================================================
       NORMALISER TYPE
    ========================================================= */

    private String normalizeItemType(
            String itemType
    ) {

        if (
                itemType == null
                ||
                itemType.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Le type d'article est obligatoire."
            );
        }


        String normalized =
                itemType
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );


        if (
                !CustomerCartItem.TYPE_PRODUCT.equals(
                        normalized
                )
                &&
                !CustomerCartItem.TYPE_PIECE.equals(
                        normalized
                )
        ) {

            throw new IllegalArgumentException(
                    "Type d'article invalide."
            );
        }


        return normalized;
    }



    /* =========================================================
       NORMALISER EMAIL
    ========================================================= */

    private String normalizeEmail(
            String customerEmail
    ) {

        if (
                customerEmail == null
                ||
                customerEmail.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Client non connecté."
            );
        }


        return customerEmail
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }

}