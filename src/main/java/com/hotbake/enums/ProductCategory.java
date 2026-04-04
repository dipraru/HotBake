package com.hotbake.enums;

public enum ProductCategory {
    CUPCAKE("Cupcake"),
    TUB_CAKE("Tub Cake"),
    VANILLA_CAKE("Vanilla Cake"),
    CHOCOLATE_CAKE("Chocolate Cake"),
    OTHERS("Others");

    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
