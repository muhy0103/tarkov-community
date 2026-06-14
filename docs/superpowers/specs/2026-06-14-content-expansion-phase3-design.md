# Content Expansion Phase 3 Design

## Objective

Expand the demo data for the Tarkov community project so the homepage, catalog center, catalog detail pages, and related-discussion blocks feel noticeably richer during course demonstration.

## Scope

This phase is data-first. It does not change the database schema or the existing frontend layout. It adds a standalone SQL seed file that can be imported after the base seed, updates the data-source document, and verifies that the existing Spring Boot APIs and Vue pages can display the larger dataset.

## Content Shape

- Add common quest-chain samples across Prapor, Therapist, Skier, Mechanic, Ragman, Peacekeeper, Jaeger, Lightkeeper, Ref, Fence, and BTR Driver.
- Add common barter, quest, hideout, medical, tool, electronics, provisions, valuables, and keycard items.
- Add more complete weapon and ammunition samples using full-item image links where reliable.
- Add map extracts and loot areas for maps that still feel sparse.
- Add hideout stations and upgrade samples.
- Add community-style posts that link to catalog entries through `post_catalog_relation`.

## Data Principles

The site should stay community-oriented instead of becoming a dense wiki clone. Descriptions are short Chinese summaries focused on player decisions: route planning, task preparation, loadout choice, market retention, boss risk, hideout progression, raid review, and team-up planning.

## Verification

After importing the SQL, verify minimum counts for the expanded tables, build the Vue frontend, and browser-check catalog tabs plus the post list. The phase is considered complete when the seed imports repeatedly without duplicate failures and the UI renders the larger catalog without broken layout.
