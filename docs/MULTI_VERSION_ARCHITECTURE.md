# NarrativeCraft Multi-Version Architecture

This document explains how NarrativeCraft supports multiple Minecraft versions with a single codebase.

## Supported Versions

| Minecraft | Loader | Module | Java |
|-----------|--------|--------|------|
| 1.19.4 | Fabric | `fabric-1.19.4` | 17+ |
| 1.20.6 | Fabric | `fabric-1.20.6` | 17+ |
| 1.20.6 | NeoForge | `neoforge-1.20.6` | 17+ |
| 1.21.11 | Fabric | `fabric-1.21.11` | 21+ |
| 1.21.11 | NeoForge | `neoforge-1.21.11` | 21+ |

## Module Structure

```
NarrativeCraft/
├── common/                    # Shared code (MC-independent)
├── common-mc119/              # MC 1.19.x API overrides
├── common-mc120/              # MC 1.20.x API overrides
├── common-mc121/              # MC 1.21.x API overrides
├── compat-api/                # Version adapter interfaces
├── compat-mc119x/             # 1.19.x version adapter
├── compat-mc120x/             # 1.20.x version adapter
├── compat-mc121x/             # 1.21.x version adapter
├── fabric-1.19.4/             # Fabric 1.19.4 loader
├── fabric-1.20.6/             # Fabric 1.20.6 loader
├── fabric-1.21.11/            # Fabric 1.21.11 loader
├── neoforge-1.20.6/           # NeoForge 1.20.6 loader
└── neoforge-1.21.11/          # NeoForge 1.21.11 loader
```

## Architecture Layers

### 1. Common Layer (`common/`)

Contains all version-independent code:
- Core logic (narrative engine, Ink integration)
- Data models (Character, Scene, Chapter, etc.)
- Business logic (PlaybackManager, RecordingManager, etc.)
- Screens and UI (with abstract rendering)

**Rule:** Code in `common/` must NOT use any MC APIs that changed between versions.

### 2. Common-MCxxx Overrides (`common-mc119/`, `common-mc120/`, `common-mc121/`)

Override files that handle version-specific API differences. These modules use a source overlay pattern:

```groovy
// In fabric-1.19.4/build.gradle
sourceSets.main.java.srcDirs += [
    project(":common").file("src/main/java"),
    project(":common-mc119").file("src/main/java")  // Overrides common
]
```

When a file exists in both `common/` and `common-mc119/`, the `common-mc119/` version takes precedence.

### 3. Compat Layer (`compat-api/`, `compat-mc*x/`)

Version adapters that provide abstraction over changed MC APIs:

```java
// compat-api/IVersionAdapter.java
public interface IVersionAdapter {
    String getMcVersion();
    IColorCompat getColorCompat();
    IUtilCompat getUtilCompat();
    boolean supportsFeature(String featureId);
}

// compat-mc120x/Mc120xVersionAdapter.java
public class Mc120xVersionAdapter implements IVersionAdapter {
    @Override
    public String getMcVersion() { return "1.20.6"; }
}
```

### 4. Loader Modules (`fabric-*/`, `neoforge-*/`)

Entry points for each loader/version combination:
- Mod initialization
- Event registration
- Mixin configuration
- Resource loading

## Version-Specific API Differences

### MC 1.19.4 vs 1.20+

| API | 1.19.4 | 1.20+ |
|-----|--------|-------|
| Entity level | `entity.getLevel()` | `entity.level()` |
| Entity on ground | `entity.isOnGround()` | `entity.onGround()` |
| Registry access | `level.registryAccess()` | `entity.registryAccess()` |
| Command success | `sendSuccess(Component, boolean)` | `sendSuccess(Supplier<Component>, boolean)` |
| Screen render | `render(PoseStack, ...)` | `render(GuiGraphics, ...)` |
| ObjectSelectionList | 6-param constructor | 5-param constructor |
| Checkbox | Constructor | `Checkbox.builder()` |
| Layout | Manual positioning | `LinearLayout`, `HeaderAndFooterLayout` |

### MC 1.20.x vs 1.21.x

| API | 1.20.x | 1.21.x |
|-----|--------|--------|
| Horse package | `animal.horse` | `animal.equine` |
| Boat class | `Boat` | `AbstractBoat` |
| Equipment slots | 6 slots | 8 slots (BODY, SADDLE) |
| VertexConsumer | Old chain API | New addVertex() API |
| Matrix2D | `Matrix3x2fStack` | Built into GuiGraphics |

## Adding a New MC Version

### Step 1: Create Common Override Module

```bash
mkdir -p common-mc1XX/src/main/java
```

Copy files from an existing common-mcXXX and update for API differences.

### Step 2: Create Compat Module

```bash
mkdir -p compat-mc1XXx/src/main/java
```

Implement `IVersionAdapter` for the new version.

### Step 3: Create Loader Module

```bash
mkdir -p fabric-1.XX.X/src/main/java
```

Configure `build.gradle` with correct dependencies and source overlays.

### Step 4: Update settings.gradle

```groovy
include 'common-mc1XX'
include 'compat-mc1XXx'
include 'fabric-1.XX.X'
```

### Step 5: Update CI/Release Workflows

Add the new target to `.github/workflows/ci.yml` and `.github/workflows/release.yml`:

```yaml
matrix:
  include:
    - loader: fabric
      mc_version: "1.XX.X"
      module: "fabric-1.XX.X"
```

### Step 6: Run Override Inventory

Compile and fix all errors:

```bash
./gradlew :fabric-1.XX.X:compileJava
```

Create override files for any API differences.

### Step 7: Validate

```bash
# Build all targets
./gradlew build

# Verify the new target
./gradlew :fabric-1.XX.X:build
```

## Build Commands

```bash
# Build specific target
./gradlew :fabric-1.19.4:build
./gradlew :fabric-1.20.6:build
./gradlew :fabric-1.21.11:build
./gradlew :neoforge-1.20.6:build
./gradlew :neoforge-1.21.11:build

# Build all targets
./gradlew build

# Clean and rebuild
./gradlew clean build
```

## Validation

The `MULTI_VERSION_BUILD` constant in `NarrativeCraftMod.java` serves as a propagation marker. After any change to `common/`, rebuild and verify:

```bash
for target in fabric-1.19.4 fabric-1.20.6 fabric-1.21.11 neoforge-1.20.6 neoforge-1.21.11; do
    strings "$target/build/classes/java/main/fr/loudo/narrativecraft/NarrativeCraftMod.class" | grep "5-target"
done
```

All 5 targets should show the constant.

## Internationalization (i18n)

NarrativeCraft uses Minecraft's built-in translation system with a shared language file location.

### Language Files Location

All language files are in the `common/` module:
```
common/src/main/resources/assets/narrativecraft/lang/
├── en_us.json   (English - source of truth)
├── fr_fr.json   (French)
├── de_de.json   (German)
├── es_es.json   (Spanish)
├── zh_cn.json   (Simplified Chinese)
├── ru_ru.json   (Russian)
└── ar_sa.json   (Arabic - RTL)
```

### Why in common/?

Language files are **version-independent** because:
1. Minecraft's translation system works identically across all versions
2. JSON format is the same across MC 1.19.x through 1.21.x
3. Key names don't reference version-specific APIs

### Translation API

Use the `Translation` helper class (in `common/`):
```java
// Returns Component.translatable("narrativecraft.button.done")
Translation.message("button.done")

// With placeholder
Translation.message("screen.scene_manager.title", chapterIndex)
```

### CI Validation

The `i18nCheck` Gradle task validates all language files:
```bash
./gradlew :common:i18nCheck
```

This runs in CI and blocks PRs with:
- Missing translation keys
- Placeholder mismatches (`%s`, `%d`)
- Invalid JSON syntax

### Adding Translations

See:
- [CONTRIBUTING.md](../CONTRIBUTING.md#translations-i18n) - How to translate
- [I18N_RULES.md](I18N_RULES.md) - Translation rules and conventions
- [ADDING_NEW_LANGUAGE.md](ADDING_NEW_LANGUAGE.md) - Adding a new language

---

## Troubleshooting

### "Method not found" errors

The MC version being compiled uses a different API. Create an override in the appropriate `common-mcXXX/` module.

### "Cannot access class" errors

The class moved to a different package. Check the MC version's package structure and create an override.

### "Mixin target not found" errors

The mixin target method doesn't exist in that MC version. Create a version-specific mixin or use `@Pseudo` annotation.

### Build succeeds but feature doesn't work

Check `CompatLogger` output for degraded features. Some features may have fallback implementations.
