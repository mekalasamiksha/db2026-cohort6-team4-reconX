package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.TradeRequest;
import com.dbtraining.reconx.exception.DuplicateTradeRefException;
import com.dbtraining.reconx.exception.TradeNotFoundException;
import com.dbtraining.reconx.kafka.TradeEventProducer;
import com.dbtraining.reconx.observability.TradeMetrics;
import com.dbtraining.reconx.repository.CounterpartyRepository;
import com.dbtraining.reconx.repository.InstrumentRepository;
import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.entity.Trade;
import com.dbtraining.reconx.dto.TradeEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static com.dbtraining.reconx.repository.TradeSpecifications.*;

/**
 * ============================================================================
 * TICKET-ADV064 — TradeService.create (POST endpoint backing)
 * TICKET-ADV065 — update
 * TICKET-ADV066 — updateStatus (PATCH)
 * TICKET-ADV067 — softDelete
 * TICKET-ADV083 — increments trade_created_total Counter on create
 * TICKET-ADV129 — publishes TradeEvent on every state change
 * TICKET-ADV055/ADV056 — list() uses Specifications + filter query
 * ============================================================================
 */
@Service
@Transactional
public class TradeService {

    private final TradeRepository tradeRepo;
    private final CounterpartyRepository cpRepo;
    private final InstrumentRepository instRepo;
    private final TradeEventProducer events;
    private final TradeMetrics metrics;

    public TradeService(TradeRepository tradeRepo,
                        CounterpartyRepository cpRepo,
                        InstrumentRepository instRepo,
                        TradeEventProducer events,
                        TradeMetrics metrics) {
        this.tradeRepo = tradeRepo;
        this.cpRepo = cpRepo;
        this.instRepo = instRepo;
        this.events = events;
        this.metrics = metrics;
    }

    public Trade create(TradeRequest req, String actor) {

    if (tradeRepo.findByTradeRef(req.tradeRef()).isPresent()) {
        throw new DuplicateTradeRefException(req.tradeRef());
    }

    var instrument = instRepo.findById(req.instrumentId())
            .orElseThrow(() ->
                    new TradeNotFoundException("Instrument not found"));

    var counterparty = cpRepo.findById(req.counterpartyId())
            .orElseThrow(() ->
                    new TradeNotFoundException("Counterparty not found"));

    Trade trade = new Trade();
    trade.setTradeRef(req.tradeRef());
    trade.setInstrument(instrument);
    trade.setCounterparty(counterparty);
    trade.setAssetClass(req.assetClass());
    trade.setSide(req.side());
    trade.setQuantity(req.quantity());
    trade.setPrice(req.price());
    trade.setTradeDate(req.tradeDate());
    trade.setStatus("PENDING");

    Trade saved = tradeRepo.save(trade);

    // These belong to later tickets.
    // Uncomment once ADV083 and ADV129 are implemented.

    /*
    metrics.incrementTradeCreated();

    metrics.recordTradeValue(
            req.quantity()
               .multiply(req.price())
               .doubleValue());

    events.publish(
            new TradeEvent(
                    UUID.randomUUID(),
                    saved.getTradeRef(),
                    TradeEvent.EventType.TRADE_CREATED,
                    Instant.now(),
                    actor,
                    null,
                    saved.getStatus()
            ));
    */

    return saved;
}
    public Trade update(Long id, TradeRequest req, String actor) {

    // Load existing trade or return 404
    Trade trade = tradeRepo.findById(id)
            .orElseThrow(() -> new TradeNotFoundException(String.valueOf(id)));

    // Prevent duplicate trade reference
    tradeRepo.findByTradeRef(req.tradeRef())
            .filter(t -> !t.getId().equals(id))
            .ifPresent(t -> {
                throw new DuplicateTradeRefException(req.tradeRef());
            });

    // Load referenced entities
    var instrument = instRepo.findById(req.instrumentId())
            .orElseThrow(() ->
                    new IllegalArgumentException("Instrument not found"));

    var counterparty = cpRepo.findById(req.counterpartyId())
            .orElseThrow(() ->
                    new IllegalArgumentException("Counterparty not found"));

    // Replace every mutable field
    trade.setTradeRef(req.tradeRef());
    trade.setInstrument(instrument);
    trade.setCounterparty(counterparty);
    trade.setAssetClass(req.assetClass());
    trade.setSide(req.side());
    trade.setQuantity(req.quantity());
    trade.setPrice(req.price());
    trade.setTradeDate(req.tradeDate());

    /*
    // Uncomment when ADV129 is implemented
    events.publish(
            new TradeEvent(
                    UUID.randomUUID(),
                    trade.getTradeRef(),
                    TradeEvent.EventType.TRADE_UPDATED,
                    Instant.now(),
                    actor,
                    null,
                    trade.getStatus()
            )
    );
    */

    return trade;
}
    

    @Transactional
public Trade updateStatus(Long id, String status, String actor) {

    Trade trade = tradeRepo.findById(id)
            .orElseThrow(() ->
                    new TradeNotFoundException(String.valueOf(id)));

    // Update only the status field
    trade.setStatus(status);

    /*
    // Uncomment when TICKET-ADV129 is implemented
    events.publish(
            new TradeEvent(
                    UUID.randomUUID(),
                    trade.getTradeRef(),
                    TradeEvent.EventType.TRADE_UPDATED,
                    Instant.now(),
                    actor,
                    null,
                    trade.getStatus()
            )
    );
    */

    return tradeRepo.save(trade);
}

    public void softDelete(Long id, String actor) {
        // TODO(TICKET-ADV067): load, call t.softDelete() (sets deleted_at), save,
        //   publish a TRADE_CANCELLED event.
        throw new UnsupportedOperationException("TICKET-ADV067");
    }

    @Transactional(readOnly = true)
public Page<Trade> list(
        LocalDate from,
        LocalDate to,
        String status,
        Long counterpartyId,
        Pageable pageable) {

    Specification<Trade> spec = Specification
            .where(hasStatus(status))
            .and(tradeDateBetween(from, to))
            .and(hasCounterparty(counterpartyId));

    return tradeRepo.findAll(spec, pageable);
}
}
