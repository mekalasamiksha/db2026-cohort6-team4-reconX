-- ============================================================================
-- TICKET-ADV010 — VWAP per instrument per day (window function)
-- ============================================================================

SELECT
    t.trade_ref,
    t.trade_date,
    t.instrument_id,
    i.symbol,
    t.quantity,
    t.price,
    t.quantity * t.price AS notional,

    SUM(t.price * t.quantity) OVER (
        PARTITION BY t.instrument_id, t.trade_date
    )
    /
    NULLIF(
        SUM(t.quantity) OVER (
            PARTITION BY t.instrument_id, t.trade_date
        ),
        0
    ) AS vwap

FROM trades t
JOIN instruments i
    ON i.id = t.instrument_id

WHERE t.deleted_at IS NULL
  AND t.asset_class = 'EQUITY'

ORDER BY
    t.trade_date DESC,
    t.instrument_id,
    t.created_at;


-- ============================================================================
-- TICKET-ADV011 — Recursive CTE: trade lifecycle rollup
-- (execution → confirmation → settlement → recon break → resolution)
-- ============================================================================
WITH RECURSIVE trade_lifecycle AS (
    -- anchor: every trade starts in execution
    SELECT
        t.id AS trade_id,
        t.trade_ref,
        1 AS stage,
        'EXECUTION' AS stage_name,
        t.created_at AS event_at,
        COALESCE(t.status, 'PENDING') AS event_status
    FROM trades t
    WHERE t.deleted_at IS NULL

    UNION ALL

    -- recursive: each next stage is derived from the previous stage
    SELECT
        tl.trade_id,
        tl.trade_ref,
        tl.stage + 1,
        next_event.stage_name,
        next_event.event_at,
        next_event.event_status
    FROM trade_lifecycle tl
    JOIN LATERAL (
        SELECT
            'CONFIRMATION'::text AS stage_name,
            tl.event_at AS event_at,
            'CONFIRMED'::text AS event_status
        WHERE tl.stage = 1

        UNION ALL

        SELECT
            'SETTLEMENT'::text AS stage_name,
            s.settlement_date::timestamp AS event_at,
            COALESCE(s.status, 'SETTLED')::text AS event_status
        FROM settlements s
        WHERE s.trade_id = tl.trade_id
          AND tl.stage = 2

        UNION ALL

        SELECT
            'RECON_BREAK'::text AS stage_name,
            rb.detected_at AS event_at,
            COALESCE(rb.status, 'OPEN')::text AS event_status
        FROM recon_breaks rb
        WHERE rb.trade_id = tl.trade_id
          AND tl.stage = 3

        UNION ALL

        SELECT
            'RESOLUTION'::text AS stage_name,
            COALESCE(rb.resolved_at, tl.event_at)::timestamp AS event_at,
            COALESCE(rb.status, 'RESOLVED')::text AS event_status
        FROM recon_breaks rb
        WHERE rb.trade_id = tl.trade_id
          AND tl.stage = 4
    ) next_event ON TRUE
    WHERE tl.stage < 5
)
SELECT
    trade_id,
    trade_ref,
    stage,
    stage_name,
    event_at,
    event_status
FROM trade_lifecycle
ORDER BY trade_id, stage;


-- ============================================================================
-- ADV008 — REFRESH the daily-summary materialised view (concurrent so it can
--         run while the dashboard is reading it)
-- ============================================================================
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_daily_recon_summary;


-- ============================================================================
-- ADV009 — JSONB lookup: which instruments have sector = 'Banking'?
-- ============================================================================
SELECT id, symbol, metadata
FROM instruments
WHERE metadata @> '{"sector":"Banking"}'::jsonb;
