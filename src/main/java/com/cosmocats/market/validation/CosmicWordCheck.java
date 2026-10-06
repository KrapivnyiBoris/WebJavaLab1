package com.cosmocats.market.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * The annotated text must contain at least one cosmic term
 * (star, galaxy, comet, cosmic, space, ...), case-insensitive.
 * {@code null} values are considered valid - combine with {@code @NotBlank}.
 */
@Documented
@Constraint(validatedBy = CosmicWordCheckValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface CosmicWordCheck {

    String message() default "must contain a cosmic term (e.g. star, galaxy, comet, cosmic, space)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
