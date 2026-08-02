package com.dbtraining.reconx.exception;

/**
 * ============================================================================
 * TICKET-ADV025 — InvalidTradeException
 *
 * WHAT:    Thrown when a trade payload fails business validation.
 * HOW:     Extends {@link ReconException} so it can be mapped to HTTP 400.
 * WHY:     Invalid trades must be rejected before they enter reconciliation
 *          or persistence.
 * ============================================================================
 */
public class InvalidTradeException extends ReconException {

    /**
     * Create the exception for a validation failure.
     * @param message validation failure message.
     */
    public InvalidTradeException(String message) {
        super(message);
    }

    public InvalidTradeException(String message, Throwable cause) {
        super(message, cause);
    }
}