package com.mobilier.shop.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mobilier.shop.entity.Piece;
import com.mobilier.shop.service.PieceService;


@Controller
public class PieceCatalogController {


    private final PieceService pieceService;



    public PieceCatalogController(
            PieceService pieceService
    ) {

        this.pieceService =
                pieceService;
    }



    /* =========================================================
       CATALOGUE CLIENT DES PIECES

       /pieces
       /pieces?catalogue=TAPISSERIE
       /pieces?catalogue=COUTURE
       /pieces?catalogue=MATIERES_TISSUS
    ========================================================= */

    @GetMapping("/pieces")
    public String pieces(

            @RequestParam(
                    value = "catalogue",
                    required = false,
                    defaultValue = ""
            )
            String catalogue,

            Model model
    ) {


        String selectedCatalogue =
                catalogue == null
                        ? ""
                        : catalogue
                                .trim()
                                .toUpperCase(
                                        Locale.ROOT
                                );



        List<Piece> pieces;



        /* =====================================================
           AUCUN FILTRE
           → toutes les pièces disponibles
        ===================================================== */

        if (
                selectedCatalogue.isBlank()
        ) {


            pieces =
                    pieceService.findAvailable();


        } else {


            /* =================================================
               SECURISER LE CATALOGUE
            ================================================= */

            if (
                    !PieceService.CATALOGUE_TAPISSERIE
                            .equals(selectedCatalogue)

                    &&

                    !PieceService.CATALOGUE_COUTURE
                            .equals(selectedCatalogue)

                    &&

                    !PieceService.CATALOGUE_MATIERES_TISSUS
                            .equals(selectedCatalogue)
            ) {


                selectedCatalogue =
                        "";


                pieces =
                        pieceService.findAvailable();


            } else {


                pieces =
                        pieceService
                                .findAvailableByCatalogue(
                                        selectedCatalogue
                                );

            }

        }



        /* =====================================================
           LISTE
        ===================================================== */

        model.addAttribute(
                "pieces",
                pieces
        );


        model.addAttribute(
                "selectedCatalogue",
                selectedCatalogue
        );


        model.addAttribute(
                "resultCount",
                pieces.size()
        );



        /* =====================================================
           COMPTEURS DES 3 CATALOGUES
        ===================================================== */

        model.addAttribute(
                "tapisserieCount",
                pieceService
                        .countAvailableByCatalogue(
                                PieceService.CATALOGUE_TAPISSERIE
                        )
        );


        model.addAttribute(
                "coutureCount",
                pieceService
                        .countAvailableByCatalogue(
                                PieceService.CATALOGUE_COUTURE
                        )
        );


        model.addAttribute(
                "matieresTissusCount",
                pieceService
                        .countAvailableByCatalogue(
                                PieceService.CATALOGUE_MATIERES_TISSUS
                        )
        );



        /* =====================================================
           TITRE DU CATALOGUE
        ===================================================== */

        model.addAttribute(
                "catalogueTitle",
                catalogueTitle(
                        selectedCatalogue
                )
        );


        return "pieces";
    }



    /* =========================================================
       TITRE
    ========================================================= */

    private String catalogueTitle(
            String catalogue
    ) {


        if (
                catalogue == null
                ||
                catalogue.isBlank()
        ) {

            return "Toutes les pièces";
        }


        return switch (
                catalogue
        ) {


            case PieceService.CATALOGUE_TAPISSERIE ->
                    "Pièces Tapissier";


            case PieceService.CATALOGUE_COUTURE ->
                    "Pièces Couture";


            case PieceService.CATALOGUE_MATIERES_TISSUS ->
                    "Matières & Tissus";


            default ->
                    "Toutes les pièces";

        };

    }

}