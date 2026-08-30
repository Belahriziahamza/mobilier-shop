document.addEventListener("DOMContentLoaded", function () {

    "use strict";


    /* =========================================================
       ZINEB DECO
       MAIN.JS

       PANIER MYSQL PAR COMPTE CLIENT

       REGLES :

       - aucun localStorage pour le panier
       - client non connecté = panier 0
       - ajout panier = connexion obligatoire
       - /panier = connexion obligatoire
       - chaque email possède son panier MySQL
       - PRODUCT et PIECE utilisent le même panier
    ========================================================= */



    /* =========================================================
       01. OUTILS
    ========================================================= */


    function parsePrice(value) {

        if (
            value === null ||
            value === undefined ||
            value === ""
        ) {

            return 0;
        }


        const text =
            String(value)
                .trim()
                .replace(/\s/g, "")
                .replace(/[^\d,.-]/g, "")
                .replace(",", ".");


        return Number(text) || 0;
    }



    function formatPrice(value) {

        return new Intl.NumberFormat(
            "fr-FR",
            {
                minimumFractionDigits: 0,
                maximumFractionDigits: 2
            }
        ).format(
            Number(value || 0)
        ) + " DH";
    }



    function normalizeText(value) {

        return String(value || "")
            .toLowerCase()
            .normalize("NFD")
            .replace(
                /[\u0300-\u036f]/g,
                ""
            );
    }



    function normalizeImagePath(value) {

        const image =
            String(
                value || ""
            ).trim();


        if (
            !image ||
            image === "null" ||
            image === "undefined"
        ) {

            return "";
        }


        if (
            image.startsWith("/") ||
            image.startsWith("http://") ||
            image.startsWith("https://") ||
            image.startsWith("data:") ||
            image.startsWith("blob:")
        ) {

            return image;
        }


        return "/" + image;
    }



    function redirectToCustomerLogin() {

        window.location.href =
            "/compte/login";
    }



    function safeJson(text) {

        if (!text) {

            return {};
        }


        try {

            return JSON.parse(
                text
            );

        } catch (error) {

            return {

                message:
                    text
            };
        }
    }



    /* =========================================================
       02. CSRF

       Certaines pages publiques n'ont pas
       directement les meta CSRF.

       Dans ce cas :
       on récupère le token depuis /compte.
    ========================================================= */


    let csrfCache =
        null;



    function readCsrfFromDocument(root) {

        const context =
            root ||
            document;


        const token =

            context
                .querySelector(
                    'meta[name="_csrf"]'
                )
                ?.getAttribute(
                    "content"
                )

            ||

            context
                .querySelector(
                    'input[name="_csrf"]'
                )
                ?.getAttribute(
                    "value"
                )

            ||

            "";


        const header =

            context
                .querySelector(
                    'meta[name="_csrf_header"]'
                )
                ?.getAttribute(
                    "content"
                )

            ||

            "X-CSRF-TOKEN";


        if (!token) {

            return null;
        }


        return {

            token:
                token,

            header:
                header
        };
    }



    async function loadCsrf(
        forceRefresh
    ) {

        if (!forceRefresh) {


            const pageCsrf =
                readCsrfFromDocument(
                    document
                );


            if (pageCsrf) {

                csrfCache =
                    pageCsrf;


                return pageCsrf;
            }


            if (csrfCache) {

                return csrfCache;
            }

        }



        try {


            const response =
                await fetch(
                    "/compte",
                    {
                        method:
                            "GET",

                        credentials:
                            "same-origin",

                        cache:
                            "no-store",

                        headers: {

                            "Accept":
                                "text/html"
                        }
                    }
                );


            const html =
                await response.text();


            const parsed =
                new DOMParser()
                    .parseFromString(
                        html,
                        "text/html"
                    );


            const csrf =
                readCsrfFromDocument(
                    parsed
                );


            if (csrf) {

                csrfCache =
                    csrf;


                return csrf;
            }


        } catch (error) {


            console.error(
                "Impossible de récupérer le CSRF :",
                error
            );

        }


        return null;
    }



    async function requestJson(

        url,

        options,

        csrfRequired,

        allowRetry

    ) {


        const requestOptions =
            Object.assign(
                {
                    method:
                        "GET",

                    credentials:
                        "same-origin",

                    cache:
                        "no-store"
                },

                options || {}
            );



        const headers =
            Object.assign(
                {
                    "Accept":
                        "application/json"
                },

                requestOptions.headers ||
                {}
            );



        /* =====================================================
           CSRF
        ===================================================== */

        if (csrfRequired) {


            const csrf =
                await loadCsrf(
                    false
                );


            if (!csrf) {

                throw new Error(
                    "Jeton de sécurité absent. Rechargez la page puis réessayez."
                );
            }


            headers[
                csrf.header
            ] =
                csrf.token;

        }



        requestOptions.headers =
            headers;



        /* =====================================================
           REQUETE
        ===================================================== */

        let response =
            await fetch(
                url,
                requestOptions
            );


        let text =
            await response.text();


        let data =
            safeJson(
                text
            );



        /* =====================================================
           SI CSRF EXPIRE : NOUVEL ESSAI
        ===================================================== */

        if (
            response.status === 403 &&
            csrfRequired &&
            allowRetry !== false
        ) {


            csrfCache =
                null;


            const refreshedCsrf =
                await loadCsrf(
                    true
                );


            if (refreshedCsrf) {


                headers[
                    refreshedCsrf.header
                ] =
                    refreshedCsrf.token;


                response =
                    await fetch(
                        url,
                        requestOptions
                    );


                text =
                    await response.text();


                data =
                    safeJson(
                        text
                    );

            }

        }



        return {

            response:
                response,

            data:
                data
        };
    }



    /* =========================================================
       03. MENU MOBILE ANCIEN HEADER

       Le nouveau header premium
       gère également son propre menu.
    ========================================================= */


    const mobileMenuButton =
        document.getElementById(
            "mobileMenuButton"
        );


    const mainNavigation =
        document.getElementById(
            "mainNavigation"
        );



    function closeMenu() {


        if (mainNavigation) {

            mainNavigation
                .classList
                .remove(
                    "open"
                );
        }


        document.body
            .classList
            .remove(
                "menu-open"
            );


        if (mobileMenuButton) {


            mobileMenuButton.textContent =
                "☰";


            mobileMenuButton
                .setAttribute(
                    "aria-expanded",
                    "false"
                );

        }

    }



    if (
        mobileMenuButton &&
        mainNavigation
    ) {


        mobileMenuButton
            .addEventListener(
                "click",
                function () {


                    const open =
                        mainNavigation
                            .classList
                            .toggle(
                                "open"
                            );


                    document.body
                        .classList
                        .toggle(
                            "menu-open",
                            open
                        );


                    mobileMenuButton.textContent =
                        open
                            ? "✕"
                            : "☰";


                    mobileMenuButton
                        .setAttribute(
                            "aria-expanded",
                            open
                                ? "true"
                                : "false"
                        );

                }
            );



        mainNavigation
            .querySelectorAll(
                "a"
            )
            .forEach(
                function (link) {


                    link.addEventListener(
                        "click",
                        closeMenu
                    );

                }
            );



        window.addEventListener(
            "resize",
            function () {


                if (
                    window.innerWidth >
                    800
                ) {

                    closeMenu();
                }

            }
        );

    }



    /* =========================================================
       04. SUPPRIMER ANCIENS PANIERS LOCALSTORAGE

       MYSQL DEVIENT L'UNIQUE SOURCE.
    ========================================================= */


    try {


        localStorage.removeItem(
            "zinebDecoCart"
        );


        localStorage.removeItem(
            "zineb-deco-piece-cart"
        );


    } catch (error) {


        console.warn(
            "LocalStorage indisponible :",
            error
        );

    }



    /* =========================================================
       05. ETAT DU PANIER MYSQL
    ========================================================= */


    let cartState = {

        authenticated:
            false,

        customerEmail:
            null,

        items:
            [],

        totalQuantity:
            0,

        totalAmount:
            0,

        empty:
            true

    };


    let cartAuthStatus =
        "unknown";



    function emptyCartState() {


        cartState = {

            authenticated:
                false,

            customerEmail:
                null,

            items:
                [],

            totalQuantity:
                0,

            totalAmount:
                0,

            empty:
                true

        };

    }



    function calculateStateTotal() {


        return cartState.items
            .reduce(
                function (
                    total,
                    item
                ) {


                    return (
                        total
                        +
                        Number(
                            item.price ||
                            0
                        )
                        *
                        Number(
                            item.quantity ||
                            1
                        )
                    );

                },
                0
            );

    }



    function applyCartResponse(
        data
    ) {


        /* =====================================================
           NON CONNECTE
        ===================================================== */

        if (
            !data ||
            data.authenticated !== true
        ) {


            emptyCartState();


            cartAuthStatus =
                "guest";


            updateCartCounter();

            renderCartPage();

            renderPieceDrawer();


            return;
        }



        /* =====================================================
           CLIENT CONNECTE
        ===================================================== */

        cartState = {

            authenticated:
                true,

            customerEmail:
                data.customerEmail ||
                null,

            items:
                Array.isArray(
                    data.items
                )
                    ? data.items
                    : [],

            totalQuantity:
                Number(
                    data.totalQuantity ||
                    0
                ),

            totalAmount:
                Number(
                    data.totalAmount !==
                    undefined

                        ? data.totalAmount
                        : 0
                ),

            empty:
                Boolean(
                    data.empty
                )
        };



        if (
            !Number.isFinite(
                cartState.totalAmount
            )
        ) {

            cartState.totalAmount =
                calculateStateTotal();
        }



        cartAuthStatus =
            "authenticated";


        updateCartCounter();

        renderCartPage();

        renderPieceDrawer();

    }



    /* =========================================================
       COMPTEUR HEADER
    ========================================================= */


    function updateCartCounter() {


        const total =

            cartState.authenticated

                ? Number(
                    cartState.totalQuantity ||
                    0
                )

                : 0;



        document
            .querySelectorAll(
                ".cart-count"
            )
            .forEach(
                function (element) {


                    element.textContent =
                        total;

                }
            );

    }



    /* =========================================================
       CHARGER PANIER MYSQL
    ========================================================= */


    async function loadCart(
        redirectIfGuest
    ) {


        try {


            const result =
                await requestJson(
                    "/api/cart",
                    {
                        method:
                            "GET"
                    },
                    false
                );



            /* =================================================
               NON CONNECTE
            ================================================= */

            if (
                result.response.status ===
                401
            ) {


                emptyCartState();


                cartAuthStatus =
                    "guest";


                updateCartCounter();

                renderCartPage();

                renderPieceDrawer();


                if (redirectIfGuest) {

                    redirectToCustomerLogin();
                }


                return false;
            }



            /* =================================================
               ERREUR
            ================================================= */

            if (
                !result.response.ok
            ) {


                cartAuthStatus =
                    "error";


                throw new Error(

                    result.data.message
                    ||
                    "Impossible de charger le panier."

                );

            }



            /* =================================================
               OK
            ================================================= */

            applyCartResponse(
                result.data
            );


            return true;



        } catch (error) {


            cartAuthStatus =
                "error";


            console.error(
                "Chargement panier MySQL :",
                error
            );


            updateCartCounter();


            return false;

        }

    }



    /* =========================================================
       INITIALISATION PANIER

       Sur /panier :
       non connecté => page login.
    ========================================================= */


    const initialCartPromise =
        loadCart(
            window.location.pathname ===
            "/panier"
        );



    /* =========================================================
       VERIFIER CLIENT
    ========================================================= */


    async function requireCustomer() {


        await initialCartPromise;



        if (
            cartAuthStatus === "unknown" ||
            cartAuthStatus === "error"
        ) {


            await loadCart(
                false
            );

        }



        if (
            cartAuthStatus ===
            "authenticated"
        ) {

            return true;
        }



        if (
            cartAuthStatus ===
            "guest"
        ) {


            redirectToCustomerLogin();


            return false;
        }



        alert(
            "Impossible de vérifier votre compte. Rechargez la page puis réessayez."
        );


        return false;

    }



    /* =========================================================
       MODIFIER PANIER MYSQL
    ========================================================= */


    async function cartMutation(

        url,

        method,

        body

    ) {


        const connected =
            await requireCustomer();


        if (!connected) {

            return null;
        }



        const options = {

            method:
                method,

            headers:
                {}
        };



        if (
            body !== undefined &&
            body !== null
        ) {


            options.headers[
                "Content-Type"
            ] =
                "application/json";


            options.body =
                JSON.stringify(
                    body
                );

        }



        const result =
            await requestJson(

                url,

                options,

                true

            );



        /* =====================================================
           SESSION EXPIREE
        ===================================================== */

        if (
            result.response.status ===
            401
        ) {


            emptyCartState();


            cartAuthStatus =
                "guest";


            updateCartCounter();

            renderCartPage();

            renderPieceDrawer();


            redirectToCustomerLogin();


            return null;
        }



        /* =====================================================
           ERREUR
        ===================================================== */

        if (
            !result.response.ok
        ) {


            if (
                result.response.status ===
                403
            ) {


                throw new Error(
                    "Accès refusé par la sécurité. Rechargez la page puis réessayez."
                );

            }



            throw new Error(

                result.data.message
                ||
                result.data.error
                ||
                "Impossible de modifier le panier."

            );

        }



        /* =====================================================
           NOUVEAU PANIER
        ===================================================== */

        applyCartResponse(
            result.data
        );


        return result.data;

    }



    /* =========================================================
       06. EXTRAIRE PRODUIT
    ========================================================= */


    function extractProductFromButton(
        button
    ) {


        const card =
            button.closest(
                ".product-card, " +
                ".catalog-product-card"
            );



        let id =

            button.dataset.id

            ||

            button.dataset.productId

            ||

            card?.dataset.id

            ||

            card?.dataset.productId

            ||

            "";



        /* =====================================================
           FALLBACK /produit/12
        ===================================================== */

        if (
            !id &&
            card
        ) {


            const detailLink =
                card.querySelector(
                    'a[href*="/produit/"]'
                );


            const href =
                detailLink
                    ?.getAttribute(
                        "href"
                    )
                ||
                "";


            const match =
                href.match(
                    /\/produit\/(\d+)/
                );


            if (match) {

                id =
                    match[1];
            }

        }



        return {


            id:
                id,


            name:

                button.dataset.name

                ||

                card?.dataset.name

                ||

                card
                    ?.querySelector(
                        ".product-card-title, h3, h2"
                    )
                    ?.textContent
                    ?.trim()

                ||

                "Produit",



            price:

                parsePrice(

                    button.dataset.price

                    ||

                    card?.dataset.price

                    ||

                    card
                        ?.querySelector(
                            ".product-price, " +
                            ".product-price-row strong, " +
                            ".catalog-product-bottom strong"
                        )
                        ?.textContent

                    ||

                    ""

                ),



            image:

                button.dataset.image

                ||

                card?.dataset.image

                ||

                card
                    ?.querySelector(
                        "img"
                    )
                    ?.getAttribute(
                        "src"
                    )

                ||

                ""

        };

    }



    /* =========================================================
       ETAT BOUTON AJOUT
    ========================================================= */


    function restoreAddButton(

        button,

        originalText

    ) {


        button.disabled =
            false;


        button.classList
            .remove(
                "added"
            );


        button.textContent =
            originalText;

    }



    function showAddSuccess(

        button,

        originalText

    ) {


        button.textContent =
            "Ajouté ✓";


        button.classList
            .add(
                "added"
            );



        window.setTimeout(
            function () {


                restoreAddButton(
                    button,
                    originalText
                );


            },
            900
        );

    }



    /* =========================================================
       AJOUT ARTICLE MYSQL
    ========================================================= */


    async function addItemToMysqlCart(

        button,

        itemType,

        itemId

    ) {


        const id =
            Number(
                itemId
            );



        if (
            !Number.isInteger(
                id
            )
            ||
            id <= 0
        ) {


            alert(
                "Identifiant MySQL invalide."
            );


            return;
        }



        const originalText =

            button.dataset
                .cartOriginalText

            ||

            button.textContent
                .trim()

            ||

            "Ajouter au panier";



        button.dataset
            .cartOriginalText =
            originalText;



        button.disabled =
            true;


        button.textContent =
            "Ajout...";



        try {


            const data =
                await cartMutation(

                    "/api/cart/add",

                    "POST",

                    {

                        itemType:
                            itemType,

                        itemId:
                            id
                    }

                );



            if (!data) {

                return;
            }



            showAddSuccess(
                button,
                originalText
            );



        } catch (error) {


            console.error(
                "Ajout panier :",
                error
            );


            restoreAddButton(
                button,
                originalText
            );


            alert(

                error.message
                ||
                "Impossible d'ajouter cet article au panier."

            );

        }

    }



    /* =========================================================
       IMPORTANT

       CAPTURE = TRUE

       pieces.html contient encore son ancien JS localStorage.

       Nous arrêtons le clic AVANT qu'il arrive
       à l'ancien listener.

       Ainsi :
       uniquement MySQL reçoit l'ajout.
    ========================================================= */


    document.addEventListener(

        "click",

        function (event) {


            /* =================================================
               PIECE
            ================================================= */

            const pieceButton =
                event.target.closest(
                    ".piece-add-cart"
                );


            if (pieceButton) {


                event.preventDefault();

                event.stopPropagation();

                event.stopImmediatePropagation();



                if (
                    pieceButton.disabled
                ) {

                    return;
                }



                addItemToMysqlCart(

                    pieceButton,

                    "PIECE",

                    pieceButton.dataset
                        .pieceId

                );


                return;
            }



            /* =================================================
               DETAIL PRODUIT
            ================================================= */

            const detailButton =
                event.target.closest(
                    ".add-detail-cart"
                );


            if (detailButton) {


                event.preventDefault();

                event.stopPropagation();

                event.stopImmediatePropagation();



                if (
                    detailButton.disabled
                ) {

                    return;
                }



                addItemToMysqlCart(

                    detailButton,

                    "PRODUCT",

                    detailButton.dataset.id

                    ||

                    detailButton.dataset
                        .productId

                );


                return;
            }



            /* =================================================
               PRODUIT CATALOGUE
            ================================================= */

            const productButton =
                event.target.closest(
                    ".add-cart-button"
                );


            if (productButton) {


                event.preventDefault();

                event.stopPropagation();

                event.stopImmediatePropagation();



                if (
                    productButton.disabled
                ) {

                    return;
                }



                const product =
                    extractProductFromButton(
                        productButton
                    );



                if (!product.id) {


                    alert(
                        "Impossible d'ajouter ce produit : identifiant MySQL absent."
                    );


                    return;
                }



                addItemToMysqlCart(

                    productButton,

                    "PRODUCT",

                    product.id

                );


                return;
            }



            /* =================================================
               ANCIEN DRAWER PIECES
               MOINS
            ================================================= */

            const pieceMinus =
                event.target.closest(
                    "#pieceCartItems [data-cart-minus]"
                );


            if (pieceMinus) {


                event.preventDefault();

                event.stopPropagation();

                event.stopImmediatePropagation();



                changeCartQuantity(

                    Number(
                        pieceMinus.dataset
                            .cartMinus
                    ),

                    -1

                );


                return;
            }



            /* =================================================
               ANCIEN DRAWER PIECES
               PLUS
            ================================================= */

            const piecePlus =
                event.target.closest(
                    "#pieceCartItems [data-cart-plus]"
                );


            if (piecePlus) {


                event.preventDefault();

                event.stopPropagation();

                event.stopImmediatePropagation();



                changeCartQuantity(

                    Number(
                        piecePlus.dataset
                            .cartPlus
                    ),

                    1

                );


                return;
            }



            /* =================================================
               ANCIEN DRAWER PIECES
               SUPPRIMER
            ================================================= */

            const pieceRemove =
                event.target.closest(
                    "#pieceCartItems [data-cart-remove]"
                );


            if (pieceRemove) {


                event.preventDefault();

                event.stopPropagation();

                event.stopImmediatePropagation();



                removeCartItem(

                    Number(
                        pieceRemove.dataset
                            .cartRemove
                    )

                );

            }


        },

        true

    );



    /* =========================================================
       07. FAVORIS MYSQL
    ========================================================= */


    const mysqlFavoriteButtons =
        document.querySelectorAll(
            ".mysql-favorite-button"
        );



    function updateFavoriteButton(

        button,

        favorite

    ) {


        button.classList.toggle(
            "is-favorite",
            favorite
        );


        button.textContent =
            favorite
                ? "♥"
                : "♡";


        button.setAttribute(

            "aria-label",

            favorite
                ? "Retirer des favoris"
                : "Ajouter aux favoris"

        );


        button.setAttribute(

            "title",

            favorite
                ? "Retirer des favoris"
                : "Ajouter aux favoris"

        );

    }



    async function loadFavorites() {


        if (
            mysqlFavoriteButtons.length ===
            0
        ) {

            return;
        }



        try {


            const result =
                await requestJson(

                    "/api/favorites",

                    {
                        method:
                            "GET"
                    },

                    false

                );



            if (
                result.response.status === 401
                ||
                !result.response.ok
                ||
                !result.data
                ||
                !result.data.authenticated
            ) {

                return;
            }



            const ids =

                Array.isArray(
                    result.data.productIds
                )

                    ? result.data
                        .productIds
                        .map(
                            Number
                        )

                    : [];



            mysqlFavoriteButtons
                .forEach(
                    function (button) {


                        updateFavoriteButton(

                            button,

                            ids.includes(

                                Number(
                                    button.dataset
                                        .productId
                                )

                            )

                        );

                    }
                );



        } catch (error) {


            console.error(
                "Chargement favoris :",
                error
            );

        }

    }



    mysqlFavoriteButtons
        .forEach(
            function (button) {


                button.addEventListener(

                    "click",

                    async function (event) {


                        event.preventDefault();

                        event.stopPropagation();



                        const productId =
                            Number(
                                button.dataset
                                    .productId
                            );



                        if (
                            !Number.isInteger(
                                productId
                            )
                            ||
                            productId <= 0
                        ) {

                            return;
                        }



                        const connected =
                            await requireCustomer();



                        if (!connected) {

                            return;
                        }



                        button.disabled =
                            true;



                        try {


                            const result =
                                await requestJson(

                                    "/api/favorites/"
                                    +
                                    productId
                                    +
                                    "/toggle",

                                    {
                                        method:
                                            "POST"
                                    },

                                    true

                                );



                            if (
                                result.response.status ===
                                401
                            ) {


                                redirectToCustomerLogin();


                                return;
                            }



                            if (
                                !result.response.ok
                            ) {


                                throw new Error(

                                    result.data.message

                                    ||

                                    "Impossible de modifier les favoris."

                                );

                            }



                            updateFavoriteButton(

                                button,

                                Boolean(
                                    result.data.favorite
                                )

                            );



                        } catch (error) {


                            console.error(
                                "Favoris :",
                                error
                            );


                            alert(

                                error.message

                                ||

                                "Impossible de modifier les favoris."

                            );



                        } finally {


                            button.disabled =
                                false;

                        }

                    }

                );

            }
        );



    loadFavorites();



    /* =========================================================
       08. ELEMENTS PAGE PANIER
    ========================================================= */


    const cartPageItems =
        document.getElementById(
            "cartPageItems"
        );


    const cartPageEmpty =
        document.getElementById(
            "cartPageEmpty"
        );


    const cartPageLayout =
        document.getElementById(
            "cartPageLayout"
        );


    const cartSubtotal =
        document.getElementById(
            "cartSubtotal"
        );


    const cartTotal =
        document.getElementById(
            "cartTotal"
        );


    const clearCartButton =
        document.getElementById(
            "clearCartButton"
        );


    const cartOrderButton =
        document.getElementById(
            "cartOrderButton"
        );



    /* =========================================================
       LABEL ARTICLE
    ========================================================= */


    function itemLabel(item) {


        if (
            item.itemType !==
            "PIECE"
        ) {

            return "ZINEB DÉCO";
        }



        switch (
            item.catalogue
        ) {


            case "TAPISSERIE":

                return "PIÈCE TAPISSIER";


            case "COUTURE":

                return "PIÈCE COUTURE";


            case "MATIERES_TISSUS":

                return "MATIÈRE / TISSU";


            default:

                return "PIÈCE";
        }

    }



    /* =========================================================
       TOTAL PANIER
    ========================================================= */


    function calculateCartTotal() {


        if (
            Number.isFinite(
                Number(
                    cartState.totalAmount
                )
            )
            &&
            Number(
                cartState.totalAmount
            ) >= 0
        ) {


            return Number(
                cartState.totalAmount
            );

        }



        return calculateStateTotal();

    }



    /* =========================================================
       QUANTITE + / -
    ========================================================= */


    async function changeCartQuantity(

        cartItemId,

        change

    ) {


        const id =
            Number(
                cartItemId
            );



        if (
            !Number.isInteger(
                id
            )
            ||
            id <= 0
        ) {

            return;
        }



        const endpoint =

            change > 0

                ? "/api/cart/"
                    +
                    id
                    +
                    "/plus"

                : "/api/cart/"
                    +
                    id
                    +
                    "/minus";



        try {


            await cartMutation(

                endpoint,

                "POST"

            );



        } catch (error) {


            console.error(
                "Quantité panier :",
                error
            );


            alert(

                error.message

                ||

                "Impossible de modifier la quantité."

            );

        }

    }



    /* =========================================================
       SUPPRIMER LIGNE
    ========================================================= */


    async function removeCartItem(
        cartItemId
    ) {


        const id =
            Number(
                cartItemId
            );



        if (
            !Number.isInteger(
                id
            )
            ||
            id <= 0
        ) {

            return;
        }



        try {


            await cartMutation(

                "/api/cart/"
                +
                id,

                "DELETE"

            );



        } catch (error) {


            console.error(
                "Suppression panier :",
                error
            );


            alert(

                error.message

                ||

                "Impossible de supprimer cet article."

            );

        }

    }



    /* =========================================================
       CREER ARTICLE PANIER
    ========================================================= */


    function createCartProduct(
        item
    ) {


        const article =
            document.createElement(
                "article"
            );


        article.className =
            "cart-product";



        /* =====================================================
           IMAGE
        ===================================================== */

        const imageWrapper =
            document.createElement(
                "div"
            );


        imageWrapper.className =
            "cart-product-image";



        function addPlaceholder() {


            imageWrapper.innerHTML =
                "";


            const placeholder =
                document.createElement(
                    "div"
                );


            placeholder.className =
                "cart-image-placeholder";


            placeholder.textContent =
                "ZD";


            imageWrapper.appendChild(
                placeholder
            );

        }



        const imagePath =
            normalizeImagePath(

                item.imagePath

                ||

                item.image

            );



        if (imagePath) {


            const image =
                document.createElement(
                    "img"
                );


            image.src =
                imagePath;


            image.alt =
                item.name ||
                "Article";


            image.loading =
                "lazy";


            image.addEventListener(

                "error",

                addPlaceholder,

                {
                    once:
                        true
                }

            );


            imageWrapper.appendChild(
                image
            );



        } else {


            addPlaceholder();

        }



        /* =====================================================
           INFORMATIONS
        ===================================================== */

        const content =
            document.createElement(
                "div"
            );


        content.className =
            "cart-product-info";



        const top =
            document.createElement(
                "div"
            );


        top.className =
            "cart-product-top";



        const info =
            document.createElement(
                "div"
            );



        const label =
            document.createElement(
                "span"
            );


        label.className =
            "cart-product-label";


        label.textContent =
            itemLabel(
                item
            );



        const title =
            document.createElement(
                "h3"
            );


        title.textContent =
            item.name ||
            "Article";



        info.appendChild(
            label
        );


        info.appendChild(
            title
        );



        /* =====================================================
           TYPE PIECE / PRODUIT
        ===================================================== */

        if (
            item.type
        ) {


            const type =
                document.createElement(
                    "small"
                );


            type.className =
                "cart-product-type";


            type.textContent =
                item.type;


            info.appendChild(
                type
            );

        }



        /* =====================================================
           SUPPRIMER
        ===================================================== */

        const removeButton =
            document.createElement(
                "button"
            );


        removeButton.type =
            "button";


        removeButton.className =
            "cart-remove";


        removeButton.textContent =
            "Supprimer";



        removeButton.addEventListener(

            "click",

            function () {


                removeCartItem(
                    item.cartItemId
                );

            }

        );



        top.appendChild(
            info
        );


        top.appendChild(
            removeButton
        );



        /* =====================================================
           BAS
        ===================================================== */

        const bottom =
            document.createElement(
                "div"
            );


        bottom.className =
            "cart-product-bottom";



        /* =====================================================
           QUANTITE
        ===================================================== */

        const quantity =
            document.createElement(
                "div"
            );


        quantity.className =
            "cart-quantity";



        const minus =
            document.createElement(
                "button"
            );


        minus.type =
            "button";


        minus.textContent =
            "−";



        minus.addEventListener(

            "click",

            function () {


                changeCartQuantity(

                    item.cartItemId,

                    -1

                );

            }

        );



        const qty =
            document.createElement(
                "span"
            );


        qty.textContent =
            Number(
                item.quantity ||
                1
            );



        const plus =
            document.createElement(
                "button"
            );


        plus.type =
            "button";


        plus.textContent =
            "+";



        plus.addEventListener(

            "click",

            function () {


                changeCartQuantity(

                    item.cartItemId,

                    1

                );

            }

        );



        quantity.appendChild(
            minus
        );


        quantity.appendChild(
            qty
        );


        quantity.appendChild(
            plus
        );



        /* =====================================================
           PRIX
        ===================================================== */

        const prices =
            document.createElement(
                "div"
            );


        prices.className =
            "cart-product-prices";



        const unitPrice =
            document.createElement(
                "small"
            );


        unitPrice.textContent =
            formatPrice(
                item.price
            )
            +
            " / unité";



        const lineTotal =
            document.createElement(
                "strong"
            );


        lineTotal.textContent =
            formatPrice(

                item.lineTotal !==
                undefined

                    ? item.lineTotal

                    : Number(
                        item.price ||
                        0
                    )
                    *
                    Number(
                        item.quantity ||
                        1
                    )

            );



        prices.appendChild(
            unitPrice
        );


        prices.appendChild(
            lineTotal
        );



        bottom.appendChild(
            quantity
        );


        bottom.appendChild(
            prices
        );



        content.appendChild(
            top
        );


        content.appendChild(
            bottom
        );



        article.appendChild(
            imageWrapper
        );


        article.appendChild(
            content
        );


        return article;

    }



    /* =========================================================
       AFFICHER PAGE PANIER
    ========================================================= */


    function renderCartPage() {


        /*
         * Pas sur /panier.
         */

        if (
            !cartPageItems ||
            !cartPageEmpty ||
            !cartPageLayout
        ) {

            return;
        }



        cartPageItems.innerHTML =
            "";



        /* =====================================================
           NON CONNECTE
        ===================================================== */

        if (
            !cartState.authenticated
        ) {


            cartPageEmpty.style.display =
                "none";


            cartPageLayout.style.display =
                "none";


            return;
        }



        /* =====================================================
           PANIER VIDE
        ===================================================== */

        if (
            cartState.items.length ===
            0
        ) {


            cartPageEmpty.style.display =
                "flex";


            cartPageLayout.style.display =
                "none";


            if (cartSubtotal) {

                cartSubtotal.textContent =
                    "0 DH";
            }


            if (cartTotal) {

                cartTotal.textContent =
                    "0 DH";
            }


            return;
        }



        /* =====================================================
           PANIER AVEC ARTICLES
        ===================================================== */

        cartPageEmpty.style.display =
            "none";


        cartPageLayout.style.display =
            "grid";



        cartState.items
            .forEach(
                function (item) {


                    cartPageItems.appendChild(

                        createCartProduct(
                            item
                        )

                    );

                }
            );



        const total =
            calculateCartTotal();



        if (cartSubtotal) {


            cartSubtotal.textContent =
                formatPrice(
                    total
                );

        }



        if (cartTotal) {


            cartTotal.textContent =
                formatPrice(
                    total
                );

        }

    }



    /* =========================================================
       VIDER PANIER
    ========================================================= */


    if (clearCartButton) {


        clearCartButton.addEventListener(

            "click",

            async function () {


                const confirmed =
                    window.confirm(
                        "Voulez-vous vider votre panier ?"
                    );


                if (!confirmed) {

                    return;
                }



                clearCartButton.disabled =
                    true;



                try {


                    await cartMutation(

                        "/api/cart/clear",

                        "DELETE"

                    );



                } catch (error) {


                    console.error(
                        "Vider panier :",
                        error
                    );


                    alert(

                        error.message

                        ||

                        "Impossible de vider le panier."

                    );



                } finally {


                    clearCartButton.disabled =
                        false;

                }

            }

        );

    }



    /* =========================================================
       09. PANIER FLOTTANT PIECES

       IL UTILISE MAINTENANT MYSQL.
    ========================================================= */


    const pieceCartDrawer =
        document.getElementById(
            "pieceCartDrawer"
        );


    const pieceCartOverlay =
        document.getElementById(
            "pieceCartOverlay"
        );


    const pieceCartOpen =
        document.getElementById(
            "pieceCartOpen"
        );


    const pieceCartClose =
        document.getElementById(
            "pieceCartClose"
        );


    const pieceCartItems =
        document.getElementById(
            "pieceCartItems"
        );


    const pieceCartCount =
        document.getElementById(
            "pieceCartCount"
        );


    const pieceCartTotal =
        document.getElementById(
            "pieceCartTotal"
        );


    const pieceCartFloatingTotal =
        document.getElementById(
            "pieceCartFloatingTotal"
        );



    /* =========================================================
       OUVRIR
    ========================================================= */


    function openPieceDrawer() {


        if (
            !cartState.authenticated
        ) {


            redirectToCustomerLogin();


            return;
        }



        pieceCartDrawer
            ?.classList
            .add(
                "open"
            );


        pieceCartOverlay
            ?.classList
            .add(
                "open"
            );


        document.body.style.overflow =
            "hidden";

    }



    /* =========================================================
       FERMER
    ========================================================= */


    function closePieceDrawer() {


        pieceCartDrawer
            ?.classList
            .remove(
                "open"
            );


        pieceCartOverlay
            ?.classList
            .remove(
                "open"
            );


        document.body.style.overflow =
            "";

    }



    /* =========================================================
       CREER ARTICLE DRAWER
    ========================================================= */


    function createPieceDrawerItem(
        item
    ) {


        const article =
            document.createElement(
                "article"
            );


        article.className =
            "piece-cart-item";



        /* =====================================================
           IMAGE
        ===================================================== */

        const thumb =
            document.createElement(
                "div"
            );


        thumb.className =
            "piece-cart-thumb";



        const imagePath =
            normalizeImagePath(

                item.imagePath

                ||

                item.image

            );



        function addPiecePlaceholder() {


            thumb.innerHTML =
                "";


            const placeholder =
                document.createElement(
                    "div"
                );


            placeholder.textContent =
                "ZD";


            placeholder.style.width =
                "100%";


            placeholder.style.height =
                "100%";


            placeholder.style.display =
                "grid";


            placeholder.style.placeItems =
                "center";


            placeholder.style.color =
                "#d99a2b";


            placeholder.style.fontFamily =
                "Georgia, serif";


            thumb.appendChild(
                placeholder
            );

        }



        if (imagePath) {


            const image =
                document.createElement(
                    "img"
                );


            image.src =
                imagePath;


            image.alt =
                item.name ||
                "Pièce";


            image.addEventListener(

                "error",

                addPiecePlaceholder,

                {
                    once:
                        true
                }

            );


            thumb.appendChild(
                image
            );



        } else {


            addPiecePlaceholder();

        }



        /* =====================================================
           INFOS
        ===================================================== */

        const content =
            document.createElement(
                "div"
            );



        const title =
            document.createElement(
                "h4"
            );


        title.textContent =
            item.name ||
            "Pièce";



        const type =
            document.createElement(
                "span"
            );


        type.className =
            "piece-cart-item-type";


        type.textContent =
            item.type ||
            "Pièce";



        const price =
            document.createElement(
                "span"
            );


        price.className =
            "piece-cart-item-price";


        price.textContent =
            formatPrice(

                item.lineTotal !==
                undefined

                    ? item.lineTotal

                    : Number(
                        item.price ||
                        0
                    )
                    *
                    Number(
                        item.quantity ||
                        1
                    )

            );



        /* =====================================================
           CONTROLES
        ===================================================== */

        const controls =
            document.createElement(
                "div"
            );


        controls.className =
            "piece-cart-controls";



        const minus =
            document.createElement(
                "button"
            );


        minus.type =
            "button";


        minus.dataset.cartMinus =
            item.cartItemId;


        minus.textContent =
            "−";



        const qty =
            document.createElement(
                "span"
            );


        qty.textContent =
            Number(
                item.quantity ||
                1
            );



        const plus =
            document.createElement(
                "button"
            );


        plus.type =
            "button";


        plus.dataset.cartPlus =
            item.cartItemId;


        plus.textContent =
            "+";



        const remove =
            document.createElement(
                "button"
            );


        remove.type =
            "button";


        remove.className =
            "piece-cart-remove";


        remove.dataset.cartRemove =
            item.cartItemId;


        remove.textContent =
            "Supprimer";



        controls.appendChild(
            minus
        );


        controls.appendChild(
            qty
        );


        controls.appendChild(
            plus
        );


        controls.appendChild(
            remove
        );



        content.appendChild(
            title
        );


        content.appendChild(
            type
        );


        content.appendChild(
            price
        );


        content.appendChild(
            controls
        );



        article.appendChild(
            thumb
        );


        article.appendChild(
            content
        );


        return article;

    }



    /* =========================================================
       AFFICHER PANIER PIECES
    ========================================================= */


    function renderPieceDrawer() {


        if (
            !pieceCartOpen &&
            !pieceCartItems
        ) {

            return;
        }



        /* =====================================================
           NON CONNECTE
           PAS DE PANIER FLOTTANT
        ===================================================== */

        if (pieceCartOpen) {


            pieceCartOpen.style.display =

                cartState.authenticated

                    ? ""

                    : "none";

        }



        const pieces =

            cartState.authenticated

                ? cartState.items
                    .filter(
                        function (item) {


                            return (
                                item.itemType ===
                                "PIECE"
                            );

                        }
                    )

                : [];



        /* =====================================================
           QUANTITE PIECES
        ===================================================== */

        const quantity =
            pieces.reduce(
                function (
                    sum,
                    item
                ) {


                    return (
                        sum
                        +
                        Number(
                            item.quantity ||
                            1
                        )
                    );

                },
                0
            );



        /* =====================================================
           TOTAL PIECES
        ===================================================== */

        const total =
            pieces.reduce(
                function (
                    sum,
                    item
                ) {


                    return (
                        sum
                        +
                        Number(
                            item.price ||
                            0
                        )
                        *
                        Number(
                            item.quantity ||
                            1
                        )
                    );

                },
                0
            );



        if (pieceCartCount) {


            pieceCartCount.textContent =
                quantity;

        }



        if (pieceCartTotal) {


            pieceCartTotal.textContent =
                formatPrice(
                    total
                );

        }



        if (pieceCartFloatingTotal) {


            pieceCartFloatingTotal.textContent =
                formatPrice(
                    total
                );

        }



        if (!pieceCartItems) {

            return;
        }



        pieceCartItems.innerHTML =
            "";



        /* =====================================================
           VIDE
        ===================================================== */

        if (
            pieces.length ===
            0
        ) {


            const empty =
                document.createElement(
                    "div"
                );


            empty.className =
                "piece-cart-empty";


            empty.textContent =
                "Votre panier est vide.";


            pieceCartItems.appendChild(
                empty
            );


            return;
        }



        /* =====================================================
           ARTICLES
        ===================================================== */

        pieces.forEach(
            function (item) {


                pieceCartItems.appendChild(

                    createPieceDrawerItem(
                        item
                    )

                );

            }
        );

    }



    /* =========================================================
       DRAWER EVENTS
    ========================================================= */


    pieceCartOpen
        ?.addEventListener(
            "click",
            openPieceDrawer
        );


    pieceCartClose
        ?.addEventListener(
            "click",
            closePieceDrawer
        );


    pieceCartOverlay
        ?.addEventListener(
            "click",
            closePieceDrawer
        );



    /* =========================================================
       10. RESERVATION WHATSAPP

       IMPORTANT :

       Le backend /api/orders actuel
       sait encore enregistrer PRODUCT.

       Il ne faut SURTOUT PAS envoyer
       un piece.id comme productId.

       Donc si PIECE existe :
       on bloque temporairement la réservation
       jusqu'à l'étape backend suivante.
    ========================================================= */


    if (cartOrderButton) {


        cartOrderButton.addEventListener(

            "click",

            async function () {


                /* =================================================
                   CONNEXION
                ================================================= */

                const connected =
                    await requireCustomer();


                if (!connected) {

                    return;
                }



                /* =================================================
                   PANIER VIDE
                ================================================= */

                if (
                    cartState.items.length ===
                    0
                ) {

                    return;
                }



                const errorElement =
                    document.getElementById(
                        "orderErrorMessage"
                    );



                function showOrderError(
                    message
                ) {


                    if (errorElement) {


                        errorElement.textContent =
                            message ||
                            "";

                    }

                }



                /* =================================================
                   PIECES PAS ENCORE BRANCHEES A ORDER
                ================================================= */

                const pieceItem =
                    cartState.items.find(
                        function (item) {


                            return (
                                item.itemType ===
                                "PIECE"
                            );

                        }
                    );



                if (pieceItem) {


                    showOrderError(
                        "Le panier contient des pièces. La réservation des pièces doit encore être connectée au système de commande."
                    );


                    return;
                }



                /* =================================================
                   FORMULAIRE
                ================================================= */

                const customerName =

                    document
                        .getElementById(
                            "orderCustomerName"
                        )
                        ?.value
                        ?.trim()

                    ||

                    "";



                const phone =

                    document
                        .getElementById(
                            "orderPhone"
                        )
                        ?.value
                        ?.trim()

                    ||

                    "";



                const city =

                    document
                        .getElementById(
                            "orderCity"
                        )
                        ?.value
                        ?.trim()

                    ||

                    "";



                const address =

                    document
                        .getElementById(
                            "orderAddress"
                        )
                        ?.value
                        ?.trim()

                    ||

                    "";



                const notes =

                    document
                        .getElementById(
                            "orderNotes"
                        )
                        ?.value
                        ?.trim()

                    ||

                    "";



                /* =================================================
                   VALIDATION
                ================================================= */

                if (
                    !customerName ||
                    !phone
                ) {


                    showOrderError(
                        "Le nom et le téléphone sont obligatoires."
                    );


                    return;
                }



                /* =================================================
                   PRODUITS UNIQUEMENT
                ================================================= */

                const productItems =
                    cartState.items
                        .filter(
                            function (item) {


                                return (
                                    item.itemType ===
                                    "PRODUCT"
                                );

                            }
                        );



                const invalidItem =
                    productItems.find(
                        function (item) {


                            const id =
                                Number(
                                    item.itemId
                                );


                            return (
                                !Number.isInteger(
                                    id
                                )
                                ||
                                id <= 0
                            );

                        }
                    );



                if (invalidItem) {


                    showOrderError(
                        "Un produit du panier ne possède pas un ID MySQL valide."
                    );


                    return;
                }



                /* =================================================
                   DONNEES COMMANDE
                ================================================= */

                const requestData = {


                    customerName:
                        customerName,


                    phone:
                        phone,


                    city:
                        city,


                    address:
                        address,


                    notes:
                        notes,


                    items:
                        productItems.map(
                            function (item) {


                                return {


                                    productId:
                                        Number(
                                            item.itemId
                                        ),


                                    quantity:
                                        Math.max(
                                            1,
                                            Number(
                                                item.quantity ||
                                                1
                                            )
                                        )

                                };

                            }
                        )

                };



                showOrderError(
                    ""
                );



                cartOrderButton.disabled =
                    true;


                cartOrderButton.textContent =
                    "Enregistrement...";



                /* =================================================
                   SNAPSHOT MESSAGE WHATSAPP
                ================================================= */

                const snapshot =
                    productItems.map(
                        function (item) {


                            return {


                                name:
                                    item.name,


                                quantity:
                                    Number(
                                        item.quantity ||
                                        1
                                    )

                            };

                        }
                    );



                try {


                    /* =================================================
                       CREER COMMANDE
                    ================================================= */

                    const result =
                        await requestJson(

                            "/api/orders",

                            {
                                method:
                                    "POST",

                                headers: {

                                    "Content-Type":
                                        "application/json"
                                },

                                body:
                                    JSON.stringify(
                                        requestData
                                    )
                            },

                            true

                        );



                    /* =================================================
                       SESSION EXPIREE
                    ================================================= */

                    if (
                        result.response.status ===
                        401
                    ) {


                        redirectToCustomerLogin();


                        return;
                    }



                    /* =================================================
                       ERREUR
                    ================================================= */

                    if (
                        !result.response.ok
                    ) {


                        if (
                            result.response.status ===
                            403
                        ) {


                            throw new Error(
                                "Accès refusé par Spring Security. Rechargez la page puis réessayez."
                            );

                        }



                        throw new Error(

                            result.data.message

                            ||

                            result.data.error

                            ||

                            "Impossible d'enregistrer la commande."

                        );

                    }



                    /* =================================================
                       ID COMMANDE
                    ================================================= */

                    const orderId =

                        result.data.orderId

                        ??

                        result.data.id

                        ??

                        result.data.order?.id

                        ??

                        "";



                    /* =================================================
                       TOTAL SERVEUR
                    ================================================= */

                    const serverTotal =

                        result.data.total

                        ??

                        result.data.totalAmount

                        ??

                        result.data.order
                            ?.totalAmount

                        ??

                        calculateCartTotal();



                    /* =================================================
                       MESSAGE WHATSAPP
                    ================================================= */

                    let message =
                        "Bonjour Zineb Déco,\n\n";



                    if (orderId) {


                        message +=

                            "Je viens d'enregistrer la réservation n°"

                            +

                            orderId

                            +

                            ".\n\n";


                    } else {


                        message +=

                            "Je viens d'enregistrer une nouvelle réservation.\n\n";

                    }



                    message +=
                        "Client : "
                        +
                        customerName
                        +
                        "\n";


                    message +=
                        "Téléphone : "
                        +
                        phone
                        +
                        "\n";



                    if (city) {


                        message +=
                            "Ville : "
                            +
                            city
                            +
                            "\n";

                    }



                    if (address) {


                        message +=
                            "Adresse : "
                            +
                            address
                            +
                            "\n";

                    }



                    if (notes) {


                        message +=
                            "Remarque : "
                            +
                            notes
                            +
                            "\n";

                    }



                    message +=
                        "\nProduits :\n";



                    snapshot.forEach(
                        function (item) {


                            message +=

                                "• "

                                +

                                item.name

                                +

                                " × "

                                +

                                item.quantity

                                +

                                "\n";

                        }
                    );



                    message +=

                        "\nTotal : "

                        +

                        formatPrice(
                            serverTotal
                        );



                    message +=

                        "\n\nMerci de me confirmer la disponibilité et la livraison.";



                    /* =================================================
                       VIDER PANIER MYSQL
                    ================================================= */

                    try {


                        await cartMutation(

                            "/api/cart/clear",

                            "DELETE"

                        );


                    } catch (clearError) {


                        console.error(

                            "Commande créée mais panier non vidé :",

                            clearError

                        );

                    }



                    /* =================================================
                       OUVRIR WHATSAPP
                    ================================================= */

                    const whatsappUrl =

                        "https://wa.me/212667928660?text="

                        +

                        encodeURIComponent(
                            message
                        );



                    window.location.href =
                        whatsappUrl;



                } catch (error) {


                    console.error(
                        "Commande :",
                        error
                    );


                    showOrderError(

                        error.message

                        ||

                        "Impossible d'enregistrer la commande."

                    );



                } finally {


                    cartOrderButton.disabled =
                        false;


                    cartOrderButton.textContent =
                        "Réserver sur WhatsApp";

                }

            }

        );

    }



    /* =========================================================
       11. FILTRES CATALOGUE
    ========================================================= */


    const salonGrid =
        document.getElementById(
            "salonProductGrid"
        );



    if (salonGrid) {


        const cards =
            Array.from(

                salonGrid
                    .querySelectorAll(
                        ".catalog-product-card"
                    )

            );



        const categoryButtons =
            document.querySelectorAll(
                ".catalog-type"
            );


        const categoryCheckboxes =
            document.querySelectorAll(
                ".salon-filter"
            );


        const priceRadios =
            document.querySelectorAll(
                ".price-filter"
            );


        const colorButtons =
            document.querySelectorAll(
                ".color-filter"
            );


        const availableFilter =
            document.getElementById(
                "availableFilter"
            );


        const customFilter =
            document.getElementById(
                "customFilter"
            );


        const sortProducts =
            document.getElementById(
                "sortProducts"
            );


        const resultCount =
            document.getElementById(
                "salonResultCount"
            );


        const toolbarResultCount =
            document.getElementById(
                "toolbarResultCount"
            );


        const catalogEmpty =
            document.getElementById(
                "catalogEmpty"
            );



        let quickCategory =
            "all";


        let selectedColor =
            null;



        /* =====================================================
           COMPTEUR RESULTATS
        ===================================================== */


        function updateCatalogCount(
            count
        ) {


            if (resultCount) {


                resultCount.textContent =
                    count;

            }



            if (toolbarResultCount) {


                toolbarResultCount.textContent =
                    count;

            }



            if (catalogEmpty) {


                catalogEmpty
                    .classList
                    .toggle(
                        "visible",
                        count === 0
                    );

            }

        }



        /* =====================================================
           APPLIQUER FILTRES
        ===================================================== */


        function applyFilters() {


            const selectedCategories =
                Array.from(
                    categoryCheckboxes
                )
                    .filter(
                        function (input) {


                            return (
                                input.checked
                            );

                        }
                    )
                    .map(
                        function (input) {


                            return (
                                input.value
                            );

                        }
                    );



            const selectedPrice =
                document.querySelector(
                    ".price-filter:checked"
                );



            let visibleCount =
                0;



            cards.forEach(
                function (card) {


                    let visible =
                        true;



                    const category =
                        card.dataset.category ||
                        "";


                    const color =
                        card.dataset.color ||
                        "";


                    const price =
                        Number(
                            card.dataset.price ||
                            0
                        );


                    const availability =
                        card.dataset.availability ||
                        "";



                    /* =================================================
                       CATEGORIE RAPIDE
                    ================================================= */

                    if (
                        quickCategory !== "all"
                        &&
                        category !== quickCategory
                    ) {


                        visible =
                            false;

                    }



                    /* =================================================
                       CATEGORIES CHECKBOX
                    ================================================= */

                    if (
                        selectedCategories.length > 0
                        &&
                        !selectedCategories.includes(
                            category
                        )
                    ) {


                        visible =
                            false;

                    }



                    /* =================================================
                       COULEUR
                    ================================================= */

                    if (
                        selectedColor
                        &&
                        selectedColor !== color
                    ) {


                        visible =
                            false;

                    }



                    /* =================================================
                       PRIX
                    ================================================= */

                    if (
                        selectedPrice
                        &&
                        selectedPrice.value !==
                        "all"
                    ) {


                        const range =
                            selectedPrice
                                .value
                                .split(
                                    "-"
                                );


                        const min =
                            Number(
                                range[0] ||
                                0
                            );


                        const max =
                            Number(

                                range[1]

                                ||

                                Number
                                    .MAX_SAFE_INTEGER

                            );



                        if (
                            price < min
                            ||
                            price > max
                        ) {


                            visible =
                                false;

                        }

                    }



                    /* =================================================
                       DISPONIBLE
                    ================================================= */

                    if (
                        availableFilter
                        &&
                        availableFilter.checked
                        &&
                        availability !== "available"
                        &&
                        availability !== "stock"
                    ) {


                        visible =
                            false;

                    }



                    /* =================================================
                       SUR COMMANDE
                    ================================================= */

                    if (
                        customFilter
                        &&
                        customFilter.checked
                        &&
                        availability !== "custom"
                    ) {


                        visible =
                            false;

                    }



                    card.classList.toggle(

                        "filtered-out",

                        !visible

                    );



                    if (visible) {


                        visibleCount++;

                    }

                }
            );



            updateCatalogCount(
                visibleCount
            );

        }



        /* =====================================================
           CATEGORIES RAPIDES
        ===================================================== */


        categoryButtons.forEach(
            function (button) {


                button.addEventListener(

                    "click",

                    function () {


                        categoryButtons.forEach(
                            function (item) {


                                item.classList
                                    .remove(
                                        "active"
                                    );

                            }
                        );


                        button.classList
                            .add(
                                "active"
                            );


                        quickCategory =
                            button.dataset
                                .category
                            ||
                            "all";


                        applyFilters();

                    }

                );

            }
        );



        /* =====================================================
           CHECKBOXES
        ===================================================== */


        categoryCheckboxes.forEach(
            function (input) {


                input.addEventListener(
                    "change",
                    applyFilters
                );

            }
        );



        /* =====================================================
           PRIX
        ===================================================== */


        priceRadios.forEach(
            function (input) {


                input.addEventListener(
                    "change",
                    applyFilters
                );

            }
        );



        /* =====================================================
           COULEURS
        ===================================================== */


        colorButtons.forEach(
            function (button) {


                button.addEventListener(

                    "click",

                    function () {


                        const color =
                            button.dataset
                                .color;



                        selectedColor =

                            selectedColor ===
                            color

                                ? null

                                : color;



                        colorButtons.forEach(
                            function (item) {


                                item.classList.toggle(

                                    "active",

                                    item.dataset.color ===
                                    selectedColor

                                );

                            }
                        );


                        applyFilters();

                    }

                );

            }
        );



        availableFilter
            ?.addEventListener(
                "change",
                applyFilters
            );


        customFilter
            ?.addEventListener(
                "change",
                applyFilters
            );



        /* =====================================================
           RESET
        ===================================================== */


        function resetFilters() {


            quickCategory =
                "all";


            selectedColor =
                null;



            categoryButtons.forEach(
                function (button) {


                    button.classList.toggle(

                        "active",

                        button.dataset.category ===
                        "all"

                    );

                }
            );



            categoryCheckboxes.forEach(
                function (input) {


                    input.checked =
                        false;

                }
            );



            priceRadios.forEach(
                function (input) {


                    input.checked =
                        input.value ===
                        "all";

                }
            );



            colorButtons.forEach(
                function (button) {


                    button.classList
                        .remove(
                            "active"
                        );

                }
            );



            if (availableFilter) {


                availableFilter.checked =
                    false;

            }



            if (customFilter) {


                customFilter.checked =
                    false;

            }



            if (sortProducts) {


                sortProducts.value =
                    "default";

            }



            applyFilters();

        }



        document
            .getElementById(
                "resetFilters"
            )
            ?.addEventListener(
                "click",
                resetFilters
            );



        document
            .getElementById(
                "emptyResetFilters"
            )
            ?.addEventListener(
                "click",
                resetFilters
            );



        /* =====================================================
           TRI
        ===================================================== */


        if (sortProducts) {


            sortProducts.addEventListener(

                "change",

                function () {


                    const value =
                        sortProducts.value;


                    const sorted =
                        [
                            ...cards
                        ];



                    /* =============================================
                       PRIX CROISSANT
                    ============================================= */

                    if (
                        value ===
                        "price-asc"
                    ) {


                        sorted.sort(
                            function (
                                a,
                                b
                            ) {


                                return (

                                    Number(
                                        a.dataset.price ||
                                        0
                                    )

                                    -

                                    Number(
                                        b.dataset.price ||
                                        0
                                    )

                                );

                            }
                        );

                    }



                    /* =============================================
                       PRIX DECROISSANT
                    ============================================= */

                    if (
                        value ===
                        "price-desc"
                    ) {


                        sorted.sort(
                            function (
                                a,
                                b
                            ) {


                                return (

                                    Number(
                                        b.dataset.price ||
                                        0
                                    )

                                    -

                                    Number(
                                        a.dataset.price ||
                                        0
                                    )

                                );

                            }
                        );

                    }



                    /* =============================================
                       NOM
                    ============================================= */

                    if (
                        value ===
                        "name"
                    ) {


                        sorted.sort(
                            function (
                                a,
                                b
                            ) {


                                return String(
                                    a.dataset.name ||
                                    ""
                                )
                                    .localeCompare(

                                        String(
                                            b.dataset.name ||
                                            ""
                                        ),

                                        "fr"

                                    );

                            }
                        );

                    }



                    /* =============================================
                       DEFAUT
                    ============================================= */

                    if (
                        value ===
                        "default"
                    ) {


                        sorted.sort(
                            function (
                                a,
                                b
                            ) {


                                return (

                                    Number(
                                        b.dataset.id ||
                                        0
                                    )

                                    -

                                    Number(
                                        a.dataset.id ||
                                        0
                                    )

                                );

                            }
                        );

                    }



                    sorted.forEach(
                        function (card) {


                            salonGrid.appendChild(
                                card
                            );

                        }
                    );

                }

            );

        }



        /* =====================================================
           FILTRES MOBILE
        ===================================================== */


        const mobileFilterButton =
            document.getElementById(
                "mobileFilterButton"
            );


        const closeFilterButton =
            document.getElementById(
                "closeFilterButton"
            );


        const catalogSidebar =
            document.getElementById(
                "catalogSidebar"
            );



        mobileFilterButton
            ?.addEventListener(

                "click",

                function () {


                    catalogSidebar
                        ?.classList
                        .add(
                            "open"
                        );

                }

            );



        closeFilterButton
            ?.addEventListener(

                "click",

                function () {


                    catalogSidebar
                        ?.classList
                        .remove(
                            "open"
                        );

                }

            );



        applyFilters();

    }



    /* =========================================================
       12. PREVIEW IMAGE ADMIN
    ========================================================= */


    function setupImagePreview(

        inputId,

        previewIds

    ) {


        const input =
            document.getElementById(
                inputId
            );


        if (!input) {

            return;
        }



        input.addEventListener(

            "change",

            function () {


                const file =
                    input.files?.[0];


                if (!file) {

                    return;
                }



                const allowedTypes = [

                    "image/jpeg",

                    "image/png",

                    "image/webp"

                ];



                if (
                    !allowedTypes.includes(
                        file.type
                    )
                ) {


                    alert(
                        "Formats autorisés : JPG, PNG et WebP."
                    );


                    input.value =
                        "";


                    return;
                }



                if (
                    file.size >
                    10 * 1024 * 1024
                ) {


                    alert(
                        "L'image ne doit pas dépasser 10 MB."
                    );


                    input.value =
                        "";


                    return;
                }



                let preview =
                    null;



                for (
                    const previewId
                    of previewIds
                ) {


                    preview =
                        document.getElementById(
                            previewId
                        );


                    if (preview) {

                        break;
                    }

                }



                if (!preview) {

                    return;
                }



                const objectUrl =
                    URL.createObjectURL(
                        file
                    );


                preview.src =
                    objectUrl;


                preview.style.display =
                    "block";



                preview.onload =
                    function () {


                        URL.revokeObjectURL(
                            objectUrl
                        );

                    };

            }

        );

    }



    setupImagePreview(

        "productImageInput",

        [

            "productImagePreview",

            "emptyProductImagePreview"

        ]

    );



    setupImagePreview(

        "salonImageInput",

        [

            "imagePreview",

            "emptyImagePreview"

        ]

    );



    /* =========================================================
       13. RECHERCHE HEADER ANCIENNE STRUCTURE

       Le header premium possède déjà
       sa propre recherche.
    ========================================================= */


    document
        .querySelectorAll(
            ".search-box"
        )
        .forEach(
            function (form) {


                const input =
                    form.querySelector(
                        'input[type="search"]'
                    );


                if (!input) {

                    return;
                }



                form.addEventListener(

                    "submit",

                    function (event) {


                        event.preventDefault();



                        const query =
                            input.value
                                .trim();



                        if (!query) {


                            input.focus();


                            return;
                        }



                        const cards =
                            document.querySelectorAll(

                                ".product-card, " +

                                ".catalog-product-card"

                            );



                        /* =================================================
                           RECHERCHE LOCALE
                        ================================================= */

                        if (
                            cards.length >
                            0
                        ) {


                            const normalizedQuery =
                                normalizeText(
                                    query
                                );



                            cards.forEach(
                                function (card) {


                                    const text =
                                        normalizeText(

                                            (
                                                card.dataset.name ||
                                                ""
                                            )

                                            +

                                            " "

                                            +

                                            card.textContent

                                        );



                                    card.style.display =

                                        text.includes(
                                            normalizedQuery
                                        )

                                            ? ""

                                            : "none";

                                }
                            );



                            document
                                .querySelector(

                                    ".generic-catalog-products, " +

                                    ".catalog-section, " +

                                    ".products-section"

                                )
                                ?.scrollIntoView(
                                    {
                                        behavior:
                                            "smooth",

                                        block:
                                            "start"
                                    }
                                );



                        } else {


                            window.location.href =

                                "/salons?search="

                                +

                                encodeURIComponent(
                                    query
                                );

                        }

                    }

                );

            }
        );



    /* =========================================================
       RECHERCHE VIA URL
    ========================================================= */


    const urlParams =
        new URLSearchParams(
            window.location.search
        );


    const searchQuery =
        urlParams.get(
            "search"
        );



    if (searchQuery) {


        const normalized =
            normalizeText(
                searchQuery
            );



        document
            .querySelectorAll(

                ".product-card, " +

                ".catalog-product-card"

            )
            .forEach(
                function (card) {


                    const text =
                        normalizeText(

                            (
                                card.dataset.name ||
                                ""
                            )

                            +

                            " "

                            +

                            card.textContent

                        );



                    card.style.display =

                        text.includes(
                            normalized
                        )

                            ? ""

                            : "none";

                }
            );

    }



    /* =========================================================
       14. MENU ACTIF
    ========================================================= */


    const currentPath =
        window.location.pathname;



    document
        .querySelectorAll(

            ".main-navigation a, " +

            ".zd-premium-nav a"

        )
        .forEach(
            function (link) {


                const href =
                    link.getAttribute(
                        "href"
                    );


                if (!href) {

                    return;
                }



                if (

                    (
                        href === "/"
                        &&
                        currentPath === "/"
                    )

                    ||

                    (
                        href !== "/"
                        &&
                        currentPath.startsWith(
                            href
                        )
                    )

                ) {


                    link.classList
                        .add(
                            "active"
                        );

                }

            }
        );



    /* =========================================================
       15. TOUCHE ESCAPE
    ========================================================= */


    document.addEventListener(

        "keydown",

        function (event) {


            if (
                event.key !==
                "Escape"
            ) {

                return;
            }



            closeMenu();


            closePieceDrawer();



            document
                .getElementById(
                    "catalogSidebar"
                )
                ?.classList
                .remove(
                    "open"
                );

        }

    );



    /* =========================================================
       16. INITIALISATION VISUELLE
    ========================================================= */


    updateCartCounter();


    renderCartPage();


    renderPieceDrawer();



    console.log(

        "ZINEB DECO main.js chargé - panier MySQL par compte."

    );

});