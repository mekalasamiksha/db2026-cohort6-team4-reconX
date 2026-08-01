// Implemented Ticket-ADV018
package com.dbtraining.reconx.model;

import java.time.LocalDate;
import java.util.Comparator;

/**
 * ============================================================================
 * TICKET-ADV018 — Sealed interface TradeType
 *
 * WHAT:    Sealed root of the trade hierarchy. Only the four named
 *          permitted classes can implement it. Any new asset class needs an
 *          explicit code change here — by design.
 * HOW:     `sealed ... permits ...` on Java 21.
 * WHY:     Without sealing, anyone could write their own `Trade` subclass and
 *          slip through the reconciliation engine's pattern-matching switch.
 *          Sealing turns the engine's switch into an exhaustive one — the
 *          compiler enforces that every case is handled.
 * OBSERVE: Removing `permits BondTrade` causes a compile error in
 *          ReconciliationEngine's switch expression.
 * HINT:    See Day 2 trainer guide §"Sprint 1A — sealed hierarchy" for the
 *          design discussion.
 * ============================================================================
 *
 * TICKET-ADV027 — Comparable natural ordering (most-recent trade first)
 * TICKET-ADV028 — equals/hashCode based on tradeRef (the natural key)
 *
 * Comparator lives on the sealed interface, so every impl shares the same
 * ordering rule — there is no per-class compareTo override to forget to
 * update when adding a new field.
 */
public sealed interface TradeType
        extends Comparable<TradeType>
        permits EquityTrade, FXTrade, BondTrade, DerivativeTrade {

    /**
     * Stable natural key for the trade.
     * @return unique trade reference used for identity and reconciliation.
     */
    TradeRef tradeRef();

    /**
     * Monetary notional value used by reconciliation and reporting.
     * @return notional amount expressed in the trade currency.
     */
    Money notional();

    /**
     * Business date when the trade was executed.
     * @return trade business date.
     */
    LocalDate tradeDate();

    /**
     * Asset class discriminator for routing and persistence.
     * @return asset class of this trade.
     */
    AssetClass assetClass();

//    Comparator<TradeType> NATURAL = Comparator
//            .comparing(TradeType::tradeDate).reversed()
//            .thenComparing(t -> t.tradeRef().value());
//
//    @Override
//    default int compareTo(TradeType other) {
//        return NATURAL.compare(this, other);
//    }

    enum AssetClass { EQUITY, FX, BOND, DERIVATIVE }
}
