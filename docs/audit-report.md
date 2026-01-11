# NarrativeCraft Production Hardening - Audit Report

**Version**: 1.0.0
**Date**: 2026-01-09
**Auditor**: Claude Code (Production Hardening Initiative)
**Scope**: Full codebase audit for production readiness

---

## Executive Summary

This audit report documents all critical bugs, technical debt, performance issues, and security risks identified in the NarrativeCraft codebase. The audit covers 328 Java files across the common, fabric, and neoforge modules.

### Key Findings

| Category | Critical | Major | Minor | Total |
|----------|----------|-------|-------|-------|
| Concurrency/Thread Safety | 18 | 15 | 2 | 35 |
| Null Safety | 22 | 28 | 8 | 58 |
| State Management | 8 | 18 | 6 | 32 |
| Performance | 4 | 12 | 5 | 21 |
| Security | 1 | 3 | 2 | 6 |
| Documentation | 0 | 5 | 8 | 13 |
| **Total** | **53** | **81** | **31** | **165** |

### Risk Assessment

- **Production Readiness**: NOT READY - Critical issues must be resolved
- **Stability Risk**: HIGH - Concurrency and null safety issues cause crashes
- **Performance Risk**: MEDIUM - Hot path allocations affect tick rate
- **Security Risk**: HIGH - Command injection vulnerability exploitable

---

## 1. Concurrency & Thread Safety Issues

### 1.1 Core Managers (T015)

All five core managers expose internal ArrayLists without synchronization:

| Manager | Location | Severity | Issue |
|---------|----------|----------|-------|
| ChapterManager | Line 131 | Major | Direct ArrayList exposure via getChapters() |
| CharacterManager | Line 65 | Major | Direct ArrayList exposure via getCharacterStories() |
| PlayerSessionManager | Line 56 | Critical | Direct ArrayList exposure via getPlayerSessions() |
| RecordingManager | Line 68 | Major | Direct ArrayList exposure via getRecordings() |
| PlaybackManager | Line 105 | Critical | Direct ArrayList exposure via getPlaybacks() |

**Impact**: ConcurrentModificationException crashes when lists are iterated while being modified from event handlers or tick threads.

### 1.2 Recording Package (T018)

| File | Line | Severity | Issue |
|------|------|----------|-------|
| Recording.java | 54-55 | Critical | Unsynchronized ArrayList access (recordingDataList, trackedEntities) |
| Recording.java | 59-60 | Critical | Non-volatile isRecording and tick fields |
| Recording.java | 166-173 | Critical | First-tick race condition in vehicle detection |
| ActionsData.java | 48-49 | Critical | Unsynchronized locations and actions lists |
| ActionDifferenceListener.java | 60-69 | Critical | Non-volatile state fields with TOCTOU races |
| ActionDifferenceListener.java | 71 | Critical | Unsynchronized HashMap for equipment slots |
| RecordingManager.java | 34 | Critical | Unsynchronized recordings ArrayList |

### 1.3 Session Package (T016)

| File | Line | Severity | Issue |
|------|------|----------|-------|
| PlayerSession.java | 125-134 | Major | ConcurrentModificationException silently suppressed |
| PlayerSession.java | 52-58 | Major | Mutable list fields without synchronization |
| OnServerTick.java | 45-48 | Critical | Missing null check on playerSession |

---

## 2. Null Safety Issues

### 2.1 Mixin Classes (T024)

33 mixin classes audited. 18 classes have null safety issues:

| Mixin | Line | Severity | Issue |
|-------|------|----------|-------|
| CameraMixin | 60 | Critical | Minecraft.getInstance().player may be null |
| AbstractHorseMixin | 50 | Critical | Loop early return processes only first player |
| AbstractBoatMixin | 50 | Critical | Loop early return processes only first player |
| AbstractContainerMenuMixin | 48 | Critical | getRecordingDataFromEntity() unchecked |
| ItemStackMixin | 50 | Critical | getActionDataFromEntity() unchecked |
| GameRendererMixin | 46 | Critical | player null check missing |
| MinecraftCommonMixin | 91 | Critical | server null in delayed lambda |
| EntityMixin | 70 | Critical | ActionsData null dereference |
| LivingEntityMixin | 83 | Critical | ActionsData null dereference |
| PlayerInfoMixin | 59, 108 | Critical | Multiple null dereference risks |
| ServerLevelMixin | 50 | Critical | Loop early return |
| ServerPlayerCommonMixin | 51, 64 | Critical | ActionsData null dereferences |

### 2.2 InkAction Classes (T019)

| Class | Line | Severity | Issue |
|-------|------|----------|-------|
| AnimationInkAction | 81-83 | Major | Duplicate flag assignments |
| ChangeDayTimeInkAction | 82, 89 | Major | Array bounds violations |
| CooldownInkAction | 48 | Major | blockEndTask executed without null check |
| CutsceneInkAction | 50, 76 | Major | Null safety and unsafe list operations |
| DialogParametersInkAction | 139 | Major | Missing break in switch statement |
| MinecraftCommandInkAction | 57-58 | Critical | Unsafe CommandSourceStack construction |
| OnEnterInkAction | 61, 63, 70 | Major | Array bounds and unsafe list operations |
| TextInkAction | 200, 312 | Major | Array bounds and Identifier validation |

### 2.3 Dialog Package (T023)

| Class | Line | Severity | Issue |
|-------|------|----------|-------|
| DialogRenderer | 113, 116 | Critical | Callbacks executed without null check |
| DialogRenderer2D | 93 | Critical | backgroundColor field mutated during render |
| DialogRenderer3D | 125 | Critical | backgroundColor field mutated during render |
| DialogEntityBobbing | 82-91 | Major | Entity rotation modified without restoration |

---

## 3. State Management Issues

### 3.1 HUD State (T021, T022)

| Class | Line | Severity | Issue |
|-------|------|----------|-------|
| MainScreen | 177 | Critical | hideGui set without restoration guarantee |
| DialogCustomOptionsScreen | 109 | Critical | hideGui modified in init() without finally |
| CameraAngleOptionsScreen | 65 | Critical | Empty onClose() prevents parent cleanup |
| StoryChoicesScreen | 231 | Critical | Empty onClose() - no widget cleanup |

### 3.2 Playback State (T017)

| Class | Line | Severity | Issue |
|-------|------|----------|-------|
| PlaybackTickHandler | 37 | Critical | Early return skips remaining players |
| PlaybackData | 75 | Critical | NPE in getLevel() when entity null |
| PlaybackData | 57, 81, 89 | Major | Array index out of bounds risks |
| Playback | 69 | Critical | masterEntity null in constructor |
| Playback | 208 | Critical | NPE in killMasterEntity on empty list |
| Playback | 269-304 | Major | No error handling in action listeners |

### 3.3 Cleanup Guarantees

**Missing try/finally patterns identified in:**
- StoryHandler.playStitch() (Line 193-203)
- StoryHandler.start() (Line 118-149)
- StoryHandler.chooseChoiceAndNext() (Line 237-249)
- InkTagHandler.execute() (Line 46-109)

---

## 4. Performance Issues

### 4.1 Hot Path Allocations (T027)

| Class | Line | Severity | Issue |
|-------|------|----------|-------|
| OnServerTick | 47 | Critical | new ArrayList<>() every server tick per player |
| OnClientTick | 48 | Critical | new ArrayList<>() every client tick |
| TextEffectAnimation | 99 | Major | new HashMap<>() every frame |
| TextEffectAnimation | 38-39 | Major | Multiple HashMap allocations |

### 4.2 O(n) Lookups (T028)

| Class | Line | Severity | Issue |
|-------|------|----------|-------|
| OnServerTick | 90 | Major | List.contains() O(n) search in tick |
| Recording | 134 | Major | Stream + contains() O(n) lookup |
| CutscenePlayback | 152-157 | Major | Linear keyframe search |

### 4.3 Uncompiled Regex (T029)

| Class | Line | Severity | Issue |
|-------|------|----------|-------|
| ParsedDialog | 41-47 | Critical | Pattern.compile() on every dialog parse |
| StoryValidation | 86 | Major | Pattern.compile() during validation |

### 4.4 Stream API in Hot Paths (T030)

| Class | Line | Severity | Issue |
|-------|------|----------|-------|
| TextEffectAnimation | 52-54 | Critical | Stream filter().toList() every frame |
| Playback | 157 | Major | allMatch() in tick() |
| Playback | 322-326 | Major | mapToInt().max() for tick calculation |
| CutscenePlayback | 91-93 | Major | filter().toList() in tick() |

---

## 5. Security Issues

### 5.1 Command Injection (T031)

| Class | Line | Severity | Issue |
|-------|------|----------|-------|
| MinecraftCommandInkAction | 77 | **CRITICAL** | Player name injection in command string |
| MinecraftCommandInkAction | 54-56 | Major | Insufficient command validation |

**Exploit Vector**: Player names containing command metacharacters are directly substituted into command strings via `.replace("@p", playerName)` without escaping.

### 5.2 Path Traversal (T032)

| Class | Line | Severity | Issue |
|-------|------|----------|-------|
| NarrativeCraftFile | 783-787 | Major | World directory path from user input |
| NarrativeCraftFile | 350 | Minor | getParent() with user-controlled names |

### 5.3 PII in Logs (T033)

| Class | Line | Severity | Issue |
|-------|------|----------|-------|
| PlayerSessionManager | 39 | Major | Player info logged via toString() |
| NarrativeCraftFile | 778 | Minor | World level name in error logs |

---

## 6. Event Handler Issues (T025)

### 6.1 Missing Exception Handling

| Handler | Line | Severity | Issue |
|---------|------|----------|-------|
| OnServerTick | 44-153 | Major | No exception handling in tick loop |
| OnClientTick | 43-52 | Major | Unchecked tick methods |
| OnRenderWorld | 43-68 | Critical | PoseStack may become unbalanced |
| OnHudRender | 43-88 | Major | Multiple unchecked render calls |
| OnDeath | 37-45 | Critical | NPE in recording loop |
| OnBreakBlock | 39 | Major | Unsafe null dereference |
| OnRightClickBlock | 49 | Major | Unsafe null dereference |
| OnPlaceBlock | 45 | Major | Unsafe null dereference |
| OnRespawn | 44-45 | Major | Unsafe null dereference |
| OnGameModeChange | 39 | Major | Unsafe null dereference |

---

## 7. Command Permission Issues (T026)

| Command | Line | Severity | Issue |
|---------|------|----------|-------|
| PlayerSessionCommand | 78-80, 91-95 | Critical | Null playerSession dereference |
| RecordCommand | 127-128 | Major | Unsafe regex replacement |
| RecordCommand | 213-214 | Major | Race condition in override list |
| OpenScreenCommand | 54-112 | Critical | Missing null checks (4 methods) |
| StoryCommand | 78 | Major | Missing getPlayer() null check |

---

## 8. Documentation Gaps (T034-T036)

### 8.1 Repository Health

| Item | Status | Notes |
|------|--------|-------|
| Issue Templates | Complete | Bug report and feature request templates present |
| CHANGELOG.md | Missing | No release notes documentation |
| CI/CD Workflows | Missing | No .github/workflows/ directory |
| Git Tags | Present | 31 tags (v0.1.0 - v1.0.1) |

### 8.2 Documentation Coverage

| Document | Status |
|----------|--------|
| README.md | Partial - missing installation instructions |
| CONTRIBUTING.md | Missing |
| docs/INK_GUIDE.md | Missing |
| docs/TAG_REFERENCE.md | Missing |
| docs/TROUBLESHOOTING.md | Present |
| docs/scope-guarantees.md | Present |

### 8.3 Version Inconsistency

- gradle.properties: version=1.0.1
- scope-guarantees.md: Version 1.1.0

---

## 9. Player State Mapping (T039)

### State Components

```
+------------------+     +------------------+     +------------------+
|       HUD        |     |      Camera      |     |      Input       |
+------------------+     +------------------+     +------------------+
| hideGui          |     | position         |     | keyboard capture |
| dialogRenderer   |     | rotation         |     | mouse capture    |
| borderInkAction  |     | fov              |     | escape blocked   |
| fadeInkAction    |     | shake            |     | movement blocked |
| storySaveIcon    |     | cutsceneLock     |     +------------------+
+------------------+     +------------------+

+------------------+     +------------------+
|      Audio       |     |    Narration     |
+------------------+     +------------------+
| musicInstance    |     | storyHandler     |
| soundInkActions  |     | inkActions[]     |
| ambientSounds    |     | dialogRenderer   |
+------------------+     | playbackManager  |
                         | recordingManager |
                         +------------------+
```

### State Transitions

```
                    +------------+
                    |  GAMEPLAY  |
                    +------------+
                         |
         +---------------+---------------+---------------+
         |               |               |               |
         v               v               v               v
    +----------+   +-----------+   +-----------+   +----------+
    | DIALOGUE |   | CUTSCENE  |   | RECORDING |   | PLAYBACK |
    +----------+   +-----------+   +-----------+   +----------+
    | hideGui  |   | hideGui   |   | (normal)  |   | hideGui  |
    | input    |   | camera    |   |           |   | camera   |
    | dialog   |   | input     |   |           |   | input    |
    +----------+   +-----------+   +-----------+   +----------+
         |               |               |               |
         +---------------+---------------+---------------+
                         |
                         v
                    +------------+
                    |  GAMEPLAY  |
                    +------------+
```

### Cleanup Requirements by State

| State | HUD | Camera | Input | Audio | Narration |
|-------|-----|--------|-------|-------|-----------|
| DIALOGUE | hideGui=false, clear dialogRenderer | - | release capture | stop dialog sounds | clear inkActions |
| CUTSCENE | hideGui=false | restore pos/rot/fov | release capture | stop scene audio | clear playback |
| RECORDING | - | - | - | - | clear recording |
| PLAYBACK | hideGui=false | restore pos/rot/fov | release capture | stop playback audio | clear playback |

---

## 10. Severity Matrix (T038)

### Priority 1 - Critical (Must Fix Before Release)

| ID | Category | Location | Issue |
|----|----------|----------|-------|
| C1 | Security | MinecraftCommandInkAction:77 | Command injection via player name |
| C2 | Concurrency | All Managers | Unsynchronized ArrayList exposure |
| C3 | Concurrency | Recording package | Non-volatile fields, race conditions |
| C4 | Null Safety | 22 mixin classes | Unchecked null dereferences |
| C5 | State | Dialog renderers | backgroundColor mutation without reset |
| C6 | State | Screen classes | hideGui without restoration guarantee |
| C7 | Performance | OnServerTick/OnClientTick | ArrayList allocation every tick |

### Priority 2 - Major (Fix Before Stable)

| ID | Category | Location | Issue |
|----|----------|----------|-------|
| M1 | Null Safety | InkAction classes | Array bounds, list operations |
| M2 | State | Playback package | No error handling in actions |
| M3 | Events | All event handlers | Missing exception handling |
| M4 | Performance | TextEffectAnimation | Stream API in render path |
| M5 | Security | NarrativeCraftFile | Path traversal risk |
| M6 | Commands | OpenScreenCommand | Missing null checks |

### Priority 3 - Minor (Fix When Possible)

| ID | Category | Location | Issue |
|----|----------|----------|-------|
| N1 | Performance | Various | Minor allocation overhead |
| N2 | Documentation | Repository | Missing guides and changelog |
| N3 | Code Quality | Various | Switch fallthrough, dead code |

---

## Appendix A: Files Audited

### Common Module (287 files)
- managers/ (5 files)
- narrative/ (89 files)
- screens/ (45 files)
- mixin/ (33 files)
- events/ (18 files)
- commands/ (8 files)
- api/ (12 files)
- Other (77 files)

### Fabric Module (21 files)
- events/ (12 files)
- platform/ (1 file)
- Other (8 files)

### NeoForge Module (20 files)
- events/ (12 files)
- platform/ (1 file)
- Other (7 files)

---

## Appendix B: Audit Methodology

1. **Code Audit (T015-T026)**: Manual review of all packages for patterns indicating bugs, state leaks, and safety issues
2. **Performance Audit (T027-T030)**: Search for allocations in tick/render methods, O(n) operations, uncompiled regex, stream API usage
3. **Security Audit (T031-T033)**: Search for command execution, file operations, and logging statements containing user data
4. **Repository Audit (T034-T036)**: Review of documentation, issue templates, release practices, and versioning

---

*End of Audit Report*
