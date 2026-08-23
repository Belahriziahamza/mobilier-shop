package com.mobilier.shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilier.shop.entity.CustomColor;

public interface CustomColorRepository
        extends JpaRepository<CustomColor, Long> {

    List<CustomColor> findByActiveTrueOrderByNameAsc();

}