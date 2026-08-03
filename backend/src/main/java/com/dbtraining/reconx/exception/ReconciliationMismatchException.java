package com.dbtraining.reconx.exception;

/**
 * ============================================================================
 * TICKET-ADV025 — ReconciliationMismatchException
 *
 * WHAT:    Thrown when internal and external trades do not reconcile.
 * HOW:     Extends {@link ReconException} so it can be mapped to HTTP 422.
 * WHY:     A mismatch indicates a break that requires investigation before
 *          settlement or reporting.
 * ============================================================================
 */
public class ReconciliationMismatchException extends ReconException {

    /**
     * Create the exception for a reconciliation mismatch.
     * @param message mismatch description.
     */
    private final Long reconBreakId;

    public ReconciliationMismatchException(String message) {
        this(message, null, null);
    }

    public ReconciliationMismatchException(String message, Long reconBreakId) {
        this(message, reconBreakId, null);
    }

    public ReconciliationMismatchException(String message, Throwable cause) {
        this(message, null, cause);
    }

    public ReconciliationMismatchException(String message, Long reconBreakId, Throwable cause) {
        super(message, cause);
        this.reconBreakId = reconBreakId;
    }

    public Long getReconBreakId() {
        return reconBreakId;
    }
}
