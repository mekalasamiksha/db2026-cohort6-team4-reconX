-- ============================================================================
-- TICKET-ADV007 — Convert trades to monthly range-partitioned table (Postgres)
--
-- WARNING: destructive. Run in a maintenance window — drops and recreates the
-- trades table as a partitioned parent, then creates the monthly children.
-- ============================================================================

-- 1. Drop the non-partitioned trades table created in 002 (this also removes
--    the settlements FK on Postgres via CASCADE).
DROP TABLE IF EXISTS trades CASCADE;

-- 2. Create partitioned parent (same columns, plus partition-key-compatible
--    unique constraints).
CREATE TABLE trades (
    id              BIGSERIAL,
    trade_ref       VARCHAR(30)   NOT NULL,
    instrument_id   BIGINT        NOT NULL REFERENCES instruments(id),
    counterparty_id BIGINT        NOT NULL REFERENCES counterparties(id),
    asset_class     VARCHAR(20)   NOT NULL,
    side            VARCHAR(4)    NOT NULL,
    quantity        NUMERIC(18,4) NOT NULL,
    price           NUMERIC(18,4) NOT NULL,
    trade_date      DATE          NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    modified_at     TIMESTAMPTZ,
    PRIMARY KEY (id, trade_date),
    UNIQUE (trade_ref, trade_date)
) PARTITION BY RANGE (trade_date);

CREATE INDEX idx_trades_trade_date ON trades (trade_date);
CREATE INDEX idx_trades_status     ON trades (status);

-- 3. Rolling 12 monthly partitions (current month - 11 .. current month).
DO $$
DECLARE
    start_month DATE := (date_trunc('month', CURRENT_DATE) - INTERVAL '11 months')::DATE;
    m       INT;
    p_start DATE;
    p_end   DATE;
    p_name  TEXT;
BEGIN
    FOR m IN 0..11 LOOP
        p_start := (start_month + (m || ' months')::INTERVAL)::DATE;
        p_end   := (p_start     + INTERVAL '1 month')::DATE;
        p_name  := 'trades_' || to_char(p_start, 'YYYY_MM');
        EXECUTE format(
            'CREATE TABLE IF NOT EXISTS %I PARTITION OF trades FOR VALUES FROM (%L) TO (%L);',
            p_name, p_start, p_end
        );
    END LOOP;
END $$;

-- 4. Seed / migrate data into the partitioned parent after partitions exist.
-- INSERT INTO trades SELECT * FROM trades_legacy;

-- 5. Drop legacy table after verification if you used the rename/copy path.
-- DROP TABLE trades_legacy;
