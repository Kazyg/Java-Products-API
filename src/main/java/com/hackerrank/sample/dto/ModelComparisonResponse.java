package com.hackerrank.sample.dto;

import java.math.BigDecimal;
import java.util.List;

public class ModelComparisonResponse {
    @com.fasterxml.jackson.annotation.JsonProperty("products")
    private List<ModelResponseDto> models;
    private BigDecimal lowestPrice;
    private BigDecimal highestPrice;
    private BigDecimal averagePrice;
    private int lowestRating;
    private int highestRating;
    private double averageRating;

    public ModelComparisonResponse() {
    }

    public List<ModelResponseDto> getModels() {
        return models;
    }

    public void setModels(List<ModelResponseDto> models) {
        this.models = models;
    }

    public BigDecimal getLowestPrice() {
        return lowestPrice;
    }

    public void setLowestPrice(BigDecimal lowestPrice) {
        this.lowestPrice = lowestPrice;
    }

    public BigDecimal getHighestPrice() {
        return highestPrice;
    }

    public void setHighestPrice(BigDecimal highestPrice) {
        this.highestPrice = highestPrice;
    }

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }

    public int getLowestRating() {
        return lowestRating;
    }

    public void setLowestRating(int lowestRating) {
        this.lowestRating = lowestRating;
    }

    public int getHighestRating() {
        return highestRating;
    }

    public void setHighestRating(int highestRating) {
        this.highestRating = highestRating;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }
}
