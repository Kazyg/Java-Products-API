package com.hackerrank.sample.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CreateModelRequest {
    @NotBlank(message = "name must not be blank")
    private String name;

    private String urlImage;

    @NotBlank(message = "description must not be blank")
    private String description;

    @NotNull(message = "price must not be null")
    @DecimalMin(value = "0.01", message = "price must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "price must have up to 2 decimal places")
    private BigDecimal price;

    @Min(value = 1, message = "rating must be between 1 and 5")
    @Max(value = 5, message = "rating must be between 1 and 5")
    private int rating;

    @NotBlank(message = "specifications must not be blank")
    private String specifications;

    public CreateModelRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }
}
