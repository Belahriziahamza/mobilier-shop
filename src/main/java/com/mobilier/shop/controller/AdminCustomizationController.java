package com.mobilier.shop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.mobilier.shop.entity.CustomColor;
import com.mobilier.shop.entity.Fabric;
import com.mobilier.shop.repository.CustomColorRepository;
import com.mobilier.shop.repository.FabricRepository;

@Controller
@RequestMapping("/admin/customization")
public class AdminCustomizationController {

    private final FabricRepository fabricRepository;
    private final CustomColorRepository customColorRepository;

    public AdminCustomizationController(
            FabricRepository fabricRepository,
            CustomColorRepository customColorRepository) {
        this.fabricRepository = fabricRepository;
        this.customColorRepository = customColorRepository;
    }

    /*
     * =====================================================
     * LISTE
     * =====================================================
     */

    @GetMapping
    public String index(Model model) {

        model.addAttribute(
                "fabrics",
                fabricRepository.findAll());

        model.addAttribute(
                "colors",
                customColorRepository.findAll());

        return "admin/customization";
    }

    /*
     * =====================================================
     * AJOUT TISSU
     * =====================================================
     */

    @PostMapping("/fabrics/add")
    public String addFabric(
            @RequestParam String name,
            @RequestParam(required = false) String referenceCode,
            @RequestParam(required = false) String description) {

        Fabric fabric = new Fabric();

        fabric.setName(name);
        fabric.setReferenceCode(referenceCode);
        fabric.setDescription(description);
        fabric.setActive(true);

        fabricRepository.save(fabric);

        return "redirect:/admin/customization";
    }

    /*
     * =====================================================
     * ACTIVER / DESACTIVER TISSU
     * =====================================================
     */

    @PostMapping("/fabrics/{id}/toggle")
    public String toggleFabric(
            @PathVariable Long id) {

        Fabric fabric = fabricRepository
                .findById(id)
                .orElseThrow();

        fabric.setActive(
                !fabric.isActive());

        fabricRepository.save(fabric);

        return "redirect:/admin/customization";
    }

    /*
     * =====================================================
     * SUPPRIMER TISSU
     * =====================================================
     */

    @PostMapping("/fabrics/{id}/delete")
    public String deleteFabric(
            @PathVariable Long id) {

        fabricRepository.deleteById(id);

        return "redirect:/admin/customization";
    }

    /*
     * =====================================================
     * AJOUT COULEUR
     * =====================================================
     */

    @PostMapping("/colors/add")
    public String addColor(
            @RequestParam String name,
            @RequestParam String hexCode) {

        CustomColor color = new CustomColor();

        color.setName(name);
        color.setHexCode(hexCode);
        color.setActive(true);

        customColorRepository.save(color);

        return "redirect:/admin/customization";
    }

    /*
     * =====================================================
     * ACTIVER / DESACTIVER COULEUR
     * =====================================================
     */

    @PostMapping("/colors/{id}/toggle")
    public String toggleColor(
            @PathVariable Long id) {

        CustomColor color = customColorRepository
                .findById(id)
                .orElseThrow();

        color.setActive(
                !color.isActive());

        customColorRepository.save(color);

        return "redirect:/admin/customization";
    }

    /*
     * =====================================================
     * SUPPRIMER COULEUR
     * =====================================================
     */

    @PostMapping("/colors/{id}/delete")
    public String deleteColor(
            @PathVariable Long id) {

        customColorRepository.deleteById(id);

        return "redirect:/admin/customization";
    }
}