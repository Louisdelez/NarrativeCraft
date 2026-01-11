# Data Model: Multi-Version Minecraft Support

**Feature**: 002-multi-version-support
**Date**: 2026-01-09

## Version Matrix

### Target Configurations

| ID | Minecraft | Loader | Fabric API | NeoForge | Parchment | Java Runtime |
|----|-----------|--------|------------|----------|-----------|--------------|
| `fabric-1.19.4` | 1.19.4 | Fabric 0.14.24 | 0.87.2+1.19.4 | N/A | 2023.06.26 | 17 |
| `fabric-1.20.6` | 1.20.6 | Fabric 0.15.11 | 0.97.8+1.20.5 | N/A | 2024.06.16 | 17 |
| `neoforge-1.20.6` | 1.20.6 | NeoForge 20.6.119 | N/A | 20.6.119 | 2024.06.16 | 17 |
| `fabric-1.21.11` | 1.21.11 | Fabric 0.18.3 | 0.140.0+1.21.11 | N/A | 2025.10.12 | 21 |
| `neoforge-1.21.11` | 1.21.11 | NeoForge 21.11.12 | N/A | 21.11.12-beta | 2025.10.12 | 21 |

### Version Target Entity

```java
/**
 * Represents a build target configuration for a specific
 * Minecraft version and mod loader combination.
 */
public record VersionTarget(
    String id,                    // e.g., "fabric-1.20.6"
    String minecraftVersion,      // e.g., "1.20.6"
    String minecraftMajor,        // e.g., "1.20"
    ModLoader loader,             // FABRIC or NEOFORGE
    int javaVersion,              // 17 or 21
    String loaderVersion,         // Fabric Loader or NeoForge version
    String apiVersion,            // Fabric API version (null for NeoForge)
    String parchmentVersion       // Parchment mappings version
) {
    public enum ModLoader {
        FABRIC,
        NEOFORGE
    }

    public boolean isNeoForge() {
        return loader == ModLoader.NEOFORGE;
    }

    public boolean isFabric() {
        return loader == ModLoader.FABRIC;
    }
}
```

---

## Capability System

### Feature Capability Entity

```java
/**
 * Defines a feature and its availability across Minecraft versions.
 * Used for runtime capability detection and graceful degradation.
 */
public record VersionCapability(
    String featureId,                    // Unique identifier
    String displayName,                  // User-friendly name
    Set<String> supportedMcMajors,       // e.g., {"1.20", "1.21"}
    String degradationMessage,           // Warning shown when unavailable
    CapabilityLevel level                // CORE, ENHANCED, EXPERIMENTAL
) {
    public enum CapabilityLevel {
        CORE,        // Must work on all versions
        ENHANCED,    // May be degraded on older versions
        EXPERIMENTAL // May not work on all versions
    }

    public boolean isSupported(String mcMajor) {
        return supportedMcMajors.contains(mcMajor);
    }
}
```

### Capability Registry

| Feature ID | Display Name | 1.19 | 1.20 | 1.21 | Level | Degradation |
|------------|--------------|------|------|------|-------|-------------|
| `dialog` | Dialog System | Yes | Yes | Yes | CORE | N/A |
| `choices` | Ink Choices | Yes | Yes | Yes | CORE | N/A |
| `variables` | Ink Variables | Yes | Yes | Yes | CORE | N/A |
| `cutscene` | Cutscene Playback | Yes | Yes | Yes | CORE | N/A |
| `recording` | Scene Recording | Yes | Yes | Yes | CORE | N/A |
| `camera` | Camera Control | Yes | Yes | Yes | CORE | N/A |
| `triggers` | Area Triggers | Yes | Yes | Yes | CORE | N/A |
| `screen_effects` | Screen Effects | Verify | Yes | Yes | ENHANCED | "Screen effects may be limited on MC 1.19.x" |
| `advanced_hud` | Advanced HUD | No | Yes | Yes | ENHANCED | "Advanced HUD features require MC 1.20+" |
| `emotes` | Emotes Integration | Verify | Yes | Yes | EXPERIMENTAL | "Emotes API may not be available" |

---

## Compatibility Layer

### Module Structure

```
compat/
├── api/
│   ├── IVersionAdapter.java
│   ├── IGuiRenderCompat.java
│   ├── ICameraCompat.java
│   ├── IAudioCompat.java
│   ├── ICapabilityChecker.java
│   └── VersionCapability.java
├── mc119x/
│   ├── Mc119xVersionAdapter.java
│   ├── Mc119xGuiRenderCompat.java
│   └── Mc119xCameraCompat.java
├── mc120x/
│   ├── Mc120xVersionAdapter.java
│   ├── Mc120xGuiRenderCompat.java
│   └── Mc120xCameraCompat.java
└── mc121x/
    ├── Mc121xVersionAdapter.java
    ├── Mc121xGuiRenderCompat.java
    └── Mc121xCameraCompat.java
```

### Service Loader Registration

```java
// META-INF/services/fr.loudo.narrativecraft.compat.api.IVersionAdapter
// For fabric-1.19.4:
fr.loudo.narrativecraft.compat.mc119x.Mc119xVersionAdapter

// For fabric-1.20.6 and neoforge-1.20.6:
fr.loudo.narrativecraft.compat.mc120x.Mc120xVersionAdapter

// For fabric-1.21.11 and neoforge-1.21.11:
fr.loudo.narrativecraft.compat.mc121x.Mc121xVersionAdapter
```

---

## Mixin Configuration

### Mixin Set Structure

| Mixin Config | Applies To | Contains |
|--------------|------------|----------|
| `narrativecraft.mixins.json` | All targets | Version-independent mixins (Camera, Sound, Input) |
| `narrativecraft.fabric.mixins.json` | Fabric targets | Fabric-specific mixins |
| `narrativecraft.neoforge.mixins.json` | NeoForge targets | NeoForge-specific mixins |
| `narrativecraft.mc119.mixins.json` | 1.19.x targets | MC 1.19 specific mixins (DrawableHelper) |
| `narrativecraft.mc120.mixins.json` | 1.20.x targets | MC 1.20 specific mixins (GuiGraphics) |
| `narrativecraft.mc121.mixins.json` | 1.21.x targets | MC 1.21 specific mixins (current) |

### Mixin Compatibility Matrix

| Mixin Class | Common | MC 1.19 | MC 1.20 | MC 1.21 | Notes |
|-------------|--------|---------|---------|---------|-------|
| CameraMixin | Yes | - | - | - | Stable API |
| SoundEngineMixin | Yes | - | - | - | Stable API |
| SoundManagerMixin | Yes | - | - | - | Stable API |
| KeyboardHandlerMixin | Yes | - | - | - | Stable API |
| MouseHandlerMixin | Yes | - | - | - | Stable API |
| EntityMixin | Yes | - | - | - | Stable API |
| LivingEntityMixin | Yes | - | - | - | Stable API |
| ServerLevelMixin | Yes | - | - | - | Stable API |
| GameRendererMixin | Yes | - | - | - | PoseStack access |
| GuiTextRenderStateMixin | - | - | - | Yes | 1.21+ only |
| GuiGraphicsMixin (Fabric) | - | - | Yes | Yes | 1.20+ GuiGraphics |
| GuiGraphicsMixin (NeoForge) | - | - | Yes | Yes | peekScissorStack() |
| DrawableHelperMixin | - | Yes | - | - | 1.19 rendering |
| LevelRendererMixin | - | Verify | Yes | Yes | World render |
| AvatarRendererMixin | - | - | - | Yes | CameraRenderState |

---

## Artifact Naming

### JAR Naming Convention

```
narrativecraft-{mod_version}-mc{mc_version}-{loader}.jar
```

**Examples:**
- `narrativecraft-1.2.0-mc1.19.4-fabric.jar`
- `narrativecraft-1.2.0-mc1.20.6-fabric.jar`
- `narrativecraft-1.2.0-mc1.20.6-neoforge.jar`
- `narrativecraft-1.2.0-mc1.21.11-fabric.jar`
- `narrativecraft-1.2.0-mc1.21.11-neoforge.jar`

### Metadata Files

**fabric.mod.json (version-specific):**
```json
{
  "schemaVersion": 1,
  "id": "narrativecraft",
  "version": "${version}",
  "name": "NarrativeCraft",
  "depends": {
    "fabricloader": ">=${fabric_loader_version}",
    "minecraft": "${minecraft_version_range}",
    "fabric-api": ">=${fabric_api_version}"
  }
}
```

**neoforge.mods.toml (version-specific):**
```toml
modLoader = "javafml"
loaderVersion = "${neoforge_loader_range}"
[[mods]]
modId = "narrativecraft"
version = "${version}"
[[dependencies.narrativecraft]]
modId = "minecraft"
type = "required"
versionRange = "${minecraft_version_range}"
```

---

## Build Configuration

### gradle.properties Structure

```properties
# ===== Common =====
mod_id=narrativecraft
mod_name=NarrativeCraft
mod_version=1.2.0
mod_group=fr.loudo.narrativecraft

# Build with Java 21 (target runtime varies)
java_compile_version=21

# ===== Shared Dependencies =====
mixin_version=0.8.5
mixinextras_version=0.3.5
blade_ink_version=1.2.1+nc

# ===== MC 1.19.4 =====
mc_1194_version=1.19.4
mc_1194_version_range=[1.19.4,1.20)
mc_1194_java_target=17
mc_1194_fabric_loader=0.14.24
mc_1194_fabric_api=0.87.2+1.19.4
mc_1194_parchment_mc=1.19.4
mc_1194_parchment=2023.06.26

# ===== MC 1.20.6 =====
mc_1206_version=1.20.6
mc_1206_version_range=[1.20.6,1.21)
mc_1206_java_target=17
mc_1206_fabric_loader=0.15.11
mc_1206_fabric_api=0.97.8+1.20.5
mc_1206_neoforge=20.6.119
mc_1206_neoforge_loader_range=[4,)
mc_1206_parchment_mc=1.20.6
mc_1206_parchment=2024.06.16

# ===== MC 1.21.11 =====
mc_12111_version=1.21.11
mc_12111_version_range=[1.21.11,1.22)
mc_12111_java_target=21
mc_12111_fabric_loader=0.18.3
mc_12111_fabric_api=0.140.0+1.21.11
mc_12111_neoforge=21.11.12-beta
mc_12111_neoforge_loader_range=[4,)
mc_12111_parchment_mc=1.21.11
mc_12111_parchment=2025.10.12
```

---

## State Transitions

### Build State Machine

```
[INIT] → [CONFIGURE] → [COMPILE] → [PROCESS_RESOURCES] → [JAR] → [PUBLISH]
           │
           ├── common (version-independent)
           │
           ├── fabric-1.19.4
           │      └── depends on: common, compat/mc119x
           │
           ├── fabric-1.20.6
           │      └── depends on: common, compat/mc120x
           │
           ├── neoforge-1.20.6
           │      └── depends on: common, compat/mc120x
           │
           ├── fabric-1.21.11
           │      └── depends on: common, compat/mc121x
           │
           └── neoforge-1.21.11
                  └── depends on: common, compat/mc121x
```

### Runtime Initialization

```
[MOD_LOAD]
    │
    ├── Load IVersionAdapter via ServiceLoader
    │
    ├── Initialize ICapabilityChecker
    │
    ├── Register capabilities for current MC version
    │
    ├── Log version info and available capabilities
    │
    └── [READY]
```

---

## Validation Rules

### Version Constraint Validation

1. **Loader Exclusion**: NeoForge builds MUST NOT be generated for MC 1.19.x
2. **Java Target**: MC 1.19.x and 1.20.x MUST target Java 17; MC 1.21.x MUST target Java 21
3. **API Versions**: Each target MUST use exact specified API versions
4. **Mixin Compatibility**: Each target MUST only include compatible mixins

### Capability Validation

1. **CORE capabilities**: MUST work on all versions without degradation
2. **ENHANCED capabilities**: MAY be degraded with warning on older versions
3. **EXPERIMENTAL capabilities**: MAY be unavailable on any version

### Artifact Validation

1. **Unique naming**: Each artifact MUST have unique filename
2. **Metadata accuracy**: Each artifact MUST declare correct version dependencies
3. **No cross-contamination**: Each artifact MUST only contain code for its target
