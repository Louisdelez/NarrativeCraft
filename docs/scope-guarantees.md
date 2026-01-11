# NarrativeCraft Scope & Guarantees

**Version**: 1.1.0 (Production Hardening)
**Date**: 2026-01-09

## Target Environment

### Minecraft Version

- **Primary Version**: 1.21.11
- **Version Range**: [1.21.11, 1.22)

### Java Version

- **Required Version**: Java 21 (LTS)
- **Build Requirement**: JDK 21 must be used to run Gradle (not just compile)
- **Runtime Requirement**: JRE 21+ for Minecraft

> **Note**: Fabric Loom 1.14+ and NeoForge ModDevGradle require Java 21 at Gradle
> configuration time. See `docs/TROUBLESHOOTING.md` for installation instructions.

### Supported Loaders

| Loader | Version | Status |
|--------|---------|--------|
| Fabric | 0.18.3+ | Supported |
| NeoForge | 21.11.12+ | Supported |

## Stability Guarantees

### Singleplayer

**Status**: Stable (Priority)

- All narrative features fully functional
- Player state recovery guaranteed
- No stuck states (HUD, camera, input)
- Performance within budget (<0.5ms/tick)

### Multiplayer

**Status**: Experimental (Not Prioritized)

- Basic functionality available
- Server crashes prevented
- Orphaned state cleanup on disconnect
- Advanced features not guaranteed
- Synchronized narratives out of scope

## Feature Freeze

During the production hardening phase (v1.1.0):

- **Allowed**: Bug fixes, stability improvements, documentation
- **Not Allowed**: New narrative features, new tag types, new rendering modes

## Quality Guarantees

### Reliability

- Zero known crash conditions
- Guaranteed cleanup on scene exit (success or failure)
- Defensive error handling throughout

### State Recovery Guarantees (Phase 4)

The following guarantees are provided by the cleanup system:

| Trigger | HUD | Camera | Input | Audio |
|---------|-----|--------|-------|-------|
| Dialogue exit | ✓ hideGui restored | ✓ | ✓ | ✓ sounds stopped |
| Cutscene abort | ✓ hideGui restored | ✓ returned to player | ✓ | ✓ |
| Screen close | ✓ hideGui restored | - | ✓ escape unblocked | - |
| Player disconnect | ✓ | ✓ | ✓ | ✓ |
| Server stop | ✓ | ✓ | ✓ | ✓ |
| World reload | ✓ | ✓ | ✓ | ✓ |
| Exception/crash | ✓ via try/finally | ✓ | ✓ | ✓ |

**Implementation**:
- `NarrativeCleanupService`: Centralized cleanup orchestration
- `CleanupHandlerRegistry`: Priority-based handler execution
- Four cleanup handlers: HUD (50), Camera (150), Input (250), Audio (350)
- All handlers are idempotent and null-safe

### Performance

- Tick overhead: <0.5ms when narrative active
- Memory footprint: <50MB for loaded stories
- O(1) lookups for state/trigger/session data

### Security

- Whitelisted tags and commands only
- Input validation on all user-provided data
- No PII in logs

### Creator Experience

- Clear error messages (what/where/why/fix)
- Complete documentation for all tags
- Example pack for learning

## Out of Scope for v1.1.0

- Full multiplayer synchronization
- New narrative features
- Mod loader backports (1.20.x)
- Localization/i18n
- Complete rewrite
