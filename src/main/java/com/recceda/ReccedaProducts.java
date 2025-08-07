package com.recceda;

import java.util.List;

import com.recceda.entity.Product;

import lombok.Getter;

@Getter
public final class ReccedaProducts {
    private static final List<Product> PRODUCTS = List.of(
        new Product("1", "Reccede One", "Payfast example one", 299.99),
        new Product("2", "Recceda Two", "Payfast example two", 219.99),
        new Product("3", "Recceda Three", "Payfast example three", 379.99)
    );

    public static List<Product> getProducts() {
        return PRODUCTS;
    }

    public static Product getProductById(String productId) {
        return PRODUCTS.stream()
                .filter(product -> product.getProductId().equals(productId))
                .findFirst()
                .orElse(null);
    }
}
