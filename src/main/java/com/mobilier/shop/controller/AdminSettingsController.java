package com.mobilier.shop.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mobilier.shop.entity.ShopSetting;
import com.mobilier.shop.repository.ShopSettingRepository;

@Controller
@RequestMapping("/admin/parametres")
public class AdminSettingsController {

        private final ShopSettingRepository shopSettingRepository;

        public AdminSettingsController(
                        ShopSettingRepository shopSettingRepository) {

                this.shopSettingRepository = shopSettingRepository;
        }

        /*
         * =========================================================
         * PAGE PARAMETRES
         * =========================================================
         */

        @GetMapping
        public String settings(
                        Model model) {

                ShopSetting shopSetting = getShopSetting();

                model.addAttribute(
                                "shopSetting",
                                shopSetting);

                return "admin/settings";
        }

        /*
         * =========================================================
         * ENREGISTRER LES INFORMATIONS
         * =========================================================
         */

        @PostMapping("/save")
        public String save(

                        @ModelAttribute("shopSetting") ShopSetting form,

                        RedirectAttributes redirectAttributes) {

                ShopSetting setting = getShopSetting();

                /*
                 * =====================================================
                 * INFORMATIONS BOUTIQUE
                 * =====================================================
                 */

                setting.setShopName(
                                clean(
                                                form.getShopName()));

                setting.setPhone(
                                clean(
                                                form.getPhone()));

                setting.setWhatsapp(
                                clean(
                                                form.getWhatsapp()));

                setting.setEmail(
                                clean(
                                                form.getEmail()));

                setting.setAddress(
                                clean(
                                                form.getAddress()));

                setting.setInstagramUrl(
                                clean(
                                                form.getInstagramUrl()));

                setting.setFacebookUrl(
                                clean(
                                                form.getFacebookUrl()));

                setting.setMapsUrl(
                                clean(
                                                form.getMapsUrl()));

                /*
                 * IMPORTANT :
                 *
                 * On ne touche PAS à maintenanceMode ici.
                 *
                 * Le bouton maintenance possède
                 * sa propre route indépendante.
                 */

                shopSettingRepository.save(
                                setting);

                redirectAttributes
                                .addFlashAttribute(
                                                "success",
                                                "Les paramètres de la boutique ont été enregistrés.");

                return "redirect:/admin/parametres";
        }

        /*
         * =========================================================
         * MODE MAINTENANCE ON / OFF
         * =========================================================
         */

        @PostMapping("/maintenance/toggle")
        public String toggleMaintenance(
                        RedirectAttributes redirectAttributes) {

                ShopSetting setting = getShopSetting();

                boolean newStatus = !setting.isMaintenanceMode();

                setting.setMaintenanceMode(
                                newStatus);

                shopSettingRepository.save(
                                setting);

                /*
                 * =====================================================
                 * MESSAGE ADMIN
                 * =====================================================
                 */

                if (newStatus) {

                        redirectAttributes
                                        .addFlashAttribute(
                                                        "success",
                                                        "Mode maintenance activé. La boutique publique est maintenant indisponible pour les visiteurs.");

                } else {

                        redirectAttributes
                                        .addFlashAttribute(
                                                        "success",
                                                        "Mode maintenance désactivé. La boutique est de nouveau accessible.");

                }

                return "redirect:/admin/parametres";
        }

        /*
         * =========================================================
         * RECUPERER LA CONFIGURATION
         * =========================================================
         */

        private ShopSetting getShopSetting() {

                List<ShopSetting> settings = shopSettingRepository
                                .findAll();

                if (!settings.isEmpty()) {

                        return settings.get(0);

                }

                ShopSetting setting = new ShopSetting();

                setting.setShopName(
                                "ZINEB DÉCO");

                setting.setMaintenanceMode(
                                false);

                return shopSettingRepository.save(
                                setting);
        }

        /*
         * =========================================================
         * NETTOYAGE
         * =========================================================
         */

        private String clean(
                        String value) {

                if (value == null) {

                        return "";

                }

                return value.trim();
        }

}