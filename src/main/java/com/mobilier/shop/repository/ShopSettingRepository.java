package com.mobilier.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilier.shop.entity.ShopSetting;

public interface ShopSettingRepository
        extends JpaRepository<ShopSetting, Long> {

}