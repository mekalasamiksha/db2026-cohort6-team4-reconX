import java.util.concurrent.*;

private final ExecutorService executor =
        Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                r -> new Thread(r, "recon-worker-" + r.hashCode())
        );

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