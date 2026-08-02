# ADR-0003 — Use GIN with jsonb_path_ops instead of a B-tree index for metadata

- Status: Accepted
- Date: 2026-07-30
- Deciders: ReconX team

## Context

ReconX needs to query instrument metadata by nested fields such as sector and issuer attributes. A B-tree index is effective for exact equality on a single column, but it is not well suited to JSON path lookups over semi-structured data. We need query performance for metadata filters without turning every metadata field into a dedicated column.

## Decision

We will use a GIN index with jsonb_path_ops on the instruments metadata column rather than a B-tree index. This supports efficient path-based and containment queries over JSONB data and aligns with the operational need to search metadata in a flexible way while keeping the schema simple.

## Consequences

Positive:
- Path-based queries over instrument metadata are efficient enough for the expected workload.
- The GIN index supports flexible querying without additional schema changes.
- The approach fits the JSONB metadata strategy and reduces the need for many auxiliary columns.

Negative:
- GIN indexes are larger and more expensive to maintain than a simple B-tree index.
- The index choice is optimized for JSONB path queries, not for all possible metadata access patterns.
- Developers must be deliberate about query shapes so the index remains effective.
