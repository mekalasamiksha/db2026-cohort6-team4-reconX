package com.dbtraining.reconx.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * ============================================================================
 * TICKET-ADV062 — RFC 7807 ProblemDetail for every ReconException
 *
 * Maps each domain exception subtype to the right HTTP status, with a
 * structured ProblemDetail body so clients don't have to parse free text.
 * ============================================================================
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    
    @ExceptionHandler(TradeNotFoundException.class)
    /**
     * Handle {@link TradeNotFoundException} and translate it into a ProblemDetail.
     *
     * @param ex thrown when the requested trade cannot be found.
     * @return RFC 7807 ProblemDetail describing the missing trade.
     */
    public ProblemDetail notFound(TradeNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
    }

    @ExceptionHandler(DuplicateTradeRefException.class)
    /**
     * Handle {@link DuplicateTradeRefException} and translate it into a ProblemDetail.
     *
     * @param ex thrown when a tradeRef already exists.
     * @return RFC 7807 ProblemDetail describing the conflict.
     */
    public ProblemDetail duplicate(DuplicateTradeRefException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
    }

    @ExceptionHandler(InvalidTradeException.class)
    /**
     * Handle {@link InvalidTradeException} and translate it into a ProblemDetail.
     *
     * @param ex thrown when a trade payload fails business validation.
     * @return RFC 7807 ProblemDetail describing the invalid payload.
     */
    public ProblemDetail invalid(InvalidTradeException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    @ExceptionHandler(ReconciliationMismatchException.class)
    /**
     * Handle {@link ReconciliationMismatchException} and translate it into a ProblemDetail.
     *
     * @param ex thrown when internal and external trades do not reconcile.
     * @return RFC 7807 ProblemDetail describing the mismatch.
     */
    public ProblemDetail mismatch(ReconciliationMismatchException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    /**
     * Handle {@link MethodArgumentNotValidException} raised by invalid controller input.
     *
     * @param ex validation exception from Spring MVC binding.
     * @return RFC 7807 ProblemDetail describing the field-level errors.
     */
    public ProblemDetail validation(MethodArgumentNotValidException ex) {

        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                errors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    /**
     * Handle {@link ConstraintViolationException} raised by bean validation.
     *
     * @param ex constraint violation exception.
     * @return RFC 7807 ProblemDetail describing the constraint violations.
     */
    public ProblemDetail constraint(ConstraintViolationException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }
}
