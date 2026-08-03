
/**
 * ====================================================================
 * TICKET-ADV019 — EquityTrade model
 *
 * WHAT:
 * Immutable representation of a listed equity trade.
 * Captures instrument, quantity, price, side, and counterparty.
 *
 * HOW:
 * Instances are constructed via the {@link Builder} and are immutable
 * after creation. Notional is computed as {@code quantity * price}
 * in the trade currency.
 *
 * INVARIANTS:
 * - quantity > 0
 * - price > 0
 * - tradeRef uniquely identifies the trade
 *
 * EQUALITY:
 * Equality and hash code are based solely on {@code tradeRef}.
 *
 * WHY:
 * Provides a stable contract for reconciliation, valuation, and audit.
 * ====================================================================
 */
package com.dbtraining.reconx.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;

public final class EquityTrade implements TradeType {

    private final TradeRef tradeRef;
    private final String instrumentSymbol;
    private final BigDecimal quantity;
    private final BigDecimal price;
    private final Currency currency;
    private final Side side;
    private final LocalDate tradeDate;
    private final long counterpartyId;

    private EquityTrade(Builder b) {
        this.tradeRef         = b.tradeRef;
        this.instrumentSymbol = b.instrumentSymbol;
        this.quantity         = b.quantity;
        this.price            = b.price;
        this.currency         = b.currency;
        this.side             = b.side;
        this.tradeDate        = b.tradeDate;
        this.counterpartyId   = b.counterpartyId;
    }

    /**
     * Create a new builder for an immutable {@link EquityTrade}.
     * @return fresh builder instance.
     */
    public static Builder builder() { return new Builder(); }

    /**
     * Stable natural key for the trade.
     * @return unique trade reference.
     */
    @Override public TradeRef tradeRef()    { return tradeRef; }

    /**
     * Business date when the trade was agreed.
     * @return trade date.
     */
    @Override public LocalDate tradeDate()  { return tradeDate; }

    /**
     * The trade asset class.
     * @return {@link AssetClass#EQUITY}.
     */
    @Override public AssetClass assetClass(){ return AssetClass.EQUITY; }

    /**
     * Compute trade notional for reconciliation and reporting.
     * @return quantity multiplied by price in the trade currency.
     */
    @Override public Money notional()       { return new Money(quantity.multiply(price), currency); }

    /**
 * Orders trades by trade date descending (newest first).
 *
 * @param other the trade to compare against
 * @return a negative integer, zero, or positive integer as this trade
 *         is newer than, equal to, or older than the specified trade
 */
    @Override public int compareTo(TradeType other) { return this.tradeDate().compareTo(other.tradeDate()) * -1; }

    /**
     * Equity ticker or instrument symbol.
     * @return instrument symbol.
     */
    public String instrumentSymbol() { return instrumentSymbol; }

    /**
     * Number of shares in the trade.
     * @return quantity.
     */
    public BigDecimal quantity()     { return quantity; }

    /**
     * Price per share.
     * @return price.
     */
    public BigDecimal price()        { return price; }

    /**
     * Currency used to price the trade.
     * @return currency.
     */
    public Currency currency()       { return currency; }

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
    @Override
    public boolean equals(Object o) {
        return (o instanceof EquityTrade other) && tradeRef.equals(other.tradeRef);
    }

    @Override public int hashCode() {
        return tradeRef.hashCode();
    }

    @Override
    public String toString() {
        return "EquityTrade[ref=%s, symbol=%s, qty=%s, price=%s %s, side=%s]"
                .formatted(tradeRef, instrumentSymbol, quantity.toPlainString(),
                        price.toPlainString(), currency.getCurrencyCode(), side);
        // NOTE: deliberately omit counterpartyId and other PII fields from logs.
    }

    /** Fluent builder. Required fields validated in {@link #build()}. */
    public static final class Builder {
        private TradeRef tradeRef;
        private String instrumentSymbol;
        private BigDecimal quantity;
        private BigDecimal price;
        private Currency currency;
        private Side side;
        private LocalDate tradeDate;
        private long counterpartyId;

        /**
 * @param v trade reference
 * @return this builder
 */
        public Builder tradeRef(TradeRef v)           { this.tradeRef = v;        return this; }
        /**
 * @param v instrument symbol
 * @return this builder
 */
        public Builder instrumentSymbol(String v)     { this.instrumentSymbol = v; return this; }
        /**
 * @param v quantity
 * @return this builder
 */
        public Builder quantity(BigDecimal v)         { this.quantity = v;        return this; }
        /**
 * @param v price
 * @return this builder
 */
        public Builder price(BigDecimal v)            { this.price = v;           return this; }
        /**
 * @param v currency
 * @return this builder
 */
        public Builder currency(Currency v)           { this.currency = v;        return this; }
        /**
 * @param code currency code
 * @return this builder
 */
        public Builder currency(String code)          { return currency(Currency.getInstance(code)); }
        /**
 * @param v trade side
 * @return this builder
 */
        public Builder side(Side v)                   { this.side = v;            return this; }
        /**
 * @param v trade date
 * @return this builder
 */
        public Builder tradeDate(LocalDate v)         { this.tradeDate = v;       return this; }
        /**
 * @param v counterparty ID
 * @return this builder
 */
        public Builder counterpartyId(long v)         { this.counterpartyId = v;  return this; }

        /**
         * Build the immutable {@link EquityTrade}, validating required fields.
         *
         * @return a fully-constructed, validated {@code EquityTrade} — never {@code null}.
         * @throws NullPointerException if any required field
         *                              ({@code tradeRef}, {@code instrumentSymbol},
         *                              {@code quantity}, {@code price}, {@code currency},
         *                              {@code side}, {@code tradeDate}) is missing.
         * @throws IllegalStateException if {@code quantity} is not strictly positive
         *                               or {@code price} is not strictly positive.
         */
        public EquityTrade build() {
            Objects.requireNonNull(tradeRef,         "tradeRef");
            Objects.requireNonNull(instrumentSymbol, "instrumentSymbol");
            Objects.requireNonNull(quantity,         "quantity");
            Objects.requireNonNull(price,            "price");
            Objects.requireNonNull(currency,         "currency");
            Objects.requireNonNull(side,             "side");
            Objects.requireNonNull(tradeDate,        "tradeDate");
            if (quantity.signum() <= 0) throw new IllegalStateException("quantity must be > 0");
            if (price.signum() <= 0)    throw new IllegalStateException("price must be > 0");
            return new EquityTrade(this);
        }
    }
}