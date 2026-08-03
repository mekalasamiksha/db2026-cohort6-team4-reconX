class ReconciliationEngineTest {

    private final ReconciliationEngine engine = new ReconciliationEngine();

    @Test
    void testReconcile_exactMatch_returnsMatched() {
        var in = List.<TradeType>of(equity("EQU-20260603-0001", "100.00", "10"));
        var out = List.<TradeType>of(equity("EQU-20260603-0001", "100.00", "10"));

        List<ReconResult> results = engine.reconcile(in, out, ReconciliationRule.EXACT);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).status()).isEqualTo(ReconResult.Status.MATCHED);
        assertThat(results.get(0).tradeRef()).isEqualTo("EQU-20260603-0001");
    }

    @Test
    void testReconcile_priceTolerance_withinThreshold() {
        EquityTrade internal = equity("TRD-002", "100.00", "10");
        EquityTrade external = equity("TRD-002", "100.50", "10");

        List<ReconResult> results = engine.reconcile(
                List.of(internal), List.of(external),
                ReconciliationRule.PRICE_TOLERANCE_1PCT);

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
        List<ReconResult> results =
                engine.reconcile(List.of(), List.of(), ReconciliationRule.EXACT);

        assertThat(results).isEmpty();
    }

    private EquityTrade equity(String ref, String price, String qty) {
        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .instrumentSymbol("SAP.DE")
                .price(new BigDecimal(price))
                .quantity(new BigDecimal(qty))
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}