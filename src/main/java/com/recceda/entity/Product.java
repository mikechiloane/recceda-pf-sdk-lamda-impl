package com.recceda.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Product entity representing items available for purchase
 */
@Data
@AllArgsConstructor
public class Product {
    @JsonProperty("productId")
    private final String productId;
    
    @JsonProperty("name")
    private final String name;
    
    @JsonProperty("description")
    private final String description;
    
    @JsonProperty("price")
    private final double price;
}
