package com.dbtraining.reconx.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import org.junit.jupiter.api.Test;

class TradeToStringTest {

    @Test
    void equityTradeToStringOmitsCounterpartyAndUsesPlainDecimals() {
        EquityTrade trade = EquityTrade.builder()
                .tradeRef(new TradeRef("EQI-20260730-0001"))
                .instrumentSymbol("AAPL")
                .quantity(new BigDecimal("100.50"))
                .price(new BigDecimal("12.34"))
                .currency(Currency.getInstance("USD"))
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 7, 30))
                .counterpartyId(42L)
                .build();

        String text = trade.toString();

        assertThat(text).contains("EquityTrade");
        assertThat(text).contains("AAPL");
        assertThat(text).contains("100.50");
        assertThat(text).doesNotContain(String.valueOf(trade.counterpartyId()));
        assertThat(text).doesNotContain("1.005E2");
    }

    @Test
    void fxTradeToStringOmitsCounterpartyAndUsesPlainDecimals() {
        FXTrade trade = FXTrade.builder()
                .tradeRef(new TradeRef("FXI-20260730-0002"))
                .ccy1("USD")
                .ccy2("EUR")
                .notionalCcy1(new BigDecimal("12345.67"))
                .fxRate(new BigDecimal("0.92"))
                .side(Side.SELL)
                .tradeDate(LocalDate.of(2026, 7, 30))
                .counterpartyId(77L)
                .build();

        String text = trade.toString();

        assertThat(text).contains("FXTrade");
        assertThat(text).contains("USD/EUR");
        assertThat(text).contains("12345.67");
        assertThat(text).doesNotContain(String.valueOf(trade.counterpartyId()));
    }

    @Test
    void bondTradeToStringOmitsCounterpartyAndUsesPlainDecimals() {
        BondTrade trade = BondTrade.builder()
                .tradeRef(new TradeRef("BND-20260730-0003"))
                .isin("US0000000001")
                .faceValue(new BigDecimal("1000000.25"))
                .couponRate(new BigDecimal("0.0450"))
                .maturityDate(LocalDate.of(2031, 6, 30))
                .currency("USD")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 7, 30))
                .counterpartyId(99L)
                .build();

        String text = trade.toString();

        assertThat(text).contains("BondTrade");
        assertThat(text).contains("US0000000001");
        assertThat(text).contains("1000000.25");
        assertThat(text).doesNotContain(String.valueOf(trade.counterpartyId()));
    }

    @Test
    void derivativeTradeToStringOmitsCounterpartyAndUsesPlainDecimals() {
        DerivativeTrade trade = DerivativeTrade.builder()
                .tradeRef(new TradeRef("DER-20260730-0004"))
                .underlying("AAPL")
                .strike(new BigDecimal("150.50"))
                .quantity(new BigDecimal("10"))
                .expiry(LocalDate.of(2026, 9, 18))
                .optionType(DerivativeTrade.OptionType.CALL)
                .currency("USD")
                .side(Side.SELL)
                .tradeDate(LocalDate.of(2026, 7, 30))
                .counterpartyId(123L)
                .build();

        String text = trade.toString();

        assertThat(text).contains("DerivativeTrade");
        assertThat(text).contains("AAPL");
        assertThat(text).contains("150.50");
        assertThat(text).doesNotContain(String.valueOf(trade.counterpartyId()));
    }
}
