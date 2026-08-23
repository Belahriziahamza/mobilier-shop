package com.mobilier.shop.config;

import java.util.List;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.mobilier.shop.entity.ShopSetting;
import com.mobilier.shop.repository.ShopSettingRepository;


@ControllerAdvice
public class GlobalShopSettings {


    private final ShopSettingRepository shopSettingRepository;


    public GlobalShopSettings(
            ShopSettingRepository shopSettingRepository
    ) {

        this.shopSettingRepository = shopSettingRepository;
    }



    /* =========================================================
       PARAMETRES DISPONIBLES DANS TOUTES LES PAGES THYMELEAF

       Utilisation HTML :

       ${shopSetting.shopName}
       ${shopSetting.phone}
       ${shopSetting.whatsapp}
       ${shopSetting.email}
       ${shopSetting.address}
       ${shopSetting.instagramUrl}
       ${shopSetting.facebookUrl}
       ${shopSetting.mapsUrl}
    ========================================================= */

    @ModelAttribute("shopSetting")
    public ShopSetting shopSetting() {


        List<ShopSetting> settings =
                shopSettingRepository.findAll();


        /*
         * Aucun paramètre encore enregistré.
         */

        if (settings.isEmpty()) {


            ShopSetting defaultSetting =
                    new ShopSetting();


            defaultSetting.setShopName(
                    "ZINEB DÉCO"
            );


            return defaultSetting;
        }


        /*
         * On utilise la configuration principale.
         */

        return settings.get(0);
    }

}