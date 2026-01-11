# Contributing to NarrativeCraft

Thank you for your interest in contributing to NarrativeCraft! This document provides guidelines for contributing.

## Table of Contents

- [Development Setup](#development-setup)
- [Code Style](#code-style)
- [Pull Request Process](#pull-request-process)
- [Architecture Overview](#architecture-overview)
- [Testing](#testing)

---

## Development Setup

### Requirements

- **Java 21** (JDK, not just JRE)
- **Git**
- **IDE**: IntelliJ IDEA recommended

### Setup Steps

1. **Fork and clone the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/NarrativeCraft.git
   cd NarrativeCraft
   ```

2. **Ensure Java 21 is active**
   ```bash
   java -version  # Should show 21.x
   ```

3. **Import into IDE**
   - IntelliJ: Open as Gradle project
   - Let Gradle sync complete

4. **Build the project**
   ```bash
   ./gradlew :fabric:build :neoforge:build
   ```

5. **Run tests**
   ```bash
   ./gradlew test
   ```

### Project Structure

```
NarrativeCraft/
├── common/           # Shared code (MC-independent)
├── common-mc119/     # MC 1.19.x API overrides
├── common-mc120/     # MC 1.20.x API overrides
├── common-mc121/     # MC 1.21.x API overrides
├── compat-api/       # Version adapter interfaces
├── compat-mc119x/    # 1.19.x version adapter
├── compat-mc120x/    # 1.20.x version adapter
├── compat-mc121x/    # 1.21.x version adapter
├── fabric-1.19.4/    # Fabric 1.19.4 loader
├── fabric-1.20.6/    # Fabric 1.20.6 loader
├── fabric-1.21.11/   # Fabric 1.21.11 loader
├── neoforge-1.20.6/  # NeoForge 1.20.6 loader
├── neoforge-1.21.11/ # NeoForge 1.21.11 loader
└── docs/             # Documentation
```

For detailed multi-version architecture, see [MULTI_VERSION_ARCHITECTURE.md](docs/MULTI_VERSION_ARCHITECTURE.md).

---

## Code Style

### General Guidelines

1. **Keep it simple** - Don't over-engineer. Prefer clarity over cleverness.
2. **No unnecessary abstractions** - Three similar lines is better than a premature abstraction.
3. **Defensive programming** - Null checks, bounds checks, try/finally for cleanup.

### Formatting

- **Indentation**: 4 spaces (no tabs)
- **Line length**: 120 characters max
- **Braces**: K&R style (opening brace on same line)

```java
// Good
public void doSomething() {
    if (condition) {
        action();
    }
}

// Avoid
public void doSomething()
{
    if (condition)
    {
        action();
    }
}
```

### Naming Conventions

- **Classes**: PascalCase (`StoryHandler`, `InkActionRegistry`)
- **Methods**: camelCase (`validateTag`, `enterState`)
- **Constants**: UPPER_SNAKE_CASE (`MAX_DISTANCE`, `VALID_TAGS`)
- **Variables**: camelCase (`playerSession`, `errorCount`)

### Documentation

- Add Javadoc for public APIs
- Comment complex logic, not obvious code
- Reference issue numbers in comments when fixing bugs: `// T055: Fix for...`

```java
/**
 * Validates an Ink tag before execution.
 *
 * @param tag The full tag command
 * @param scene The current scene context
 * @return ValidationResult with any errors found
 */
public ValidationResult validateTag(String tag, Scene scene) {
    // Implementation
}
```

---

## Pull Request Process

### CI Requirements

All pull requests must pass our continuous integration checks before merging:

1. **Spotless Check** - Code formatting must pass
   ```bash
   ./gradlew spotlessCheck
   # To auto-fix formatting issues:
   ./gradlew spotlessApply
   ```

2. **Unit Tests** - All tests must pass
   ```bash
   ./gradlew :common:test
   ```

3. **Build All Targets** - All 5 MC versions must compile
   ```bash
   ./gradlew :fabric-1.19.4:build :fabric-1.20.6:build :fabric-1.21.11:build :neoforge-1.20.6:build :neoforge-1.21.11:build
   ```

The CI pipeline runs automatically on every push and pull request. All 5 targets must build successfully. You can see the status badge at the top of the README.

### Adding a New Minecraft Version

See [MULTI_VERSION_ARCHITECTURE.md](docs/MULTI_VERSION_ARCHITECTURE.md) for detailed instructions on:
- Creating common override modules
- Implementing version adapters
- Setting up loader modules
- Updating CI/release workflows

### Before Submitting

1. **Create a feature branch**
   ```bash
   git checkout -b feature/my-feature
   # or
   git checkout -b fix/issue-123
   ```

2. **Make your changes**
   - Follow the code style guidelines
   - Add tests for new functionality
   - Update documentation if needed

3. **Run Spotless to format code**
   ```bash
   ./gradlew spotlessApply
   ```

4. **Run the test suite**
   ```bash
   ./gradlew :common:test
   ```

5. **Build both loaders**
   ```bash
   ./gradlew :fabric:build :neoforge:build
   ```

6. **Verify all CI checks pass locally**
   ```bash
   ./gradlew spotlessCheck :common:test :fabric:build :neoforge:build
   ```

### PR Guidelines

1. **Clear title**: Describe what the PR does
   - Good: "Add typo suggestions for unknown tags"
   - Bad: "Fix stuff"

2. **Description**: Include:
   - What changes were made
   - Why the changes were needed
   - How to test the changes

3. **Small PRs**: Prefer multiple small PRs over one large PR

4. **Link issues**: Reference related issues with `Fixes #123` or `Relates to #123`

### Review Process

1. All PRs require review before merge
2. Address reviewer feedback promptly
3. Keep discussions constructive and focused

---

## Architecture Overview

### Core Components

#### Narrative State Management
- `NarrativeState`: Enum of states (GAMEPLAY, DIALOGUE, CUTSCENE, etc.)
- `NarrativeStateManager`: Manages state transitions
- `NarrativeCleanupService`: Ensures cleanup on state exit

#### Ink Tag System
- `InkAction`: Base class for all tag handlers
- `InkActionRegistry`: Maps tag names to handlers
- `InkTagHandler`: Executes tags during story playback

#### Validation System (v1.1.0)
- `TagValidator`: Validates tags at load time
- `ValidationError`: Structured error with context
- `ErrorFormatter`: Formats errors for display
- `TypoSuggester`: Suggests corrections using Levenshtein distance

#### Session Management
- `PlayerSession`: Per-player state during narratives
- `StoryHandler`: Manages story execution

### Key Patterns

1. **Cleanup Handlers**: Priority-based cleanup on state exit
   ```java
   stateManager.registerCleanupHandler("HUD", PRIORITY_HUD, () -> {
       // Cleanup logic
   });
   ```

2. **Try/Finally for State**: Always clean up, even on exception
   ```java
   try {
       // Story execution
   } finally {
       stateManager.exitToGameplay();
   }
   ```

3. **Null Safety in Mixins**: Always check `Minecraft.getInstance().player`
   ```java
   LocalPlayer player = Minecraft.getInstance().player;
   if (player == null) return;
   ```

---

## Testing

### Test Categories

1. **Unit Tests**: Test individual components in isolation
   ```
   common/src/test/java/.../unit/
   ```

2. **Integration Tests**: Test component interactions
   ```
   common/src/test/java/.../integration/
   ```

### Writing Tests

```java
@DisplayName("TagValidator Tests")
class TagValidatorTest {

    @Test
    @DisplayName("Should detect unknown tag")
    void shouldDetectUnknownTag() {
        TagValidator validator = new TagValidator();
        ValidationResult result = validator.validateTag("unknowntag arg", "story", "scene", 1);

        assertFalse(result.isValid());
        assertEquals(ValidationError.ErrorCode.UNKNOWN_TAG,
                     result.getFirstError().getCode());
    }
}
```

### Running Tests

```bash
# All tests
./gradlew test

# Specific test class
./gradlew test --tests "*.TagValidatorTest"

# With output
./gradlew test --info
```

---

## CI Pipeline Verification

The CI pipeline is designed to catch regressions before they reach the main branch. Here's how to verify it's working correctly.

### Verification Branch

A dedicated branch `ci/intentional-failure` exists to verify CI catches failures:

```bash
# View the verification branch (DO NOT MERGE)
git log ci/intentional-failure --oneline
```

### What CI Catches

| Failure Type | File | CI Job | Expected Result |
|--------------|------|--------|-----------------|
| Spotless (formatting) | `CISpotlessFailure.java` | `spotlessCheck` | ❌ FAIL |
| Test failure | `CITestFailure.java` | `:common:test` | ❌ FAIL |
| Build failure | `CIBuildFailure.java` | `:fabric:build`, `:neoforge:build` | ❌ FAIL |

### Reproducing CI Failures Locally

```bash
# Check for Spotless violations
./gradlew spotlessCheck

# Run tests (will show failures)
./gradlew :common:test

# Build (will show compilation errors)
./gradlew :fabric:build :neoforge:build
```

### Verification Commits

The `ci/intentional-failure` branch contains three commits:

1. **Commit A - Spotless Failure**
   - File: `common/src/main/java/fr/loudo/narrativecraft/ci/CISpotlessFailure.java`
   - Issues: Mixed tabs/spaces, wrong spacing, long lines
   - Expected: `spotlessCheck` fails

2. **Commit B - Test Failure**
   - File: `common/src/test/java/fr/loudo/narrativecraft/ci/CITestFailure.java`
   - Issues: `fail()` assertion, wrong `assertEquals()` values
   - Expected: `:common:test` fails with 2 test failures

3. **Commit C - Build Failure**
   - File: `common/src/main/java/fr/loudo/narrativecraft/ci/CIBuildFailure.java`
   - Issues: Undefined types, missing semicolons, type mismatches
   - Expected: Compilation fails on both loaders

### Important Notes

- **DO NOT MERGE** the `ci/intentional-failure` branch
- Branch exists solely to prove CI blocks regressions
- If CI passes on this branch, the pipeline is misconfigured

---

## Translations (i18n)

NarrativeCraft supports 7 languages. Community translations are welcome!

### Supported Languages

| Code | Language | Status |
|------|----------|--------|
| `en_us` | English | Source (reference) |
| `fr_fr` | French | Complete |
| `de_de` | German | Complete |
| `es_es` | Spanish | Complete |
| `zh_cn` | Simplified Chinese | Complete |
| `ru_ru` | Russian | Complete |
| `ar_sa` | Arabic (RTL) | Complete |

### How to Translate

1. **Find the language files**
   ```
   common/src/main/resources/assets/narrativecraft/lang/
   ├── en_us.json   ← Source of truth
   ├── fr_fr.json
   ├── de_de.json
   ├── es_es.json
   ├── zh_cn.json
   ├── ru_ru.json
   └── ar_sa.json
   ```

2. **Edit your locale file**
   - Copy keys from `en_us.json` as reference
   - Translate values (not keys!)
   - Keep placeholders (`%s`, `%d`) in the same positions

3. **Validate your changes**
   ```bash
   ./gradlew :common:i18nCheck
   ```

4. **Submit a PR** with your translations

### Translation Rules

1. **Never translate keys** - only translate values
   ```json
   // en_us.json (source)
   "narrativecraft.button.done": "Done"

   // fr_fr.json (translated)
   "narrativecraft.button.done": "Terminé"
   ```

2. **Preserve placeholders** - `%s` and `%d` must appear in translations
   ```json
   // en_us.json
   "narrativecraft.screen.scene_manager.title": "Scene Manager - Chapter %s"

   // fr_fr.json
   "narrativecraft.screen.scene_manager.title": "Gestionnaire de scènes - Chapitre %s"
   ```

3. **Match placeholder count** - same number of `%s`/`%d` in all locales

4. **Don't translate symbols** - `✔`, `✖`, `◀`, `▶` are universal

### Adding a New Language

See [docs/ADDING_NEW_LANGUAGE.md](docs/ADDING_NEW_LANGUAGE.md) for a complete checklist.

Quick steps:
1. Create `xx_yy.json` in the `lang/` directory
2. Copy all keys from `en_us.json`
3. Translate all values
4. Run `./gradlew :common:i18nCheck`
5. Submit PR

### CI Validation

The CI pipeline runs `i18nCheck` which validates:
- ✓ All keys from `en_us.json` exist in other locales
- ✓ Placeholder count matches (`%s`, `%d`)
- ✓ Valid JSON syntax
- ✓ No orphan keys (warnings only)

PRs with missing translations will fail CI.

For detailed rules, see [docs/I18N_RULES.md](docs/I18N_RULES.md).

---

## Questions?

- **Discord**: [Join our Discord](https://discord.com/invite/E3zzNv79DN)
- **Issues**: [GitHub Issues](https://github.com/LOUDO56/NarrativeCraft/issues)

Thank you for contributing!
