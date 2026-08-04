package com.dbtraining.reconx.model;

import com.dbtraining.reconx.dto.TradeEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dlq_messages")
public class DlqMessage {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    private UUID eventId;

    @Column(name = "trade_ref", nullable = false, length = 30)
    private String tradeRef;

    @Column(name = "original_topic", nullable = false, length = 100)
    private String originalTopic;

    @Column(nullable = false)
    private int partition;

    @Column(nullable = false)
    private long offset;

    @Lob
    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "first_seen", nullable = false)
    private Instant firstSeen;

    public DlqMessage() {}

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() { return id; }
    public UUID getEventId() { return eventId; }
    public String getTradeRef() { return tradeRef; }
    public String getOriginalTopic() { return originalTopic; }
    public int getPartition() { return partition; }
    public long getOffset() { return offset; }
    public String getReason() { return reason; }
    public Instant getFirstSeen() { return firstSeen; }

    public TradeEvent getPayload() {
        try {
            return OBJECT_MAPPER.readValue(payloadJson, TradeEvent.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid DLQ payload for eventId=" + eventId, ex);
        }
    }

    public static final class Builder {
        private UUID eventId;
        private String tradeRef;
        private String originalTopic;
        private int partition;
        private long offset;
        private TradeEvent payload;
        private String reason;
        private Instant firstSeen;

        public Builder eventId(UUID value) { this.eventId = value; return this; }
        public Builder tradeRef(String value) { this.tradeRef = value; return this; }
        public Builder originalTopic(String value) { this.originalTopic = value; return this; }
        public Builder partition(int value) { this.partition = value; return this; }
        public Builder offset(long value) { this.offset = value; return this; }
        public Builder payload(TradeEvent value) { this.payload = value; return this; }
        public Builder reason(String value) { this.reason = value; return this; }
        public Builder firstSeen(Instant value) { this.firstSeen = value; return this; }

        public DlqMessage build() {
            DlqMessage message = new DlqMessage();
            message.eventId = eventId;
            message.tradeRef = tradeRef;
            message.originalTopic = originalTopic;
            message.partition = partition;
            message.offset = offset;
            message.reason = reason;
            message.firstSeen = firstSeen;
            try {
                message.payloadJson = payload == null ? null : OBJECT_MAPPER.writeValueAsString(payload);
            } catch (Exception ex) {
                throw new IllegalStateException("Unable to serialize DLQ payload for eventId=" + eventId, ex);
            }
            return message;
        }
    }
}