document.addEventListener("DOMContentLoaded", function () {

    "use strict";


    /* =========================================================
       ZINEB DECO
       MAIN.JS
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


    /*
     * Récupération du token CSRF généré
     * par Spring Security.
     */

    function getCsrf() {

        const tokenElement =
            document.querySelector(
                'meta[name="_csrf"]'
            );


        const headerElement =
            document.querySelector(
                'meta[name="_csrf_header"]'
            );


        return {

            token:
                tokenElement
                    ? tokenElement.getAttribute(
                        "content"
                    ) || ""
                    : "",

            header:
                headerElement
                    ? headerElement.getAttribute(
                        "content"
                    ) || ""
                    : ""
        };
    }



    /* =========================================================
       02. MENU MOBILE
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
                .remove("open");
        }


        document.body
            .classList
            .remove(
                "menu-open"
            );


        if (mobileMenuButton) {

            mobileMenuButton.textContent =
                "☰";


            mobileMenuButton.setAttribute(
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
                            .toggle("open");


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


                    mobileMenuButton.setAttribute(
                        "aria-expanded",
                        open
                            ? "true"
                            : "false"
                    );
                }
            );


        mainNavigation
            .querySelectorAll("a")
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
       03. PANIER LOCAL STORAGE
    ========================================================= */

    const CART_KEY =
        "zinebDecoCart";


    let cart = [];


    try {

        const data =
            JSON.parse(
                localStorage.getItem(
                    CART_KEY
                )
            );


        cart =
            Array.isArray(data)
                ? data
                : [];

    } catch (error) {

        console.error(
            "Erreur lecture panier :",
            error
        );


        cart = [];
    }


    function saveCart() {

        localStorage.setItem(
            CART_KEY,
            JSON.stringify(cart)
        );


        updateCartCounter();
    }


    function updateCartCounter() {

        const total =
            cart.reduce(
                function (
                    sum,
                    product
                ) {

                    return (
                        sum +
                        Number(
                            product.quantity ||
                            1
                        )
                    );
                },
                0
            );


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
       04. AJOUTER PRODUIT
    ========================================================= */

    function addProductToCart(
        product
    ) {

        if (!product) {
            return false;
        }


        if (
            product.id === null ||
            product.id === undefined ||
            String(product.id).trim() === ""
        ) {

            console.error(
                "ID produit manquant.",
                product
            );


            return false;
        }


        const id =
            String(
                product.id
            );


        const existing =
            cart.find(
                function (item) {

                    return (
                        String(item.id) ===
                        id
                    );
                }
            );


        if (existing) {

            existing.quantity =
                Number(
                    existing.quantity ||
                    1
                ) + 1;

        } else {

            cart.push(
                {
                    id:
                        product.id,

                    name:
                        product.name ||
                        "Produit",

                    price:
                        Number(
                            product.price ||
                            0
                        ),

                    image:
                        product.image ||
                        "",

                    quantity:
                        1
                }
            );
        }


        saveCart();


        return true;
    }


    function animateAddButton(
        button
    ) {

        if (!button) {
            return;
        }


        const oldText =
            button.textContent;


        button.disabled =
            true;


        button.textContent =
            "Ajouté ✓";


        window.setTimeout(
            function () {

                button.disabled =
                    false;


                button.textContent =
                    oldText;
            },
            900
        );
    }



    /* =========================================================
       05. EXTRAIRE PRODUIT D'UNE CARTE
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
            button.dataset.id ||
            button.dataset.productId ||
            card?.dataset.id ||
            card?.dataset.productId ||
            "";


        /*
         * Fallback :
         * récupérer ID dans /produit/12
         */

        if (
            !id &&
            card
        ) {

            const link =
                card.querySelector(
                    'a[href*="/produit/"]'
                );


            const href =
                link?.getAttribute(
                    "href"
                );


            const match =
                href?.match(
                    /\/produit\/(\d+)/
                );


            if (match) {

                id =
                    match[1];
            }
        }


        const name =
            button.dataset.name ||
            card?.dataset.name ||
            card
                ?.querySelector(
                    ".product-card-title, h3, h2"
                )
                ?.textContent
                ?.trim() ||
            "Produit";


        let price =
            button.dataset.price ||
            card?.dataset.price ||
            "";


        if (
            !price &&
            card
        ) {

            price =
                card
                    .querySelector(
                        ".product-price, " +
                        ".product-price-row strong, " +
                        ".catalog-product-bottom strong"
                    )
                    ?.textContent ||
                "";
        }


        const image =
            button.dataset.image ||
            card?.dataset.image ||
            card
                ?.querySelector("img")
                ?.getAttribute("src") ||
            "";


        return {

            id:
                id,

            name:
                name,

            price:
                parsePrice(price),

            image:
                image
        };
    }



    /* =========================================================
       06. BOUTONS AJOUT PANIER
    ========================================================= */

    document
        .querySelectorAll(
            ".add-cart-button"
        )
        .forEach(
            function (button) {

                button.addEventListener(
                    "click",
                    function (event) {

                        event.preventDefault();

                        event.stopPropagation();


                        if (button.disabled) {
                            return;
                        }


                        const product =
                            extractProductFromButton(
                                button
                            );


                        if (!product.id) {

                            alert(
                                "Impossible d'ajouter ce produit : identifiant MySQL absent."
                            );


                            return;
                        }


                        if (
                            addProductToCart(
                                product
                            )
                        ) {

                            animateAddButton(
                                button
                            );
                        }
                    }
                );
            }
        );



    /* =========================================================
       07. DETAIL PRODUIT
    ========================================================= */

    document
        .querySelectorAll(
            ".add-detail-cart"
        )
        .forEach(
            function (button) {

                button.addEventListener(
                    "click",
                    function (event) {

                        event.preventDefault();


                        const product = {

                            id:
                                button.dataset.id,

                            name:
                                button.dataset.name ||
                                "Produit",

                            price:
                                parsePrice(
                                    button.dataset.price
                                ),

                            image:
                                button.dataset.image ||
                                ""
                        };


                        if (!product.id) {

                            alert(
                                "Identifiant produit manquant."
                            );


                            return;
                        }


                        if (
                            addProductToCart(
                                product
                            )
                        ) {

                            animateAddButton(
                                button
                            );
                        }
                    }
                );
            }
        );



    /* =========================================================
       08. FAVORIS MYSQL
    ========================================================= */

    const favoriteButtons =
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
            favoriteButtons.length ===
            0
        ) {
            return;
        }


        try {

            const response =
                await fetch(
                    "/api/favorites",
                    {
                        method:
                            "GET",

                        credentials:
                            "same-origin",

                        headers: {
                            "Accept":
                                "application/json"
                        }
                    }
                );


            if (
                response.status ===
                401
            ) {
                return;
            }


            if (!response.ok) {
                return;
            }


            const data =
                await response.json();


            if (
                !data ||
                !data.authenticated
            ) {
                return;
            }


            const ids =
                Array.isArray(
                    data.productIds
                )
                    ? data.productIds.map(
                        Number
                    )
                    : [];


            favoriteButtons.forEach(
                function (button) {

                    updateFavoriteButton(
                        button,
                        ids.includes(
                            Number(
                                button.dataset.productId
                            )
                        )
                    );
                }
            );


        } catch (error) {

            console.error(
                "Erreur favoris :",
                error
            );
        }
    }


    favoriteButtons.forEach(
        function (button) {

            button.addEventListener(
                "click",
                async function (event) {

                    event.preventDefault();

                    event.stopPropagation();


                    const productId =
                        button.dataset.productId;


                    if (!productId) {
                        return;
                    }


                    button.disabled =
                        true;


                    try {

                        const csrf =
                            getCsrf();


                        const headers = {
                            "Accept":
                                "application/json"
                        };


                        if (
                            csrf.token &&
                            csrf.header
                        ) {

                            headers[
                                csrf.header
                            ] =
                                csrf.token;
                        }


                        const response =
                            await fetch(
                                "/api/favorites/" +
                                productId +
                                "/toggle",
                                {
                                    method:
                                        "POST",

                                    credentials:
                                        "same-origin",

                                    headers:
                                        headers
                                }
                            );


                        if (
                            response.status ===
                            401
                        ) {

                            window.location.href =
                                "/compte/login";


                            return;
                        }


                        if (
                            response.status ===
                            403
                        ) {

                            throw new Error(
                                "Accès refusé."
                            );
                        }


                        const data =
                            await response.json();


                        if (!response.ok) {

                            throw new Error(
                                data.message ||
                                "Erreur favoris."
                            );
                        }


                        updateFavoriteButton(
                            button,
                            Boolean(
                                data.favorite
                            )
                        );


                    } catch (error) {

                        console.error(
                            error
                        );


                        alert(
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
       09. ELEMENTS PANIER
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
       10. TOTAL
    ========================================================= */

    function calculateCartTotal() {

        return cart.reduce(
            function (
                total,
                product
            ) {

                return (
                    total +
                    Number(
                        product.price ||
                        0
                    ) *
                    Number(
                        product.quantity ||
                        1
                    )
                );
            },
            0
        );
    }



    /* =========================================================
       11. QUANTITE
    ========================================================= */

    function changeCartQuantity(
        productId,
        change
    ) {

        const product =
            cart.find(
                function (item) {

                    return (
                        String(item.id) ===
                        String(productId)
                    );
                }
            );


        if (!product) {
            return;
        }


        product.quantity =
            Number(
                product.quantity ||
                1
            ) +
            Number(
                change
            );


        if (
            product.quantity <=
            0
        ) {

            cart =
                cart.filter(
                    function (item) {

                        return (
                            String(item.id) !==
                            String(productId)
                        );
                    }
                );
        }


        saveCart();

        renderCartPage();
    }



    /* =========================================================
       12. SUPPRIMER
    ========================================================= */

    function removeCartProduct(
        productId
    ) {

        cart =
            cart.filter(
                function (item) {

                    return (
                        String(item.id) !==
                        String(productId)
                    );
                }
            );


        saveCart();

        renderCartPage();
    }



    /* =========================================================
       13. PRODUIT PANIER HTML
    ========================================================= */

    function createCartProduct(
        product
    ) {

        const article =
            document.createElement(
                "article"
            );


        article.className =
            "cart-product";


        /* IMAGE */

        const imageWrapper =
            document.createElement(
                "div"
            );


        imageWrapper.className =
            "cart-product-image";


        function placeholder() {

            imageWrapper.innerHTML =
                "";


            const div =
                document.createElement(
                    "div"
                );


            div.className =
                "cart-image-placeholder";


            div.textContent =
                "ZD";


            imageWrapper.appendChild(
                div
            );
        }


        if (product.image) {

            const image =
                document.createElement(
                    "img"
                );


            image.src =
                product.image;


            image.alt =
                product.name ||
                "Produit";


            image.loading =
                "lazy";


            image.addEventListener(
                "error",
                placeholder,
                {
                    once: true
                }
            );


            imageWrapper.appendChild(
                image
            );

        } else {

            placeholder();
        }


        /* CONTENT */

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
            "ZINEB DÉCO";


        const title =
            document.createElement(
                "h3"
            );


        title.textContent =
            product.name ||
            "Produit";


        info.appendChild(
            label
        );


        info.appendChild(
            title
        );


        /* SUPPRIMER */

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

                removeCartProduct(
                    product.id
                );
            }
        );


        top.appendChild(
            info
        );


        top.appendChild(
            removeButton
        );


        /* BAS */

        const bottom =
            document.createElement(
                "div"
            );


        bottom.className =
            "cart-product-bottom";


        /* QUANTITE */

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
                    product.id,
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
                product.quantity ||
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
                    product.id,
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


        /* PRIX */

        const prices =
            document.createElement(
                "div"
            );


        prices.className =
            "cart-product-prices";


        const unit =
            document.createElement(
                "small"
            );


        unit.textContent =
            formatPrice(
                product.price
            ) +
            " / unité";


        const total =
            document.createElement(
                "strong"
            );


        total.textContent =
            formatPrice(
                Number(
                    product.price ||
                    0
                ) *
                Number(
                    product.quantity ||
                    1
                )
            );


        prices.appendChild(
            unit
        );


        prices.appendChild(
            total
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
       14. AFFICHER PANIER
    ========================================================= */

    function renderCartPage() {

        if (
            !cartPageItems ||
            !cartPageEmpty ||
            !cartPageLayout
        ) {
            return;
        }


        cartPageItems.innerHTML =
            "";


        if (
            cart.length ===
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


        cartPageEmpty.style.display =
            "none";


        cartPageLayout.style.display =
            "grid";


        cart.forEach(
            function (product) {

                cartPageItems.appendChild(
                    createCartProduct(
                        product
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
       15. VIDER PANIER
    ========================================================= */

    if (clearCartButton) {

        clearCartButton.addEventListener(
            "click",
            function () {

                const confirmed =
                    window.confirm(
                        "Voulez-vous vider votre panier ?"
                    );


                if (!confirmed) {
                    return;
                }


                cart = [];


                saveCart();

                renderCartPage();
            }
        );
    }



    /* =========================================================
       16. ENREGISTRER COMMANDE MYSQL + WHATSAPP
    ========================================================= */

    if (cartOrderButton) {

        cartOrderButton.addEventListener(
            "click",
            async function () {


                if (
                    cart.length ===
                    0
                ) {
                    return;
                }


                const errorElement =
                    document.getElementById(
                        "orderErrorMessage"
                    );


                function showError(
                    message
                ) {

                    if (errorElement) {

                        errorElement.textContent =
                            message ||
                            "";
                    }
                }


                /* =========================
                   CHAMPS
                ========================= */

                const customerName =
                    document
                        .getElementById(
                            "orderCustomerName"
                        )
                        ?.value
                        ?.trim() ||
                    "";


                const phone =
                    document
                        .getElementById(
                            "orderPhone"
                        )
                        ?.value
                        ?.trim() ||
                    "";


                const city =
                    document
                        .getElementById(
                            "orderCity"
                        )
                        ?.value
                        ?.trim() ||
                    "";


                const address =
                    document
                        .getElementById(
                            "orderAddress"
                        )
                        ?.value
                        ?.trim() ||
                    "";


                const notes =
                    document
                        .getElementById(
                            "orderNotes"
                        )
                        ?.value
                        ?.trim() ||
                    "";


                /* =========================
                   VALIDATION
                ========================= */

                if (
                    !customerName ||
                    !phone
                ) {

                    showError(
                        "Le nom et le téléphone sont obligatoires."
                    );


                    return;
                }


                const invalidProduct =
                    cart.find(
                        function (product) {

                            const id =
                                Number(
                                    product.id
                                );


                            return (
                                !Number.isInteger(id) ||
                                id <= 0
                            );
                        }
                    );


                if (invalidProduct) {

                    showError(
                        "Un produit du panier ne possède pas un ID MySQL valide. Videz le panier puis ajoutez à nouveau les produits."
                    );


                    return;
                }


                /* =========================
                   CSRF
                ========================= */

                const csrf =
                    getCsrf();


                console.log(
                    "CSRF :",
                    {
                        header:
                            csrf.header,

                        tokenPresent:
                            Boolean(
                                csrf.token
                            )
                    }
                );


                if (
                    !csrf.token ||
                    !csrf.header
                ) {

                    showError(
                        "Jeton de sécurité absent. Rechargez la page avec Ctrl + F5."
                    );


                    return;
                }


                /* =========================
                   DATA
                ========================= */

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
                        cart.map(
                            function (product) {

                                return {

                                    productId:
                                        Number(
                                            product.id
                                        ),

                                    quantity:
                                        Math.max(
                                            1,
                                            Number(
                                                product.quantity ||
                                                1
                                            )
                                        )
                                };
                            }
                        )
                };


                /*
                 * On garde une copie avant
                 * de vider le panier.
                 */

                const cartSnapshot =
                    cart.map(
                        function (product) {

                            return {
                                id:
                                    product.id,

                                name:
                                    product.name,

                                price:
                                    Number(
                                        product.price ||
                                        0
                                    ),

                                quantity:
                                    Number(
                                        product.quantity ||
                                        1
                                    )
                            };
                        }
                    );


                showError("");


                cartOrderButton.disabled =
                    true;


                cartOrderButton.textContent =
                    "Enregistrement...";


                try {


                    /* =====================
                       HEADERS
                    ====================== */

                    const headers = {

                        "Content-Type":
                            "application/json",

                        "Accept":
                            "application/json"
                    };


                    /*
                     * Ex :
                     *
                     * X-CSRF-TOKEN: token
                     */

                    headers[
                        csrf.header
                    ] =
                        csrf.token;


                    /* =====================
                       API
                    ====================== */

                    const response =
                        await fetch(
                            "/api/orders",
                            {

                                method:
                                    "POST",

                                credentials:
                                    "same-origin",

                                cache:
                                    "no-store",

                                headers:
                                    headers,

                                body:
                                    JSON.stringify(
                                        requestData
                                    )
                            }
                        );


                    /* =====================
                       LECTURE REPONSE
                    ====================== */

                    const responseText =
                        await response.text();


                    let data = {};


                    if (responseText) {

                        try {

                            data =
                                JSON.parse(
                                    responseText
                                );

                        } catch (jsonError) {

                            data = {
                                message:
                                    responseText
                            };
                        }
                    }


                    console.log(
                        "Réponse commande :",
                        response.status,
                        data
                    );


                    /* =====================
                       ERREURS
                    ====================== */

                    if (!response.ok) {


                        if (
                            response.status ===
                            403
                        ) {

                            throw new Error(
                                "Accès refusé par Spring Security (403). Rechargez la page puis reconnectez-vous."
                            );
                        }


                        if (
                            response.status ===
                            401
                        ) {

                            throw new Error(
                                "Votre session a expiré. Reconnectez-vous."
                            );
                        }


                        throw new Error(
                            data.message ||
                            data.error ||
                            "Impossible d'enregistrer la commande."
                        );
                    }


                    /* =====================
                       ID COMMANDE
                    ====================== */

                    const orderId =
                        data.orderId ??
                        data.id ??
                        data.order?.id ??
                        "";


                    /* =====================
                       TOTAL SERVEUR
                    ====================== */

                    const serverTotal =
                        data.total ??
                        data.totalAmount ??
                        data.order?.totalAmount ??
                        calculateCartTotal();


                    /* =====================
                       WHATSAPP
                    ====================== */

                    let message =
                        "Bonjour Zineb Déco,\n\n";


                    if (orderId) {

                        message +=
                            "Je viens d'enregistrer la réservation n°" +
                            orderId +
                            ".\n\n";

                    } else {

                        message +=
                            "Je viens d'enregistrer une nouvelle réservation.\n\n";
                    }


                    message +=
                        "Client : " +
                        customerName +
                        "\n";


                    message +=
                        "Téléphone : " +
                        phone +
                        "\n";


                    if (city) {

                        message +=
                            "Ville : " +
                            city +
                            "\n";
                    }


                    if (address) {

                        message +=
                            "Adresse : " +
                            address +
                            "\n";
                    }


                    if (notes) {

                        message +=
                            "Remarque : " +
                            notes +
                            "\n";
                    }


                    message +=
                        "\nProduits :\n";


                    cartSnapshot.forEach(
                        function (product) {

                            message +=
                                "• " +
                                product.name +
                                " × " +
                                product.quantity +
                                "\n";
                        }
                    );


                    message +=
                        "\nTotal : " +
                        formatPrice(
                            serverTotal
                        );


                    message +=
                        "\n\nMerci de me confirmer la disponibilité et la livraison.";


                    /* =====================
                       PANIER VIDE
                    ====================== */

                    cart = [];


                    saveCart();

                    renderCartPage();


                    /* =====================
                       OUVRIR WHATSAPP
                    ====================== */

                    const whatsappUrl =
                        "https://wa.me/212667928660?text=" +
                        encodeURIComponent(
                            message
                        );


                    window.location.href =
                        whatsappUrl;


                } catch (error) {


                    console.error(
                        "Erreur commande :",
                        error
                    );


                    showError(
                        error.message ||
                        "Impossible d'enregistrer la commande."
                    );


                    cartOrderButton.disabled =
                        false;


                    cartOrderButton.textContent =
                        "Réserver sur WhatsApp";
                }
            }
        );
    }



    /* =========================================================
       17. FILTRES CATALOGUE
    ========================================================= */

    const salonGrid =
        document.getElementById(
            "salonProductGrid"
        );


    if (salonGrid) {

        const cards =
            Array.from(
                salonGrid.querySelectorAll(
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

                catalogEmpty.classList.toggle(
                    "visible",
                    count === 0
                );
            }
        }


        function applyFilters() {

            const selectedCategories =
                Array.from(
                    categoryCheckboxes
                )
                    .filter(
                        function (input) {

                            return input.checked;
                        }
                    )
                    .map(
                        function (input) {

                            return input.value;
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


                    if (
                        quickCategory !==
                            "all" &&
                        category !==
                            quickCategory
                    ) {

                        visible =
                            false;
                    }


                    if (
                        selectedCategories.length >
                            0 &&
                        !selectedCategories.includes(
                            category
                        )
                    ) {

                        visible =
                            false;
                    }


                    if (
                        selectedColor &&
                        selectedColor !==
                            color
                    ) {

                        visible =
                            false;
                    }


                    if (
                        selectedPrice &&
                        selectedPrice.value !==
                            "all"
                    ) {

                        const range =
                            selectedPrice
                                .value
                                .split("-");


                        const min =
                            Number(
                                range[0] ||
                                0
                            );


                        const max =
                            Number(
                                range[1] ||
                                Number.MAX_SAFE_INTEGER
                            );


                        if (
                            price < min ||
                            price > max
                        ) {

                            visible =
                                false;
                        }
                    }


                    if (
                        availableFilter &&
                        availableFilter.checked &&
                        card.dataset.availability !==
                            "available"
                    ) {

                        visible =
                            false;
                    }


                    if (
                        customFilter &&
                        customFilter.checked &&
                        card.dataset.availability !==
                            "custom"
                    ) {

                        visible =
                            false;
                    }


                    card.classList.toggle(
                        "filtered-out",
                        !visible
                    );


                    card.style.display =
                        visible
                            ? ""
                            : "none";


                    if (visible) {

                        visibleCount++;
                    }
                }
            );


            updateCatalogCount(
                visibleCount
            );
        }


        categoryButtons.forEach(
            function (button) {

                button.addEventListener(
                    "click",
                    function () {

                        categoryButtons.forEach(
                            function (item) {

                                item.classList.remove(
                                    "active"
                                );
                            }
                        );


                        button.classList.add(
                            "active"
                        );


                        quickCategory =
                            button.dataset.category ||
                            "all";


                        applyFilters();
                    }
                );
            }
        );


        categoryCheckboxes.forEach(
            function (input) {

                input.addEventListener(
                    "change",
                    applyFilters
                );
            }
        );


        priceRadios.forEach(
            function (input) {

                input.addEventListener(
                    "change",
                    applyFilters
                );
            }
        );


        colorButtons.forEach(
            function (button) {

                button.addEventListener(
                    "click",
                    function () {

                        const color =
                            button.dataset.color;


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


        availableFilter?.addEventListener(
            "change",
            applyFilters
        );


        customFilter?.addEventListener(
            "change",
            applyFilters
        );


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

                    button.classList.remove(
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


        /* TRI */

        if (sortProducts) {

            sortProducts.addEventListener(
                "change",
                function () {

                    const value =
                        sortProducts.value;


                    const sorted =
                        [...cards];


                    if (
                        value ===
                        "price-asc"
                    ) {

                        sorted.sort(
                            function (a, b) {

                                return (
                                    Number(
                                        a.dataset.price ||
                                        0
                                    ) -
                                    Number(
                                        b.dataset.price ||
                                        0
                                    )
                                );
                            }
                        );
                    }


                    if (
                        value ===
                        "price-desc"
                    ) {

                        sorted.sort(
                            function (a, b) {

                                return (
                                    Number(
                                        b.dataset.price ||
                                        0
                                    ) -
                                    Number(
                                        a.dataset.price ||
                                        0
                                    )
                                );
                            }
                        );
                    }


                    if (
                        value ===
                        "name"
                    ) {

                        sorted.sort(
                            function (a, b) {

                                return String(
                                    a.dataset.name ||
                                    ""
                                ).localeCompare(
                                    String(
                                        b.dataset.name ||
                                        ""
                                    ),
                                    "fr"
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


        mobileFilterButton?.addEventListener(
            "click",
            function () {

                catalogSidebar
                    ?.classList
                    .add("open");
            }
        );


        closeFilterButton?.addEventListener(
            "click",
            function () {

                catalogSidebar
                    ?.classList
                    .remove("open");
            }
        );


        applyFilters();
    }



    /* =========================================================
       18. IMAGE PREVIEW ADMIN
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
       19. RECHERCHE
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
                            input.value.trim();


                        if (!query) {

                            input.focus();

                            return;
                        }


                        const cards =
                            document.querySelectorAll(
                                ".product-card, " +
                                ".catalog-product-card"
                            );


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
                                            ) +
                                            " " +
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
                                            "smooth"
                                    }
                                );

                        } else {

                            window.location.href =
                                "/salons?search=" +
                                encodeURIComponent(
                                    query
                                );
                        }
                    }
                );
            }
        );



    /* =========================================================
       20. SEARCH URL
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
                            ) +
                            " " +
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
       21. MENU ACTIF
    ========================================================= */

    const currentPath =
        window.location.pathname;


    document
        .querySelectorAll(
            ".main-navigation a"
        )
        .forEach(
            function (link) {

                const href =
                    link.getAttribute(
                        "href"
                    );


                link.classList.remove(
                    "active"
                );


                if (
                    href ===
                    currentPath
                ) {

                    link.classList.add(
                        "active"
                    );
                }
            }
        );



    /* =========================================================
       22. ESCAPE
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


            document
                .getElementById(
                    "catalogSidebar"
                )
                ?.classList
                .remove("open");
        }
    );



    /* =========================================================
       23. INITIALISATION
    ========================================================= */

    updateCartCounter();

    renderCartPage();


    console.log(
        "ZINEB DECO main.js chargé."
    );


    console.log(
        "Panier :",
        cart
    );
/* ============================================================
   PETIT MENU FLOTTANT AU SCROLL
============================================================ */

document.addEventListener(
    "DOMContentLoaded",
    function () {

        const button =
            document.getElementById(
                "zdFloatingMenuButton"
            );

        const menu =
            document.getElementById(
                "zdFloatingMenu"
            );


        if (!button || !menu) {

            return;
        }



        /* =====================================================
           AFFICHER APRES SCROLL
        ===================================================== */

        function updateFloatingMenu() {

            if (window.scrollY > 280) {

                button.classList.add(
                    "visible"
                );

            } else {

                button.classList.remove(
                    "visible"
                );


                button.classList.remove(
                    "open"
                );


                menu.classList.remove(
                    "open"
                );


                button.setAttribute(
                    "aria-expanded",
                    "false"
                );
            }

        }



        window.addEventListener(
            "scroll",
            updateFloatingMenu,
            {
                passive: true
            }
        );


        updateFloatingMenu();



        /* =====================================================
           OUVRIR / FERMER
        ===================================================== */

        button.addEventListener(
            "click",
            function () {

                const opened =
                    menu.classList.toggle(
                        "open"
                    );


                button.classList.toggle(
                    "open",
                    opened
                );


                button.setAttribute(
                    "aria-expanded",
                    opened
                        ? "true"
                        : "false"
                );

            }
        );



        /* =====================================================
           FERMER SI CLIC EN DEHORS
        ===================================================== */

        document.addEventListener(
            "click",
            function (event) {

                if (
                    menu.contains(event.target)
                    ||
                    button.contains(event.target)
                ) {

                    return;
                }


                menu.classList.remove(
                    "open"
                );


                button.classList.remove(
                    "open"
                );


                button.setAttribute(
                    "aria-expanded",
                    "false"
                );

            }
        );



        /* =====================================================
           FERMER APRES CLIC SUR UN LIEN
        ===================================================== */

        menu
            .querySelectorAll("a")
            .forEach(
                function (link) {

                    link.addEventListener(
                        "click",
                        function () {

                            menu.classList.remove(
                                "open"
                            );

                            button.classList.remove(
                                "open"
                            );

                        }
                    );

                }
            );

    }
);
});