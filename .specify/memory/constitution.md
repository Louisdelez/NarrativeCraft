<!--
================================================================================
SYNC IMPACT REPORT
================================================================================
Version change: 0.0.0 → 1.0.0 (MAJOR - Initial constitution establishment)

Modified principles: N/A (initial creation)

Added sections:
  - 7 Core Principles (Security, Reliability, Performance, Quality, Standards,
    Creator UX, Maintainability)
  - Additional Constraints section
  - Development Workflow section
  - Governance rules

Removed sections: N/A (initial creation)

Templates requiring updates:
  ✅ .specify/templates/plan-template.md - Constitution Check section compatible
  ✅ .specify/templates/spec-template.md - Requirements section aligned
  ✅ .specify/templates/tasks-template.md - Task categorization compatible
  ✅ .specify/templates/checklist-template.md - No updates needed
  ✅ .specify/templates/agent-file-template.md - No updates needed

Follow-up TODOs: None
================================================================================
-->

# NarrativeCraft Constitution

## Core Principles

### I. Security

All Ink script execution and Minecraft command dispatch MUST operate under a strict
permission policy:

- **Whitelist enforcement**: Only explicitly permitted tags and commands are executable;
  any unknown tag or command MUST be rejected with a clear error message
- **No privilege escalation**: Scripts MUST NOT gain elevated permissions beyond
  their declared scope; operator-level commands require explicit operator context
- **Input validation**: All user-provided inputs (tag parameters, dialogue choices,
  external file paths) MUST be validated and sanitized before processing
- **Sensitive data protection**: Logs MUST NOT contain player credentials, session
  tokens, or personally identifiable information; use redaction or hashing where
  context is needed for debugging

**Rationale**: Minecraft mods execute in a shared environment where malicious or
malformed content can compromise server integrity and player data.

### II. Reliability

Player state MUST remain recoverable at all times:

- **No stuck states**: HUD overlays, camera locks, and input captures MUST have
  guaranteed cleanup paths; every state modification MUST be wrapped in try/finally
  or equivalent lifecycle hooks
- **Scene failure fallback**: If a narrative scene fails (missing asset, parse error,
  runtime exception), the system MUST gracefully exit the scene and restore default
  player state rather than leaving the player trapped
- **Trigger/area stability**: Zone triggers and area detection MUST NOT cause server
  crashes regardless of edge cases (player teleport, world unload, rapid re-entry);
  defensive null checks and boundary validation are mandatory
- **Cleanup guarantee**: Every action that modifies world or player state MUST have
  a corresponding rollback or cleanup registered before execution

**Rationale**: Narrative systems interact deeply with gameplay; failures that trap
players or crash servers destroy trust and playability.

### III. Performance

The mod MUST NOT degrade server TPS or client FPS under normal operation:

- **Zero avoidable allocations in ticks**: Hot paths (tick handlers, render hooks)
  MUST NOT allocate new objects per tick; use pooling, caching, or pre-allocation
- **O(1) critical lookups**: Story state, active triggers, and player session data
  MUST be accessible in constant time; avoid linear scans over large collections
- **Profiling hooks**: A simple runtime flag MUST enable performance metrics output
  (tick time per system, memory allocation delta) without requiring recompilation
- **Lazy loading**: Assets (Ink stories, dialogue audio, textures) MUST load on
  demand or in background threads, never blocking the main thread during gameplay

**Rationale**: Minecraft servers and clients are performance-sensitive; mods that
cause lag are quickly disabled by users.

### IV. Quality

Code quality is enforced through automated gates:

- **CI mandatory**: Every push MUST trigger build verification, linting, and test
  execution; merge is blocked if any gate fails
- **Unit test coverage**: Parser logic, tag handlers, and state machine transitions
  MUST have unit tests covering happy path, error cases, and edge conditions
- **Non-regression**: Every fixed bug MUST have a corresponding test that would
  fail if the bug reappeared
- **Code review required**: No merge to main without at least one approving review;
  reviewers MUST verify compliance with this constitution

**Rationale**: Production stability requires verifiable quality; manual testing alone
is insufficient for complex narrative systems.

### V. Standards

The codebase MUST follow consistent architectural and stylistic conventions:

- **Architecture**: Clear separation between common/core logic and loader-specific
  adapters (Forge, Fabric, NeoForge); core MUST NOT import loader-specific classes
- **Readability**: Code MUST be self-documenting with meaningful names; comments
  explain "why", not "what"; complex algorithms include inline rationale
- **Naming conventions**: Classes use PascalCase, methods use camelCase, constants
  use UPPER_SNAKE_CASE; package structure reflects module boundaries
- **No magic numbers**: All thresholds, timeouts, and configuration values MUST be
  defined as named constants or loaded from configuration files with documentation

**Rationale**: Consistent standards reduce cognitive load and enable safe
contribution from multiple developers over time.

### VI. Creator UX

Content creators (map makers, story authors) are first-class users:

- **Error messages**: Every error MUST include: (1) what failed, (2) where it failed
  (file, line, tag), (3) why it likely failed, (4) how to fix it; avoid cryptic
  stack traces in user-facing output
- **Documentation**: Complete reference documentation for all tags, commands, and
  configuration options; updated with every release
- **Example pack**: A reference content pack demonstrating all features with
  annotated source files
- **Stable workflow**: The author's iteration cycle (edit story → test in-game)
  MUST be fast and reliable; hot-reload where possible, clear cache invalidation

**Rationale**: The mod's value is realized through creator content; poor creator
experience limits adoption and content quality.

### VII. Maintainability

The codebase MUST remain manageable as it evolves:

- **Single Responsibility Principle**: Each module, class, and method MUST have one
  clear purpose; split when responsibilities diverge
- **Structured logging**: All log entries MUST include context (session ID, story
  name, player UUID hash) in a parseable format; avoid unstructured string dumps
- **Version compatibility**: Breaking changes MUST be documented in CHANGELOG with
  migration notes; deprecation warnings MUST precede removal by at least one
  minor version
- **Technical debt tracking**: Known shortcuts, TODOs, and architectural concerns
  MUST be tracked via GitHub issues with a `tech-debt` label; debt is reviewed
  each release cycle

**Rationale**: Long-lived projects accumulate complexity; explicit discipline
prevents gradual decay.

## Additional Constraints

### Technology Stack

- **Primary language**: Java (Minecraft version target)
- **Build system**: Gradle with multi-loader support
- **Narrative engine**: Ink runtime (Java port)
- **Target platforms**: Forge, Fabric, NeoForge (via common abstraction)

### Security Boundaries

- **Script sandbox**: Ink scripts execute in a restricted context with no direct
  Java reflection or file system access
- **Command proxy**: All Minecraft commands dispatched by scripts go through a
  validation layer that checks permissions before execution
- **External resources**: External file loading (resource packs, data packs) is
  limited to designated directories with path traversal prevention

### Performance Budgets

- **Tick overhead**: Less than 0.5ms per tick when narrative is active
- **Memory footprint**: Less than 50MB additional heap for loaded stories
- **Startup impact**: Less than 500ms added to game load time

## Development Workflow

### Branch Strategy

- `main`: Protected, always deployable, requires passing CI and review
- `feature/*`: Development branches for new features
- `fix/*`: Bug fix branches
- `release/*`: Release preparation branches

### Review Checklist

Every PR review MUST verify:

1. No security violations (Principle I)
2. State cleanup is guaranteed (Principle II)
3. No tick allocations added (Principle III)
4. Tests cover new code paths (Principle IV)
5. Code follows naming/architecture standards (Principle V)
6. Error messages are actionable (Principle VI)
7. No undocumented technical debt introduced (Principle VII)

### Release Process

1. All tests pass on release branch
2. CHANGELOG updated with user-facing changes
3. Migration notes added for breaking changes
4. Example pack validated against new version
5. Documentation site updated
6. Version tag created and pushed

## Governance

This constitution is the authoritative source for project standards. All other
documentation, code comments, and tribal knowledge defer to these principles.

### Amendment Process

1. Propose amendment via GitHub issue with `constitution` label
2. Discussion period of at least 7 days
3. Amendment requires approval from at least two maintainers
4. Upon approval: update this document, increment version, update dependent
   templates if affected
5. Announce in release notes

### Versioning Policy

- **MAJOR**: Principle removed, redefined, or made incompatible
- **MINOR**: New principle added, existing guidance materially expanded
- **PATCH**: Clarifications, typo fixes, non-semantic wording improvements

### Compliance

- CI pipelines SHOULD include automated checks for constitution violations where
  feasible (e.g., lint rules for naming, test coverage thresholds)
- Manual review serves as the final compliance gate
- Violations discovered post-merge MUST be addressed in the next release cycle

**Version**: 1.0.0 | **Ratified**: 2026-01-09 | **Last Amended**: 2026-01-09
