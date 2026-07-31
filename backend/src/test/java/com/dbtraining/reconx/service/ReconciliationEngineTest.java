package com.dbtraining.reconx.service;
import com.dbtraining.reconx.repository.ReconResultRepository;
import com.dbtraining.reconx.repository.entity.Trade;

import org.mockito.ArgumentCaptor;
import com.dbtraining.reconx.model.EquityTrade;
import com.dbtraining.reconx.model.Side;
import com.dbtraining.reconx.model.TradeRef;
import com.dbtraining.reconx.dto.ReconResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
/**
 * TICKET-ADV040 / ADV041 / ADV042 — TDD: write the test FIRST, then the impl.
 */
class ReconciliationEngineTest {

    private final ReconciliationEngine engine = new ReconciliationEngine();
    
    @Test
    void testReconcile_savesResultWithMatchedStatus() {
        // given
        ReconResultRepository repo = mock(ReconResultRepository.class);
        ReconciliationEngine engine = new ReconciliationEngine();
        ReconciliationService svc = new ReconciliationService(engine, repo);

        Trade i = new Trade("TRD-1", "CP-1", "SAP.DE",
                new BigDecimal("10"), new BigDecimal("100"), LocalDate.now());
        Trade e = new Trade("TRD-1", "CP-1", "SAP.DE",
                new BigDecimal("10"), new BigDecimal("100"), LocalDate.now());

        // when
        svc.runRecon(List.of(i), List.of(e));

        // then
        ArgumentCaptor<ReconResult> captor = ArgumentCaptor.forClass(ReconResult.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().tradeRef()).isEqualTo("TRD-1");
        assertThat(captor.getValue().status()).isEqualTo(ReconResult.Status.MATCHED);
    }

        // when
        svc.runRecon(List.of(i), List.of(e));

        // then
        ArgumentCaptor<ReconResult> captor = ArgumentCaptor.forClass(ReconResult.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().tradeRef()).isEqualTo("TRD-1");
        assertThat(captor.getValue().status()).isEqualTo(ReconResult.Status.MATCHED);
    }
    @Test
    void testReconcile_exactMatch_returnsMatched() {
        // TODO(TICKET-ADV040): two identical EquityTrades + EXACT rule -> one ReconResult with status MATCHED.
        org.junit.jupiter.api.Assertions.fail("TICKET-ADV040 not implemented yet");
    }

   @Test
    void testReconcile_priceTolerance_withinThreshold() {
       EquityTrade internal = equity("TRD-002", "100.00", "10");
       EquityTrade external = equity("TRD-002", "100.50", "10");
    
       List<ReconResult> results = engine.reconcile(
               List.of(internal), List.of(external), ReconciliationRule.PRICE_TOLERANCE_1PCT);
    
       Assertions.assertEquals(1, results.size());
       Assertions.assertEquals(ReconResult.Status.MATCHED, results.get(0).status());
    }

    @Test
    void testReconcile_missingCounterpartyTrade_returnsBreak() {
        var in  = List.<TradeType>of(equity("EQU-20260603-0003", "100.00", "10"));
        var out = List.<TradeType>of();

        List<ReconResult> results = engine.reconcile(in, out, ReconciliationRule.EXACT);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).status()).isEqualTo(ReconResult.Status.BREAK);
        assertThat(results.get(0).discrepancyType()).isEqualTo("MISSING_EXTERNAL");
    }


    @Test
    void testReconcile_emptyInternal_returnsEmpty() {
        // TODO(TICKET-ADV040): empty internal + empty external -> reconcile returns an empty list.
        org.junit.jupiter.api.Assertions.fail("TICKET-ADV040 not implemented yet");
    }

    private EquityTrade equity(String ref, String price, String qty) {
        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .instrumentSymbol("SAP.DE")
                .price(new BigDecimal(price))
                .quantity(new BigDecimal(qty))
                .currency("EUR").side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}
