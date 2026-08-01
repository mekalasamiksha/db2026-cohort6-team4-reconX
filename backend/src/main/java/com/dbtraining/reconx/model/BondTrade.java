package com.dbtraining.reconx.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;

/**
 * ============================================================================
 * TICKET-ADV021 — BondTrade with Builder pattern
 *
 * WHAT:    Fixed-income trade — couponRate, maturityDate, faceValue, isin.
 * HOW:     Same builder pattern. notional() = faceValue (in the bond's ccy).
 * WHY:     Bonds need couponRate/maturity for downstream cashflow modelling.
 *          Modelling them on the trade is the simplest path for the demo.
 * ============================================================================
 */
public final class BondTrade implements TradeType {

    private final TradeRef tradeRef;
    private final String isin;
    private final BigDecimal faceValue;
    private final BigDecimal couponRate;
    private final LocalDate maturityDate;
    private final Currency currency;
    private final Side side;
    private final LocalDate tradeDate;
    private final long counterpartyId;

    private BondTrade(Builder b) {
        this.tradeRef       = b.tradeRef;
        this.isin           = b.isin;
        this.faceValue      = b.faceValue;
        this.couponRate     = b.couponRate;
        this.maturityDate   = b.maturityDate;
        this.currency       = b.currency;
        this.side           = b.side;
        this.tradeDate      = b.tradeDate;
        this.counterpartyId = b.counterpartyId;
    }

    /**
     * Create a new builder for an immutable {@link BondTrade}.
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
     * @return {@link AssetClass#BOND}.
     */
    @Override public AssetClass assetClass() { return AssetClass.BOND; }
    @Override public int compareTo(TradeType other) { return this.tradeDate().compareTo(other.tradeDate()) * -1; }

    /**
 * Orders trades by trade date descending (newest first).
 *
 * @param other the trade to compare against
 * @return a negative integer, zero, or positive integer as this trade
 *         is newer than, equal to, or older than the specified trade
 */
    @Override public int compareTo(TradeType other) { return this.tradeDate().compareTo(other.tradeDate()) * -1; }

    /**
     * Bond notional is the face value in the bond currency.
     * @return face value wrapped in {@link Money}.
     */
    @Override public Money notional() {
        return new Money(faceValue, currency);
    }

    /**
     * International Securities Identification Number.
     * @return ISIN.
     */
    public String isin()              { return isin; }

    /**
     * Bond face value.
     * @return face value.
     */
    public BigDecimal faceValue()     { return faceValue; }

    /**
     * Coupon rate applied to the bond.
     * @return coupon rate.
     */
    public BigDecimal couponRate()    { return couponRate; }

    /**
     * Maturity date when the bond principal is due.
     * @return maturity date.
     */
    public LocalDate maturityDate()   { return maturityDate; }

    /**
     * Currency in which the bond is denominated.
     * @return bond currency.
     */
    public Currency currency()        { return currency; }

    /**
     * Whether the trade is a buy or sell.
     * @return trade side.
     */
    public Side side()                { return side; }

    /**
     * Counterparty identifier used for matching and reporting.
     * @return counterparty id.
     */
    public long counterpartyId()      { return counterpartyId; }
    /**
 * Equality(between EquityTrades) based solely on tradeRef.
 */
    @Override public boolean equals(Object o) {
    return (o instanceof BondTrade other) && tradeRef.equals(other.tradeRef);
    }
    @Override public int hashCode() {
        return tradeRef.hashCode();
    }

    @Override public String toString() {
        return "BondTrade[ref=%s, isin=%s, face=%s %s, coupon=%s, maturity=%s, side=%s]"
                .formatted(tradeRef, isin, faceValue.toPlainString(),
                        currency.getCurrencyCode(), couponRate.toPlainString(), maturityDate, side);
        // NOTE:
        // Deliberately omitted:
        // - counterpartyId (PII)
        // - settlement amount
        // - any LEI / counterparty identifiers
        // Use audit formatter if full detail is required.
    }

    public static final class Builder {
        private TradeRef tradeRef;
        private String isin;
        private BigDecimal faceValue, couponRate;
        private LocalDate maturityDate, tradeDate;
        private Currency currency;
        private Side side;
        private long counterpartyId;
        
        /**
 * @param v trade reference
 * @return this builder
 */
        public Builder tradeRef(TradeRef v)        { this.tradeRef = v; return this; }
        /**
 * @param v ISIN identifier
 * @return this builder
 */
        public Builder isin(String v)              { this.isin = v; return this; }
        /**
 * @param v face value
 * @return this builder
 */
        public Builder faceValue(BigDecimal v)     { this.faceValue = v; return this; }
        /**
 * @param v coupon rate
 * @return this builder
 */
        public Builder couponRate(BigDecimal v)    { this.couponRate = v; return this; }
        /**
 * @param v maturity date
 * @return this builder
 */
        public Builder maturityDate(LocalDate v)   { this.maturityDate = v; return this; }
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
         * Build the immutable {@link BondTrade}, validating required fields.
         *
         * @return a fully-constructed, validated {@code BondTrade} — never {@code null}.
         * @throws NullPointerException if any required field
         *                              ({@code tradeRef}, {@code isin},
         *                              {@code faceValue}, {@code couponRate},
         *                              {@code maturityDate}, {@code currency},
         *                              {@code side}, {@code tradeDate}) is missing.
         * @throws IllegalStateException if {@code faceValue} is not positive,
         *                               if {@code couponRate} is negative, or if
         *                               {@code maturityDate} is before
         *                               {@code tradeDate}.
         */
        public BondTrade build() {
            Objects.requireNonNull(tradeRef, "tradeRef");
            Objects.requireNonNull(isin, "isin");
            Objects.requireNonNull(faceValue, "faceValue");
            Objects.requireNonNull(couponRate, "couponRate");
            Objects.requireNonNull(maturityDate, "maturityDate");
            Objects.requireNonNull(currency, "currency");
            Objects.requireNonNull(side, "side");
            Objects.requireNonNull(tradeDate, "tradeDate");
            if (faceValue.signum() <= 0) throw new IllegalStateException("faceValue must be > 0");
            if (couponRate.signum() < 0) throw new IllegalStateException("couponRate must be >= 0");
            if (maturityDate.isBefore(tradeDate)) throw new IllegalStateException("maturityDate must not be before tradeDate");
            return new BondTrade(this);
        }
    }
}
