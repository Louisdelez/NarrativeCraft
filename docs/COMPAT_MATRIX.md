# NarrativeCraft Compatibility Matrix

> Multi-version support architecture documentation

## Supported Versions

| Minecraft Version | Mod Loader | Module | Status |
|-------------------|------------|--------|--------|
| 1.20.6 | Fabric | `fabric-1.20.6` | ✅ Supported |
| 1.20.6 | NeoForge | `neoforge-1.20.6` | ✅ Supported |
| 1.21.11 | Fabric | `fabric-1.21.11` | ✅ Supported |
| 1.21.11 | NeoForge | `neoforge-1.21.11` | ✅ Supported |

## Architecture Overview

```
NarrativeCraft/
├── common/                    # Shared code (1.21.x API baseline)
├── common-mc120/              # 1.20.x version-specific overrides
├── common-mc121/              # 1.21.x version-specific code (if needed)
├── compat-api/                # Version-neutral abstractions
│   └── src/main/java/
│       └── fr/loudo/narrativecraft/compat/api/
│           ├── IGuiRenderCompat.java
│           ├── IIdBridge.java
│           ├── IInputCompat.java
│           ├── NcId.java
│           ├── NcKeyEvent.java
│           └── NcMouseEvent.java
├── compat-mc120x/             # 1.20.x compat implementations
├── compat-mc121x/             # 1.21.x compat implementations
├── fabric-1.20.6/             # Fabric 1.20.6 loader module
├── fabric-1.21.11/            # Fabric 1.21.11 loader module
├── neoforge-1.20.6/           # NeoForge 1.20.6 loader module
└── neoforge-1.21.11/          # NeoForge 1.21.11 loader module
```

## Source Set Overlay Pattern

The build uses source set merging where `common-mc120` overlays `common`:
- Files in `common-mc120` **replace** same-path files in `common`
- Files only in `common` are used as-is
- This allows targeted overrides without full file duplication

## What Goes Where

### `common/` (Base Implementation)
- All business logic
- Default implementations using 1.21.x APIs
- Any code that doesn't need version-specific changes

### `common-mc120/` (Version Overrides)
Override a file when:
- Package paths differ (e.g., `net.minecraft.util.Util` vs `net.minecraft.Util`)
- Method signatures changed (e.g., `startRiding(e, true, true)` vs `startRiding(e, true)`)
- Methods don't exist (e.g., `snapTo()`, `isLookingAtMe()`)
- API patterns differ (e.g., VertexConsumer chaining)

**DO NOT override for:**
- Logic changes (refactor to use compat-api instead)
- Adding features (add to common/)

### `compat-api/` (Abstraction Layer)
Create bridges for:
- Frequently used API differences (ResourceLocation, Camera, Font)
- Cross-cutting concerns (rendering, input handling)
- Any pattern used in 5+ overrides

## Key API Differences (1.20.x vs 1.21.x)

### Package Paths
| 1.21.x | 1.20.x |
|--------|--------|
| `net.minecraft.util.Util` | `net.minecraft.Util` |
| `net.minecraft.world.entity.animal.equine.AbstractHorse` | `net.minecraft.world.entity.animal.horse.AbstractHorse` |
| `net.minecraft.world.entity.vehicle.boat.AbstractBoat` | `net.minecraft.world.entity.vehicle.Boat` |
| `net.minecraft.server.permissions.PermissionSet` | _(doesn't exist - use int permission level)_ |

### Camera API
| 1.21.x | 1.20.x |
|--------|--------|
| `camera.position()` | `camera.getPosition()` |
| `camera.leftVector()` | `camera.getLeftVector()` |
| `camera.upVector()` | `camera.getUpVector()` |
| `camera.entity()` | `camera.getEntity()` |

### VertexConsumer API
| 1.21.x | 1.20.x |
|--------|--------|
| `.addVertex(matrix, x, y, z).setColor(c).setLight(l)` | `.vertex(matrix, x, y, z).color(c).uv(u,v).overlayCoords(o1,o2).uv2(l).normal(nx,ny,nz).endVertex()` |

### Entity API
| 1.21.x | 1.20.x |
|--------|--------|
| `entity.snapTo(vec3)` | `entity.setPos(x, y, z)` |
| `entity.startRiding(e, true, true)` | `entity.startRiding(e, true)` |
| `livingEntity.isLookingAtMe(player)` | _(custom implementation needed)_ |

### Screen/GUI API
| 1.21.x | 1.20.x |
|--------|--------|
| `screen.getFont()` | `Minecraft.getInstance().font` |
| `ResourceLocation.withDefaultNamespace(path)` | `new ResourceLocation(path)` |
| `renderBlurredBackground(GuiGraphics)` | _(doesn't exist)_ |
| `Matrix3x2fStack` (2D transforms) | `PoseStack` |
| `guiGraphics.pose().pushMatrix()` | `guiGraphics.pose().pushPose()` |
| `pose.translate(float, float)` | `pose.translate(double, double, double)` |

### Math API
| 1.21.x | 1.20.x |
|--------|--------|
| `Mth.lerp(double, Vec3, Vec3)` | _(component-wise implementation needed)_ |

### Other APIs
| 1.21.x | 1.20.x |
|--------|--------|
| `Util.getPlatform().openPath(path)` | `Util.getPlatform().openFile(file)` |
| `getToastManager()` | `getToasts()` |
| `SharedConstants.getCurrentVersion().name()` | `SharedConstants.getCurrentVersion().getName()` |
| `DynamicTexture(supplier, name, image)` | `DynamicTexture(image)` |

## How to Add a New Minecraft Version

### Checklist

1. **Create loader module**
   ```
   mkdir -p new-loader-x.xx.x/src/main/java
   ```

2. **Configure build.gradle**
   - Add new subproject to settings.gradle
   - Configure Minecraft/Fabric versions
   - Set up source set dependencies

3. **Identify API differences**
   - Compare against base version (1.21.x)
   - Document in this file
   - Determine if common-mc1XX override needed

4. **Create version-specific code**
   - If minor changes: Add to existing common-mc1XX
   - If major changes: Create new common-mcXXX

5. **Update compat bridges**
   - Add implementation to compat-mcXXX
   - Register in VersionAdapterLoader

6. **Test**
   - Run `:new-loader:build`
   - Verify existing versions still build
   - Run smoke tests

### Version Compatibility Tiers

| Tier | Description | Example |
|------|-------------|---------|
| **Same major** | Usually compatible, minor overrides | 1.21.0 → 1.21.1 |
| **Adjacent minor** | Some API changes | 1.20.6 → 1.21.0 |
| **Major version jump** | Significant changes | 1.19.x → 1.21.x |

## Maintenance Guidelines

### When modifying `common/`:
1. Ensure change works with 1.21.x API
2. Check if mc120 override needs update
3. Run both `:fabric-1.20.6:build` and `:fabric-1.21.11:build`

### When adding features:
1. Add to `common/` first
2. Create mc120 override only if API differs
3. Document why override is needed (comment in file)

### When fixing bugs:
1. Fix in `common/` if possible
2. Mirror fix to mc120 override if exists
3. Consider if bug fix can eliminate override

## CI/CD Integration

All versions must build on every PR:
```yaml
build-matrix:
  - fabric-1.20.6
  - fabric-1.21.11
  - neoforge-1.20.6
  - neoforge-1.21.11
```

Release artifacts:
- `narrativecraft-fabric-1.20.6-{version}.jar`
- `narrativecraft-fabric-1.21.11-{version}.jar`
- `narrativecraft-neoforge-1.20.6-{version}.jar`
- `narrativecraft-neoforge-1.21.11-{version}.jar`

## Current Override Count

| Category | Count | Bridge Potential |
|----------|-------|------------------|
| screen | ~35 | Medium |
| render | ~10 | High |
| entity | ~8 | Medium |
| mixin | ~7 | Low |
| recording | ~6 | Low |
| util | ~5 | High |
| cutscene | ~4 | Medium |
| commands | ~6 | Low |
| **Total** | **~80** | - |

_Last updated: 2026-01-10_
