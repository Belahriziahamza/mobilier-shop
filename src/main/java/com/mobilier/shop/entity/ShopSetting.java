package com.mobilier.shop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "shop_settings")
public class ShopSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_name")
    private String shopName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "whatsapp")
    private String whatsapp;

    @Column(name = "email")
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "instagram_url")
    private String instagramUrl;

    @Column(name = "facebook_url")
    private String facebookUrl;

    @Column(name = "maps_url")
    private String mapsUrl;

    /*
     * =====================================================
     * MODE MAINTENANCE
     * =====================================================
     */

    @Column(name = "maintenance_mode", nullable = false)
    private boolean maintenanceMode = false;

    /*
     * =====================================================
     * GETTERS / SETTERS
     * =====================================================
     */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getInstagramUrl() {
        return instagramUrl;
    }

    public void setInstagramUrl(String instagramUrl) {
        this.instagramUrl = instagramUrl;
    }

    public String getFacebookUrl() {
        return facebookUrl;
    }

    public void setFacebookUrl(String facebookUrl) {
        this.facebookUrl = facebookUrl;
    }

    public String getMapsUrl() {
        return mapsUrl;
    }

    public void setMapsUrl(String mapsUrl) {
        this.mapsUrl = mapsUrl;
    }

    /*
     * =====================================================
     * MAINTENANCE
     * =====================================================
     */

    public boolean isMaintenanceMode() {
        return maintenanceMode;
    }

    public void setMaintenanceMode(
            boolean maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
    }

    /*
     * =====================================================
     * LIEN TELEPHONE
     * =====================================================
     */

    public String getPhoneLink() {

        if (phone == null
                ||
                phone.isBlank()) {

            return null;
        }

        String cleanPhone = phone.replaceAll(
                "[^0-9+]",
                "");

        return "tel:" + cleanPhone;
    }

    /*
     * =====================================================
     * LIEN WHATSAPP
     * =====================================================
     */

    public String getWhatsappLink() {

        if (whatsapp == null
                ||
                whatsapp.isBlank()) {

            return null;
        }

        String cleanWhatsapp = whatsapp.replaceAll(
                "[^0-9]",
                "");

        return "https://wa.me/"
                + cleanWhatsapp;
    }

}