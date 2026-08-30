(function () {


    /* =====================================================
       EVITER DOUBLE INITIALISATION
    ===================================================== */

    if (window.__zinebAdminThemeLoaded) {
        return;
    }

    window.__zinebAdminThemeLoaded = true;



    const root =
        document.documentElement;



    /* =====================================================
       THEME SAUVEGARDE
    ===================================================== */

    const savedTheme =
        localStorage.getItem(
            "zd-admin-theme"
        );


    const initialTheme =
        savedTheme === "dark"
            ? "dark"
            : "light";


    root.setAttribute(
        "data-admin-theme",
        initialTheme
    );



    /* =====================================================
       UPDATE TOUS LES BOUTONS
    ===================================================== */

    function updateButtons(theme) {


        const buttons =
            document.querySelectorAll(
                "[data-admin-theme-toggle]"
            );


        buttons.forEach(
            function (button) {


                const icon =
                    button.querySelector(
                        ".admin-theme-icon"
                    );


                const text =
                    button.querySelector(
                        ".admin-theme-text"
                    );


                if (theme === "dark") {


                    if (icon) {
                        icon.textContent = "☀";
                    }


                    if (text) {
                        text.textContent = "Mode clair";
                    }


                    button.setAttribute(
                        "aria-label",
                        "Activer le mode clair"
                    );


                } else {


                    if (icon) {
                        icon.textContent = "☾";
                    }


                    if (text) {
                        text.textContent = "Mode sombre";
                    }


                    button.setAttribute(
                        "aria-label",
                        "Activer le mode sombre"
                    );

                }


            }
        );

    }



    /* =====================================================
       CHANGER THEME
    ===================================================== */

    function changeTheme() {


        const current =
            root.getAttribute(
                "data-admin-theme"
            );


        const next =
            current === "dark"
                ? "light"
                : "dark";


        root.setAttribute(
            "data-admin-theme",
            next
        );


        localStorage.setItem(
            "zd-admin-theme",
            next
        );


        updateButtons(next);

    }



    /* =====================================================
       DOM
    ===================================================== */

    function initializeButtons() {


        updateButtons(
            root.getAttribute(
                "data-admin-theme"
            )
        );


        const buttons =
            document.querySelectorAll(
                "[data-admin-theme-toggle]"
            );


        buttons.forEach(
            function (button) {


                if (
                    button.dataset.themeReady
                    ===
                    "true"
                ) {

                    return;
                }


                button.dataset.themeReady =
                    "true";


                button.addEventListener(
                    "click",
                    changeTheme
                );


            }
        );

    }



    if (
        document.readyState
        ===
        "loading"
    ) {


        document.addEventListener(
            "DOMContentLoaded",
            initializeButtons
        );


    } else {


        initializeButtons();

    }


})();