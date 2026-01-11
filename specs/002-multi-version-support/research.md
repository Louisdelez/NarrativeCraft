# Research: Multi-Version Minecraft Support

**Feature**: 002-multi-version-support
**Date**: 2026-01-09

## 1. Architectury Loom Multi-Version Setup

### Decision: Use Architectury Loom with version-specific subprojects

### Rationale
- Architectury Loom is the industry-standard solution for multi-loader Minecraft mods
- Built-in support for Fabric, Forge, NeoForge, and Quilt
- Layered mappings support (Mojmap + Parchment)
- Active development and community support
- Compatible with existing multiloader-common structure in NarrativeCraft

### Alternatives Considered
| Alternative | Why Rejected |
|-------------|--------------|
| Essential Gradle Toolkit | More complex preprocessor-based approach; overkill for 3 versions |
| VanillaGradle + manual modules | Lack of multiloader abstractions; more boilerplate |
| Stonecutter | Newer, less mature; inline version comments reduce readability |

### Implementation Approach

**Build Structure:**
```
settings.gradle
├── include("common")
├── include("fabric-1.19.4")
├── include("fabric-1.20.6")
├── include("fabric-1.21.11")
├── include("neoforge-1.20.6")
└── include("neoforge-1.21.11")
```

**Key Configuration:**
```groovy
// build.gradle (root)
plugins {
    id 'dev.architectury.loom' version '1.7-SNAPSHOT' apply false
}

// Per-version subproject
subprojects {
    apply plugin: 'dev.architectury.loom'

    loom {
        silentMojangMappingsLicense()
    }

    dependencies {
        minecraft "com.mojang:minecraft:${project.minecraft_version}"
        mappings loom.layered() {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-${parchment_mc}:${parchment_version}@zip")
        }
    }
}
```

### Sources
- [Architectury Loom GitHub](https://github.com/architectury/architectury-loom)
- [Architectury Documentation](https://docs.architectury.dev/plugin/get_started)
- [Essential Gradle Toolkit](https://github.com/EssentialGG/essential-gradle-toolkit)

---

## 2. Minecraft API Changes (1.19 → 1.20 → 1.21)

### Decision: Abstract version-sensitive APIs behind compatibility interfaces

### Key Breaking Changes

#### 1.19.4 → 1.20: GuiComponent → GuiGraphics

| 1.19.4 | 1.20+ |
|--------|-------|
| `GuiComponent.fill()` | `GuiGraphics.fill()` |
| `GuiComponent.blit()` | `GuiGraphics.blit()` |
| `Font.draw()` / `Font.drawShadow()` | `GuiGraphics.drawString()` |
| `DrawableHelper` (Fabric) | `DrawContext` (Fabric) |
| `MatrixStack` parameter | `GuiGraphics` / `DrawContext` contains MatrixStack |

**Impact on NarrativeCraft:**
- `GuiGraphicsFabricMixin.java` - uses scissor stack APIs
- `GuiGraphicsNeoForgeMixin.java` - uses `peekScissorStack()`
- All HUD rendering code

#### 1.20.x → 1.21.x: Minor API refinements

| 1.20.x | 1.21.x |
|--------|--------|
| `blit(texture, ...)` | `blit(RenderType::guiTextured, texture, ...)` |
| RGB color format | ARGB color format (1.21.6+) |

**Impact on NarrativeCraft:**
- Color handling in text rendering
- Sprite blitting calls

#### 1.21.5 → 1.21.6+: GUI Rendering Pipeline Overhaul

Major changes to GUI rendering with `GuiRenderState`:
- Prepare phase submits to GuiRenderState
- Render phase processes stored state
- `RenderPipelines.GUI_TEXTURE` replaces `RenderType::guiTextured`

**Impact on NarrativeCraft:**
- Current code targets 1.21.11, needs verification for latest API
- May need `mc121x` compat adjustments for future 1.21.6+ support

### Mixin Target Changes

| Mixin | 1.19.4 | 1.20.6 | 1.21.11 |
|-------|--------|--------|---------|
| GuiGraphics | N/A (class doesn't exist) | `GuiGraphics` | `GuiGraphics` |
| DrawContext (Fabric) | `DrawableHelper` | `DrawContext` | `DrawContext` |
| Camera | `Camera` | `Camera` | `Camera` (stable) |
| SoundEngine | `SoundEngine` | `SoundEngine` | `SoundEngine` (stable) |
| LevelRenderer | `LevelRenderer` | `LevelRenderer` | `LevelRenderer` (stable) |

### Compatibility Interface Design

```java
// IGuiRenderCompat.java
public interface IGuiRenderCompat {
    void fill(Object graphics, int x1, int y1, int x2, int y2, int color);
    void drawString(Object graphics, Font font, String text, int x, int y, int color);
    void enableScissor(Object graphics, int x1, int y1, int x2, int y2);
    void disableScissor(Object graphics);
}
```

### Sources
- [1.19.4 → 1.20 Migration Primer](https://gist.github.com/ChampionAsh5357/cf818acc53ffea6f4387fe28c2977d56)
- [1.20.5 → 1.21 Migration Primer](https://gist.github.com/ChampionAsh5357/d895a7b1a34341e19c80870720f9880f)
- [Fabric DrawContext Documentation](https://docs.fabricmc.net/develop/rendering/draw-context)
- [NeoForge GuiGraphics](https://github.com/neoforged/NeoForge/blob/1.21.x/patches/net/minecraft/client/gui/GuiGraphics.java.patch)

---

## 3. Fabric API Version Compatibility

### Decision: Pin to latest stable Fabric API per Minecraft version

### Version Matrix

| Minecraft | Fabric API | Fabric Loader | Java |
|-----------|------------|---------------|------|
| 1.19.4 | 0.87.2+1.19.4 | 0.14.x+ | 17 |
| 1.20.6 | 0.97.8+1.20.5 | 0.15.x+ | 17 |
| 1.21.11 | 0.140.0+1.21.11 | 0.18.x+ | 21 |

### Fabric Loader Version Ranges

```properties
# fabric.mod.json depends block
"fabric-loader": ">=0.14.0"  # 1.19.4
"fabric-loader": ">=0.15.0"  # 1.20.6
"fabric-loader": ">=0.18.0"  # 1.21.11
```

### API Deprecations

| Deprecated in | API | Replacement |
|---------------|-----|-------------|
| 1.20 | `DrawableHelper` | `DrawContext` |
| 1.20 | `MatrixStack` (direct use in render) | `DrawContext.getMatrices()` |

### Sources
- [Fabric API on Modrinth](https://modrinth.com/mod/fabric-api/versions)
- [Fabric API Releases](https://github.com/FabricMC/fabric-api/releases)

---

## 4. NeoForge Version Compatibility

### Decision: Support NeoForge 1.20.6 (20.6.x) and 1.21.11 (21.11.x)

### Version Matrix

| Minecraft | NeoForge | Loader Range | Java |
|-----------|----------|--------------|------|
| 1.19.4 | N/A | N/A | N/A |
| 1.20.6 | 20.6.119+ | [4,) | 17 |
| 1.21.11 | 21.11.12-beta | [4,) | 21 |

### NeoForge Versioning System

NeoForge uses adapted semver:
- **Major** = Minecraft minor version (20 = 1.20.x, 21 = 1.21.x)
- **Minor** = Minecraft patch version (6 = 1.20.6)
- **Patch** = NeoForge build number

Example: `20.6.119` = 120th build for Minecraft 1.20.6

### API Differences: NeoForge vs Legacy Forge

| Feature | Legacy Forge | NeoForge |
|---------|--------------|----------|
| Event Bus | MinecraftForge.EVENT_BUS | NeoForge.EVENT_BUS |
| Mod annotation | @Mod | @Mod |
| Config | ForgeConfigSpec | NeoForgeConfigSpec |
| Network | SimpleImpl | PayloadRegistrar |

**Impact on NarrativeCraft:**
- `NarrativeCraftNeoForge.java` uses `@Mod` annotation (compatible)
- Event registration uses NeoForge patterns (compatible)
- Packet sending via `NeoForgePacketSender.java` (verify compatibility)

### Sources
- [NeoForge Releases](https://neoforged.net/categories/releases/)
- [NeoForge Versioning](https://docs.neoforged.net/docs/gettingstarted/versioning/)

---

## 5. blade-ink Java 17 Compatibility

### Decision: blade-ink 1.2.1+nc is Java 17 compatible

### Analysis

The blade-ink library (1.2.1+nc custom build) is a Java port of the Ink narrative scripting runtime. Based on the dependency declaration:

```groovy
implementation "com.bladecoder.ink:blade-ink:1.2.1+nc"
```

**Compatibility Check:**
1. blade-ink is built targeting Java 8+ baseline
2. The `+nc` suffix indicates NarrativeCraft custom build
3. No Java 21-specific features (records, sealed classes, pattern matching) used in Ink runtime
4. Story file format (.ink, .json) is version-independent

**Verification Required:**
- [ ] Test blade-ink loading on Java 17 runtime
- [ ] Verify `getCurrentKnot()` and `getCurrentStitch()` custom methods work

### Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| blade-ink fails on Java 17 | Low | Medium | Rebuild blade-ink with Java 17 target |
| Custom methods missing | Low | High | Verify methods exist in custom build |

---

## 6. Dependency Version Summary

### gradle.properties Configuration

```properties
# Common
mod_version=1.2.0
java_version=21

# Minecraft 1.19.4 (Fabric only)
mc_1_19_4=1.19.4
fabric_api_1_19_4=0.87.2+1.19.4
fabric_loader_1_19_4=0.14.24
parchment_1_19_4=2023.06.26

# Minecraft 1.20.6 (Fabric + NeoForge)
mc_1_20_6=1.20.6
fabric_api_1_20_6=0.97.8+1.20.5
fabric_loader_1_20_6=0.15.11
neoforge_1_20_6=20.6.119
parchment_1_20_6=2024.06.16

# Minecraft 1.21.11 (Fabric + NeoForge)
mc_1_21_11=1.21.11
fabric_api_1_21_11=0.140.0+1.21.11
fabric_loader_1_21_11=0.18.3
neoforge_1_21_11=21.11.12-beta
parchment_1_21_11=2025.10.12

# Shared dependencies
mixin_version=0.8.5
mixinextras_version=0.3.5
blade_ink_version=1.2.1+nc
```

---

## 7. Mixin Strategy

### Decision: Maintain separate mixin sets per version with shared common mixins

### Mixin Organization

```
common/src/main/resources/
└── narrativecraft.mixins.json          # Version-independent mixins

fabric-1.19.4/src/main/resources/
├── narrativecraft.fabric.mixins.json   # Fabric 1.19.4 specific
└── narrativecraft.mc119.mixins.json    # MC 1.19.4 specific

fabric-1.20.6/src/main/resources/
├── narrativecraft.fabric.mixins.json   # Fabric 1.20.6 specific
└── narrativecraft.mc120.mixins.json    # MC 1.20.6 specific

fabric-1.21.11/src/main/resources/
├── narrativecraft.fabric.mixins.json   # Fabric 1.21.11 specific (current)
└── narrativecraft.mc121.mixins.json    # MC 1.21.11 specific

neoforge-1.20.6/src/main/resources/
├── narrativecraft.neoforge.mixins.json # NeoForge 1.20.6 specific
└── narrativecraft.mc120.mixins.json    # MC 1.20.6 specific

neoforge-1.21.11/src/main/resources/
├── narrativecraft.neoforge.mixins.json # NeoForge 1.21.11 specific (current)
└── narrativecraft.mc121.mixins.json    # MC 1.21.11 specific
```

### Mixin Audit Results

| Mixin | 1.19.4 Compatible | 1.20.6 Compatible | 1.21.11 Compatible | Notes |
|-------|-------------------|-------------------|-------------------|-------|
| CameraMixin | Yes | Yes | Yes | Camera API stable |
| SoundEngineMixin | Yes | Yes | Yes | Sound API stable |
| GameRendererMixin | Verify | Yes | Yes | PoseStack handling |
| GuiGraphicsFabricMixin | No | Yes | Yes | Class doesn't exist in 1.19 |
| GuiGraphicsNeoForgeMixin | N/A | Yes | Yes | NeoForge only |
| KeyboardHandlerMixin | Yes | Yes | Yes | Input API stable |
| MouseHandlerMixin | Yes | Yes | Yes | Input API stable |
| LevelRendererMixin | Verify | Yes | Yes | World render changes |

### High-Risk Mixins Requiring Version-Specific Implementations

1. **GuiGraphics/DrawContext rendering** - Class doesn't exist in 1.19.4
2. **LevelRenderer** - Potential changes between 1.19 and 1.20
3. **AvatarRendererMixin** - Uses CameraRenderState which may not exist in 1.19

---

## 8. Resolved NEEDS CLARIFICATION Items

| Item | Resolution |
|------|------------|
| Fabric API versions | 0.87.2+1.19.4, 0.97.8+1.20.5, 0.140.0+1.21.11 |
| NeoForge versions | 20.6.119 (1.20.6), 21.11.12-beta (1.21.11) |
| Parchment versions | 2023.06.26 (1.19.4), 2024.06.16 (1.20.6), 2025.10.12 (1.21.11) |
| Architectury Loom version | 1.7-SNAPSHOT |
| blade-ink Java 17 | Compatible (needs runtime verification) |
| GuiGraphics existence | Exists from 1.20+, use DrawableHelper abstraction for 1.19 |
