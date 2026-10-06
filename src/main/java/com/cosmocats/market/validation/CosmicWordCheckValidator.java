package com.cosmocats.market.validation;

import java.util.List;
import java.util.Locale;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CosmicWordCheckValidator implements ConstraintValidator<CosmicWordCheck, String> {

    static final List<String> COSMIC_TERMS = List.of(
            "star", "galaxy", "comet", "cosmic", "space",
            "nebula", "meteor", "planet", "orbit", "universe");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // presence is checked by @NotBlank / @NotNull
        }
        String lower = value.toLowerCase(Locale.ROOT);
        return COSMIC_TERMS.stream().anyMatch(lower::contains);
    }
}
