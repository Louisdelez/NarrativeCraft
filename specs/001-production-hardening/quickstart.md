# Quickstart: NarrativeCraft Development

**Date**: 2026-01-09
**Target Audience**: Developers contributing to NarrativeCraft production hardening

## Prerequisites

- **Java**: JDK 21 (required)
- **IDE**: IntelliJ IDEA recommended (with Minecraft Dev plugin)
- **Git**: For version control
- **Gradle**: Wrapper included (no global install needed)

## Repository Setup

### 1. Clone the Repository

```bash
git clone https://github.com/LOUDO56/NarrativeCraft.git
cd NarrativeCraft
```

### 2. Checkout Production Hardening Branch

```bash
git checkout production-hardening
```

### 3. Build the Project

```bash
# Build both Fabric and NeoForge
./gradlew build

# Build specific loader
./gradlew :fabric:build
./gradlew :neoforge:build
```

### 4. IDE Setup (IntelliJ IDEA)

1. Open project folder in IntelliJ
2. Wait for Gradle sync to complete
3. Run `./gradlew genSources` for decompiled Minecraft sources
4. Refresh Gradle project

## Project Structure

```
NarrativeCraft/
├── common/              # Platform-agnostic code (edit here first)
│   └── src/main/java/fr/loudo/narrativecraft/
├── fabric/              # Fabric-specific implementation
├── neoforge/            # NeoForge-specific implementation
├── buildSrc/            # Gradle plugin configuration
└── specs/               # Specification documents (separate repo)
```

## Running the Mod

### Fabric (Development)

```bash
./gradlew :fabric:runClient
```

### NeoForge (Development)

```bash
./gradlew :neoforge:runClient
```

### With Debug Logging

```bash
./gradlew :fabric:runClient --args="--debug"
```

## Running Tests

### All Tests

```bash
./gradlew test
```

### Specific Test Class

```bash
./gradlew test --tests "fr.loudo.narrativecraft.unit.state.*"
```

### With Coverage Report

```bash
./gradlew test jacocoTestReport
# Report at: common/build/reports/jacoco/test/html/index.html
```

## Code Quality Checks

### Format Check

```bash
./gradlew spotlessCheck
```

### Auto-Format

```bash
./gradlew spotlessApply
```

### Full CI Check (Local)

```bash
./gradlew build test spotlessCheck
```

## Key Development Tasks

### Adding a New Ink Tag

1. Create handler in `common/src/main/java/fr/loudo/narrativecraft/narrative/story/inkAction/`
2. Implement `InkAction` interface
3. Register in `InkActionRegister.java`
4. Add to whitelist in `TagValidator`
5. Write unit test in `common/src/test/java/.../unit/tags/`
6. Document in `docs/TAG_REFERENCE.md`

### Modifying State Machine

1. Edit `NarrativeState` enum if adding states
2. Update `NarrativeStateManager` transition logic
3. Ensure cleanup handlers cover new state
4. Write state transition tests
5. Update data-model.md diagram

### Adding Platform-Specific Code

1. Define interface in `common/src/main/java/.../platform/services/`
2. Implement in `fabric/src/main/java/.../platform/`
3. Implement in `neoforge/src/main/java/.../platform/`
4. Register via ServiceLoader in respective modules

## Debugging Tips

### Enable Narrative Profiler

```java
// In game or via config
NarrativeProfiler.ENABLED = true;
// Check logs for timing data
```

### Debug Stuck States

```java
// Force cleanup (emergency)
NarrativeCraftMod.getInstance().getStateManager().forceReset();
```

### Inspect Player Session

```
/narrativecraft debug session
```

## Common Issues

### Build Fails with "Could not resolve dependencies"

```bash
./gradlew --refresh-dependencies build
```

### Mixin Not Applied

1. Check mixin JSON files in resources
2. Verify mixin class is in correct package
3. Run with `-Dmixin.debug=true`

### Tests Fail to Find Classes

```bash
./gradlew clean build
```

## Git Workflow

### Branch Naming

- `fix/BUG-XXX-description` - Bug fixes
- `refactor/component-name` - Refactoring
- `test/component-name` - Test additions
- `docs/topic` - Documentation

### Commit Messages

```
fix(state): prevent HUD stuck after scene error

- Add cleanup handler registration before HUD modification
- Ensure try/finally wraps all HUD operations
- Add regression test for BUG-042

Refs: BUG-042
```

### Before Submitting PR

```bash
# Run full check
./gradlew build test spotlessCheck

# Verify both loaders
./gradlew :fabric:build :neoforge:build
```

## Useful Commands

| Command | Description |
|---------|-------------|
| `./gradlew tasks` | List all available tasks |
| `./gradlew dependencies` | Show dependency tree |
| `./gradlew :common:test --info` | Verbose test output |
| `./gradlew build -x test` | Build without tests |
| `./gradlew clean` | Clean build outputs |

## Resources

- **NarrativeCraft Docs**: https://loudo56.github.io/NarrativeCraft-docs/
- **Ink Language**: https://github.com/inkle/ink/blob/master/Documentation/WritingWithInk.md
- **Fabric Wiki**: https://fabricmc.net/wiki/
- **NeoForge Docs**: https://docs.neoforged.net/
- **Mixin Wiki**: https://github.com/SpongePowered/Mixin/wiki

## Getting Help

- **Discord**: Check NarrativeCraft community
- **Issues**: GitHub issue tracker
- **Code Review**: Request review from maintainers
