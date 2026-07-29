package com.dbtraining.reconx.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;

/**
 * ============================================================================
 * TICKET-ADV022 — DerivativeTrade with Builder pattern
 *
 * WHAT:
 * Fixed-income derivative (option) trade with underlying asset,
 * strike price, expiry date and option type.
 *
 * HOW:
 * Uses the Builder pattern with validation.
 * notional() = strike × quantity (simplified).
 *
 * IMPORTANT:
 * Only business invariants are validated.
 * We intentionally DO NOT compare expiry with LocalDate.now().
 * A derivative that has already expired is still a valid historical
 * trade for replay, reconciliation and audit.
 * ============================================================================
 */
public final class DerivativeTrade implements TradeType {

    /**
     * Closed set of supported option types.
     */
    public enum OptionType {
        CALL,
        PUT
    }

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
        this.tradeRef = b.tradeRef;
        this.underlying = b.underlying;
        this.strike = b.strike;
        this.quantity = b.quantity;
        this.expiry = b.expiry;
        this.optionType = b.optionType;
        this.currency = b.currency;
        this.side = b.side;
        this.tradeDate = b.tradeDate;
        this.counterpartyId = b.counterpartyId;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public TradeRef tradeRef() {
        return tradeRef;
    }

    @Override
    public LocalDate tradeDate() {
        return tradeDate;
    }

    @Override
    public AssetClass assetClass() {
        return AssetClass.DERIVATIVE;
    }

    /**
     * Simplified notional = strike × quantity.
     */
    @Override
    public Money notional() {
        return new Money(strike.multiply(quantity), currency);
    }

    public String underlying() {
        return underlying;
    }

    public BigDecimal strike() {
        return strike;
    }

    public BigDecimal quantity() {
        return quantity;
    }

    public LocalDate expiry() {
        return expiry;
    }

    public OptionType optionType() {
        return optionType;
    }

    public Currency currency() {
        return currency;
    }

    public Side side() {
        return side;
    }

    public long counterpartyId() {
        return counterpartyId;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof DerivativeTrade other)
                && tradeRef.equals(other.tradeRef);
    }

    @Override
    public int hashCode() {
        return tradeRef.hashCode();
    }

    @Override
    public String toString() {
        return "DerivativeTrade[ref=%s, %s %s on %s, strike=%s %s, qty=%s, expiry=%s, side=%s]"
                .formatted(
                        tradeRef,
                        optionType,
                        underlying,
                        tradeDate,
                        strike,
                        currency.getCurrencyCode(),
                        quantity,
                        expiry,
                        side
                );
    }

    public static final class Builder {

        private TradeRef tradeRef;
        private String underlying;
        private BigDecimal strike;
        private BigDecimal quantity;
        private LocalDate expiry;
        private LocalDate tradeDate;
        private OptionType optionType;
        private Currency currency;
        private Side side;
        private long counterpartyId;

        public Builder tradeRef(TradeRef v) {
            this.tradeRef = v;
            return this;
        }

        public Builder underlying(String v) {
            this.underlying = v;
            return this;
        }

        public Builder strike(BigDecimal v) {
            this.strike = v;
            return this;
        }

        public Builder quantity(BigDecimal v) {
            this.quantity = v;
            return this;
        }

        public Builder expiry(LocalDate v) {
            this.expiry = v;
            return this;
        }

        public Builder optionType(OptionType v) {
            this.optionType = v;
            return this;
        }

        public Builder currency(String code) {
            this.currency = Currency.getInstance(code);
            return this;
        }

        public Builder side(Side v) {
            this.side = v;
            return this;
        }

        public Builder tradeDate(LocalDate v) {
            this.tradeDate = v;
            return this;
        }

        public Builder counterpartyId(long v) {
            this.counterpartyId = v;
            return this;
        }

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

            if (underlying.isBlank()) {
                throw new IllegalStateException("underlying cannot be blank");
            }

            if (strike.signum() <= 0) {
                throw new IllegalStateException("strike must be > 0");
            }

            if (quantity.signum() <= 0) {
                throw new IllegalStateException("quantity must be > 0");
            }

            // Validate only the invariant.
            // Do NOT compare with LocalDate.now().
            // Historical expired options are valid records.
            if (!expiry.isAfter(tradeDate)) {
                throw new IllegalStateException("expiry cannot be before tradeDate");
            }

            return new DerivativeTrade(this);
        }
    }
}