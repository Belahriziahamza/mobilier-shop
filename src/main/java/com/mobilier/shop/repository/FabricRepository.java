package com.mobilier.shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilier.shop.entity.Fabric;

public interface FabricRepository
        extends JpaRepository<Fabric, Long> {

    List<Fabric> findByActiveTrueOrderByNameAsc();

}