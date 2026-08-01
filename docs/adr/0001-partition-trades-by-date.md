# ADR-0001 — Partition trades by trade_date

- Status: Accepted
- Date: 2026-07-30
- Deciders: ReconX team

## Context

ReconX ingests roughly 50,000 trades per day and keeps data for five years, which implies a steady-state volume of about 91 million rows in the trades table. Most operational queries are date-bounded, including dashboard lookups, recon runs, and analyst investigations for a single day or month. A single unpartitioned table would force broader scans and make retention and archival more expensive.

## Decision

We will partition the trades table by range on trade_date, using one monthly partition per calendar month. The partition key is trade_date, and child partitions are named using the year and month pattern so maintenance can be automated. A default partition will capture any out-of-range inserts so writes continue to succeed while operators investigate unexpected data.

## Consequences

Positive:
- Partition pruning reduces the amount of data scanned for typical date-window queries.
- Retention and archival become simpler because old partitions can be detached and archived as units.
- Indexes and maintenance operations remain smaller and more predictable per partition.

Negative:
- The primary key and uniqueness strategy become more complex because trade_date is now part of the partitioning boundary.
- Partition creation and monitoring become recurring operational tasks.
- Some cross-partition reporting needs explicit logic to combine results from multiple partitions.
