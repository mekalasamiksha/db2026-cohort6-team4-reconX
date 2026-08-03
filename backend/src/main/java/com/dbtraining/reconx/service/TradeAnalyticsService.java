package com.dbtraining.reconx.service;

import com.dbtraining.reconx.model.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TradeAnalyticsService {

    /** TICKET-ADV034 — count + sum of notional per counterparty. */
    public Map<Long, NotionalSummary> notionalByCounterparty(List<? extends TradeType> trades) {
        return trades.stream().collect(Collectors.groupingBy(
                this::counterpartyIdOf,
                Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> new NotionalSummary(
                                list.size(),
                                list.stream()
                                        .map(t -> t.notional().amount())
                                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        )
                )
        ));
    }

    /** TICKET-ADV035 — VWAP per instrument */
    public Map<String, BigDecimal> vwapByInstrument(List<EquityTrade> equityTrades) {

        return equityTrades.stream()
                .collect(Collectors.groupingBy(EquityTrade::instrumentSymbol))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> {
                            BigDecimal totalQty = e.getValue().stream()
                                    .map(EquityTrade::quantity)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            if (totalQty.signum() == 0) return BigDecimal.ZERO;

                            BigDecimal weighted = e.getValue().stream()
                                    .map(t -> t.price().multiply(t.quantity()))
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            return weighted.divide(totalQty, 6, RoundingMode.HALF_UP);
                        }
                ));
    }

    /** TICKET-ADV036 — P&L per instrument */
    public Map<String, BigDecimal> pnlByInstrument(List<EquityTrade> equityTrades) {

        return equityTrades.stream().collect(Collectors.groupingBy(
                EquityTrade::instrumentSymbol,
                Collectors.mapping(
                        this::pnl,
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                )
        ));
    }

    private BigDecimal pnl(EquityTrade t) {
        BigDecimal abs = t.price().multiply(t.quantity());
        return t.side() == Side.SELL ? abs : abs.negate();
    }

    /** TICKET-ADV018 — exhaustive switch */
    private long counterpartyIdOf(TradeType t) {
        return switch (t) {
            case EquityTrade e     -> e.counterpartyId();
            case FXTrade fx        -> fx.counterpartyId();
            case BondTrade b       -> b.counterpartyId();
            case DerivativeTrade d -> d.counterpartyId();
        };
    }

    public record NotionalSummary(long count, BigDecimal total) {}

    /** Optional: custom collector usage */
    public BigDecimal calculateVwap(List<EquityTrade> trades) {
        return trades.stream().collect(new VwapCollector());
    }

    public BigDecimal calculateParallelVwap(List<EquityTrade> trades) {
        return trades.parallelStream().collect(new VwapCollector());
    }
}