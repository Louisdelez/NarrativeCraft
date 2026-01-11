# Feature Specification: NarrativeCraft Production Hardening

**Feature Branch**: `001-production-hardening`
**Created**: 2026-01-09
**Status**: Draft
**Input**: Audit complet et mise en production professionnelle du mod NarrativeCraft

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Prioritized Issue List for Maintainers (Priority: P1)

As a mod maintainer, I want a comprehensive, prioritized list of all critical bugs,
technical debt, performance issues, and security risks so that I can stabilize
the mod efficiently and address the most impactful problems first.

**Why this priority**: Without visibility into existing problems, stabilization
work is blind guessing. This audit provides the foundation for all subsequent
improvement work.

**Independent Test**: Can be fully tested by reviewing the generated audit report
and verifying each identified issue is reproducible, categorized, and prioritized.
Delivers a clear roadmap for stabilization.

**Acceptance Scenarios**:

1. **Given** the NarrativeCraft codebase exists, **When** the audit is performed,
   **Then** a documented list of critical bugs (crashes, state corruption, logic
   errors) is produced with reproduction steps and severity ratings.

2. **Given** the codebase has been analyzed, **When** technical debt is catalogued,
   **Then** each debt item includes: location, SRP/coupling/naming violations,
   and refactoring priority.

3. **Given** performance analysis is complete, **When** the report is reviewed,
   **Then** tick-allocation hotspots, O(n) lookups, and missing caches are identified
   with estimated impact.

4. **Given** security analysis is complete, **When** risks are documented,
   **Then** command execution vulnerabilities, input validation gaps, and script
   sandbox issues are listed with remediation guidance.

5. **Given** the GitHub repository is analyzed, **When** the audit includes repo
   health, **Then** issue triage status, release practices, documentation gaps,
   and onboarding friction points are documented.

---

### User Story 2 - Player State Always Recovers (Priority: P2)

As a player, I want the HUD, camera, and input controls to always return to
normal after a narrative scene ends (whether successfully or due to failure)
so that I am never stuck unable to play.

**Why this priority**: Stuck states are the most visible user-facing bug and
destroy player trust. Guaranteed cleanup is a core reliability requirement
from the constitution.

**Independent Test**: Can be tested by running various narrative scenes,
interrupting them (disconnection, teleport, crash), and verifying player state
is restored in all cases.

**Acceptance Scenarios**:

1. **Given** a player is in a narrative scene with HUD overlay active, **When**
   the scene completes normally, **Then** the HUD overlay is removed and default
   HUD is restored.

2. **Given** a player has camera lock active during a cutscene, **When** an error
   occurs in the scene script, **Then** camera control is returned to the player
   and an error message is shown.

3. **Given** a player has input captured (dialogue choices), **When** the server
   restarts or player disconnects, **Then** upon reconnection, input control is
   restored and no orphaned state remains.

4. **Given** any player state modification (HUD, camera, input, movement), **When**
   the modifying code executes, **Then** a cleanup handler is registered before
   the modification occurs.

---

### User Story 3 - Clear Errors and Complete Documentation for Creators (Priority: P3)

As a map creator/storyteller, I want clear error messages with context and solutions,
plus complete documentation of all Ink tags and commands, so that I can write
narrative scripts without guessing or trial-and-error.

**Why this priority**: The mod's value comes from creator content. Poor creator
experience limits adoption and content quality. This directly implements the
"Creator UX" constitution principle.

**Independent Test**: Can be tested by intentionally introducing common errors
in Ink scripts and verifying error messages identify the problem, location,
and fix. Documentation can be validated against all implemented tags.

**Acceptance Scenarios**:

1. **Given** a creator writes an Ink script with an invalid tag, **When** the
   script is loaded, **Then** the error message includes: what failed (invalid
   tag name), where (file and line), why (tag not in whitelist), and how to fix
   (list valid tags or check spelling).

2. **Given** a creator uses a tag with wrong parameters, **When** the script
   is parsed, **Then** the error shows expected parameters vs provided parameters
   with an example of correct usage.

3. **Given** a creator needs to learn available features, **When** they access
   documentation, **Then** every Ink tag, command, and configuration option is
   documented with description, parameters, and examples.

4. **Given** a creator wants to see features in action, **When** they access the
   example pack, **Then** annotated sample scripts demonstrate all major features
   with inline comments explaining usage.

---

### User Story 4 - CI Prevents Regressions (Priority: P4)

As a developer, I want a CI pipeline that blocks merges on build failures, lint
errors, and test failures so that regressions are caught before reaching the
main branch.

**Why this priority**: Automated quality gates enforce the "Quality" constitution
principle without manual discipline. This enables confident contribution from
multiple developers.

**Independent Test**: Can be tested by pushing commits with intentional build
errors, lint violations, or failing tests and verifying the CI blocks merge.

**Acceptance Scenarios**:

1. **Given** a PR is opened, **When** CI runs, **Then** the build is verified
   for both Fabric and NeoForge loaders.

2. **Given** code contains lint violations, **When** CI runs, **Then** the
   pipeline fails and violations are reported with file/line locations.

3. **Given** a test fails, **When** CI runs, **Then** the pipeline fails and
   the failing test is clearly identified.

4. **Given** all checks pass, **When** a maintainer approves the PR, **Then**
   merge is permitted.

5. **Given** a release is tagged, **When** CI runs release workflow, **Then**
   artifacts are built for all supported loaders and published.

---

### Edge Cases

- What happens when a scene references a missing asset file?
  - System gracefully exits scene, restores player state, logs error with
    asset path and expected location.

- How does the system handle rapid zone trigger entry/exit?
  - Debounce or rate-limit trigger events to prevent state thrashing; no
    crash regardless of entry/exit frequency.

- What happens when a player disconnects mid-scene on a server?
  - Server-side cleanup removes orphaned session state; client reconnection
    does not resume interrupted scene (clean slate).

- How does the system behave when Ink story file is malformed?
  - Parse errors are caught before execution; detailed error with line number
    and syntax issue; no partial execution.

- What happens when a whitelisted command fails at runtime?
  - Error is logged with command, context, and failure reason; scene can
    specify fallback behavior or gracefully exit.

## Requirements *(mandatory)*

### Functional Requirements

**Audit & Analysis**

- **FR-001**: System MUST produce a documented audit report identifying all
  critical bugs with reproduction steps, severity, and affected components.

- **FR-002**: System MUST catalogue technical debt items including architecture
  violations, SRP breaches, naming inconsistencies, and code duplication with
  file locations and refactoring priority.

- **FR-003**: System MUST identify performance issues including tick-time
  allocations, O(n) lookups in hot paths, and missing caches with estimated
  impact on TPS/FPS.

- **FR-004**: System MUST document security risks including command execution
  vulnerabilities, input validation gaps, and script sandbox weaknesses with
  remediation guidance.

- **FR-005**: System MUST analyze repository health including issue triage
  status, release practices, documentation coverage, and contributor onboarding
  experience.

**Reliability & State Management**

- **FR-006**: System MUST guarantee that all player state modifications (HUD,
  camera, input, movement) have cleanup handlers registered before modification
  occurs.

- **FR-007**: System MUST restore player to default state when a scene fails
  due to any error (parse, runtime, missing asset, network).

- **FR-008**: System MUST NOT leave orphaned session state when players
  disconnect, teleport, or when the server restarts.

- **FR-009**: System MUST NOT crash the server regardless of trigger/area
  edge cases (rapid entry/exit, world unload, null player references).

**Creator Experience**

- **FR-010**: Error messages MUST include four components: what failed, where
  (file/line/tag), why (likely cause), and how to fix (solution or suggestion).

- **FR-011**: System MUST provide complete reference documentation for all
  Ink tags, commands, and configuration options, updated with each release.

- **FR-012**: System MUST include an example content pack demonstrating all
  features with annotated source files.

- **FR-013**: System MUST support hot-reload of Ink stories during development
  without requiring game restart where technically feasible.

**Quality & CI/CD**

- **FR-014**: CI pipeline MUST verify builds for all supported loaders (Fabric,
  NeoForge) on every push.

- **FR-015**: CI pipeline MUST run linting and block merge on violations.

- **FR-016**: CI pipeline MUST run tests and block merge on failures.

- **FR-017**: CI pipeline MUST produce release artifacts for all supported
  loaders when a release tag is pushed.

- **FR-018**: Every fixed bug MUST have a corresponding non-regression test.

**Architecture & Standards**

- **FR-019**: Codebase MUST maintain clear separation between common/core logic
  and loader-specific adapters (no loader imports in core).

- **FR-020**: All thresholds, timeouts, and configuration values MUST be defined
  as named constants or configuration options (no magic numbers).

- **FR-021**: Breaking changes MUST be documented in CHANGELOG with migration
  notes; deprecation warnings MUST precede removal by at least one minor version.

**Performance**

- **FR-022**: Hot paths (tick handlers, render hooks) MUST NOT allocate new
  objects per tick.

- **FR-023**: Story state, active triggers, and player session data MUST be
  accessible in constant time O(1).

- **FR-024**: System MUST provide a runtime flag to enable performance metrics
  output without recompilation.

**Security**

- **FR-025**: Only whitelisted Ink tags and Minecraft commands MUST be executable;
  unknown tags/commands MUST be rejected with clear error.

- **FR-026**: All user inputs (tag parameters, dialogue choices, file paths)
  MUST be validated and sanitized before processing.

- **FR-027**: Logs MUST NOT contain sensitive player information (credentials,
  tokens, PII); use redaction or hashing where context is needed.

### Key Entities

- **Audit Report**: A structured document containing categorized findings (bugs,
  debt, performance, security, repo health) with severity, priority, location,
  and remediation guidance for each item.

- **Player Session State**: The runtime state tracking a player's narrative
  engagement including active scene, HUD modifications, camera state, input
  captures, and registered cleanup handlers.

- **Ink Tag**: A narrative script command with name, parameters, execution
  behavior, and documentation including description, parameter spec, and examples.

- **Quality Gate**: A CI check that validates a specific quality dimension (build,
  lint, test) and blocks or allows merge based on results.

- **Cleanup Handler**: A registered callback that restores a specific aspect of
  player state (HUD, camera, input) when triggered by scene completion, error,
  or disconnection.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of identified critical bugs are documented with reproduction
  steps and severity rating within the audit report.

- **SC-002**: Players can complete any narrative scene and return to normal
  gameplay state within 1 second of scene end (success or failure).

- **SC-003**: 0 instances of stuck player state (HUD, camera, input) occur
  during testing across 50+ scene completions with various interruption scenarios.

- **SC-004**: 100% of Ink tags and commands are documented with description,
  parameters, and at least one usage example.

- **SC-005**: Creators can identify and fix script errors within 2 minutes on
  average, based on error message clarity (measured via user testing).

- **SC-006**: CI pipeline catches 100% of build failures, lint violations, and
  test failures before merge (verified via intentional failure injection).

- **SC-007**: Release artifacts are produced for all supported loaders within
  10 minutes of release tag push.

- **SC-008**: Narrative system adds less than 0.5ms per tick to server processing
  time when active (verified via profiling).

- **SC-009**: Story state lookups complete in under 0.1ms (constant time verified
  via benchmarks).

- **SC-010**: 0 security vulnerabilities from input validation or command
  execution remain after remediation (verified via security testing).

## Assumptions

- The existing NarrativeCraft codebase is accessible and in a buildable state.
- Git history and GitHub repository are available for analysis.
- The mod already has basic functionality (story loading, tag execution, HUD
  rendering) that needs stabilization rather than creation from scratch.
- Test infrastructure (JUnit or equivalent) can be integrated into the Gradle
  build.
- Multi-loader architecture (common + adapters) is the target structure even
  if not fully implemented currently.

## Out of Scope

- Adding major new narrative features (new tag types, new rendering modes)
  unless required for stability.
- Rewriting the mod from scratch.
- Supporting mod loaders other than Fabric and NeoForge.
- Multiplayer-specific features beyond basic server stability (synchronized
  narratives across players, etc.) unless critical bugs exist.
- Localization/internationalization of documentation (English only for initial
  release).
