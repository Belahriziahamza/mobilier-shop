document.addEventListener(
    "DOMContentLoaded",
    function () {


        /* =====================================================
           MODE CLAIR / SOMBRE
        ===================================================== */

        const themeButton =
            document.getElementById(
                "zdThemeToggle"
            );


        const rootElement =
            document.documentElement;



        function applyTheme(theme) {


            rootElement.setAttribute(
                "data-theme",
                theme
            );


            localStorage.setItem(
                "zd-theme",
                theme
            );


            if (!themeButton) {
                return;
            }


            const darkMode =
                theme === "dark";


            themeButton.setAttribute(
                "aria-pressed",
                String(darkMode)
            );


            themeButton.setAttribute(
                "aria-label",
                darkMode
                    ? "Activer le mode clair"
                    : "Activer le mode sombre"
            );


            themeButton.setAttribute(
                "title",
                darkMode
                    ? "Mode clair"
                    : "Mode sombre"
            );

        }



        const savedTheme =
            localStorage.getItem(
                "zd-theme"
            );


        const initialTheme =
            savedTheme === "dark"
                ? "dark"
                : "light";


        applyTheme(
            initialTheme
        );



        if (themeButton) {


            themeButton.addEventListener(
                "click",
                function () {


                    const currentTheme =
                        rootElement.getAttribute(
                            "data-theme"
                        );


                    if (
                        currentTheme === "dark"
                    ) {

                        applyTheme(
                            "light"
                        );

                    } else {

                        applyTheme(
                            "dark"
                        );

                    }

                }
            );

        }



        /* =====================================================
           NAVIGATION ACTIVE
        ===================================================== */

        const currentPath =
            window.location.pathname;


        document
            .querySelectorAll(
                "#zdPremiumNavigation a"
            )
            .forEach(
                function (link) {


                    const path =
                        link.getAttribute(
                            "data-nav-path"
                        );


                    if (
                        path === "/"
                        &&
                        currentPath === "/"
                    ) {

                        link.classList.add(
                            "active"
                        );

                    }


                    if (
                        path !== "/"
                        &&
                        currentPath.startsWith(
                            path
                        )
                    ) {

                        link.classList.add(
                            "active"
                        );

                    }

                }
            );



        /* =====================================================
           MENU MOBILE
        ===================================================== */

        const mobileButton =
            document.getElementById(
                "zdPremiumMobileButton"
            );


        const mobileMenu =
            document.getElementById(
                "zdPremiumMobileMenu"
            );


        if (
            mobileButton
            &&
            mobileMenu
        ) {


            mobileButton.addEventListener(
                "click",
                function (event) {


                    event.stopPropagation();


                    const open =
                        mobileMenu
                            .classList
                            .toggle(
                                "open"
                            );


                    mobileButton
                        .classList
                        .toggle(
                            "open",
                            open
                        );


                    mobileButton
                        .setAttribute(
                            "aria-expanded",
                            String(open)
                        );

                }
            );

        }



        /* =====================================================
           RECHERCHE
        ===================================================== */

        const searchButton =
            document.getElementById(
                "zdSearchButton"
            );


        const searchPanel =
            document.getElementById(
                "zdPremiumSearchPanel"
            );


        const searchInput =
            document.getElementById(
                "zdPremiumSearchInput"
            );


        const searchSubmit =
            document.getElementById(
                "zdPremiumSearchSubmit"
            );



        if (
            searchButton
            &&
            searchPanel
        ) {


            searchButton.addEventListener(
                "click",
                function (event) {


                    event.stopPropagation();


                    searchPanel
                        .classList
                        .toggle(
                            "open"
                        );


                    if (
                        searchPanel
                            .classList
                            .contains(
                                "open"
                            )
                        &&
                        searchInput
                    ) {


                        setTimeout(
                            function () {

                                searchInput.focus();

                            },
                            100
                        );

                    }

                }
            );

        }



        function executeSearch() {


            if (!searchInput) {
                return;
            }


            const value =
                searchInput
                    .value
                    .trim()
                    .toLowerCase();


            if (!value) {
                return;
            }



            if (
                value.includes(
                    "salon"
                )
            ) {

                window.location.href =
                    "/salons";

                return;

            }



            if (
                value.includes(
                    "canap"
                )
            ) {

                window.location.href =
                    "/canapes";

                return;

            }



            if (
                value.includes(
                    "chambre"
                )
            ) {

                window.location.href =
                    "/chambres";

                return;

            }



            if (
                value.includes("tete")
                ||
                value.includes("tête")
            ) {

                window.location.href =
                    "/tetes-de-lit";

                return;

            }



            if (
                value.includes(
                    "lit"
                )
            ) {

                window.location.href =
                    "/lits";

                return;

            }



            if (
                value.includes("chaise")
                ||
                value.includes("fauteuil")
            ) {

                window.location.href =
                    "/chaises";

                return;

            }



            if (
                value.includes(
                    "table"
                )
            ) {

                window.location.href =
                    "/tables";

                return;

            }



            if (
                value.includes(
                    "promo"
                )
            ) {

                window.location.href =
                    "/promotions";

                return;

            }



            if (
                value.includes(
                    "sur mesure"
                )
            ) {

                window.location.href =
                    "/sur-mesure";

                return;

            }



            window.location.href =
                "/nouveautes";

        }



        if (searchSubmit) {


            searchSubmit.addEventListener(
                "click",
                executeSearch
            );

        }



        if (searchInput) {


            searchInput.addEventListener(
                "keydown",
                function (event) {


                    if (
                        event.key === "Enter"
                    ) {

                        event.preventDefault();

                        executeSearch();

                    }

                }
            );

        }



        /* =====================================================
           MENU FLOTTANT
        ===================================================== */

        const floatingButton =
            document.getElementById(
                "zdFloatingMenuButton"
            );


        const floatingMenu =
            document.getElementById(
                "zdFloatingMenu"
            );



        if (
            floatingButton
            &&
            floatingMenu
        ) {


            function updateFloatingMenu() {


                if (
                    window.scrollY > 250
                ) {


                    floatingButton
                        .classList
                        .add(
                            "visible"
                        );


                } else {


                    floatingButton
                        .classList
                        .remove(
                            "visible"
                        );


                    floatingButton
                        .classList
                        .remove(
                            "open"
                        );


                    floatingMenu
                        .classList
                        .remove(
                            "open"
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



            floatingButton.addEventListener(
                "click",
                function (event) {


                    event.stopPropagation();


                    const open =
                        floatingMenu
                            .classList
                            .toggle(
                                "open"
                            );


                    floatingButton
                        .classList
                        .toggle(
                            "open",
                            open
                        );


                    floatingButton
                        .setAttribute(
                            "aria-expanded",
                            String(open)
                        );

                }
            );



            floatingMenu.addEventListener(
                "click",
                function (event) {

                    event.stopPropagation();

                }
            );


            updateFloatingMenu();

        }



        /* =====================================================
           CLIC EXTERIEUR
        ===================================================== */

        document.addEventListener(
            "click",
            function () {


                if (
                    floatingButton
                    &&
                    floatingMenu
                ) {


                    floatingButton
                        .classList
                        .remove(
                            "open"
                        );


                    floatingMenu
                        .classList
                        .remove(
                            "open"
                        );

                }


                if (searchPanel) {


                    searchPanel
                        .classList
                        .remove(
                            "open"
                        );

                }


                if (
                    mobileButton
                    &&
                    mobileMenu
                ) {


                    mobileButton
                        .classList
                        .remove(
                            "open"
                        );


                    mobileMenu
                        .classList
                        .remove(
                            "open"
                        );


                    mobileButton
                        .setAttribute(
                            "aria-expanded",
                            "false"
                        );

                }

            }
        );



        /* =====================================================
           EVITER FERMETURE RECHERCHE AU CLIC
        ===================================================== */

        if (searchPanel) {


            searchPanel.addEventListener(
                "click",
                function (event) {

                    event.stopPropagation();

                }
            );

        }



        if (mobileMenu) {


            mobileMenu.addEventListener(
                "click",
                function (event) {

                    event.stopPropagation();

                }
            );

        }


    }
);
/* =========================================================
   PANIER CLIENT - COMPTEUR GLOBAL DU HEADER
   Source unique : MySQL via /api/cart
========================================================= */

async function refreshCustomerCartBadge() {

    const counters =
        document.querySelectorAll(".cart-count");


    if (!counters.length) {
        return;
    }


    try {

        const response =
            await fetch("/api/cart", {
                method: "GET",
                credentials: "same-origin",
                headers: {
                    "Accept": "application/json"
                }
            });


        /* =============================================
           CLIENT NON CONNECTE
        ============================================== */

        if (response.status === 401) {

            counters.forEach(counter => {
                counter.textContent = "0";
            });

            return;
        }


        if (!response.ok) {

            throw new Error(
                "Erreur chargement panier"
            );
        }


        const data =
            await response.json();


        const quantity =
            Number(data.totalQuantity || 0);


        counters.forEach(counter => {

            counter.textContent =
                String(quantity);

        });


    } catch (error) {

        console.error(
            "Erreur compteur panier :",
            error
        );

    }

}



/* =========================================================
   RENDRE DISPONIBLE AUX AUTRES SCRIPTS
========================================================= */

window.refreshCustomerCartBadge =
    refreshCustomerCartBadge;



/* =========================================================
   CHARGEMENT DE CHAQUE PAGE
========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    function () {

        refreshCustomerCartBadge();

    }
);



/* =========================================================
   RETOUR NAVIGATEUR / CACHE
========================================================= */

window.addEventListener(
    "pageshow",
    function () {

        refreshCustomerCartBadge();

    }
);



/* =========================================================
   EVENEMENT APRES MODIFICATION PANIER
========================================================= */

window.addEventListener(
    "zineb:cart-updated",
    function () {

        refreshCustomerCartBadge();

    }
);