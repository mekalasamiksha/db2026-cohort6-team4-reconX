package com.dbtraining.reconx.exception;

/**
 * ============================================================================
 * TICKET-ADV025 — DuplicateTradeRefException
 *
 * WHAT:    Thrown when an attempt is made to create a trade with an existing
 *          trade reference.
 * HOW:     Extends {@link ReconException} so it can be mapped to HTTP 409.
 * WHY:     Duplicate trade references must be rejected to guarantee trade
 *          identity and avoid reconciliation ambiguity.
 * ============================================================================
 */
public class DuplicateTradeRefException extends ReconException {

    /**
     * Create the exception for a duplicate tradeRef.
     * @param tradeRef duplicate trade reference.
     */
    public DuplicateTradeRefException(String tradeRef) {
        super("Duplicate tradeRef: " + tradeRef);
    }

    public DuplicateTradeRefException(String message, Throwable cause) {
        super(message, cause);
    }
}