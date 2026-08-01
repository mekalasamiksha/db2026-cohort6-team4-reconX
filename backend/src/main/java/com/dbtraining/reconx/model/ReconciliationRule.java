package com.dbtraining.reconx.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * ============================================================================
 * TICKET-ADV026 — ReconciliationRule enum with configurable thresholds
 *
 * WHAT:    Each enum value carries its own price tolerance (%) and quantity
 *          tolerance (absolute units). {@link #matches} returns true if the
 *          internal vs external trade pair is within tolerance.
 * HOW:     Enum-with-state pattern — instance fields + a behaviour method.
 * WHY:     Putting the rule on the enum keeps "what is a match" co-located
 *          with the rule's name, so the reconciliation engine is just:
 *          `if (rule.matches(internal, external)) ... matched ...`.
 * OBSERVE: PRICE_TOLERANCE_1PCT.matches(p, p*1.005) is true; *1.02 is false.
 * ============================================================================
 */
public enum ReconciliationRule {

    EXACT(BigDecimal.ZERO, BigDecimal.ZERO),
    PRICE_TOLERANCE_1PCT(new BigDecimal("0.01"), BigDecimal.ZERO),
    PRICE_TOLERANCE_50BPS(new BigDecimal("0.005"), BigDecimal.ZERO),
    QTY_TOLERANCE_5UNITS(BigDecimal.ZERO, new BigDecimal("5")),
    LOOSE(new BigDecimal("0.05"), new BigDecimal("10"));

    private final BigDecimal priceTolerancePct;
    private final BigDecimal qtyToleranceAbs;

    ReconciliationRule(BigDecimal priceTolerancePct, BigDecimal qtyToleranceAbs) {
        this.priceTolerancePct = priceTolerancePct;
        this.qtyToleranceAbs = qtyToleranceAbs;
    }

    /**
     * Price tolerance percentage for this rule.
     * @return price tolerance as a decimal fraction.
     */
    public BigDecimal priceTolerancePct() {
        return priceTolerancePct;
    }

    /**
     * Quantity tolerance in absolute units for this rule.
     * @return quantity tolerance.
     */
    public BigDecimal qtyToleranceAbs() {
        return qtyToleranceAbs;
    }

    /**
     * Decide whether two prices/quantities are within this rule's tolerance.
     *
     * @param internalPrice internal trade price.
     * @param internalQty internal trade quantity.
     * @param externalPrice external trade price.
     * @param externalQty external trade quantity.
     * @return true if both the price difference and quantity difference are within tolerance.
     */
    public boolean matches(BigDecimal internalPrice, BigDecimal internalQty,
                           BigDecimal externalPrice, BigDecimal externalQty) {

        // Absolute price difference
        BigDecimal priceDiff = internalPrice.subtract(externalPrice).abs();

        // Calculate percentage difference (guard against divide by zero)
        BigDecimal priceDiffPct;

        if (internalPrice.compareTo(BigDecimal.ZERO) == 0) {
            if (priceDiff.compareTo(BigDecimal.ZERO) == 0) {
                priceDiffPct = BigDecimal.ZERO;
            } else {
                return false;
            }
        } else {
            priceDiffPct = priceDiff.divide(
                    internalPrice,
                    6,
                    RoundingMode.HALF_UP
            );
        }

        // Absolute quantity difference
        BigDecimal qtyDiff = internalQty.subtract(externalQty).abs();

        // Check both tolerances
        return priceDiffPct.compareTo(priceTolerancePct) <= 0
                && qtyDiff.compareTo(qtyToleranceAbs) <= 0;
    }
}