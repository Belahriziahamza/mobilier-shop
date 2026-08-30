package com.mobilier.shop.controller;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mobilier.shop.entity.Couleur;
import com.mobilier.shop.entity.Tissu;

import com.mobilier.shop.service.CouleurService;
import com.mobilier.shop.service.TissuService;


@Controller
@RequestMapping("/admin")
public class AdminTissuCouleurController {


    private final TissuService tissuService;

    private final CouleurService couleurService;



    public AdminTissuCouleurController(

            TissuService tissuService,

            CouleurService couleurService
    ) {

        this.tissuService =
                tissuService;

        this.couleurService =
                couleurService;
    }



    /* =====================================================
       PAGE PRINCIPALE
    ===================================================== */

    @GetMapping("/tissus-couleurs")
    public String page(

            @RequestParam(
                    required = false
            )
            Long editTissu,

            @RequestParam(
                    required = false
            )
            Long editCouleur,

            Model model
    ) {


        model.addAttribute(
                "tissus",
                tissuService.findAll()
        );


        model.addAttribute(
                "couleurs",
                couleurService.findAll()
        );



        /* STATS */

        model.addAttribute(
                "totalTissus",
                tissuService.countAll()
        );


        model.addAttribute(
                "activeTissus",
                tissuService.countActive()
        );


        model.addAttribute(
                "totalCouleurs",
                couleurService.countAll()
        );


        model.addAttribute(
                "activeCouleurs",
                couleurService.countActive()
        );



        /* FORM TISSU */

        if (editTissu != null) {

            model.addAttribute(
                    "tissuForm",
                    tissuService.findById(
                            editTissu
                    )
            );

        } else {

            Tissu form =
                    new Tissu();

            form.setActive(true);
            form.setAvailable(true);


            model.addAttribute(
                    "tissuForm",
                    form
            );

        }



        /* FORM COULEUR */

        if (editCouleur != null) {

            model.addAttribute(
                    "couleurForm",
                    couleurService.findById(
                            editCouleur
                    )
            );

        } else {

            Couleur form =
                    new Couleur();

            form.setActive(true);
            form.setAvailable(true);

            form.setCodeHex(
                    "#D8C3A5"
            );


            model.addAttribute(
                    "couleurForm",
                    form
            );

        }


        return "admin/tissus-couleurs";
    }



    /* =====================================================
       AJOUT TISSU
    ===================================================== */

    @PostMapping("/tissus/ajouter")
    public String addTissu(

            @ModelAttribute
            Tissu tissu,

            @RequestParam(
                    name = "image",
                    required = false
            )
            MultipartFile image,

            RedirectAttributes redirectAttributes
    ) {


        try {


            tissuService.create(
                    tissu,
                    image
            );


            redirectAttributes
                    .addFlashAttribute(
                            "success",
                            "Tissu ajouté avec succès."
                    );


        } catch (IllegalArgumentException e) {


            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            e.getMessage()
                    );

        }


        return "redirect:/admin/tissus-couleurs";
    }



    /* =====================================================
       MODIFIER TISSU
    ===================================================== */

    @PostMapping(
            "/tissus/{id}/modifier"
    )
    public String updateTissu(

            @PathVariable
            Long id,

            @ModelAttribute
            Tissu tissu,

            @RequestParam(
                    name = "image",
                    required = false
            )
            MultipartFile image,

            RedirectAttributes redirectAttributes
    ) {


        try {


            tissuService.update(
                    id,
                    tissu,
                    image
            );


            redirectAttributes
                    .addFlashAttribute(
                            "success",
                            "Tissu modifié avec succès."
                    );


        } catch (IllegalArgumentException e) {


            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            e.getMessage()
                    );

        }


        return "redirect:/admin/tissus-couleurs";
    }



    /* =====================================================
       STATUT TISSU
    ===================================================== */

    @PostMapping(
            "/tissus/{id}/statut"
    )
    public String toggleTissu(

            @PathVariable
            Long id,

            RedirectAttributes redirectAttributes
    ) {


        try {


            Tissu tissu =
                    tissuService.toggleStatus(
                            id
                    );


            redirectAttributes
                    .addFlashAttribute(
                            "success",
                            tissu.isActive()
                                    ? "Tissu activé avec succès."
                                    : "Tissu désactivé avec succès."
                    );


        } catch (IllegalArgumentException e) {


            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            e.getMessage()
                    );

        }


        return "redirect:/admin/tissus-couleurs";
    }



    /* =====================================================
       DELETE TISSU
    ===================================================== */

    @PostMapping(
            "/tissus/{id}/supprimer"
    )
    public String deleteTissu(

            @PathVariable
            Long id,

            RedirectAttributes redirectAttributes
    ) {


        try {


            tissuService.delete(
                    id
            );


            redirectAttributes
                    .addFlashAttribute(
                            "success",
                            "Tissu supprimé avec succès."
                    );


        } catch (IllegalArgumentException e) {


            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            e.getMessage()
                    );

        }


        return "redirect:/admin/tissus-couleurs";
    }



    /* =====================================================
       AJOUT COULEUR
    ===================================================== */

    @PostMapping(
            "/couleurs/ajouter"
    )
    public String addCouleur(

            @ModelAttribute
            Couleur couleur,

            RedirectAttributes redirectAttributes
    ) {


        try {


            couleurService.create(
                    couleur
            );


            redirectAttributes
                    .addFlashAttribute(
                            "success",
                            "Couleur ajoutée avec succès."
                    );


        } catch (IllegalArgumentException e) {


            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            e.getMessage()
                    );

        }


        return "redirect:/admin/tissus-couleurs";
    }



    /* =====================================================
       MODIFIER COULEUR
    ===================================================== */

    @PostMapping(
            "/couleurs/{id}/modifier"
    )
    public String updateCouleur(

            @PathVariable
            Long id,

            @ModelAttribute
            Couleur couleur,

            RedirectAttributes redirectAttributes
    ) {


        try {


            couleurService.update(
                    id,
                    couleur
            );


            redirectAttributes
                    .addFlashAttribute(
                            "success",
                            "Couleur modifiée avec succès."
                    );


        } catch (IllegalArgumentException e) {


            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            e.getMessage()
                    );

        }


        return "redirect:/admin/tissus-couleurs";
    }



    /* =====================================================
       STATUT COULEUR
    ===================================================== */

    @PostMapping(
            "/couleurs/{id}/statut"
    )
    public String toggleCouleur(

            @PathVariable
            Long id,

            RedirectAttributes redirectAttributes
    ) {


        try {


            Couleur couleur =
                    couleurService.toggleStatus(
                            id
                    );


            redirectAttributes
                    .addFlashAttribute(
                            "success",
                            couleur.isActive()
                                    ? "Couleur activée avec succès."
                                    : "Couleur désactivée avec succès."
                    );


        } catch (IllegalArgumentException e) {


            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            e.getMessage()
                    );

        }


        return "redirect:/admin/tissus-couleurs";
    }



    /* =====================================================
       DELETE COULEUR
    ===================================================== */

    @PostMapping(
            "/couleurs/{id}/supprimer"
    )
    public String deleteCouleur(

            @PathVariable
            Long id,

            RedirectAttributes redirectAttributes
    ) {


        try {


            couleurService.delete(
                    id
            );


            redirectAttributes
                    .addFlashAttribute(
                            "success",
                            "Couleur supprimée avec succès."
                    );


        } catch (IllegalArgumentException e) {


            redirectAttributes
                    .addFlashAttribute(
                            "error",
                            e.getMessage()
                    );

        }


        return "redirect:/admin/tissus-couleurs";
    }

}