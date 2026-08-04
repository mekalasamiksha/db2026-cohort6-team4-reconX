package com.dbtraining.reconx.repository.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import java.time.Instant;

/**
 * TICKET-ADV132 / ADV137 — Append-only audit row written by AuditEventConsumer.
 * Used for the event-sourcing rebuild of trade state (TICKET-ADV137).
 */
@Entity
@Table(name = "audit_log")
public class AuditLogEntry {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    private String eventId;

    @Column(name = "trade_ref", nullable = false, length = 30)
    private String tradeRef;

    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    @Column(length = 100)
    private String actor;

    // No @Lob — Hibernate 6 + Postgres treats @Lob String as OID column,
    // but Liquibase translates CLOB to TEXT. columnDefinition keeps both DBs
    // happy (H2 accepts TEXT in Postgres mode, Postgres uses it natively).
    @Lob
    @Column(name = "before_state", columnDefinition = "TEXT")
    private String beforeState;

    @Lob
    @Column(name = "after_state", columnDefinition = "TEXT")
    private String afterState;

    public static Builder builder() {
        return new Builder();
    }

    public AuditLogEntry() {}

    public AuditLogEntry(String eventId, String tradeRef, String eventType,
                         Instant ts, String actor, String before, String after) {
        this.eventId = eventId;
        this.tradeRef = tradeRef;
        this.eventType = eventType;
        this.eventTimestamp = ts;
        this.actor = actor;
        this.beforeState = before;
        this.afterState = after;
    }

    public Long getId()              { return id; }
    public String getEventId()       { return eventId; }
    public String getTradeRef()      { return tradeRef; }
    public String getEventType()     { return eventType; }
    public Instant getEventTimestamp(){ return eventTimestamp; }
    public String getActor()         { return actor; }
    public String getBeforeState()   { return beforeState; }
    public String getAfterState()    { return afterState; }

    public String getOperation()     { return eventType; }

    public JsonNode getAfterData() {
        if (afterState == null || afterState.isBlank()) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readTree(afterState);
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid after_state JSON for audit event " + eventId, ex);
        }
    }

    public static final class Builder {
        private String eventId;
        private String tradeRef;
        private String operation;
        private Instant occurredAt;
        private String actor;
        private JsonNode before;
        private JsonNode after;

        public Builder eventId(String value) {
            this.eventId = value;
            return this;
        }

        public Builder tradeRef(String value) {
            this.tradeRef = value;
            return this;
        }

        public Builder operation(String value) {
            this.operation = value;
            return this;
        }

        public Builder occurredAt(Instant value) {
            this.occurredAt = value;
            return this;
        }

        public Builder actor(String value) {
            this.actor = value;
            return this;
        }

        public Builder before(JsonNode value) {
            this.before = value;
            return this;
        }

        public Builder after(JsonNode value) {
            this.after = value;
            return this;
        }

        public AuditLogEntry build() {
            return new AuditLogEntry(
                    eventId,
                    tradeRef,
                    operation,
                    occurredAt,
                    actor,
                    before == null ? null : before.toString(),
                    after == null ? null : after.toString());
        }
    }
}
