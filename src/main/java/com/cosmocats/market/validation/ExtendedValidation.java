package com.cosmocats.market.validation;

/**
 * Marker validation group for checks that must run only after the basic (default group)
 * constraints have passed, e.g. {@link CosmicWordCheck} after {@code @NotBlank}.
 */
public interface ExtendedValidation {
}
