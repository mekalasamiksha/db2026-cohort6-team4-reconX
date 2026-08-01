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
        // TODO(TICKET-ADV062): return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        throw new UnsupportedOperationException("TICKET-ADV062");
    }

    @ExceptionHandler(DuplicateTradeRefException.class)
    /**
     * Handle {@link DuplicateTradeRefException} and translate it into a ProblemDetail.
     *
     * @param ex thrown when a tradeRef already exists.
     * @return RFC 7807 ProblemDetail describing the conflict.
     */
    public ProblemDetail duplicate(DuplicateTradeRefException ex) {
        // TODO(TICKET-ADV062): map DuplicateTradeRefException -> HttpStatus.CONFLICT (409).
        throw new UnsupportedOperationException("TICKET-ADV062");
    }

    @ExceptionHandler(InvalidTradeException.class)
    /**
     * Handle {@link InvalidTradeException} and translate it into a ProblemDetail.
     *
     * @param ex thrown when a trade payload fails business validation.
     * @return RFC 7807 ProblemDetail describing the invalid payload.
     */
    public ProblemDetail invalid(InvalidTradeException ex) {
        // TODO(TICKET-ADV062): map InvalidTradeException -> HttpStatus.BAD_REQUEST (400).
        throw new UnsupportedOperationException("TICKET-ADV062");
    }

    @ExceptionHandler(ReconciliationMismatchException.class)
    /**
     * Handle {@link ReconciliationMismatchException} and translate it into a ProblemDetail.
     *
     * @param ex thrown when internal and external trades do not reconcile.
     * @return RFC 7807 ProblemDetail describing the mismatch.
     */
    public ProblemDetail mismatch(ReconciliationMismatchException ex) {
        // TODO(TICKET-ADV062): map ReconciliationMismatchException -> HttpStatus.UNPROCESSABLE_ENTITY (422).
        throw new UnsupportedOperationException("TICKET-ADV062");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    /**
     * Handle {@link MethodArgumentNotValidException} raised by invalid controller input.
     *
     * @param ex validation exception from Spring MVC binding.
     * @return RFC 7807 ProblemDetail describing the field-level errors.
     */
    public ProblemDetail validation(MethodArgumentNotValidException ex) {
        // TODO(TICKET-ADV062): join field errors ("field: message; ...") and return BAD_REQUEST ProblemDetail.
        //   Hint: ex.getBindingResult().getFieldErrors().stream().map(...).collect(Collectors.joining("; "))
        throw new UnsupportedOperationException("TICKET-ADV062");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    /**
     * Handle {@link ConstraintViolationException} raised by bean validation.
     *
     * @param ex constraint violation exception.
     * @return RFC 7807 ProblemDetail describing the constraint violations.
     */
    public ProblemDetail constraint(ConstraintViolationException ex) {
        // TODO(TICKET-ADV062): map ConstraintViolationException -> HttpStatus.BAD_REQUEST (400).
        throw new UnsupportedOperationException("TICKET-ADV062");
    }
}
