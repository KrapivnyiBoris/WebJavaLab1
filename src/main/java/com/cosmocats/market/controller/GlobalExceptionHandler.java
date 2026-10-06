package com.cosmocats.market.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.cosmocats.market.controller.ProblemDetailsUtils.FieldViolation;
import com.cosmocats.market.exception.ProductNotFoundException;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

/**
 * Global error handler producing RFC 9457 "Problem Details for HTTP APIs" responses
 * ({@code application/problem+json}) with exactly these members:
 * type, title, status, detail, instance.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // ---- 400: validation errors ------------------------------------------------------------

    /** Invalid {@code @Valid @RequestBody}. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<FieldViolation> violations = new ArrayList<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> violations.add(new FieldViolation(e.getField(), e.getDefaultMessage())));
        ex.getBindingResult().getGlobalErrors()
                .forEach(e -> violations.add(new FieldViolation(e.getObjectName(), e.getDefaultMessage())));
        log.info("Request body validation failed: {}", violations);
        return toResponse(ProblemDetailsUtils.createValidationProblemDetail(violations, requestPath(request)));
    }

    /** Invalid {@code @RequestParam}/{@code @PathVariable} constraints (@Min, @Max, ...) of a {@code @Validated} controller. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        List<FieldViolation> violations = ex.getConstraintViolations().stream()
                .map(violation -> new FieldViolation(
                        lastPathNode(violation.getPropertyPath().toString()), violation.getMessage()))
                .toList();
        log.info("Request parameter validation failed: {}", violations);
        return toResponse(ProblemDetailsUtils.createValidationProblemDetail(violations, requestPath(request)));
    }

    /** Malformed JSON / wrong JSON types. */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.info("Request body is malformed or unreadable: {}", ex.getMessage());
        return toResponse(ProblemDetailsUtils.createValidationProblemDetail(
                "Request body is malformed or unreadable.", requestPath(request)));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status,
            WebRequest request) {
        log.info("Missing request parameter '{}'", ex.getParameterName());
        return toResponse(ProblemDetailsUtils.createValidationProblemDetail(
                List.of(new FieldViolation(ex.getParameterName(), "is required")), requestPath(request)));
    }

    // ---- other errors ----------------------------------------------------------------------

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Object> handleProductNotFoundException(ProductNotFoundException ex, WebRequest request) {
        log.info("Product not found: {}", ex.getMessage());
        return toResponse(ProblemDetailsUtils.createNotFoundProblemDetail(ex.getMessage(), requestPath(request)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpectedException(Exception ex, WebRequest request) {
        log.error("Unexpected error", ex);
        return toResponse(ProblemDetailsUtils.createInternalServerErrorProblemDetail(requestPath(request)));
    }

    // ---- helpers ---------------------------------------------------------------------------

    private ResponseEntity<Object> toResponse(ProblemDetail problemDetail) {
        return ResponseEntity.status(problemDetail.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problemDetail);
    }

    /** Turns {@code getAllProducts.size} into {@code size}. */
    private String lastPathNode(String propertyPath) {
        return propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
    }

    private String requestPath(WebRequest request) {
        return request instanceof ServletWebRequest servletRequest
                ? servletRequest.getRequest().getRequestURI()
                : "/";
    }
}
