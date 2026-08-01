
/**
 * ====================================================================
 * TICKET-ADV020 — FXTrade model
 *
 * WHAT:
 * Immutable foreign-exchange trade representing a currency pair,
 * notional, and FX rate.
 *
 * HOW:
 * Built via {@link Builder}. Notional is expressed in the second
 * currency (ccy2) as {@code notionalCcy1 * fxRate}.
 *
 * INVARIANTS:
 * - ccy1 != ccy2
 * - fxRate > 0
 *
 * EQUALITY:
 * Equality and hash code are based solely on {@code tradeRef}.
 *
 * WHY:
 * Required for reconciliation between internal and external FX trades,
 * where both currencies and conversion rates must match.
 * ====================================================================
 */
package com.dbtraining.reconx.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;

public final class FXTrade implements TradeType {

    private final TradeRef tradeRef;
    private final Currency ccy1;
    private final Currency ccy2;
    private final BigDecimal notionalCcy1;
    private final BigDecimal fxRate;
    private final Side side;
    private final LocalDate tradeDate;
    private final long counterpartyId;

    private FXTrade(Builder b) {
        this.tradeRef       = b.tradeRef;
        this.ccy1           = b.ccy1;
        this.ccy2           = b.ccy2;
        this.notionalCcy1   = b.notionalCcy1;
        this.fxRate         = b.fxRate;
        this.side           = b.side;
        this.tradeDate      = b.tradeDate;
        this.counterpartyId = b.counterpartyId;
    }

    /**
     * Create a new builder for an immutable {@link FXTrade}.
     * @return fresh builder instance.
     */
    public static Builder builder() { return new Builder(); }

    /**
     * Stable natural key for the trade.
     * @return unique trade reference.
     */
    @Override public TradeRef tradeRef()     { return tradeRef; }

    /**
     * Business date when the trade was agreed.
     * @return trade date.
     */
    @Override public LocalDate tradeDate()   { return tradeDate; }

    /**
     * The trade asset class.
     * @return {@link AssetClass#FX}.
     */
    @Override public AssetClass assetClass() { return AssetClass.FX; }

    /**
     * Compute the trade notional in the second currency.
     * @return notional amount expressed in {@link #ccy2()}.
     */
    @Override public Money notional()        { return new Money(notionalCcy1.multiply(fxRate), ccy2); }

    /**
 * Orders trades by trade date descending (newest first).
 *
 * @param other the trade to compare against
 * @return a negative integer, zero, or positive integer as this trade
 *         is newer than, equal to, or older than the specified trade
 */
    @Override public int compareTo(TradeType other) { return this.tradeDate().compareTo(other.tradeDate()) * -1; }

    /**
     * Base currency of the FX pair.
     * @return first currency.
     */
    public Currency ccy1()           { return ccy1; }

    /**
     * Terms currency of the FX pair.
     * @return second currency.
     */
    public Currency ccy2()           { return ccy2; }

    /**
     * Notional amount in the first currency.
     * @return notional amount in {@link #ccy1()}.
     */
    public BigDecimal notionalCcy1() { return notionalCcy1; }

    /**
     * Spot FX rate from {@link #ccy1()} to {@link #ccy2()}.
     * @return FX rate.
     */
    public BigDecimal fxRate()       { return fxRate; }

    /**
     * Whether the trade is a buy or sell.
     * @return trade side.
     */
    public Side side()               { return side; }

    /**
     * Counterparty identifier used for matching and reporting.
     * @return counterparty id.
     */
    public long counterpartyId()     { return counterpartyId; }
    /**
 * Equality(between EquityTrades) based solely on tradeRef.
 */
    @Override public boolean equals(Object o) {
        return (o instanceof FXTrade other) && tradeRef.equals(other.tradeRef);
    }
    @Override public int hashCode() { return tradeRef.hashCode(); }

    @Override
    public String toString() {
        return "FXTrade[ref=%s, %s/%s, notional=%s %s, rate=%s, side=%s]"
                .formatted(tradeRef, ccy1.getCurrencyCode(), ccy2.getCurrencyCode(),
                        notionalCcy1.toPlainString(), ccy1.getCurrencyCode(), fxRate.toPlainString(), side);
        // NOTE: deliberately omit counterpartyId and any settlement-specific PII from logs.
    }

    public static final class Builder {
        private TradeRef tradeRef;
        private Currency ccy1, ccy2;
        private BigDecimal notionalCcy1, fxRate;
        private Side side;
        private LocalDate tradeDate;
        private long counterpartyId;

        /**
 * @param v trade reference
 * @return this builder
 */
        public Builder tradeRef(TradeRef v)        { this.tradeRef = v; return this; }
        
        /**
 * @param code first currency
 * @return this builder
 */
        public Builder ccy1(String code)           { this.ccy1 = Currency.getInstance(code); return this; }
        /**
 * @param code second currency
 * @return this builder
 */
        public Builder ccy2(String code)           { this.ccy2 = Currency.getInstance(code); return this; }
        /**
 * @param v notional amount in current 1
 * @return this builder
 */
        public Builder notionalCcy1(BigDecimal v)  { this.notionalCcy1 = v; return this; }
        /**
 * @param v FX rate
 * @return this builder
 */
        public Builder fxRate(BigDecimal v)        { this.fxRate = v; return this; }
        /**
 * @param v trade side
 * @return this builder
 */
        public Builder side(Side v)                { this.side = v; return this; }
        /**
 * @param v trade date
 * @return this builder
 */
        public Builder tradeDate(LocalDate v)      { this.tradeDate = v; return this; }
        /**
 * @param v counterparty ID
 * @return this builder
 */
        public Builder counterpartyId(long v)      { this.counterpartyId = v; return this; }

        /**
         * Build the immutable {@link FXTrade}, validating required fields.
         *
         * @return a fully-constructed, validated {@code FXTrade} — never {@code null}.
         * @throws NullPointerException if any required field
         *                              ({@code tradeRef}, {@code ccy1},
         *                              {@code ccy2}, {@code notionalCcy1},
         *                              {@code fxRate}, {@code side},
         *                              {@code tradeDate}) is missing.
         * @throws IllegalStateException if {@code ccy1} and {@code ccy2} are equal
         *                               or if {@code fxRate} is not strictly positive.
         */
        public FXTrade build() {
            Objects.requireNonNull(tradeRef,     "tradeRef");
            Objects.requireNonNull(ccy1,         "ccy1");
            Objects.requireNonNull(ccy2,         "ccy2");
            Objects.requireNonNull(notionalCcy1, "notionalCcy1");
            Objects.requireNonNull(fxRate,       "fxRate");
            Objects.requireNonNull(side,         "side");
            Objects.requireNonNull(tradeDate,    "tradeDate");
            if (ccy1.equals(ccy2)) throw new IllegalStateException("ccy1 and ccy2 must differ");
            if (fxRate.signum() <= 0) throw new IllegalStateException("fxRate must be > 0");
            return new FXTrade(this);
        }
    }
}