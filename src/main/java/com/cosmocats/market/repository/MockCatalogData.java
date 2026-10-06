package com.cosmocats.market.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.cosmocats.market.domain.Category;
import com.cosmocats.market.domain.Product;

/** Initial mock data (3 categories and 12 cosmic products) used until a real database is connected. */
final class MockCatalogData {

    static final Category TOYS = category("Toys", "Toys for zero-gravity play");
    static final Category FOOD = category("Food", "Treats and drinks from across the universe");
    static final Category ACCESSORIES = category("Accessories", "Gear for space-faring cats");

    static List<Category> categories() {
        return List.of(TOYS, FOOD, ACCESSORIES);
    }

    static List<Product> products() {
        return List.of(
                product("Anti-gravity Star Yarn", "Yarn ball that floats around the cabin", "19.99", TOYS, 120),
                product("Cosmic Galaxy Milk", "Milk bottled on the edge of the Milky Way", "7.50", FOOD, 300),
                product("Comet Tail Feather Wand", "Feather wand with a glowing comet tail", "12.00", TOYS, 80),
                product("Nebula Catnip Space Mix", "Catnip blend grown in a nebula greenhouse", "9.99", FOOD, 200),
                product("Space Helmet for Cats", "Pressurized helmet with a tail-friendly vent", "89.90",
                        ACCESSORIES, 25),
                product("Black Hole Space Laser Pointer", "Laser pointer with a gravitational twist", "24.50",
                        TOYS, 60),
                product("Galaxy Collar with Star Tracker", "Collar that always knows where the nearest star is",
                        "34.00", ACCESSORIES, 45),
                product("Meteor Crunch Space Treats", "Crunchy treats shaped like tiny meteors", "5.25", FOOD, 500),
                product("Cosmic Scratching Planet", "Planet-shaped scratching post", "49.00", TOYS, 30),
                product("Starlight Moon Milk Powder", "Instant moon milk, just add water", "11.40", FOOD, 150),
                product("Orbit Feeder Bowl", "Self-rotating feeder bowl for orbital stations", "29.90",
                        ACCESSORIES, 40),
                product("Supernova Star Mouse", "Plush mouse that explodes into glitter (once)", "6.75", TOYS, 220));
    }

    private static Category category(String name, String description) {
        return Category.builder()
                .id(UUID.randomUUID())
                .name(name)
                .description(description)
                .build();
    }

    private static Product product(String name, String description, String price, Category category, int stock) {
        return Product.builder()
                .id(UUID.randomUUID())
                .name(name)
                .description(description)
                .price(new BigDecimal(price))
                .category(category)
                .stockQuantity(stock)
                .build();
    }
}
