package com.dbtraining.reconx.exception;

/**
 * ============================================================================
 * TICKET-ADV025 — TradeNotFoundException
 *
 * WHAT:    Thrown when a requested trade reference does not exist.
 * HOW:     Extends {@link ReconException} so it can be caught by the global
 *          exception handler and mapped to HTTP 404.
 * WHY:     Clients should be able to distinguish a missing trade from bad
 *          payload or reconciliation failures.
 * ============================================================================
 */
public class TradeNotFoundException extends ReconException {

    /**
     * Create the exception for a missing tradeRef.
     * @param tradeRef missing trade reference.
     */
    public TradeNotFoundException(String tradeRef) {
        super("Trade not found: " + tradeRef);
    }

    public TradeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}