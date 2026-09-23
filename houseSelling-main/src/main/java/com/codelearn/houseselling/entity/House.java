package com.codelearn.houseselling.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
public class House {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long houseId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    private Double price;

    @Column(length = 30)
    private String status = "AVAILABLE";

    @NotNull(message = "Bedrooms is required")
    @Positive(message = "Bedrooms must be greater than zero")
    private Integer bedrooms;

    @NotNull(message = "Bathrooms is required")
    @Positive(message = "Bathrooms must be greater than zero")
    private Integer bathrooms;

    @NotNull(message = "Halls are required")
    @PositiveOrZero(message = "Halls cannot be negative")
    private Integer halls;

    @NotNull(message = "Kitchens are required")
    @PositiveOrZero(message = "Kitchens cannot be negative")
    private Integer kitchens;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String image;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private Seller seller;

    public Long getHouseId() {
        return houseId;
    }

    public void setHouseId(Long houseId) {
        this.houseId = houseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(Integer bedrooms) {
        this.bedrooms = bedrooms;
    }

    public Integer getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(Integer bathrooms) {
        this.bathrooms = bathrooms;
    }

    public Integer getHalls() { return halls; }
    public void setHalls(Integer halls) { this.halls = halls; }

    public Integer getKitchens() { return kitchens; }
    public void setKitchens(Integer kitchens) { this.kitchens = kitchens; }

    public Seller getSeller() {
        return seller;
    }

    public void setSeller(Seller seller) {
        this.seller = seller;
    }
}