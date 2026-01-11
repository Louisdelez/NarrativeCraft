# Tasks: Multi-Version Minecraft Support

**Input**: Design documents from `/specs/002-multi-version-support/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: No explicit test tasks - spec focuses on manual smoke tests per version.

**Organization**: Tasks grouped by user story priority (P1 → P3) for independent implementation.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1-US7)
- Include exact file paths in descriptions

## Path Conventions

```
NarrativeCraft/                    # Source codebase
├── common/                        # Version-independent code
├── compat-api/                    # Compatibility interfaces
├── compat-mc119x/                 # 1.19.4 implementations
├── compat-mc120x/                 # 1.20.6 implementations
├── compat-mc121x/                 # 1.21.11 implementations
├── common-mc119/                  # 1.19.x common overrides
├── common-mc120/                  # 1.20.x common overrides
├── common-mc121/                  # 1.21.x common overrides
├── fabric-1.19.4/                 # Fabric 1.19.4 module
├── fabric-1.20.6/                 # Fabric 1.20.6 module
├── fabric-1.21.11/                # Fabric 1.21.11 module
├── neoforge-1.20.6/               # NeoForge 1.20.6 module
├── neoforge-1.21.11/              # NeoForge 1.21.11 module
└── buildSrc/                      # Gradle plugins
```

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization, Gradle restructuring, and Architectury Loom setup

- [x] T001 Backup existing project structure and create `002-multi-version-support` branch in NarrativeCraft repo
- [x] T002 Add Architectury Loom plugin to `buildSrc/build.gradle` with version `1.7-SNAPSHOT`
- [x] T003 [P] Update `gradle.properties` with multi-version configuration (see data-model.md Section "gradle.properties Structure")
- [x] T004 [P] Create `buildSrc/src/main/groovy/version-matrix.gradle` plugin for multi-version build configuration
- [x] T005 Restructure `settings.gradle` to include version-specific subprojects: `fabric-1.19.4`, `fabric-1.20.6`, `neoforge-1.20.6`
- [x] T006 [P] Create `compat-api/` directory structure with placeholder files
- [x] T007 Verify `./gradlew tasks` runs without errors after restructure

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core compatibility interfaces and existing 1.21.11 refactoring - MUST complete before any user story

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### Compatibility API Interfaces

- [x] T008 Create `compat-api/IVersionAdapter.java` interface per contracts/IVersionAdapter.java spec
- [x] T009 [P] Create `compat-api/IGuiRenderCompat.java` interface per contracts/IGuiRenderCompat.java spec
- [x] T010 [P] Create `compat-api/ICameraCompat.java` interface per contracts/ICameraCompat.java spec
- [x] T011 [P] Create `compat-api/IAudioCompat.java` interface per contracts/IAudioCompat.java spec
- [x] T012 Create `compat-api/ICapabilityChecker.java` interface per contracts/ICapabilityChecker.java spec
- [x] T013 [P] Create `compat-api/VersionCapability.java` record class per data-model.md spec

### MC 1.21.11 Compat Implementation (Current Behavior)

- [x] T014 Create `compat-mc121x/Mc121xVersionAdapter.java` implementing IVersionAdapter for 1.21.11
- [x] T015 [P] Create `compat-mc121x/Mc121xGuiRenderCompat.java` wrapping current GuiGraphics usage
- [x] T016 [P] Create `compat-mc121x/Mc121xCameraCompat.java` wrapping current Camera usage
- [x] T017 [P] Create `compat-mc121x/Mc121xAudioCompat.java` wrapping current SoundEngine usage
- [x] T018 Create `compat-mc121x/Mc121xCapabilityChecker.java` with all capabilities enabled

### ServiceLoader Registration

- [x] T019 Create `compat-mc121x/resources/META-INF/services/fr.loudo.narrativecraft.compat.api.IVersionAdapter`
- [x] T020 Update `common/src/main/java/fr/loudo/narrativecraft/platform/Services.java` to load IVersionAdapter

### Verify Existing Behavior

- [x] T021 Run `./gradlew :fabric:build` and `./gradlew :neoforge:build` to verify 1.21.11 still works
- [x] T022 Manual smoke test: Load mod on MC 1.21.11 Fabric, verify existing functionality unchanged

**Checkpoint**: Foundation ready - 1.21.11 works through new compat layer, user story implementation can begin

---

## Phase 3: User Story 1 - Player on Minecraft 1.21.x (Priority: P1) 🎯 MVP

**Goal**: Existing 1.21.11 support continues working through new multi-version architecture

**Independent Test**: Install `narrativecraft-mc1.21.11-fabric.jar` on MC 1.21.11, verify mod loads and all features work

### Build System for 1.21.x

- [x] T023 [US1] Create `fabric-1.21.11/build.gradle` extending multiloader-loader with MC 1.21.11 deps
- [x] T024 [P] [US1] Create `fabric-1.21.11/src/main/resources/fabric.mod.json` with 1.21.11 version ranges
- [x] T025 [P] [US1] Create `fabric-1.21.11/src/main/resources/narrativecraft.fabric.mixins.json` (copy from fabric/)
- [x] T026 [P] [US1] Create `fabric-1.21.11/src/main/resources/narrativecraft.mc121.mixins.json` with MC 1.21-specific mixins
- [x] T027 [US1] Migrate Fabric 1.21.11-specific mixins from `fabric/src/main/java/.../mixin/` to `fabric-1.21.11/`

### NeoForge 1.21.x

- [x] T028 [US1] Create `neoforge-1.21.11/build.gradle` extending multiloader-loader with NeoForge 21.11.12-beta
- [x] T029 [P] [US1] Create `neoforge-1.21.11/src/main/resources/META-INF/neoforge.mods.toml` with 1.21.11 version ranges
- [x] T030 [P] [US1] Create `neoforge-1.21.11/src/main/resources/narrativecraft.neoforge.mixins.json` (copy from neoforge/)
- [x] T031 [US1] Migrate NeoForge 1.21.11-specific code from `neoforge/` to `neoforge-1.21.11/`

### Artifact Naming

- [x] T032 [US1] Configure Gradle to produce `narrativecraft-{version}-mc1.21.11-fabric.jar`
- [x] T033 [P] [US1] Configure Gradle to produce `narrativecraft-{version}-mc1.21.11-neoforge.jar`

### Verification

- [x] T034 [US1] Run `./gradlew :fabric-1.21.11:build` and verify JAR created with correct name
- [x] T035 [P] [US1] Run `./gradlew :neoforge-1.21.11:build` and verify JAR created with correct name
- [x] T036 [US1] Manual smoke test: Install fabric-1.21.11 JAR, verify dialog/choices/cutscene work

**Checkpoint**: User Story 1 complete - MC 1.21.11 Fabric + NeoForge builds work through new architecture

---

## Phase 4: User Story 2 - Player on Minecraft 1.20.x (Priority: P1)

**Goal**: Full NarrativeCraft support on Minecraft 1.20.6 with Fabric and NeoForge

**Independent Test**: Install `narrativecraft-mc1.20.6-fabric.jar` on MC 1.20.6, verify mod loads and core features work

### MC 1.20.x Compat Implementation

- [x] T037 [US2] Create `compat-mc120x/Mc120xVersionAdapter.java` implementing IVersionAdapter for 1.20.6
- [x] T038 [P] [US2] Create `compat-mc120x/Mc120xGuiRenderCompat.java` adapting GuiGraphics for 1.20.x API
- [x] T039 [P] [US2] Create `compat-mc120x/Mc120xCameraCompat.java` adapting Camera for 1.20.x
- [x] T040 [P] [US2] Create `compat-mc120x/Mc120xAudioCompat.java` adapting SoundEngine for 1.20.x
- [x] T041 [US2] Create `compat-mc120x/Mc120xCapabilityChecker.java` with version-appropriate capabilities
- [x] T042 [US2] Create `compat-mc120x/resources/META-INF/services/fr.loudo.narrativecraft.compat.api.IVersionAdapter`

### Fabric 1.20.6 Module

- [x] T043 [US2] Create `fabric-1.20.6/build.gradle` with MC 1.20.6, Fabric API 0.97.8+1.20.5, Java 17 target
- [x] T044 [P] [US2] Create `fabric-1.20.6/src/main/resources/fabric.mod.json` with 1.20.6 version ranges
- [x] T045 [P] [US2] Create `fabric-1.20.6/src/main/resources/narrativecraft.fabric.mixins.json` for Fabric 1.20.x
- [x] T046 [P] [US2] Create `fabric-1.20.6/src/main/resources/narrativecraft.mc120.mixins.json` for MC 1.20 mixins
- [x] T047 [US2] Port `GuiGraphicsFabricMixin.java` to 1.20.x API in `fabric-1.20.6/src/main/java/.../mixin/`
- [x] T048 [US2] Port other Fabric-specific mixins to 1.20.x in `fabric-1.20.6/`

### NeoForge 1.20.6 Module

- [x] T049 [US2] Create `neoforge-1.20.6/build.gradle` with NeoForge 20.6.119, Java 17 target
- [x] T050 [P] [US2] Create `neoforge-1.20.6/src/main/resources/META-INF/neoforge.mods.toml` with 1.20.6 ranges
- [x] T051 [P] [US2] Create `neoforge-1.20.6/src/main/resources/narrativecraft.neoforge.mixins.json` for NeoForge 1.20.x
- [x] T052 [US2] Port `GuiGraphicsNeoForgeMixin.java` to 1.20.x API in `neoforge-1.20.6/src/main/java/.../mixin/`
- [x] T053 [US2] Verify NeoForge 1.20.6 event handlers compatible in `neoforge-1.20.6/src/main/java/.../events/`

### Common Mixin Compatibility for 1.20.x

- [x] T054 [US2] Audit `common/src/main/java/.../mixin/` for 1.20.x compatibility
- [x] T055 [P] [US2] Create version-specific mixin overrides if needed in `compat-mc120x/mixin/`

### Build & Test

- [x] T056 [US2] Run `./gradlew :fabric-1.20.6:build` and verify JAR `narrativecraft-*-mc1.20.6-fabric.jar`
- [x] T057 [P] [US2] Run `./gradlew :neoforge-1.20.6:build` and verify JAR `narrativecraft-*-mc1.20.6-neoforge.jar`
- [x] T058 [US2] Manual smoke test: Install fabric-1.20.6 JAR on MC 1.20.6, verify dialog/choices/cutscene
- [x] T059 [US2] Manual smoke test: Install neoforge-1.20.6 JAR on MC 1.20.6, verify same functionality

**Checkpoint**: User Story 2 complete - MC 1.20.6 Fabric + NeoForge working with full feature parity

---

## Phase 5: User Story 3 - Player on Minecraft 1.19.x (Priority: P2)

**Goal**: NarrativeCraft support on Minecraft 1.19.4 Fabric with graceful degradation

**Independent Test**: Install `narrativecraft-mc1.19.4-fabric.jar` on MC 1.19.4, verify core features work with warnings for unsupported features

### MC 1.19.x Compat Implementation

- [x] T060 [US3] Create `compat-mc119x/Mc119xVersionAdapter.java` implementing IVersionAdapter for 1.19.4
- [x] T061 [P] [US3] Create `compat-mc119x/Mc119xGuiRenderCompat.java` using DrawableHelper API (no GuiGraphics)
- [x] T062 [P] [US3] Create `compat-mc119x/Mc119xCameraCompat.java` adapting Camera for 1.19.x
- [x] T063 [P] [US3] Create `compat-mc119x/Mc119xAudioCompat.java` adapting SoundEngine for 1.19.x
- [x] T064 [US3] Create `compat-mc119x/Mc119xCapabilityChecker.java` with ENHANCED features disabled
- [x] T065 [US3] Create `compat-mc119x/resources/META-INF/services/fr.loudo.narrativecraft.compat.api.IVersionAdapter`

### Fabric 1.19.4 Module

- [x] T066 [US3] Create `fabric-1.19.4/build.gradle` with MC 1.19.4, Fabric API 0.87.2+1.19.4, Java 17 target
- [x] T067 [P] [US3] Create `fabric-1.19.4/src/main/resources/fabric.mod.json` with 1.19.4 version ranges
- [x] T068 [P] [US3] Create `fabric-1.19.4/src/main/resources/narrativecraft.fabric.mixins.json` for Fabric 1.19.x
- [x] T069 [P] [US3] Create `fabric-1.19.4/src/main/resources/narrativecraft.mc119.mixins.json` for MC 1.19 mixins
- [x] T070 [US3] Create `DrawableHelperMixin.java` for 1.19.x rendering in `fabric-1.19.4/src/main/java/.../mixin/`
- [x] T071 [US3] Port Fabric-specific mixins to 1.19.x API in `fabric-1.19.4/`

### Common Mixin Compatibility for 1.19.x

- [x] T072 [US3] Audit `common/src/main/java/.../mixin/` for 1.19.x compatibility issues
- [x] T073 [US3] Disable or stub mixins that target 1.20+ only classes (GuiTextRenderStateMixin, AvatarRendererMixin)
- [x] T074 [US3] Create `compat-mc119x/mixin/` with 1.19-specific mixin implementations

### Feature Degradation

- [x] T075 [US3] Implement version-aware warnings in `ICapabilityChecker` shown once per session
- [x] T076 [US3] Update `NarrativeLogger` to log version degradation warnings with context
- [x] T077 [US3] Test degradation: Verify `screen_effects` and `advanced_hud` show warnings on 1.19.4

### Build & Test

- [x] T078 [US3] Run `./gradlew :fabric-1.19.4:build` and verify JAR `narrativecraft-*-mc1.19.4-fabric.jar`
- [x] T079 [US3] Manual smoke test: Install fabric-1.19.4 JAR on MC 1.19.4, verify dialog/choices work
- [x] T080 [US3] Manual smoke test: Verify degraded features show warning messages, no crashes

**Checkpoint**: User Story 3 complete - MC 1.19.4 Fabric working with documented limitations

---

## Phase 6: User Story 4 - Story Creator Cross-Version (Priority: P2)

**Goal**: Stories created on any version work on all other versions without modification

**Independent Test**: Create story on 1.21.11, copy to 1.20.6 and 1.19.4, verify it plays correctly

### Cross-Version Validation

- [x] T081 [US4] Update `InkValidationService` to be version-aware per research.md findings
- [x] T082 [US4] Add version info to validation error messages in `TagValidator` classes
- [x] T083 [US4] Implement `ICapabilityChecker.warnIfUnavailable()` for tag validation context

### Story Format Verification

- [x] T084 [US4] Create test story using all common tags in `example-stories/cross-version-test.ink`
- [x] T085 [P] [US4] Test story on MC 1.21.11 Fabric - document behavior
- [x] T086 [P] [US4] Test story on MC 1.20.6 Fabric - verify identical behavior
- [x] T087 [P] [US4] Test story on MC 1.19.4 Fabric - verify works with warnings for unavailable features

**Checkpoint**: User Story 4 complete - Stories are cross-version compatible

---

## Phase 7: User Story 5 - Mod Maintainer (Priority: P2)

**Goal**: Maintainable codebase where common changes propagate to all versions automatically

**Independent Test**: Change code in `common/`, verify change appears in all 5 built JARs

### Build Verification

- [x] T088 [US5] Run full build `./gradlew build` and verify 5 JARs produced
- [x] T089 [US5] Verify each JAR contains common module classes
- [x] T090 [US5] Add modification to `common/` class, rebuild, verify change in all JARs

### Documentation

- [x] T091 [P] [US5] Create `docs/MULTI_VERSION_ARCHITECTURE.md` explaining module structure
- [x] T092 [P] [US5] Update `CONTRIBUTING.md` with multi-version development guidelines
- [x] T093 [US5] Document mixin organization strategy in `docs/MIXIN-STRATEGY.md`
  - **Note**: Mixin strategy documented in MULTI_VERSION_ARCHITECTURE.md and OVERRIDE_INVENTORY.md

**Checkpoint**: User Story 5 complete - Maintainer workflow documented and verified

---

## Phase 8: User Story 6 - Release Manager (Priority: P3)

**Goal**: Automated release workflow producing all 5 JARs with correct metadata

**Independent Test**: Run release workflow and verify all artifacts generated correctly

### CI/CD Updates

- [x] T094 [US6] Update `.github/workflows/ci.yml` with build matrix: MC [1.19.4, 1.20.6, 1.21.11] × Loader [fabric, neoforge] (exclude 1.19.4+neoforge)
- [x] T095 [US6] Configure Gradle caching in CI for < 15 minute builds
- [x] T096 [US6] Update `.github/workflows/release.yml` to build and attach all 5 JARs on version tag

### Artifact Naming & Metadata

- [x] T097 [US6] Verify CI artifact names match convention: `narrativecraft-{version}-mc{mc}-{loader}.jar`
- [x] T098 [US6] Add version compatibility table to GitHub Release description template

### Distribution Preparation

- [ ] T099 [P] [US6] Create CurseForge metadata template for multi-version release
- [ ] T100 [P] [US6] Create Modrinth metadata template for multi-version release

**Checkpoint**: User Story 6 mostly complete - Release workflow produces all artifacts (distribution templates pending)

---

## Phase 9: User Story 7 - QA/Reviewer (Priority: P3)

**Goal**: QA can verify all versions pass tests before release

**Independent Test**: Run CI, verify matrix shows pass/fail per version clearly

### CI Matrix Verification

- [x] T101 [US7] Verify CI matrix runs all 5 target builds in parallel
- [x] T102 [US7] Verify CI job naming clearly shows MC version and loader
- [x] T103 [US7] Test PR blocking: Fail one build, verify PR blocked

### Smoke Test Documentation

- [x] T104 [US7] Create `docs/SMOKE_TESTS.md` with manual test checklist per version
- [x] T105 [US7] Document core test scenarios: mod load, dialog, choices, cutscene, camera, audio
- [x] T106 [P] [US7] Create test result template for tracking smoke test results

**Checkpoint**: User Story 7 complete - QA workflow defined and CI matrix functional

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: Final documentation, cleanup, and release preparation

### Documentation Updates

- [x] T107 [P] Update README.md with version compatibility table and JAR selection guide
- [x] T108 [P] Update `docs/TAG_REFERENCE.md` with version support notes per tag
- [x] T109 [P] Update `docs/TROUBLESHOOTING.md` with version-specific issues and solutions
- [x] T110 Update CHANGELOG.md with multi-version support release notes

### Final Verification

- [x] T111 Run full smoke test suite on all 5 targets per `docs/SMOKE_TESTS.md`
- [x] T112 Verify no warnings or errors in Gradle build output
- [ ] T113 Create release candidate tag `v1.2.0-rc1` and verify CI produces artifacts

### Cleanup

- [x] T114 Remove deprecated code from original `fabric/` and `neoforge/` directories
  - **Note**: Legacy aliases kept for backwards compatibility
- [x] T115 Update `.gitignore` with new build output directories
- [x] T116 Final code review: verify no hardcoded version strings, all use gradle.properties

---

## Summary

| Metric | Value |
|--------|-------|
| Total Tasks | 116 |
| Completed | 113 |
| Remaining | 3 |
| Phases | 10 |
| Targets | 5 (fabric-1.19.4, fabric-1.20.6, neoforge-1.20.6, fabric-1.21.11, neoforge-1.21.11) |

### Remaining Tasks

- [ ] T099 - CurseForge metadata template
- [ ] T100 - Modrinth metadata template
- [ ] T113 - Create v1.2.0-rc1 release candidate tag

---

## Feature Status: DONE

**Completed:** 2026-01-11
**Build Marker:** `MULTI_VERSION_BUILD = "5-target-v1.2.0"`

All core multi-version functionality is implemented and working. Only distribution platform templates and release tag remain.
