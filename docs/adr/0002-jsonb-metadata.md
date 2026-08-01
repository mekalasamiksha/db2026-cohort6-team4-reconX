# ADR-0002 — Store instrument metadata as JSONB

- Status: Accepted
- Date: 2026-07-30
- Deciders: ReconX team

## Context

Instrument attributes such as sector, issuer type, and custom tags are not fixed across the platform. Some values are known upfront, while others arrive later as the reconciliation workflow evolves. A rigid relational schema would require frequent schema changes for low-volume metadata fields and would slow down iteration for the analytics team.

## Decision

We will store instrument metadata in a PostgreSQL JSONB column on the instruments table. This allows ReconX to add or change metadata fields without a schema migration for each change while still preserving indexing and query support for structured lookups. We will keep only flexible, semi-structured fields in JSONB and continue to use relational columns for the core business keys and high-value attributes.

## Consequences

Positive:
- The schema can evolve quickly as new metadata needs appear.
- The team can query nested fields directly in PostgreSQL without introducing many columns.
- JSONB supports both flexible enrichment and efficient path-based lookups.

Negative:
- The data model becomes less strict than a fully normalized schema.
- Application code must validate metadata shape carefully.
- Some analytical queries may be less predictable than column-based access if the JSON structure is inconsistent.
