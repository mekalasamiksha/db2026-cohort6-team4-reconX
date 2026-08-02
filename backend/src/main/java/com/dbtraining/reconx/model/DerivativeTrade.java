package com.dbtraining.reconx.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;

/**
 * ============================================================================
 * TICKET-ADV022 — DerivativeTrade with Builder pattern
 *
 * WHAT:    Option/derivative trade — underlying, strike, expiry, optionType.
 * HOW:     Same builder pattern. notional() = strike * quantity in the
 *          trade's currency (simplified — real derivatives use delta-adjusted).
 * ============================================================================
 */
public final class DerivativeTrade implements TradeType {

    /**
     * Option type for a derivative trade.
     */
    public enum OptionType { CALL, PUT }

    private final TradeRef tradeRef;
    private final String underlying;
    private final BigDecimal strike;
    private final BigDecimal quantity;
    private final LocalDate expiry;
    private final OptionType optionType;
    private final Currency currency;
    private final Side side;
    private final LocalDate tradeDate;
    private final long counterpartyId;

    private DerivativeTrade(Builder b) {
        this.tradeRef       = b.tradeRef;
        this.underlying     = b.underlying;
        this.strike         = b.strike;
        this.quantity       = b.quantity;
        this.expiry         = b.expiry;
        this.optionType     = b.optionType;
        this.currency       = b.currency;
        this.side           = b.side;
        this.tradeDate      = b.tradeDate;
        this.counterpartyId = b.counterpartyId;
    }

    /**
     * Create a new builder for an immutable {@link DerivativeTrade}.
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
     * @return {@link AssetClass#DERIVATIVE}.
     */
    @Override public AssetClass assetClass() { return AssetClass.DERIVATIVE; }

    /**
 * Orders trades by trade date descending (newest first).
 *
 * @param other the trade to compare against
 * @return a negative integer, zero, or positive integer as this trade
 *         is newer than, equal to, or older than the specified trade
 */
    @Override public int compareTo(TradeType other) { return this.tradeDate().compareTo(other.tradeDate()) * -1; }

    /**
     * Simplified derivative notional = strike * quantity.
     * @return notional amount in the trade currency.
     */
    @Override public Money notional() {
        return new Money(strike.multiply(quantity), currency);
    }

    /**
     * Underlying instrument for the derivative.
     * @return underlying symbol.
     */
    public String underlying()       { return underlying; }

    /**
     * Derivative strike price.
     * @return strike.
     */
    public BigDecimal strike()       { return strike; }

    /**
     * Number of option contracts.
     * @return quantity.
     */
    public BigDecimal quantity()     { return quantity; }

    /**
     * Expiry date of the derivative.
     * @return expiry date.
     */
    public LocalDate expiry()        { return expiry; }

    /**
     * Option type for the derivative.
     * @return option type.
     */
    public OptionType optionType()   { return optionType; }

    /**
     * Currency used for the derivative's notional.
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
    @Override public boolean equals(Object o) {return (o instanceof DerivativeTrade other) && tradeRef.equals(other.tradeRef);
    }
    @Override public int hashCode() {
        return tradeRef.hashCode();
    }

    @Override public String toString() {
        return "DerivativeTrade[ref=%s, %s %s on %s, strike=%s %s, qty=%s, expiry=%s, side=%s]"
                .formatted(tradeRef, optionType, underlying, tradeDate,
                        strike.toPlainString(), currency.getCurrencyCode(), quantity.toPlainString(), expiry, side);
        // NOTE: deliberately omit counterpartyId and settlement-sensitive fields from logs.
    }

    public static final class Builder {
        private TradeRef tradeRef;
        private String underlying;
        private BigDecimal strike, quantity;
        private LocalDate expiry, tradeDate;
        private OptionType optionType;
        private Currency currency;
        private Side side;
        private long counterpartyId;

        /**
 * @param v trade reference
 * @return this builder
 */
        public Builder tradeRef(TradeRef v)        { this.tradeRef = v; return this; }
        /**
 * @param v underlying asset
 * @return this builder
 */
        public Builder underlying(String v)        { this.underlying = v; return this; }
        /**
 * @param v strike price
 * @return this builder
 */
        public Builder strike(BigDecimal v)        { this.strike = v; return this; }
        /**
 * @param v quantity
 * @return this builder
 */
        public Builder quantity(BigDecimal v)      { this.quantity = v; return this; }
        /**
 * @param v expiry date
 * @return this builder
 */
        public Builder expiry(LocalDate v)         { this.expiry = v; return this; }
        /**
 * @param v option type
 * @return this builder
 */
        public Builder optionType(OptionType v)    { this.optionType = v; return this; }
        /**
 * @param v currency code
 * @return this builder
 */
        public Builder currency(String code)       { this.currency = Currency.getInstance(code); return this; }
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
         * Build the immutable {@link DerivativeTrade}, validating required fields.
         *
         * @return a fully-constructed, validated {@code DerivativeTrade} — never {@code null}.
         * @throws NullPointerException if any required field
         *                              ({@code tradeRef}, {@code underlying},
         *                              {@code strike}, {@code quantity},
         *                              {@code expiry}, {@code optionType},
         *                              {@code currency}, {@code side},
         *                              {@code tradeDate}) is missing.
         * @throws IllegalStateException if {@code underlying} is blank,
         *                               if {@code strike} or {@code quantity}
         *                               are not strictly positive, or if
         *                               {@code expiry} is not after {@code tradeDate}.
         */
        public DerivativeTrade build() {
            Objects.requireNonNull(tradeRef, "tradeRef");
            Objects.requireNonNull(underlying, "underlying");
            Objects.requireNonNull(strike, "strike");
            Objects.requireNonNull(quantity, "quantity");
            Objects.requireNonNull(expiry, "expiry");
            Objects.requireNonNull(optionType, "optionType");
            Objects.requireNonNull(currency, "currency");
            Objects.requireNonNull(side, "side");
            Objects.requireNonNull(tradeDate, "tradeDate");
            if (underlying.isBlank()) throw new IllegalStateException("underlying cannot be blank");
            if (strike.signum() <= 0) throw new IllegalStateException("strike must be > 0");
            if (quantity.signum() <= 0) throw new IllegalStateException("quantity must be > 0");
            if (!expiry.isAfter(tradeDate)) throw new IllegalStateException("expiry cannot be before tradeDate");
            return new DerivativeTrade(this);
        }
    }
}
