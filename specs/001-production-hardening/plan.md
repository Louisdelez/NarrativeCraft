# Implementation Plan: NarrativeCraft Production Hardening

**Branch**: `001-production-hardening` | **Date**: 2026-01-09 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-production-hardening/spec.md`

## Summary

This plan covers the complete stabilization and production-hardening of NarrativeCraft,
transforming it from MVP state to a production-ready, stable, maintainable, documented,
and tested Minecraft mod. The work is organized into 9 phases covering: baseline freeze,
technical audit, architecture improvements, bug fixes, performance optimization, security
hardening, quality assurance, documentation, and release preparation.

**Technical Approach**: Incremental improvement of existing codebase (328 Java files across
common/fabric/neoforge modules) without full rewrite. Focus on state machine centralization,
guaranteed cleanup handlers, CI/CD gates, and comprehensive documentation.

## Technical Context

**Language/Version**: Java 21
**Primary Dependencies**: Blade-Ink 1.2.1+nc (Ink runtime), Fabric API 0.140.0+1.21.11,
NeoForge 21.11.12-beta, MixinExtras 0.3.5, EmoteCraft API 3.2.0
**Storage**: File-based (JSON/TOML for story data, configuration)
**Testing**: None currently; JUnit 5 + Mockito to be added
**Target Platform**: Minecraft 1.21.11 (Fabric + NeoForge loaders)
**Project Type**: Multi-module Gradle (common + fabric + neoforge)
**Performance Goals**: <0.5ms tick overhead, O(1) state lookups, <50MB heap delta
**Constraints**: No loader imports in common module, backward-compatible changes only
**Scale/Scope**: 328 Java files, 33 mixins, 5 core managers, single-player priority

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Evidence / Action Required |
|-----------|--------|---------------------------|
| I. Security | PASS | Plan includes whitelist validation (Phase 5), input sanitization, command proxy |
| II. Reliability | PASS | Central state machine with try/finally cleanup (Phase 2), scene fallback (Phase 3) |
| III. Performance | PASS | Tick allocation audit (Phase 4), O(1) lookups, profiling hooks |
| IV. Quality | PASS | CI/CD pipeline (Phase 6), unit tests, non-regression tests |
| V. Standards | PASS | Architecture refactor (Phase 2) enforces common/adapter separation |
| VI. Creator UX | PASS | Error message overhaul (Phase 5), documentation (Phase 7), example pack |
| VII. Maintainability | PASS | SRP refactors (Phase 2), structured logging, CHANGELOG |

**Gate Result**: PASSED - All 7 principles addressed by planned work.

## Project Structure

### Documentation (this feature)

```text
specs/001-production-hardening/
├── plan.md              # This file
├── research.md          # Phase 0 output: codebase audit findings
├── data-model.md        # Phase 1 output: entity/state definitions
├── quickstart.md        # Phase 1 output: development setup guide
├── contracts/           # Phase 1 output: internal API contracts
│   ├── state-machine.md
│   ├── cleanup-handler.md
│   └── ink-action-api.md
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

**Target Repository**: `/home/louis/Documents/MinecraftModsNarrative/NarrativeCraft/`

```text
NarrativeCraft/
├── build.gradle                    # Root build config (Spotless)
├── settings.gradle                 # Module declarations
├── gradle.properties               # MC 1.21.11, Java 21
├── buildSrc/                       # Custom Gradle plugins
│   └── src/main/groovy/
│       ├── multiloader-common.gradle
│       └── multiloader-loader.gradle
│
├── common/                         # Platform-agnostic code (287 files)
│   └── src/main/java/fr/loudo/narrativecraft/
│       ├── NarrativeCraftMod.java          # Singleton entry point
│       ├── api/inkAction/                  # Public Ink action API
│       ├── narrative/                      # Core narrative system
│       │   ├── chapter/                    # Chapter/scene management
│       │   ├── character/                  # Character data
│       │   ├── dialog/                     # Dialog rendering
│       │   ├── story/                      # Story content
│       │   ├── keyframes/                  # Animation keyframes
│       │   ├── recording/                  # Cutscene recording
│       │   ├── playback/                   # Cutscene playback
│       │   └── session/                    # Player sessions
│       ├── screens/                        # UI screens
│       ├── controllers/                    # Business logic
│       ├── platform/                       # Multiloader abstraction
│       ├── events/                         # Event handlers
│       ├── commands/                       # In-game commands
│       └── mixin/                          # Common mixins (33 total)
│
├── fabric/                         # Fabric loader module
│   └── src/main/java/fr/loudo/narrativecraft/
│       ├── NarrativeCraftFabric.java
│       ├── platform/FabricPlatformHelper.java
│       └── events/
│
├── neoforge/                       # NeoForge loader module
│   └── src/main/java/fr/loudo/narrativecraft/
│       ├── NarrativeCraftNeoForge.java
│       ├── platform/NeoForgePlatformHelper.java
│       └── events/
│
└── .github/                        # GitHub configuration (to be added)
    └── workflows/
        ├── ci.yml                  # Build + test + lint
        └── release.yml             # Artifact publishing
```

**Structure Decision**: Existing multi-module architecture is sound and aligns with
Constitution Principle V (Standards). No structural changes needed; focus on internal
refactoring within common module.

## Complexity Tracking

> No constitution violations requiring justification. Architecture follows established
> multi-loader patterns.

## Phase 0: Baseline & Scope

### Deliverables

1. **Scope & Guarantees Document** (`docs/scope-guarantees.md`)
   - Minecraft version: 1.21.11 (range: [1.21.11, 1.22))
   - Singleplayer: Stable (priority)
   - Multiplayer: Experimental (not prioritized)
   - Feature freeze: Bugfix only during hardening

2. **Narrative Entry Points Inventory**
   - Ink story loading: `StoryManager`, `ChapterManager`
   - Zone triggers: `AreaTrigger` system
   - Commands: `/narrativecraft` command tree
   - UI entry: Main screen, story editor screens
   - Recording/playback: `RecordingManager`, `PlaybackManager`

3. **Branch**: `production-hardening` (created via /speckit.specify)

## Phase 1: Technical Audit

### Audit Categories

| Category | Files to Audit | Key Concerns |
|----------|----------------|--------------|
| Core Managers | 5 manager classes | Singleton patterns, thread safety |
| State Management | `session/`, `playback/`, `recording/` | Cleanup guarantees, state leaks |
| Ink Integration | `story/inkAction/`, `api/inkAction/` | Validation, error handling |
| UI/HUD | `screens/`, `hud/`, `dialog/` | State restoration on exit |
| Mixins | 33 mixin classes | Compatibility, null safety |
| Event Handlers | `events/` packages | Exception handling |

### Audit Output Format

```markdown
## [Component Name]

**Location**: `path/to/file.java:line`
**Severity**: Critical | Major | Minor
**Category**: Bug | Tech Debt | Performance | Security

**Issue**: [Description]
**Impact**: [What breaks or degrades]
**Reproduction**: [Steps if applicable]
**Remediation**: [Suggested fix]
```

## Phase 2: Architecture Improvements

### Central State Machine

**States**:
- `GAMEPLAY` - Normal player control
- `DIALOGUE` - Dialog UI active, input captured
- `CUTSCENE` - Camera locked, playback running
- `RECORDING` - Recording mode active
- `PLAYBACK` - Recorded actions playing

**Transitions**:
```
GAMEPLAY ←→ DIALOGUE
GAMEPLAY ←→ CUTSCENE
GAMEPLAY ←→ RECORDING
GAMEPLAY ←→ PLAYBACK
```

**API Contract**:
```java
public interface NarrativeStateManager {
    NarrativeState getCurrentState();
    void enterState(NarrativeState state, StateContext context);
    void exitState();  // Always returns to GAMEPLAY
    void registerCleanupHandler(CleanupHandler handler);
}
```

### Cleanup Handler System

```java
public interface CleanupHandler {
    void cleanup();  // Called on state exit or error
    int priority();  // Lower = runs first (HUD before camera)
}
```

**Registration Pattern**:
```java
try {
    stateManager.registerCleanupHandler(() -> resetHUD());
    modifyHUD();
    // ... narrative logic
} finally {
    stateManager.exitState();  // Triggers all cleanup handlers
}
```

### Refactoring Targets

| Class | Current LOC | Issue | Action |
|-------|-------------|-------|--------|
| `NarrativeCraftMod` | ~300+ | God class with 5 managers | Extract to `NarrativeStateManager` |
| `RecordingManager` | Large | Mixed recording + playback | Split responsibilities |
| Mixin classes | Varies | Null checks missing | Add defensive validation |

## Phase 3: Critical Bug Fixes

### Priority Bug Categories

1. **State Corruption**
   - HUD not restored after scene error
   - Camera stuck after cutscene crash
   - Input capture not released on disconnect

2. **Crash Vectors**
   - Null player in area triggers
   - Concurrent modification in tick handlers
   - Missing asset file handling

3. **Logic Errors**
   - Switch/case fallthrough in state handlers
   - Recording inconsistency on first tick
   - Trigger re-entrancy causing loops

### Fix Strategy

- Each bug fix requires accompanying test
- All fixes go through cleanup handler system
- Structured logging added at each fix point

## Phase 4: Performance Optimization

### Audit Checklist

| Check | Location | Action |
|-------|----------|--------|
| Tick allocations | All `tick()` methods | Replace `new` with pooling |
| O(n) lookups | State/trigger queries | Replace with HashMap |
| Stream usage | Hot paths | Convert to loops |
| Regex compilation | Ink parsing | Pre-compile patterns |

### Profiling Infrastructure

```java
public class NarrativeProfiler {
    public static boolean ENABLED = false;  // Config toggle

    public static void startSection(String name);
    public static void endSection(String name);
    public static void dumpMetrics();  // To log file
}
```

## Phase 5: Security Hardening

### Ink Tag Validation

```java
public class TagValidator {
    private static final Set<String> ALLOWED_TAGS = Set.of(
        "dialog", "choice", "camera", "sound", "wait", ...
    );

    public ValidationResult validate(String tagName, List<String> params);
}
```

### Command Whitelist

```java
public class CommandProxy {
    private static final Set<String> ALLOWED_COMMANDS = Set.of(
        "tp", "give", "effect", "playsound", ...
    );

    public boolean executeIfAllowed(String command, PermissionContext ctx);
}
```

### Error Message Format

```
[NarrativeCraft] ERROR in story "my_story.ink" at line 42
  Invalid tag: #playsoundd
  ↳ Did you mean: #playsound ?
  ↳ Available tags: dialog, choice, camera, sound, wait, ...
```

## Phase 6: Testing & CI/CD

### Test Structure

```text
common/src/test/java/fr/loudo/narrativecraft/
├── unit/
│   ├── parser/           # Ink parsing tests
│   ├── tags/             # Tag handler tests
│   └── state/            # State machine tests
├── integration/
│   └── cleanup/          # Cleanup handler integration
└── regression/
    └── bugs/             # Non-regression tests per bug
```

### CI Pipeline (GitHub Actions)

```yaml
# .github/workflows/ci.yml
jobs:
  build:
    matrix:
      loader: [fabric, neoforge]
    steps:
      - Checkout
      - Setup Java 21
      - Build with Gradle
      - Run tests
      - Run Spotless check
      - Upload artifacts
```

### Quality Gates

- Build must succeed for both loaders
- All tests must pass
- Spotless formatting must pass
- No new Spotless violations

## Phase 7: Documentation

### Documentation Deliverables

| Document | Location | Content |
|----------|----------|---------|
| README.md | Root | Installation, quick start, links |
| CONTRIBUTING.md | Root | Dev setup, PR process, style guide |
| INK_GUIDE.md | docs/ | Ink scripting for NarrativeCraft |
| TAG_REFERENCE.md | docs/ | All tags with params and examples |
| TROUBLESHOOTING.md | docs/ | Common errors and solutions |
| CHANGELOG.md | Root | Version history |

### Example Pack

```text
example-stories/
├── tutorial/
│   ├── 01-first-dialog.ink
│   ├── 02-choices.ink
│   ├── 03-cutscene.ink
│   └── README.md
└── showcase/
    ├── complex-scene.ink
    └── assets/
```

## Phase 8: Release Preparation

### Version Strategy

- Current: 1.0.1 (MVP)
- Target: 1.1.0 (Production Stable)
- Breaking changes bump to 2.0.0 (not planned)

### Release Checklist

1. All CI checks pass
2. CHANGELOG updated
3. Version bumped in gradle.properties
4. Example pack validated
5. Documentation reviewed
6. GitHub release created with artifacts
7. Optional: CurseForge/Modrinth publish

### Post-Release

- Monitor issues for 1 week
- Patch releases for critical bugs
- Plan 1.2.0 roadmap based on feedback

## Success Criteria Mapping

| Criterion | Phase | Verification |
|-----------|-------|--------------|
| Zero known crashes | 3, 4 | Regression tests pass |
| Zero stuck states | 2, 3 | Cleanup handler coverage |
| Invalid scripts detected | 5 | Validation tests |
| Stable performance | 4 | Profiler benchmarks |
| Complete documentation | 7 | Doc review checklist |
| CI blocks regressions | 6 | Intentional failure test |
| Maintainable by others | 2, 7 | Code review + docs |

## Risk Register

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Hidden state corruption | Medium | High | Extensive cleanup handler testing |
| Performance regression | Low | Medium | Before/after benchmarks |
| Mixin compatibility | Low | High | Test on both loaders |
| Scope creep | Medium | Medium | Strict feature freeze |
| Documentation lag | Medium | Low | Doc tasks in each phase |

## Next Steps

1. Run `/speckit.tasks` to generate task breakdown
2. Begin Phase 0 baseline documentation
3. Proceed to Phase 1 audit with systematic file review
