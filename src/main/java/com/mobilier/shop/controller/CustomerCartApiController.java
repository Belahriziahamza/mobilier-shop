package com.mobilier.shop.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobilier.shop.entity.CustomerCartItem;
import com.mobilier.shop.entity.Piece;
import com.mobilier.shop.entity.Product;
import com.mobilier.shop.repository.PieceRepository;
import com.mobilier.shop.repository.ProductRepository;
import com.mobilier.shop.service.CustomerCartService;

import jakarta.servlet.http.HttpSession;


@RestController
@RequestMapping("/api/cart")
public class CustomerCartApiController {


    /* =========================================================
       CONTEXTE SECURITY CLIENT

       Même clé utilisée par le système compte client.
    ========================================================= */

    private static final String CUSTOMER_SECURITY_CONTEXT_KEY =
            "ZINEB_CUSTOMER_SECURITY_CONTEXT";



    /* =========================================================
       SERVICES / REPOSITORIES
    ========================================================= */

    private final CustomerCartService cartService;

    private final ProductRepository productRepository;

    private final PieceRepository pieceRepository;



    /* =========================================================
       CONSTRUCTEUR
    ========================================================= */

    public CustomerCartApiController(

            CustomerCartService cartService,

            ProductRepository productRepository,

            PieceRepository pieceRepository
    ) {

        this.cartService =
                cartService;

        this.productRepository =
                productRepository;

        this.pieceRepository =
                pieceRepository;
    }



    /* =========================================================
       GET
       PANIER DU CLIENT CONNECTE
    ========================================================= */

    @GetMapping
    public ResponseEntity<?> getCart(
            HttpSession session
    ) {

        String customerEmail =
                getConnectedCustomerEmail(
                        session
                );


        if (customerEmail == null) {

            return unauthorized();
        }


        try {

            return ResponseEntity.ok(
                    buildCartResponse(
                            customerEmail
                    )
            );

        } catch (Exception e) {

            return serverError(
                    e
            );
        }
    }



    /* =========================================================
       POST
       AJOUTER PRODUCT OU PIECE

       JSON :
       {
           "itemType": "PRODUCT",
           "itemId": 5
       }

       OU

       {
           "itemType": "PIECE",
           "itemId": 2
       }
    ========================================================= */

    @PostMapping("/add")
    public ResponseEntity<?> addItem(

            @RequestBody
            AddCartItemRequest request,

            HttpSession session
    ) {

        String customerEmail =
                getConnectedCustomerEmail(
                        session
                );


        if (customerEmail == null) {

            return unauthorized();
        }


        try {

            if (request == null) {

                throw new IllegalArgumentException(
                        "Article invalide."
                );
            }


            cartService.addItem(
                    customerEmail,
                    request.getItemType(),
                    request.getItemId()
            );


            return ResponseEntity.ok(
                    buildCartResponse(
                            customerEmail
                    )
            );


        } catch (IllegalArgumentException e) {

            return badRequest(
                    e.getMessage()
            );


        } catch (Exception e) {

            return serverError(
                    e
            );
        }
    }



    /* =========================================================
       +1 QUANTITE
    ========================================================= */

    @PostMapping("/{cartItemId}/plus")
    public ResponseEntity<?> increment(

            @PathVariable("cartItemId")
            Long cartItemId,

            HttpSession session
    ) {

        String customerEmail =
                getConnectedCustomerEmail(
                        session
                );


        if (customerEmail == null) {

            return unauthorized();
        }


        try {

            cartService.increment(
                    customerEmail,
                    cartItemId
            );


            return ResponseEntity.ok(
                    buildCartResponse(
                            customerEmail
                    )
            );


        } catch (IllegalArgumentException e) {

            return badRequest(
                    e.getMessage()
            );


        } catch (Exception e) {

            return serverError(
                    e
            );
        }
    }



    /* =========================================================
       -1 QUANTITE

       Si quantité = 1 :
       la ligne est supprimée.
    ========================================================= */

    @PostMapping("/{cartItemId}/minus")
    public ResponseEntity<?> decrement(

            @PathVariable("cartItemId")
            Long cartItemId,

            HttpSession session
    ) {

        String customerEmail =
                getConnectedCustomerEmail(
                        session
                );


        if (customerEmail == null) {

            return unauthorized();
        }


        try {

            cartService.decrement(
                    customerEmail,
                    cartItemId
            );


            return ResponseEntity.ok(
                    buildCartResponse(
                            customerEmail
                    )
            );


        } catch (IllegalArgumentException e) {

            return badRequest(
                    e.getMessage()
            );


        } catch (Exception e) {

            return serverError(
                    e
            );
        }
    }



    /* =========================================================
       SUPPRIMER UNE LIGNE
    ========================================================= */

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<?> remove(

            @PathVariable("cartItemId")
            Long cartItemId,

            HttpSession session
    ) {

        String customerEmail =
                getConnectedCustomerEmail(
                        session
                );


        if (customerEmail == null) {

            return unauthorized();
        }


        try {

            cartService.removeItem(
                    customerEmail,
                    cartItemId
            );


            return ResponseEntity.ok(
                    buildCartResponse(
                            customerEmail
                    )
            );


        } catch (IllegalArgumentException e) {

            return badRequest(
                    e.getMessage()
            );


        } catch (Exception e) {

            return serverError(
                    e
            );
        }
    }



    /* =========================================================
       VIDER LE PANIER

       Supprime uniquement le panier
       du Gmail actuellement connecté.
    ========================================================= */

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(
            HttpSession session
    ) {

        String customerEmail =
                getConnectedCustomerEmail(
                        session
                );


        if (customerEmail == null) {

            return unauthorized();
        }


        try {

            cartService.clearCart(
                    customerEmail
            );


            return ResponseEntity.ok(
                    buildCartResponse(
                            customerEmail
                    )
            );


        } catch (Exception e) {

            return serverError(
                    e
            );
        }
    }



    /* =========================================================
       CONSTRUIRE REPONSE PANIER
    ========================================================= */

    private Map<String, Object> buildCartResponse(
            String customerEmail
    ) {

        List<CustomerCartItem> databaseItems =
                cartService.getCart(
                        customerEmail
                );


        List<Map<String, Object>> items =
                new ArrayList<>();


        BigDecimal totalAmount =
                BigDecimal.ZERO;


        int totalQuantity =
                0;



        for (
                CustomerCartItem cartItem
                :
                databaseItems
        ) {

            Map<String, Object> item =
                    buildItemResponse(
                            cartItem
                    );


            /*
             * Si le produit/pièce a été supprimé
             * entre-temps de MySQL,
             * on ne l'affiche pas.
             */

            if (item == null) {

                continue;
            }


            items.add(
                    item
            );


            Object quantityObject =
                    item.get(
                            "quantity"
                    );


            int quantity =
                    quantityObject instanceof Number
                            ? ((Number) quantityObject).intValue()
                            : 1;


            Object lineTotalObject =
                    item.get(
                            "lineTotal"
                    );


            BigDecimal lineTotal =
                    lineTotalObject instanceof BigDecimal
                            ? (BigDecimal) lineTotalObject
                            : BigDecimal.ZERO;


            totalQuantity +=
                    quantity;


            totalAmount =
                    totalAmount.add(
                            lineTotal
                    );
        }



        Map<String, Object> response =
                new LinkedHashMap<>();


        response.put(
                "authenticated",
                true
        );


        response.put(
                "customerEmail",
                customerEmail
        );


        response.put(
                "items",
                items
        );


        response.put(
                "totalQuantity",
                totalQuantity
        );


        response.put(
                "totalAmount",
                totalAmount
        );


        response.put(
                "empty",
                items.isEmpty()
        );


        return response;
    }



    /* =========================================================
       TRANSFORMER UNE LIGNE MYSQL

       CustomerCartItem
            ↓
       données nécessaires au front
    ========================================================= */

    private Map<String, Object> buildItemResponse(
            CustomerCartItem cartItem
    ) {

        if (
                cartItem == null
                ||
                cartItem.getItemId() == null
        ) {

            return null;
        }


        String itemType =
                cartItem.getItemType();


        int quantity =
                cartItem.getQuantity() == null
                        ||
                        cartItem.getQuantity() < 1

                        ? 1
                        : cartItem.getQuantity();



        /* =====================================================
           PRODUCT
        ===================================================== */

        if (
                CustomerCartItem.TYPE_PRODUCT.equals(
                        itemType
                )
        ) {

            Product product =
                    productRepository
                            .findById(
                                    cartItem.getItemId()
                            )
                            .orElse(null);


            if (product == null) {

                return null;
            }


            BigDecimal price =
                    product.getPrice() == null
                            ? BigDecimal.ZERO
                            : product.getPrice();


            BigDecimal lineTotal =
                    price.multiply(
                            BigDecimal.valueOf(
                                    quantity
                            )
                    );


            Map<String, Object> result =
                    new LinkedHashMap<>();


            result.put(
                    "cartItemId",
                    cartItem.getId()
            );


            result.put(
                    "itemType",
                    CustomerCartItem.TYPE_PRODUCT
            );


            result.put(
                    "itemId",
                    product.getId()
            );


            result.put(
                    "name",
                    product.getName()
            );


            result.put(
                    "type",
                    product.getType()
            );


            result.put(
                    "catalogue",
                    product.getCategory()
            );


            result.put(
                    "price",
                    price
            );


            result.put(
                    "imagePath",
                    product.getImagePath()
            );


            result.put(
                    "quantity",
                    quantity
            );


            result.put(
                    "lineTotal",
                    lineTotal
            );


            return result;
        }



        /* =====================================================
           PIECE
        ===================================================== */

        if (
                CustomerCartItem.TYPE_PIECE.equals(
                        itemType
                )
        ) {

            Piece piece =
                    pieceRepository
                            .findById(
                                    cartItem.getItemId()
                            )
                            .orElse(null);


            if (piece == null) {

                return null;
            }


            BigDecimal price =
                    piece.getPrice() == null
                            ? BigDecimal.ZERO
                            : piece.getPrice();


            BigDecimal lineTotal =
                    price.multiply(
                            BigDecimal.valueOf(
                                    quantity
                            )
                    );


            Map<String, Object> result =
                    new LinkedHashMap<>();


            result.put(
                    "cartItemId",
                    cartItem.getId()
            );


            result.put(
                    "itemType",
                    CustomerCartItem.TYPE_PIECE
            );


            result.put(
                    "itemId",
                    piece.getId()
            );


            result.put(
                    "name",
                    piece.getName()
            );


            result.put(
                    "type",
                    piece.getType()
            );


            result.put(
                    "catalogue",
                    piece.getCatalogue()
            );


            result.put(
                    "price",
                    price
            );


            result.put(
                    "imagePath",
                    piece.getImagePath()
            );


            result.put(
                    "quantity",
                    quantity
            );


            result.put(
                    "lineTotal",
                    lineTotal
            );


            return result;
        }


        return null;
    }



    /* =========================================================
       RECUPERER LE GMAIL DU CLIENT CONNECTE

       1. contexte client spécifique
       2. fallback SecurityContext normal
    ========================================================= */

    private String getConnectedCustomerEmail(
            HttpSession session
    ) {

        Authentication authentication =
                null;



        /* =====================================================
           CONTEXTE CLIENT EN SESSION
        ===================================================== */

        if (session != null) {

            Object sessionContext =
                    session.getAttribute(
                            CUSTOMER_SECURITY_CONTEXT_KEY
                    );


            if (
                    sessionContext instanceof SecurityContext
            ) {

                SecurityContext securityContext =
                        (SecurityContext) sessionContext;


                authentication =
                        securityContext.getAuthentication();
            }
        }



        /* =====================================================
           VERIFIER ROLE CUSTOMER
        ===================================================== */

        if (
                !isCustomerAuthentication(
                        authentication
                )
        ) {

            Authentication currentAuthentication =
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication();


            if (
                    isCustomerAuthentication(
                            currentAuthentication
                    )
            ) {

                authentication =
                        currentAuthentication;
            }
        }



        /* =====================================================
           NON CONNECTE
        ===================================================== */

        if (
                !isCustomerAuthentication(
                        authentication
                )
        ) {

            return null;
        }



        /* =====================================================
           EMAIL
        ===================================================== */

        String email =
                authentication.getName();


        if (
                email == null
                ||
                email.isBlank()
        ) {

            return null;
        }


        return email
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }



    /* =========================================================
       VERIFIER ROLE CUSTOMER
    ========================================================= */

    private boolean isCustomerAuthentication(
            Authentication authentication
    ) {

        if (
                authentication == null
                ||
                !authentication.isAuthenticated()
        ) {

            return false;
        }


        if (
                authentication.getPrincipal() == null
        ) {

            return false;
        }


        if (
                "anonymousUser".equals(
                        String.valueOf(
                                authentication.getPrincipal()
                        )
                )
        ) {

            return false;
        }


        for (
                GrantedAuthority authority
                :
                authentication.getAuthorities()
        ) {

            if (
                    "ROLE_CUSTOMER".equals(
                            authority.getAuthority()
                    )
            ) {

                return true;
            }
        }


        return false;
    }



    /* =========================================================
       401
    ========================================================= */

    private ResponseEntity<Map<String, Object>>
    unauthorized() {

        Map<String, Object> response =
                new LinkedHashMap<>();


        response.put(
                "authenticated",
                false
        );


        response.put(
                "message",
                "Vous devez vous connecter pour utiliser le panier."
        );


        response.put(
                "loginUrl",
                "/compte/login"
        );


        return ResponseEntity
                .status(
                        HttpStatus.UNAUTHORIZED
                )
                .body(
                        response
                );
    }



    /* =========================================================
       400
    ========================================================= */

    private ResponseEntity<Map<String, Object>>
    badRequest(
            String message
    ) {

        Map<String, Object> response =
                new LinkedHashMap<>();


        response.put(
                "authenticated",
                true
        );


        response.put(
                "message",
                message == null
                        ||
                        message.isBlank()

                        ? "Requête invalide."
                        : message
        );


        return ResponseEntity
                .badRequest()
                .body(
                        response
                );
    }



    /* =========================================================
       500
    ========================================================= */

    private ResponseEntity<Map<String, Object>>
    serverError(
            Exception exception
    ) {

        exception.printStackTrace();


        Map<String, Object> response =
                new LinkedHashMap<>();


        response.put(
                "message",
                "Une erreur est survenue lors de la gestion du panier."
        );


        return ResponseEntity
                .status(
                        HttpStatus.INTERNAL_SERVER_ERROR
                )
                .body(
                        response
                );
    }



    /* =========================================================
       DTO AJOUT
    ========================================================= */

    public static class AddCartItemRequest {


        private String itemType;

        private Long itemId;



        public AddCartItemRequest() {
        }



        public String getItemType() {

            return itemType;
        }


        public void setItemType(
                String itemType
        ) {

            this.itemType =
                    itemType;
        }



        public Long getItemId() {

            return itemId;
        }


        public void setItemId(
                Long itemId
        ) {

            this.itemId =
                    itemId;
        }

    }

}