# NarrativeCraft Smoke Tests

> Runtime verification checklist for multi-version testing

_Last updated: 2026-01-10_

## Overview

These smoke tests verify that NarrativeCraft functions correctly across all supported Minecraft versions. Run these tests before each release and after any significant changes.

## Test Environment Setup

### Prerequisites
- Minecraft Launcher (official or MultiMC/Prism)
- Fabric Loader 0.15+
- Fabric API (version matching MC version)
- Java 21 (for MC 1.21.x) or Java 17 (for MC 1.20.x)

### Test Profiles Required
| Profile | MC Version | Mod JAR |
|---------|------------|---------|
| `NC-Test-1.20.6` | 1.20.6 | `narrativecraft-fabric-1.20.6-*.jar` |
| `NC-Test-1.21.1` | 1.21.1 | `narrativecraft-fabric-1.21.11-*.jar` |

---

## Smoke Test Checklist

### 1. Mod Loading
**Test**: Verify mod initializes without errors

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1.1 | Launch Minecraft with NarrativeCraft | Game loads to main menu |
| 1.2 | Open Mods menu (Mod Menu mod required) | NarrativeCraft appears in list |
| 1.3 | Check game log for errors | No `[ERROR]` or `[FATAL]` entries for `narrativecraft` |

**Files to check**: `logs/latest.log`

---

### 2. Main Screen & UI
**Test**: Verify GUI screens open correctly

| Step | Action | Expected Result |
|------|--------|-----------------|
| 2.1 | Press NarrativeCraft key (default: N) | Main screen opens |
| 2.2 | Check logo rendering | Logo displays without visual artifacts |
| 2.3 | Navigate through menus | All buttons are clickable, text is readable |
| 2.4 | Open Story Manager | Story list screen opens |
| 2.5 | Open Options screen | Options render correctly |

**Key differences to verify**:
- `renderBlurredBackground()` - 1.21.x only, 1.20.x should skip gracefully
- Font rendering via `screen.getFont()` vs `screen.font`

---

### 3. Story Creation & Management
**Test**: Verify story CRUD operations

| Step | Action | Expected Result |
|------|--------|-----------------|
| 3.1 | Create new story | Story folder created in `narrativecraft/stories/` |
| 3.2 | Add chapter | Chapter appears in list |
| 3.3 | Add scene | Scene folder created, .ink file generated |
| 3.4 | Edit story metadata | Changes persist after reload |
| 3.5 | Delete story | Story removed from list and filesystem |

**Files to verify**:
- `narrativecraft/stories/<story_name>/story.json`
- `narrativecraft/stories/<story_name>/script/main.ink`

---

### 4. Character System
**Test**: Verify character creation and rendering

| Step | Action | Expected Result |
|------|--------|-----------------|
| 4.1 | Create new character | Character appears in list |
| 4.2 | Change character entity type | Entity type dropdown works |
| 4.3 | Change character skin (URL) | Skin applies to character |
| 4.4 | Spawn character in scene | Character entity spawns correctly |
| 4.5 | Character poses work | Character can be posed |

**Key differences to verify**:
- `DynamicTexture` constructor parameters
- Entity spawn reason (`EntitySpawnReason` in 1.21.x vs null in 1.20.x)

---

### 5. Recording & Playback
**Test**: Verify action recording system

| Step | Action | Expected Result |
|------|--------|-----------------|
| 5.1 | Start recording | Recording indicator shows |
| 5.2 | Move around, perform actions | Actions are captured |
| 5.3 | Stop recording | Recording saved to file |
| 5.4 | Play back recording | Recorded actions replay |
| 5.5 | Vehicle recording (boat, horse) | Riding actions record/playback |

**Key differences to verify**:
- `AbstractBoat` (1.21.x) vs `Boat` (1.20.x)
- `AbstractHorse` package path differences
- `startRiding()` parameter count

---

### 6. Dialog System
**Test**: Verify dialog rendering and interaction

| Step | Action | Expected Result |
|------|--------|-----------------|
| 6.1 | Trigger dialog with choices | Choice screen appears |
| 6.2 | Select a choice | Story progresses correctly |
| 6.3 | Dialog 2D rendering | Text displays correctly |
| 6.4 | Dialog 3D rendering | 3D bubbles render above characters |
| 6.5 | Dialog animations | Skip arrow animates |

**Key differences to verify**:
- VertexConsumer API chain (`.addVertex()` vs `.vertex()`)
- PoseStack methods (`pushPose()` vs `pushMatrix()`)
- Camera vector methods

---

### 7. Cutscene System
**Test**: Verify cutscene camera control

| Step | Action | Expected Result |
|------|--------|-----------------|
| 7.1 | Create cutscene | Cutscene appears in list |
| 7.2 | Add keyframes | Keyframes recorded |
| 7.3 | Play cutscene | Camera follows path smoothly |
| 7.4 | Easing functions | Easing applies correctly |
| 7.5 | Area triggers | Cutscene triggers on entry |

**Key differences to verify**:
- Camera position/rotation access (`position()` vs `getPosition()`)
- `Mth.lerp` Vec3 interpolation (component-wise in 1.20.x)

---

### 8. Sound System
**Test**: Verify audio playback

| Step | Action | Expected Result |
|------|--------|-----------------|
| 8.1 | Play story sound | Sound plays |
| 8.2 | Sound volume control | Volume changes apply |
| 8.3 | Looping sounds | Sounds loop correctly |
| 8.4 | 3D positioned sounds | Spatial audio works |

---

### 9. Commands
**Test**: Verify chat commands work

| Step | Action | Expected Result |
|------|--------|-----------------|
| 9.1 | `/narrativecraft help` | Help message displays |
| 9.2 | `/narrativecraft story list` | Stories listed |
| 9.3 | `/narrativecraft play <story>` | Story starts playing |
| 9.4 | `/narrativecraft stop` | Story stops |

**Key differences to verify**:
- Permission system (int level vs PermissionSet)
- `Util.getPlatform().openPath()` vs `openFile()`

---

### 10. Interaction System
**Test**: Verify entity interactions

| Step | Action | Expected Result |
|------|--------|-----------------|
| 10.1 | Create interaction point | Interaction shows in list |
| 10.2 | Set interaction trigger | Trigger type saved |
| 10.3 | Look-at detection | `isLookingAtMe()` alternative works |
| 10.4 | Interaction eye renderer | Eye icon renders correctly |

**Key differences to verify**:
- `LivingEntity.isLookingAtMe()` (1.21.x only - custom impl in 1.20.x)
- Camera entity access

---

## Version-Specific Regression Tests

### 1.20.x Only
- [ ] `net.minecraft.Util` import compiles
- [ ] `ResourceLocation` constructor works
- [ ] `Boat` class references work
- [ ] `animal.horse.AbstractHorse` import works
- [ ] `camera.getPosition()` / `getLeftVector()` / `getUpVector()` work

### 1.21.x Only
- [ ] `net.minecraft.util.Util` import compiles
- [ ] `ResourceLocation.withDefaultNamespace()` works
- [ ] `AbstractBoat` class references work
- [ ] `animal.equine.AbstractHorse` import works
- [ ] `camera.position()` / `leftVector()` / `upVector()` work
- [ ] `renderBlurredBackground()` renders correctly

---

## Reporting Issues

When reporting issues found during smoke testing:

1. **Version info**: MC version, mod version, Fabric Loader version
2. **Steps to reproduce**: Exact steps that trigger the issue
3. **Expected vs actual**: What should happen vs what happened
4. **Logs**: Attach `logs/latest.log` and `crash-reports/` if applicable
5. **Screenshots/video**: Visual bugs especially

---

## Automated Testing (Future)

The following tests could be automated with `gametest` framework:
- [ ] Mod initialization
- [ ] Screen opening (headless)
- [ ] File I/O operations
- [ ] Command parsing
- [ ] NBT serialization

---

## Release Checklist

Before releasing:
- [ ] All smoke tests pass on MC 1.20.6
- [ ] All smoke tests pass on MC 1.21.1
- [ ] No new warnings in compilation
- [ ] CI pipeline green
- [ ] Version number updated
- [ ] Changelog written
