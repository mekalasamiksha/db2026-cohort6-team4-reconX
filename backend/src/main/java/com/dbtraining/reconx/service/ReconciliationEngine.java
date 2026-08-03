package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.*;
import io.micrometer.core.annotation.Timed;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReconciliationEngine {

    private final ExecutorService executor =
            Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors(),
                    r -> new Thread(r, "recon-worker-" + r.hashCode())
            );

    @Timed(value = "reconciliation.duration", description = "Wall time of reconcile()",
            percentiles = {0.5, 0.95, 0.99}, histogram = true)
    public List<ReconResult> reconcile(List<TradeType> internal,
                                       List<TradeType> external,
                                       ReconciliationRule rule) {

        if (internal == null || internal.isEmpty()) return List.of();

        List<TradeType> ext = external == null ? List.of() : external;

        Map<String, TradeType> externalByRef = ext.stream()
                .collect(Collectors.toMap(
                        t -> t.tradeRef().value(),
                        Function.identity(),
                        (a, b) -> a
                ));

        return internal.parallelStream()
                .map(in -> matchOne(in, externalByRef.get(in.tradeRef().value()), rule))
                .toList();
    }

    public CompletableFuture<List<ReconResult>> reconcileByCounterparty(
            Map<Long, List<TradeType>> internalByCp,
            Map<Long, List<TradeType>> externalByCp,
            ReconciliationRule rule) {

        if (internalByCp == null || internalByCp.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }

        Map<Long, List<TradeType>> ext = externalByCp == null ? Map.of() : externalByCp;

        List<CompletableFuture<List<ReconResult>>> futures =
                internalByCp.entrySet().stream()
                        .map(e -> CompletableFuture.supplyAsync(
                                () -> reconcile(
                                        e.getValue(),
                                        ext.getOrDefault(e.getKey(), List.of()),
                                        rule
                                ),
                                executor
                        ))
                        .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .flatMap(f -> f.join().stream())
                        .toList());
    }

    private ReconResult matchOne(TradeType internal, TradeType external, ReconciliationRule rule) {
        String ref = internal.tradeRef().value();

        if (external == null) {
            return ReconResult.breakResult(ref, "MISSING_EXTERNAL",
                    "No external trade found for " + ref);
        }

        BigDecimal[] in = priceQty(internal);
        BigDecimal[] out = priceQty(external);

        if (rule.matches(in[0], in[1], out[0], out[1])) {
            return ReconResult.matched(ref);
        }

        return ReconResult.breakResult(ref, "VALUE_MISMATCH",
                "internal=%s/%s external=%s/%s"
                        .formatted(in[0], in[1], out[0], out[1]));
    }

    private BigDecimal[] priceQty(TradeType t) {
        return switch (t) {
            case EquityTrade e     -> new BigDecimal[]{e.price(), e.quantity()};
            case FXTrade fx        -> new BigDecimal[]{fx.fxRate(), fx.notionalCcy1()};
            case BondTrade b       -> new BigDecimal[]{b.couponRate(), b.faceValue()};
            case DerivativeTrade d -> new BigDecimal[]{d.strike(), d.quantity()};
        };
    }

    public void shutdown() {
        executor.shutdown();
    }
}