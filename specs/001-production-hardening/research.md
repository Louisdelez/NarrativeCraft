# Research: NarrativeCraft Production Hardening

**Date**: 2026-01-09
**Branch**: `001-production-hardening`

## Executive Summary

This research document consolidates findings from codebase exploration to inform
the production hardening effort. All technical unknowns have been resolved.

---

## 1. Codebase Architecture

### Decision: Multi-Module Gradle Structure

**Chosen**: Existing `common/` + `fabric/` + `neoforge/` architecture

**Rationale**:
- Already implements Constitution Principle V (Standards) separation
- Custom Gradle plugins in `buildSrc/` handle multi-loader builds
- Platform abstraction via `IPlatformHelper` ServiceLoader pattern
- No architectural change needed; focus on internal improvements

**Alternatives Considered**:
- Single-module with compile-time conditionals: Rejected (harder to maintain)
- Architectury plugin: Rejected (current custom solution works well)

---

## 2. Testing Framework

### Decision: JUnit 5 + Mockito

**Chosen**: JUnit 5 (Jupiter) with Mockito for mocking

**Rationale**:
- Standard Java testing framework with excellent Gradle integration
- Mockito enables testing components in isolation (mock managers, services)
- Parameterized tests for tag validation edge cases
- No Minecraft runtime required for unit tests of pure logic

**Alternatives Considered**:
- TestNG: Rejected (less community adoption in Minecraft modding)
- Minecraft GameTest: Rejected for unit tests (requires runtime, better for integration)

**Implementation Notes**:
```groovy
// common/build.gradle additions
dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'
    testImplementation 'org.mockito:mockito-core:5.7.0'
    testImplementation 'org.mockito:mockito-junit-jupiter:5.7.0'
}

test {
    useJUnitPlatform()
}
```

---

## 3. State Machine Implementation

### Decision: Enum-based with Handler Registry

**Chosen**: `NarrativeState` enum + `NarrativeStateManager` class

**Rationale**:
- Simple, type-safe state representation
- Single point of state transition control
- Cleanup handlers registered per state transition
- Easy to test state transitions in isolation

**Alternatives Considered**:
- Spring StateMachine: Rejected (heavy dependency for simple use case)
- Akka FSM: Rejected (overkill, adds actor model complexity)
- Switch-based: Rejected (current approach, causes bugs)

**State Definitions**:
```java
public enum NarrativeState {
    GAMEPLAY,    // Default, normal player control
    DIALOGUE,    // Dialog UI active
    CUTSCENE,    // Camera locked, playback
    RECORDING,   // Recording mode
    PLAYBACK     // Playing recorded sequence
}
```

---

## 4. Cleanup Handler Pattern

### Decision: Priority-based Handler Stack

**Chosen**: `CleanupHandler` interface with priority ordering

**Rationale**:
- Ensures deterministic cleanup order (HUD → Camera → Input)
- Handlers registered before state modification
- Executed in try/finally blocks
- Can be tested independently

**Alternatives Considered**:
- Simple callback list: Rejected (no ordering guarantees)
- Event bus cleanup: Rejected (harder to guarantee execution)

**Pattern**:
```java
public interface CleanupHandler {
    void cleanup();
    default int priority() { return 100; }  // Lower = first
}

// Cleanup order: HUD(10) → Camera(20) → Input(30) → Session(100)
```

---

## 5. CI/CD Platform

### Decision: GitHub Actions

**Chosen**: GitHub Actions with matrix builds

**Rationale**:
- Native GitHub integration (repository already on GitHub)
- Free for open source projects
- Matrix builds for Fabric + NeoForge in parallel
- Artifact upload for release automation

**Alternatives Considered**:
- Jenkins: Rejected (requires self-hosting)
- GitLab CI: Rejected (repo not on GitLab)
- CircleCI: Rejected (GitHub Actions sufficient)

**Workflow Structure**:
```yaml
# ci.yml
on: [push, pull_request]
jobs:
  build:
    strategy:
      matrix:
        loader: [fabric, neoforge]
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
      - run: ./gradlew :${{ matrix.loader }}:build
      - run: ./gradlew :common:test
      - run: ./gradlew spotlessCheck
```

---

## 6. Documentation Format

### Decision: Markdown in Repository

**Chosen**: Markdown files in `docs/` directory

**Rationale**:
- Version-controlled with code
- Renders on GitHub automatically
- Easy to update alongside code changes
- External site already exists (loudo56.github.io)

**Alternatives Considered**:
- Wiki: Rejected (separate from code, gets out of sync)
- Docusaurus: Considered for future (current site uses it)
- JavaDoc only: Rejected (not user-friendly for creators)

**Structure**:
```
docs/
├── INK_GUIDE.md          # Narrative scripting
├── TAG_REFERENCE.md      # All tags documented
├── TROUBLESHOOTING.md    # Error solutions
└── ARCHITECTURE.md       # Developer reference
```

---

## 7. Error Message Localization

### Decision: English-Only for 1.1.0

**Chosen**: English error messages, defer localization

**Rationale**:
- Aligns with Out of Scope in spec
- Reduces complexity for initial hardening
- Existing `Translation.java` utility can be extended later
- Error context (file, line) is language-agnostic

**Alternatives Considered**:
- Full i18n: Rejected for 1.1.0 (scope creep risk)
- Machine translation: Rejected (quality concerns)

---

## 8. Ink Runtime Integration

### Decision: Blade-Ink with Custom Tag Handler

**Chosen**: Continue using `blade-ink:1.2.1+nc`

**Rationale**:
- Already integrated and working
- `+nc` suffix indicates NarrativeCraft-specific fork/version
- `InkActionRegistry` provides extensible tag handling
- No need to change; focus on validation layer

**Validation Approach**:
```java
// Before executing tag:
ValidationResult result = TagValidator.validate(tagName, params);
if (!result.isValid()) {
    throw new NarrativeException(result.getErrorMessage());
}
```

---

## 9. Performance Profiling

### Decision: Simple Internal Profiler

**Chosen**: Custom `NarrativeProfiler` class

**Rationale**:
- No external dependencies
- Configurable via runtime flag
- Outputs to log file for analysis
- Minimal overhead when disabled

**Alternatives Considered**:
- JProfiler/YourKit: Rejected (requires external tools)
- Spark mod: Considered (good for runtime profiling)
- Minecraft debug screen: Limited (already used)

**Implementation**:
```java
public class NarrativeProfiler {
    public static boolean ENABLED = false;
    private static final Map<String, Long> startTimes = new HashMap<>();
    private static final Map<String, List<Long>> durations = new HashMap<>();

    public static void start(String section) {
        if (ENABLED) startTimes.put(section, System.nanoTime());
    }

    public static void end(String section) {
        if (ENABLED) {
            long duration = System.nanoTime() - startTimes.get(section);
            durations.computeIfAbsent(section, k -> new ArrayList<>()).add(duration);
        }
    }
}
```

---

## 10. Minecraft Version Support

### Decision: 1.21.11 Only for 1.1.0

**Chosen**: Target Minecraft 1.21.11 exclusively

**Rationale**:
- Current version in gradle.properties
- Version range `[1.21.11, 1.22)` already set
- Simplifies testing matrix
- Backports can be considered post-1.1.0

**Alternatives Considered**:
- Multi-version: Rejected (increases complexity significantly)
- 1.20.x LTS: Rejected (would require downport work)

---

## Key Findings Summary

| Area | Decision | Confidence |
|------|----------|------------|
| Architecture | Keep multi-module | High |
| Testing | JUnit 5 + Mockito | High |
| State Machine | Enum + Manager | High |
| Cleanup | Priority handlers | High |
| CI/CD | GitHub Actions | High |
| Documentation | Markdown in repo | High |
| Localization | English only | High |
| Ink Runtime | Keep Blade-Ink | High |
| Profiling | Internal profiler | Medium |
| MC Version | 1.21.11 only | High |

---

## Unresolved Items

None. All technical decisions have been made with sufficient confidence to proceed.

---

## References

- NarrativeCraft GitHub: https://github.com/LOUDO56/NarrativeCraft
- NarrativeCraft Docs: https://loudo56.github.io/NarrativeCraft-docs/
- Blade-Ink: https://github.com/bladecoder/blade-ink-java
- Minecraft Modding: https://fabricmc.net/ / https://neoforged.net/
