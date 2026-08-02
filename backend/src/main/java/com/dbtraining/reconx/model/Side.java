// Implemented Ticket - ADV019 (EquityTrade with builder pattern)

package com.dbtraining.reconx.model;

/**
 * ============================================================================
 * TICKET-ADV019 — Side enum
 *
 * WHAT:    Trade direction marker for buy vs sell.
 * HOW:     Simple enum prevents invalid string values in trade payloads.
 * WHY:     A typed side field avoids typos and makes downstream logic
 *          ({@code P&L} sign, matching, reporting) explicit.
 * ============================================================================
 */
public enum Side {
    BUY, SELL
}
