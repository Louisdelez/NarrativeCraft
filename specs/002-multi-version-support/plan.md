# Implementation Plan: Multi-Version Minecraft Support

**Branch**: `002-multi-version-support` | **Date**: 2026-01-09 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/002-multi-version-support/spec.md`

## Summary

Add multi-version Minecraft support (1.19.4, 1.20.6, 1.21.11) with Fabric and NeoForge loaders using Architectury Loom. The existing 1.21.11 codebase will be restructured to support version-specific modules while maximizing code sharing in the common module. Features unavailable on older versions will be disabled with clear warnings.

**Target Artifacts (5 JARs):**
- `narrativecraft-mc1.19.4-fabric.jar`
- `narrativecraft-mc1.20.6-fabric.jar`
- `narrativecraft-mc1.20.6-neoforge.jar`
- `narrativecraft-mc1.21.11-fabric.jar`
- `narrativecraft-mc1.21.11-neoforge.jar`

## Technical Context

**Language/Version**: Java 21 (all targets compiled with Java 21; runtime: Java 17 for 1.19.x/1.20.x, Java 21 for 1.21.x)
**Primary Dependencies**:
- Architectury Loom (build system)
- Fabric API: 0.140.0+1.21.11 (1.21.x), TBD (1.20.x), TBD (1.19.x)
- NeoForge: 21.11.12-beta (1.21.x), TBD (1.20.x)
- Blade Ink: 1.2.1+nc (Java 17 compatible)
- Sponge Mixin: 0.8.5
- MixinExtras: 0.3.5

**Storage**: File-based (.ink stories, .json configs) - version-independent
**Testing**: JUnit 5 + Mockito (existing), Smoke tests (manual per-version)
**Target Platform**: Minecraft clients/servers on Fabric (all versions) and NeoForge (1.20.6+, 1.21.11)
**Project Type**: Multi-module Gradle (common + fabric + neoforge + version-specific)
**Performance Goals**: <0.5ms tick overhead, <50MB heap, <500ms startup impact (per constitution)
**Constraints**: No new features, graceful degradation on 1.19.x, CI < 15 minutes
**Scale/Scope**: 309 common Java files, 20 fabric files, 21 neoforge files, 33 mixins total

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Security | PASS | No changes to permission model; CommandProxy remains |
| II. Reliability | PASS | Feature degradation with warnings (no crashes); cleanup guarantees preserved |
| III. Performance | PASS | No tick allocation changes; version checks at startup only |
| IV. Quality | PASS | CI matrix covers all 5 targets; tests run on all versions |
| V. Standards | PASS | Common module separation maintained; version-specific isolated |
| VI. Creator UX | PASS | Version-aware error messages; TAG_REFERENCE updated |
| VII. Maintainability | PASS | Version modules follow SRP; structured logging preserved |

**Additional Constraints:**
- Technology Stack: Java + Gradle + Ink (unchanged)
- Security Boundaries: No changes to script sandbox
- Performance Budgets: Same targets, verified per version

## Project Structure

### Documentation (this feature)

```text
specs/002-multi-version-support/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output (version matrix, compatibility API)
├── quickstart.md        # Phase 1 output (developer setup guide)
├── contracts/           # Phase 1 output (compatibility interfaces)
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
# Current Structure (NarrativeCraft)
common/
├── src/main/java/fr/loudo/narrativecraft/
│   ├── api/             # InkAction system
│   ├── audio/           # Sound abstractions
│   ├── commands/        # Command implementations
│   ├── controllers/     # Camera, Cutscene, Interaction controllers
│   ├── events/          # Common event handlers
│   ├── gui/             # GUI rendering abstractions
│   ├── mixin/           # 23 common mixins
│   ├── narrative/       # Core story system
│   ├── platform/        # ServiceLoader abstraction
│   └── util/            # Utilities, validation, logging
└── src/main/resources/
    └── narrativecraft.mixins.json

fabric/
├── src/main/java/fr/loudo/narrativecraft/
│   ├── events/          # Fabric event handlers
│   ├── mixin/           # 9 Fabric-specific mixins
│   ├── platform/        # FabricPlatformHelper
│   └── register/        # Fabric registrations
└── src/main/resources/
    ├── fabric.mod.json
    └── narrativecraft.fabric.mixins.json

neoforge/
├── src/main/java/fr/loudo/narrativecraft/
│   ├── events/          # NeoForge event handlers
│   ├── mixin/           # 1 NeoForge-specific mixin
│   └── platform/        # NeoForgePlatformHelper, NeoForgePacketSender
└── src/main/resources/
    ├── META-INF/neoforge.mods.toml
    └── narrativecraft.neoforge.mixins.json

# Proposed Multi-Version Structure
common/
└── (unchanged - version-independent code)

compat/
├── api/                 # Version abstraction interfaces
│   ├── IVersionAdapter.java
│   ├── IGuiGraphicsCompat.java
│   ├── ICameraCompat.java
│   └── ICapabilityChecker.java
├── mc119x/              # 1.19.4 implementations
├── mc120x/              # 1.20.6 implementations
└── mc121x/              # 1.21.11 implementations (current behavior)

fabric/
├── mc119x/              # Fabric 1.19.4 specific
├── mc120x/              # Fabric 1.20.6 specific
└── mc121x/              # Fabric 1.21.11 (current)

neoforge/
├── mc120x/              # NeoForge 1.20.6 specific
└── mc121x/              # NeoForge 1.21.11 (current)

buildSrc/
├── multiloader-common.gradle      # (existing)
├── multiloader-loader.gradle      # (existing)
└── version-matrix.gradle          # (NEW) Multi-version configuration

tests/
├── unit/                # Existing unit tests
├── integration/         # Version-independent integration tests
└── smoke/               # Smoke test definitions per version
```

**Structure Decision**: Extend existing multi-loader setup with version-specific modules (`compat/mc1XXx/`). Architectury Loom will manage the version matrix, producing 5 JARs from a single codebase with shared common module.

## Complexity Tracking

> No constitution violations requiring justification.

| Aspect | Decision | Rationale |
|--------|----------|-----------|
| Version modules | 3 compat modules (mc119x, mc120x, mc121x) | Isolates version-specific code; common remains pure |
| Loader × Version | 5 combinations (not 6) | NeoForge doesn't exist for 1.19.x |
| Mixin organization | Per-version refmaps | Prevents class resolution failures |

---

## Phase 0: Research

### Research Tasks

1. **Architectury Loom Multi-Version Setup**
   - Best practices for multi-MC-version builds
   - Version matrix configuration in Gradle
   - Artifact naming conventions

2. **Minecraft API Changes 1.19 → 1.20 → 1.21**
   - GuiGraphics / DrawContext API evolution
   - Camera API changes
   - Sound engine changes
   - Input handling changes
   - Mixin target class changes (renames, moves)

3. **Fabric API Version Compatibility**
   - Fabric API versions for 1.19.4, 1.20.6
   - Deprecated/removed APIs
   - Fabric Loader compatibility ranges

4. **NeoForge Version Compatibility**
   - NeoForge versions for 1.20.6
   - API differences from Forge
   - Event system changes

5. **blade-ink Java 17 Compatibility**
   - Verify 1.2.1+nc works with Java 17 runtime
   - Identify any Java 21-only features used

### Output: research.md

---

## Phase 1: Design

### Data Model

**Version Matrix Entity:**
```
VersionTarget {
  mcVersion: String (1.19.4 | 1.20.6 | 1.21.11)
  mcMajor: String (1.19 | 1.20 | 1.21)
  loaders: List<Loader> (fabric, neoforge)
  javaVersion: Int (17 | 21)
  fabricApiVersion: String
  neoforgeVersion: String?
  parchmentVersion: String
}
```

**Capability Entity:**
```
VersionCapability {
  feature: String (camera, recording, playback, screenEffects, ...)
  supportedVersions: List<mcMajor>
  degradationMessage: String?
}
```

### Contracts (Compatibility Interfaces)

**IVersionAdapter.java:**
```java
public interface IVersionAdapter {
    String getMcMajor();
    boolean supportsFeature(String featureName);
    IGuiGraphicsCompat getGuiGraphicsCompat();
    ICameraCompat getCameraCompat();
}
```

**ICapabilityChecker.java:**
```java
public interface ICapabilityChecker {
    boolean isFeatureAvailable(String feature);
    void warnIfUnavailable(String feature, String context);
}
```

### Output Files

- `data-model.md` - Version matrix, capability definitions
- `contracts/IVersionAdapter.java` - Interface definition
- `contracts/ICapabilityChecker.java` - Interface definition
- `quickstart.md` - Developer setup for multi-version building

---

## Phase Breakdown (Implementation)

### Phase 0 - Preparation & Inventory

| Task | Description | Exit Criteria |
|------|-------------|---------------|
| 0.1 | Create compatibility matrix document | List of version-sensitive APIs mapped to shim approach |
| 0.2 | Define core scenarios for testing | Acceptance checklist for runtime behavior |
| 0.3 | Audit existing mixins for version sensitivity | Each mixin tagged: common/version-specific |

### Phase 1 - Build System & Version Matrix

| Task | Description | Exit Criteria |
|------|-------------|---------------|
| 1.1 | Add Architectury Loom to buildSrc | Plugin resolves and configures |
| 1.2 | Define version matrix in gradle.properties | All 5 targets defined with deps |
| 1.3 | Restructure settings.gradle for multi-version | Each target builds independently |
| 1.4 | Configure artifact naming | JARs named correctly per convention |
| 1.5 | Verify local build produces 5 JARs | All JARs exist with correct metadata |

### Phase 2 - Compatibility Layer

| Task | Description | Exit Criteria |
|------|-------------|---------------|
| 2.1 | Create compat/api interfaces | IVersionAdapter, IGuiGraphicsCompat, ICameraCompat |
| 2.2 | Implement mc121x compat (current behavior) | 1.21.11 works identically |
| 2.3 | Implement mc120x compat | 1.20.6 loads and runs |
| 2.4 | Implement mc119x compat | 1.19.4 loads and runs |
| 2.5 | Migrate mixins to version-specific sets | No mixin failures on any version |

### Phase 3 - Feature Gating & Degradation

| Task | Description | Exit Criteria |
|------|-------------|---------------|
| 3.1 | Implement ICapabilityChecker | Runtime detection works |
| 3.2 | Add version-aware warnings | Unsupported features show warning once |
| 3.3 | Update TagValidator for version awareness | Validation errors include version info |
| 3.4 | Test degradation on 1.19.4 | No crashes, clear messages |

### Phase 4 - CI/CD Matrix

| Task | Description | Exit Criteria |
|------|-------------|---------------|
| 4.1 | Update .github/workflows/ci.yml | Matrix builds all 5 targets |
| 4.2 | Update .github/workflows/release.yml | Release includes all 5 JARs |
| 4.3 | Add version compatibility table to README | Users can select correct JAR |
| 4.4 | Verify CI runs < 15 minutes | Performance requirement met |

### Phase 5 - QA & Documentation

| Task | Description | Exit Criteria |
|------|-------------|---------------|
| 5.1 | Create SMOKE_TESTS.md checklist | Manual test procedure defined |
| 5.2 | Execute smoke tests on all targets | All pass or issues documented |
| 5.3 | Update TAG_REFERENCE with version notes | Each tag shows version support |
| 5.4 | Update TROUBLESHOOTING.md | Version selection guide added |
| 5.5 | Create release candidate | CI green, smoke tests pass |

---

## Risk Management

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Mixin target class renamed | High | High | Per-version mixin sets with refmaps |
| GuiGraphics API incompatible | High | High | IGuiGraphicsCompat abstraction |
| blade-ink fails on Java 17 | Medium | Low | Test early; fallback to alternate version |
| CI timeout (> 15min) | Low | Medium | Parallel jobs, Gradle caching |
| NeoForge 1.20.6 API instability | Medium | Medium | Pin to stable release |

---

## Acceptance Criteria

- [ ] 5 JARs produced by single `./gradlew build`
- [ ] Each JAR loads on target MC version without crash
- [ ] Core scenarios (dialog, choices, cutscene) work on all versions
- [ ] Unsupported features show warning, don't crash
- [ ] CI matrix passes for all 5 targets
- [ ] Release workflow produces labeled artifacts
- [ ] Documentation updated with version matrix
