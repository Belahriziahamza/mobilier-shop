package com.mobilier.shop.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mobilier.shop.entity.Piece;
import com.mobilier.shop.service.PieceService;


@Controller
@RequestMapping("/admin/pieces")
public class AdminPieceController {


    /* =========================================================
       SERVICE
    ========================================================= */

    private final PieceService pieceService;



    /* =========================================================
       CONSTRUCTEUR
    ========================================================= */

    public AdminPieceController(
            PieceService pieceService
    ) {

        this.pieceService =
                pieceService;
    }



    /* =========================================================
       PAGE PRINCIPALE
       LISTE + RECHERCHE + FILTRES
    ========================================================= */

    @GetMapping
    public String pieces(

            @RequestParam(
                    value = "search",
                    required = false,
                    defaultValue = ""
            )
            String search,

            @RequestParam(
                    value = "catalogue",
                    required = false,
                    defaultValue = ""
            )
            String catalogue,

            @RequestParam(
                    value = "availability",
                    required = false,
                    defaultValue = ""
            )
            String availability,

            Model model
    ) {


        Piece formPiece =
                new Piece();


        formPiece.setAvailable(
                true
        );


        loadPage(
                model,
                search,
                catalogue,
                availability,
                formPiece,
                false
        );


        return "admin/pieces";
    }



    /* =========================================================
       MODIFIER UNE PIECE
    ========================================================= */

    @GetMapping("/modifier/{id}")
    public String editPiece(

            @PathVariable("id")
            Long id,

            @RequestParam(
                    value = "search",
                    required = false,
                    defaultValue = ""
            )
            String search,

            @RequestParam(
                    value = "catalogue",
                    required = false,
                    defaultValue = ""
            )
            String catalogue,

            @RequestParam(
                    value = "availability",
                    required = false,
                    defaultValue = ""
            )
            String availability,

            Model model,

            RedirectAttributes redirectAttributes
    ) {


        try {


            Piece piece =
                    pieceService.findById(
                            id
                    );


            loadPage(
                    model,
                    search,
                    catalogue,
                    availability,
                    piece,
                    true
            );


            return "admin/pieces";


        } catch (IllegalArgumentException e) {


            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            e.getMessage()
                    );


            return "redirect:/admin/pieces";
        }
    }



    /* =========================================================
       AJOUTER UNE PIECE
    ========================================================= */

    @PostMapping("/ajouter")
    public String addPiece(

            @RequestParam("catalogue")
            String catalogue,

            @RequestParam("name")
            String name,

            @RequestParam("type")
            String type,

            @RequestParam("price")
            java.math.BigDecimal price,

            @RequestParam(
                    value = "description",
                    required = false
            )
            String description,

            @RequestParam(
                    value = "available",
                    required = false,
                    defaultValue = "false"
            )
            boolean available,

            @RequestParam(
                    value = "image",
                    required = false
            )
            MultipartFile image,

            RedirectAttributes redirectAttributes
    ) {


        try {


            Piece piece =
                    new Piece();


            piece.setCatalogue(
                    catalogue
            );


            piece.setName(
                    name
            );


            piece.setType(
                    type
            );


            piece.setPrice(
                    price
            );


            piece.setDescription(
                    description
            );


            piece.setAvailable(
                    available
            );


            Piece savedPiece =
                    pieceService.create(
                            piece,
                            image
                    );


            redirectAttributes
                    .addFlashAttribute(
                            "success",
                            "La pièce \""
                                    + savedPiece.getName()
                                    + "\" a été ajoutée avec succès."
                    );


        } catch (Exception e) {


            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            getErrorMessage(
                                    e
                            )
                    );

        }


        return "redirect:/admin/pieces";
    }



    /* =========================================================
       ENREGISTRER MODIFICATION
    ========================================================= */

    @PostMapping("/modifier/{id}")
    public String updatePiece(

            @PathVariable("id")
            Long id,

            @RequestParam("catalogue")
            String catalogue,

            @RequestParam("name")
            String name,

            @RequestParam("type")
            String type,

            @RequestParam("price")
            java.math.BigDecimal price,

            @RequestParam(
                    value = "description",
                    required = false
            )
            String description,

            @RequestParam(
                    value = "available",
                    required = false,
                    defaultValue = "false"
            )
            boolean available,

            @RequestParam(
                    value = "image",
                    required = false
            )
            MultipartFile image,

            RedirectAttributes redirectAttributes
    ) {


        try {


            Piece formPiece =
                    new Piece();


            formPiece.setCatalogue(
                    catalogue
            );


            formPiece.setName(
                    name
            );


            formPiece.setType(
                    type
            );


            formPiece.setPrice(
                    price
            );


            formPiece.setDescription(
                    description
            );


            formPiece.setAvailable(
                    available
            );


            Piece updatedPiece =
                    pieceService.update(
                            id,
                            formPiece,
                            image
                    );


            redirectAttributes
                    .addFlashAttribute(
                            "success",
                            "La pièce \""
                                    + updatedPiece.getName()
                                    + "\" a été modifiée avec succès."
                    );


        } catch (Exception e) {


            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            getErrorMessage(
                                    e
                            )
                    );

        }


        return "redirect:/admin/pieces";
    }



    /* =========================================================
       DISPONIBLE / NON DISPONIBLE
    ========================================================= */

    @PostMapping("/{id}/disponibilite")
    public String toggleAvailability(

            @PathVariable("id")
            Long id,

            RedirectAttributes redirectAttributes
    ) {


        try {


            Piece piece =
                    pieceService
                            .toggleAvailability(
                                    id
                            );


            String message =
                    piece.isAvailable()
                            ? "La pièce est maintenant disponible."
                            : "La pièce est maintenant non disponible.";


            redirectAttributes
                    .addFlashAttribute(
                            "success",
                            message
                    );


        } catch (Exception e) {


            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            getErrorMessage(
                                    e
                            )
                    );

        }


        return "redirect:/admin/pieces";
    }



    /* =========================================================
       SUPPRIMER
    ========================================================= */

    @PostMapping("/{id}/supprimer")
    public String deletePiece(

            @PathVariable("id")
            Long id,

            RedirectAttributes redirectAttributes
    ) {


        try {


            Piece piece =
                    pieceService.findById(
                            id
                    );


            String pieceName =
                    piece.getName();


            pieceService.delete(
                    id
            );


            redirectAttributes
                    .addFlashAttribute(
                            "success",
                            "La pièce \""
                                    + pieceName
                                    + "\" a été supprimée."
                    );


        } catch (Exception e) {


            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            getErrorMessage(
                                    e
                            )
                    );

        }


        return "redirect:/admin/pieces";
    }



    /* =========================================================
       CHARGER TOUTES LES DONNEES DE LA PAGE
    ========================================================= */

    private void loadPage(

            Model model,

            String search,

            String catalogue,

            String availability,

            Piece formPiece,

            boolean editing
    ) {


        /* =====================================================
           LISTE FILTREE
        ===================================================== */

        List<Piece> pieces =
                pieceService.search(
                        search,
                        catalogue,
                        availability
                );


        model.addAttribute(
                "pieces",
                pieces
        );


        model.addAttribute(
                "formPiece",
                formPiece
        );


        model.addAttribute(
                "editing",
                editing
        );



        /* =====================================================
           FILTRES
        ===================================================== */

        model.addAttribute(
                "search",
                search == null
                        ? ""
                        : search
        );


        model.addAttribute(
                "selectedCatalogue",
                catalogue == null
                        ? ""
                        : catalogue
        );


        model.addAttribute(
                "selectedAvailability",
                availability == null
                        ? ""
                        : availability
        );



        /* =====================================================
           STATS GENERALES
        ===================================================== */

        model.addAttribute(
                "totalPieces",
                pieceService.countAll()
        );


        model.addAttribute(
                "availablePieces",
                pieceService.countAvailable()
        );


        model.addAttribute(
                "resultCount",
                pieces.size()
        );



        /* =====================================================
           TAPISSERIE
        ===================================================== */

        model.addAttribute(
                "tapisserieCount",
                pieceService.countByCatalogue(
                        PieceService.CATALOGUE_TAPISSERIE
                )
        );


        model.addAttribute(
                "tapisserieAvailableCount",
                pieceService.countAvailableByCatalogue(
                        PieceService.CATALOGUE_TAPISSERIE
                )
        );



        /* =====================================================
           COUTURE
        ===================================================== */

        model.addAttribute(
                "coutureCount",
                pieceService.countByCatalogue(
                        PieceService.CATALOGUE_COUTURE
                )
        );


        model.addAttribute(
                "coutureAvailableCount",
                pieceService.countAvailableByCatalogue(
                        PieceService.CATALOGUE_COUTURE
                )
        );



        /* =====================================================
           MATIERES / TISSUS
        ===================================================== */

        model.addAttribute(
                "matieresTissusCount",
                pieceService.countByCatalogue(
                        PieceService.CATALOGUE_MATIERES_TISSUS
                )
        );


        model.addAttribute(
                "matieresTissusAvailableCount",
                pieceService.countAvailableByCatalogue(
                        PieceService.CATALOGUE_MATIERES_TISSUS
                )
        );

    }



    /* =========================================================
       MESSAGE ERREUR PROPRE
    ========================================================= */

    private String getErrorMessage(
            Exception e
    ) {


        if (
                e.getMessage() == null
                ||
                e.getMessage().isBlank()
        ) {

            return "Une erreur est survenue.";
        }


        return e.getMessage();
    }

}