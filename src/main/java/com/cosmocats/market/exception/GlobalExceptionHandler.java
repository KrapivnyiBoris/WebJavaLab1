package com.cosmocats.market.exception;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

/**
 * Global error handler producing RFC 9457 "Problem Details for HTTP APIs" responses
 * ({@code application/problem+json}) with exactly these members:
 * type, title, status, detail, instance.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    public static final URI VALIDATION_TYPE = URI.create("https://cosmo-cats.market/errors/validation");
    public static final URI NOT_FOUND_TYPE = URI.create("https://cosmo-cats.market/errors/not-found");
    public static final URI INTERNAL_TYPE = URI.create("https://cosmo-cats.market/errors/internal");

    private record Violation(String field, String message) {
        String sentence() {
            return "Field '" + field + "' " + message + ".";
        }
    }

    // ---- 400: validation errors ------------------------------------------------------------

    /** Invalid {@code @Valid @RequestBody}. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<Violation> violations = new ArrayList<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> violations.add(new Violation(e.getField(), e.getDefaultMessage())));
        ex.getBindingResult().getGlobalErrors()
                .forEach(e -> violations.add(new Violation(e.getObjectName(), e.getDefaultMessage())));
        return validationProblem(violations, request);
    }

    /** Invalid {@code @RequestParam}/{@code @PathVariable} constraints (@Min, @Max, ...). */
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<Violation> violations = new ArrayList<>();
        ex.getParameterValidationResults().forEach(result -> {
            String name = result.getMethodParameter().getParameterName();
            result.getResolvableErrors()
                    .forEach(e -> violations.add(new Violation(name, e.getDefaultMessage())));
        });
        return validationProblem(violations, request);
    }

    /** Bean Validation outside of Spring MVC argument resolution. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        List<Violation> violations = ex.getConstraintViolations().stream()
                .map(v -> new Violation(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        return validationProblem(violations, request);
    }

    /** Malformed JSON / wrong JSON types. */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return problem(HttpStatus.BAD_REQUEST, VALIDATION_TYPE, "Validation failed",
                "Request body is malformed or unreadable.", request);
    }

    /** Wrong type of a parameter, e.g. non-UUID id or non-numeric page. */
    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String field = ex.getPropertyName() != null ? ex.getPropertyName() : "unknown";
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "value";
        return validationProblem(List.of(new Violation(field, "must be a valid " + requiredType)), request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status,
            WebRequest request) {
        return validationProblem(List.of(new Violation(ex.getParameterName(), "is required")), request);
    }

    // ---- other errors ----------------------------------------------------------------------

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFound(NotFoundException ex, WebRequest request) {
        return problem(HttpStatus.NOT_FOUND, NOT_FOUND_TYPE, "Resource not found", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception ex, WebRequest request) {
        log.error("Unexpected error", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_TYPE, "Internal server error",
                "An unexpected error occurred.", request);
    }

    // ---- helpers ---------------------------------------------------------------------------

    private ResponseEntity<Object> validationProblem(List<Violation> violations, WebRequest request) {
        String detail = violations.stream()
                .sorted(Comparator.comparing(Violation::field).thenComparing(Violation::message))
                .map(Violation::sentence)
                .reduce((a, b) -> a + " " + b)
                .orElse("Request validation failed.");
        return problem(HttpStatus.BAD_REQUEST, VALIDATION_TYPE, "Validation failed", detail, request);
    }

    private ResponseEntity<Object> problem(HttpStatus status, URI type, String title, String detail,
            WebRequest request) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setType(type);
        body.setTitle(title);
        body.setInstance(URI.create(requestPath(request)));
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }

    private String requestPath(WebRequest request) {
        return request instanceof ServletWebRequest servletRequest
                ? servletRequest.getRequest().getRequestURI()
                : "/";
    }
}
