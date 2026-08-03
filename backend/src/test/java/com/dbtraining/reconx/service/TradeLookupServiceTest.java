package com.dbtraining.reconx.service;

import com.dbtraining.reconx.repository.CounterpartyRepository;
import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.entity.Counterparty;
import com.dbtraining.reconx.repository.entity.Trade;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TradeLookupServiceTest {

    private final TradeRepository tradeRepo = mock(TradeRepository.class);
    private final CounterpartyRepository cpRepo = mock(CounterpartyRepository.class);
    private final TradeLookupService service = new TradeLookupService(tradeRepo, cpRepo);

    @Test
    void counterpartyForTradeRef_resolvesCounterpartyViaOptionalChain() {
        Trade trade = mock(Trade.class);
        Counterparty counterparty = mock(Counterparty.class);

        when(tradeRepo.findByTradeRef("TRD-1")).thenReturn(Optional.of(trade));
        when(trade.getCounterparty()).thenReturn(counterparty);
        when(counterparty.getId()).thenReturn(42L);
        when(cpRepo.findById(42L)).thenReturn(Optional.of(counterparty));

        Counterparty result = service.counterpartyForTradeRef("TRD-1");

        assertThat(result).isSameAs(counterparty);
    }

    @Test
    void counterpartyForTradeRef_missingTradeThrowsNoSuchElementException() {
        when(tradeRepo.findByTradeRef("TRD-2")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.counterpartyForTradeRef("TRD-2"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("TRD-2");
    }
}
