# Changelog

All notable changes to NarrativeCraft will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.0] - 2026-01-11

### Added

#### Multi-Version Support
- **Minecraft 1.19.4 Support**: Full compatibility with MC 1.19.4 (Fabric)
- **Minecraft 1.20.6 Support**: Full compatibility with MC 1.20.6 (Fabric, NeoForge)
- **NeoForge Support**: Full compatibility with NeoForge mod loader for both 1.20.6 and 1.21.11
- **5 Build Targets**: fabric-1.19.4, fabric-1.20.6, fabric-1.21.11, neoforge-1.20.6, neoforge-1.21.11
- **Compat API Module**: Version-agnostic interfaces for cross-version compatibility
- **Version Adapters**: ServiceLoader-based adapter selection at runtime
- **Compat Logging**: Boot-time logging of version, loader, adapter, and feature support
- **Cross-Version Test**: example-stories/cross-version-test.ink for validation

#### Runtime Validation
- **Smoke Test Issue Template**: GitHub issue template for runtime bug reports
- **Feature Flags**: Version-specific feature availability (e.g., `is_looking_at_me`, `vec3_lerp`)

### Changed

#### Architecture
- **Bridge Pattern**: Extracted version-specific code to compat modules
- **ICameraCompat**: Camera position/vector abstraction
- **IUtilCompat**: Entity, sound, texture, and NBT utility abstraction
- **IGuiRenderCompat**: GUI rendering abstraction

#### Build System
- **Java 21 Compile**: All modules compiled with Java 21
- **Multi-Module Gradle**: Separate modules per MC version (fabric-1.20.6, fabric-1.21.11)
- **Shared Common Code**: common/ and common-mc120/common-mc121 modules

### Known Limitations

#### MC 1.19.4
- No blurred background in GUIs (blur shader unavailable)
- Uses PoseStack instead of GuiGraphics for rendering
- Manual layout positioning (no HeaderAndFooterLayout/LinearLayout)
- Checkbox constructor instead of builder pattern

#### MC 1.20.6
- No blurred background in GUIs (blur shader unavailable)
- Entity snap-to method differences
- Custom isLookingAtMe implementation (native not available)
- No EquipmentSlot.SADDLE/BODY support

---

## [1.1.0] - 2026-01-09

### Added

#### Validation & Error Handling
- **Tag Validator**: Comprehensive validation for all 21 Ink tags at load time
- **Typo Suggestions**: "Did you mean X?" suggestions using Levenshtein distance algorithm
- **4-Component Error Format**: Clear error messages with [WHAT] [WHERE] [WHY] [FIX] structure
- **Validation Error Codes**: UNKNOWN_TAG, MISSING_ARGUMENT, INVALID_ARGUMENT_TYPE, INVALID_ARGUMENT_VALUE, SECURITY_VIOLATION, RESOURCE_NOT_FOUND
- **InkValidationService**: Centralized validation service for stories

#### State Management
- **NarrativeState Enum**: Explicit state machine (GAMEPLAY, DIALOGUE, CUTSCENE, RECORDING, PLAYBACK, LOADING)
- **NarrativeStateManager**: Centralized state transitions with cleanup guarantees
- **Cleanup Handlers**: Priority-based cleanup for HUD, Camera, Input, and Audio
- **NarrativeCleanupService**: Ensures player state always recovers after narrative scenes

#### Security
- **CommandProxy**: Whitelist/blacklist for Minecraft commands executed from Ink scripts
- **Command Validation**: Blocks dangerous commands (op, ban, fill, execute, etc.)

#### Documentation
- **INK_GUIDE.md**: Comprehensive Ink scripting guide
- **TAG_REFERENCE.md**: Complete reference for all 21 tags
- **TROUBLESHOOTING.md**: Common issues and solutions
- **CONTRIBUTING.md**: Developer setup, code style, PR process

#### Example Pack
- **Tutorial Stories**: 01-first-dialog.ink, 02-choices.ink, 03-cutscene.ink
- **Showcase**: complex-scene.ink demonstrating all advanced features

#### CI/CD
- **GitHub Actions CI**: Automated testing on push/PR
- **Release Workflow**: Automated releases on version tags
- **Spotless Check**: Code formatting validation
- **Artifact Upload**: Build artifacts preserved for 14 days

#### Performance Instrumentation
- **NarrativeProfiler**: Optional profiling for subsystem timing
- **NarrativeCraftConstants**: Centralized constants replacing magic numbers

### Changed

#### Performance Optimizations
- **Tick Handlers**: Replaced ArrayList allocation + removeAll() with Iterator.remove()
  - Before: ~2 allocations per tick per player
  - After: Zero allocations
- **Recording**: Replaced List<Entity> + stream().map().toList() with HashSet<UUID>
  - Before: O(n) contains() + list allocation per tick
  - After: O(1) HashSet lookup, zero allocations
- **TextEffectAnimation**: Replaced stream().filter().toList() with direct loops
  - Before: List allocation per tick for each effect type
  - After: Zero allocations with inline filtering
- **Vector2f Reuse**: computeIfAbsent() pattern instead of new Vector2f() per tick
- **ParsedDialog**: Pre-compiled static regex patterns instead of Pattern.compile() per parse

#### Bug Fixes (Critical)
- **T057**: Fixed switch/case fallthrough in DialogParametersInkAction (WIDTH case)
- **T058**: Fixed ConcurrentModificationException using CopyOnWriteArrayList for inkActions
- **T059**: Fixed recording first-tick vehicle detection order
- **T060**: Fixed trigger re-entrancy by setting guard BEFORE playStitch()

#### Bug Fixes (Major)
- **T055**: Added null safety checks to all 33 mixin classes
- **T056**: Added debounce for area trigger to prevent rapid re-triggering
- **T048-T054**: Added try/finally blocks for guaranteed state cleanup

### Security
- **Command Whitelist**: Only safe commands allowed from Ink scripts
- **Path Traversal**: Documented audit findings for file operations

## [1.0.0] - Previous Release

Initial release of NarrativeCraft for Minecraft 1.21.11.

### Features
- Ink scripting language integration
- Dialog system with character names and text effects
- Cutscene recording and playback
- Camera angle system
- Area triggers for story progression
- Character interactions
- Sound effects and music control
- Time and weather manipulation
- Screen effects (fade, shake, borders)

---

## Version History

| Version | Date | Minecraft | Loaders | Status |
|---------|------|-----------|---------|--------|
| 1.2.0 | 2026-01-11 | 1.19.4, 1.20.6, 1.21.11 | Fabric (all 3), NeoForge (1.20.6, 1.21.11) | Current |
| 1.1.0 | 2026-01-09 | 1.21.11 | Fabric | Previous |
| 1.0.0 | 2025-xx-xx | 1.21.11 | Fabric | Legacy |

## Upgrade Notes

### From 1.0.0 to 1.1.0

**No breaking changes.** This release is fully backwards compatible.

**Recommended actions:**
1. Review new error messages - they provide better debugging info
2. Check TAG_REFERENCE.md for updated tag documentation
3. Enable profiling temporarily to identify performance bottlenecks:
   ```java
   NarrativeProfiler.setEnabled(true);
   // ... run your story ...
   NarrativeProfiler.logSummary();
   ```

**New files to review:**
- `docs/INK_GUIDE.md` - Comprehensive scripting guide
- `docs/TAG_REFERENCE.md` - All tags reference
- `example-stories/` - Tutorial and showcase examples
