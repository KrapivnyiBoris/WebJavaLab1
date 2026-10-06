package com.cosmocats.market.controller;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import lombok.experimental.UtilityClass;

/** Factory methods for RFC 9457 {@link ProblemDetail} responses (type, title, status, detail, instance). */
@UtilityClass
public class ProblemDetailsUtils {

    public static final URI VALIDATION_TYPE = URI.create("https://cosmo-cats.market/errors/validation");
    public static final URI NOT_FOUND_TYPE = URI.create("https://cosmo-cats.market/errors/not-found");
    public static final URI INTERNAL_TYPE = URI.create("https://cosmo-cats.market/errors/internal");

    private static final String DEFAULT_VALIDATION_DETAIL = "Request validation failed.";

    /** A single violated constraint of a request field. */
    public record FieldViolation(String field, String message) {
    }

    /** Builds a 400 problem whose detail looks like {@code Field 'price' must be greater than 0.}. */
    public ProblemDetail createValidationProblemDetail(List<FieldViolation> violations, String instance) {
        String detail = violations.stream()
                .sorted(Comparator.comparing(FieldViolation::field).thenComparing(FieldViolation::message))
                .map(violation -> String.format("Field '%s' %s.", violation.field(), violation.message()))
                .collect(Collectors.collectingAndThen(Collectors.joining(" "),
                        joined -> joined.isEmpty() ? DEFAULT_VALIDATION_DETAIL : joined));
        return createValidationProblemDetail(detail, instance);
    }

    public ProblemDetail createValidationProblemDetail(String detail, String instance) {
        return createProblemDetail(HttpStatus.BAD_REQUEST, VALIDATION_TYPE, "Validation failed", detail, instance);
    }

    public ProblemDetail createNotFoundProblemDetail(String detail, String instance) {
        return createProblemDetail(HttpStatus.NOT_FOUND, NOT_FOUND_TYPE, "Resource not found", detail, instance);
    }

    public ProblemDetail createInternalServerErrorProblemDetail(String instance) {
        return createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_TYPE, "Internal server error",
                "An unexpected error occurred.", instance);
    }

    private ProblemDetail createProblemDetail(HttpStatus status, URI type, String title, String detail,
            String instance) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setType(type);
        problemDetail.setTitle(title);
        problemDetail.setInstance(URI.create(instance));
        return problemDetail;
    }
}
