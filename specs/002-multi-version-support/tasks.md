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
├── compat/                        # NEW: Version compatibility layer
│   ├── api/                       # Compatibility interfaces
│   ├── mc119x/                    # 1.19.4 implementations
│   ├── mc120x/                    # 1.20.6 implementations
│   └── mc121x/                    # 1.21.11 implementations
├── fabric/                        # Fabric loader (existing)
├── fabric-1.19.4/                 # NEW: Fabric 1.19.4 module
├── fabric-1.20.6/                 # NEW: Fabric 1.20.6 module
├── neoforge/                      # NeoForge loader (existing)
├── neoforge-1.20.6/               # NEW: NeoForge 1.20.6 module
└── buildSrc/                      # Gradle plugins
```

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization, Gradle restructuring, and Architectury Loom setup

- [ ] T001 Backup existing project structure and create `002-multi-version-support` branch in NarrativeCraft repo
- [ ] T002 Add Architectury Loom plugin to `buildSrc/build.gradle` with version `1.7-SNAPSHOT`
- [ ] T003 [P] Update `gradle.properties` with multi-version configuration (see data-model.md Section "gradle.properties Structure")
- [ ] T004 [P] Create `buildSrc/src/main/groovy/version-matrix.gradle` plugin for multi-version build configuration
- [ ] T005 Restructure `settings.gradle` to include version-specific subprojects: `fabric-1.19.4`, `fabric-1.20.6`, `neoforge-1.20.6`
- [ ] T006 [P] Create `compat/api/` directory structure with placeholder files
- [ ] T007 Verify `./gradlew tasks` runs without errors after restructure

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core compatibility interfaces and existing 1.21.11 refactoring - MUST complete before any user story

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### Compatibility API Interfaces

- [ ] T008 Create `compat/api/IVersionAdapter.java` interface per contracts/IVersionAdapter.java spec
- [ ] T009 [P] Create `compat/api/IGuiRenderCompat.java` interface per contracts/IGuiRenderCompat.java spec
- [ ] T010 [P] Create `compat/api/ICameraCompat.java` interface per contracts/ICameraCompat.java spec
- [ ] T011 [P] Create `compat/api/IAudioCompat.java` interface per contracts/IAudioCompat.java spec
- [ ] T012 Create `compat/api/ICapabilityChecker.java` interface per contracts/ICapabilityChecker.java spec
- [ ] T013 [P] Create `compat/api/VersionCapability.java` record class per data-model.md spec

### MC 1.21.11 Compat Implementation (Current Behavior)

- [ ] T014 Create `compat/mc121x/Mc121xVersionAdapter.java` implementing IVersionAdapter for 1.21.11
- [ ] T015 [P] Create `compat/mc121x/Mc121xGuiRenderCompat.java` wrapping current GuiGraphics usage
- [ ] T016 [P] Create `compat/mc121x/Mc121xCameraCompat.java` wrapping current Camera usage
- [ ] T017 [P] Create `compat/mc121x/Mc121xAudioCompat.java` wrapping current SoundEngine usage
- [ ] T018 Create `compat/mc121x/Mc121xCapabilityChecker.java` with all capabilities enabled

### ServiceLoader Registration

- [ ] T019 Create `compat/mc121x/resources/META-INF/services/fr.loudo.narrativecraft.compat.api.IVersionAdapter`
- [ ] T020 Update `common/src/main/java/fr/loudo/narrativecraft/platform/Services.java` to load IVersionAdapter

### Verify Existing Behavior

- [ ] T021 Run `./gradlew :fabric:build` and `./gradlew :neoforge:build` to verify 1.21.11 still works
- [ ] T022 Manual smoke test: Load mod on MC 1.21.11 Fabric, verify existing functionality unchanged

**Checkpoint**: Foundation ready - 1.21.11 works through new compat layer, user story implementation can begin

---

## Phase 3: User Story 1 - Player on Minecraft 1.21.x (Priority: P1) 🎯 MVP

**Goal**: Existing 1.21.11 support continues working through new multi-version architecture

**Independent Test**: Install `narrativecraft-mc1.21.11-fabric.jar` on MC 1.21.11, verify mod loads and all features work

### Build System for 1.21.x

- [ ] T023 [US1] Create `fabric-1.21.11/build.gradle` extending multiloader-loader with MC 1.21.11 deps
- [ ] T024 [P] [US1] Create `fabric-1.21.11/src/main/resources/fabric.mod.json` with 1.21.11 version ranges
- [ ] T025 [P] [US1] Create `fabric-1.21.11/src/main/resources/narrativecraft.fabric.mixins.json` (copy from fabric/)
- [ ] T026 [P] [US1] Create `fabric-1.21.11/src/main/resources/narrativecraft.mc121.mixins.json` with MC 1.21-specific mixins
- [ ] T027 [US1] Migrate Fabric 1.21.11-specific mixins from `fabric/src/main/java/.../mixin/` to `fabric-1.21.11/`

### NeoForge 1.21.x

- [ ] T028 [US1] Create `neoforge-1.21.11/build.gradle` extending multiloader-loader with NeoForge 21.11.12-beta
- [ ] T029 [P] [US1] Create `neoforge-1.21.11/src/main/resources/META-INF/neoforge.mods.toml` with 1.21.11 version ranges
- [ ] T030 [P] [US1] Create `neoforge-1.21.11/src/main/resources/narrativecraft.neoforge.mixins.json` (copy from neoforge/)
- [ ] T031 [US1] Migrate NeoForge 1.21.11-specific code from `neoforge/` to `neoforge-1.21.11/`

### Artifact Naming

- [ ] T032 [US1] Configure Gradle to produce `narrativecraft-{version}-mc1.21.11-fabric.jar`
- [ ] T033 [P] [US1] Configure Gradle to produce `narrativecraft-{version}-mc1.21.11-neoforge.jar`

### Verification

- [ ] T034 [US1] Run `./gradlew :fabric-1.21.11:build` and verify JAR created with correct name
- [ ] T035 [P] [US1] Run `./gradlew :neoforge-1.21.11:build` and verify JAR created with correct name
- [ ] T036 [US1] Manual smoke test: Install fabric-1.21.11 JAR, verify dialog/choices/cutscene work

**Checkpoint**: User Story 1 complete - MC 1.21.11 Fabric + NeoForge builds work through new architecture

---

## Phase 4: User Story 2 - Player on Minecraft 1.20.x (Priority: P1)

**Goal**: Full NarrativeCraft support on Minecraft 1.20.6 with Fabric and NeoForge

**Independent Test**: Install `narrativecraft-mc1.20.6-fabric.jar` on MC 1.20.6, verify mod loads and core features work

### MC 1.20.x Compat Implementation

- [ ] T037 [US2] Create `compat/mc120x/Mc120xVersionAdapter.java` implementing IVersionAdapter for 1.20.6
- [ ] T038 [P] [US2] Create `compat/mc120x/Mc120xGuiRenderCompat.java` adapting GuiGraphics for 1.20.x API
- [ ] T039 [P] [US2] Create `compat/mc120x/Mc120xCameraCompat.java` adapting Camera for 1.20.x
- [ ] T040 [P] [US2] Create `compat/mc120x/Mc120xAudioCompat.java` adapting SoundEngine for 1.20.x
- [ ] T041 [US2] Create `compat/mc120x/Mc120xCapabilityChecker.java` with version-appropriate capabilities
- [ ] T042 [US2] Create `compat/mc120x/resources/META-INF/services/fr.loudo.narrativecraft.compat.api.IVersionAdapter`

### Fabric 1.20.6 Module

- [ ] T043 [US2] Create `fabric-1.20.6/build.gradle` with MC 1.20.6, Fabric API 0.97.8+1.20.5, Java 17 target
- [ ] T044 [P] [US2] Create `fabric-1.20.6/src/main/resources/fabric.mod.json` with 1.20.6 version ranges
- [ ] T045 [P] [US2] Create `fabric-1.20.6/src/main/resources/narrativecraft.fabric.mixins.json` for Fabric 1.20.x
- [ ] T046 [P] [US2] Create `fabric-1.20.6/src/main/resources/narrativecraft.mc120.mixins.json` for MC 1.20 mixins
- [ ] T047 [US2] Port `GuiGraphicsFabricMixin.java` to 1.20.x API in `fabric-1.20.6/src/main/java/.../mixin/`
- [ ] T048 [US2] Port other Fabric-specific mixins to 1.20.x in `fabric-1.20.6/`

### NeoForge 1.20.6 Module

- [ ] T049 [US2] Create `neoforge-1.20.6/build.gradle` with NeoForge 20.6.119, Java 17 target
- [ ] T050 [P] [US2] Create `neoforge-1.20.6/src/main/resources/META-INF/neoforge.mods.toml` with 1.20.6 ranges
- [ ] T051 [P] [US2] Create `neoforge-1.20.6/src/main/resources/narrativecraft.neoforge.mixins.json` for NeoForge 1.20.x
- [ ] T052 [US2] Port `GuiGraphicsNeoForgeMixin.java` to 1.20.x API in `neoforge-1.20.6/src/main/java/.../mixin/`
- [ ] T053 [US2] Verify NeoForge 1.20.6 event handlers compatible in `neoforge-1.20.6/src/main/java/.../events/`

### Common Mixin Compatibility for 1.20.x

- [ ] T054 [US2] Audit `common/src/main/java/.../mixin/` for 1.20.x compatibility
- [ ] T055 [P] [US2] Create version-specific mixin overrides if needed in `compat/mc120x/mixin/`

### Build & Test

- [ ] T056 [US2] Run `./gradlew :fabric-1.20.6:build` and verify JAR `narrativecraft-*-mc1.20.6-fabric.jar`
- [ ] T057 [P] [US2] Run `./gradlew :neoforge-1.20.6:build` and verify JAR `narrativecraft-*-mc1.20.6-neoforge.jar`
- [ ] T058 [US2] Manual smoke test: Install fabric-1.20.6 JAR on MC 1.20.6, verify dialog/choices/cutscene
- [ ] T059 [US2] Manual smoke test: Install neoforge-1.20.6 JAR on MC 1.20.6, verify same functionality

**Checkpoint**: User Story 2 complete - MC 1.20.6 Fabric + NeoForge working with full feature parity

---

## Phase 5: User Story 3 - Player on Minecraft 1.19.x (Priority: P2)

**Goal**: NarrativeCraft support on Minecraft 1.19.4 Fabric with graceful degradation

**Independent Test**: Install `narrativecraft-mc1.19.4-fabric.jar` on MC 1.19.4, verify core features work with warnings for unsupported features

### MC 1.19.x Compat Implementation

- [ ] T060 [US3] Create `compat/mc119x/Mc119xVersionAdapter.java` implementing IVersionAdapter for 1.19.4
- [ ] T061 [P] [US3] Create `compat/mc119x/Mc119xGuiRenderCompat.java` using DrawableHelper API (no GuiGraphics)
- [ ] T062 [P] [US3] Create `compat/mc119x/Mc119xCameraCompat.java` adapting Camera for 1.19.x
- [ ] T063 [P] [US3] Create `compat/mc119x/Mc119xAudioCompat.java` adapting SoundEngine for 1.19.x
- [ ] T064 [US3] Create `compat/mc119x/Mc119xCapabilityChecker.java` with ENHANCED features disabled
- [ ] T065 [US3] Create `compat/mc119x/resources/META-INF/services/fr.loudo.narrativecraft.compat.api.IVersionAdapter`

### Fabric 1.19.4 Module

- [ ] T066 [US3] Create `fabric-1.19.4/build.gradle` with MC 1.19.4, Fabric API 0.87.2+1.19.4, Java 17 target
- [ ] T067 [P] [US3] Create `fabric-1.19.4/src/main/resources/fabric.mod.json` with 1.19.4 version ranges
- [ ] T068 [P] [US3] Create `fabric-1.19.4/src/main/resources/narrativecraft.fabric.mixins.json` for Fabric 1.19.x
- [ ] T069 [P] [US3] Create `fabric-1.19.4/src/main/resources/narrativecraft.mc119.mixins.json` for MC 1.19 mixins
- [ ] T070 [US3] Create `DrawableHelperMixin.java` for 1.19.x rendering in `fabric-1.19.4/src/main/java/.../mixin/`
- [ ] T071 [US3] Port Fabric-specific mixins to 1.19.x API in `fabric-1.19.4/`

### Common Mixin Compatibility for 1.19.x

- [ ] T072 [US3] Audit `common/src/main/java/.../mixin/` for 1.19.x compatibility issues
- [ ] T073 [US3] Disable or stub mixins that target 1.20+ only classes (GuiTextRenderStateMixin, AvatarRendererMixin)
- [ ] T074 [US3] Create `compat/mc119x/mixin/` with 1.19-specific mixin implementations

### Feature Degradation

- [ ] T075 [US3] Implement version-aware warnings in `ICapabilityChecker` shown once per session
- [ ] T076 [US3] Update `NarrativeLogger` to log version degradation warnings with context
- [ ] T077 [US3] Test degradation: Verify `screen_effects` and `advanced_hud` show warnings on 1.19.4

### Build & Test

- [ ] T078 [US3] Run `./gradlew :fabric-1.19.4:build` and verify JAR `narrativecraft-*-mc1.19.4-fabric.jar`
- [ ] T079 [US3] Manual smoke test: Install fabric-1.19.4 JAR on MC 1.19.4, verify dialog/choices work
- [ ] T080 [US3] Manual smoke test: Verify degraded features show warning messages, no crashes

**Checkpoint**: User Story 3 complete - MC 1.19.4 Fabric working with documented limitations

---

## Phase 6: User Story 4 - Story Creator Cross-Version (Priority: P2)

**Goal**: Stories created on any version work on all other versions without modification

**Independent Test**: Create story on 1.21.11, copy to 1.20.6 and 1.19.4, verify it plays correctly

### Cross-Version Validation

- [ ] T081 [US4] Update `InkValidationService` to be version-aware per research.md findings
- [ ] T082 [US4] Add version info to validation error messages in `TagValidator` classes
- [ ] T083 [US4] Implement `ICapabilityChecker.warnIfUnavailable()` for tag validation context

### Story Format Verification

- [ ] T084 [US4] Create test story using all common tags in `example-stories/cross-version-test.ink`
- [ ] T085 [P] [US4] Test story on MC 1.21.11 Fabric - document behavior
- [ ] T086 [P] [US4] Test story on MC 1.20.6 Fabric - verify identical behavior
- [ ] T087 [P] [US4] Test story on MC 1.19.4 Fabric - verify works with warnings for unavailable features

**Checkpoint**: User Story 4 complete - Stories are cross-version compatible

---

## Phase 7: User Story 5 - Mod Maintainer (Priority: P2)

**Goal**: Maintainable codebase where common changes propagate to all versions automatically

**Independent Test**: Change code in `common/`, verify change appears in all 5 built JARs

### Build Verification

- [ ] T088 [US5] Run full build `./gradlew build` and verify 5 JARs produced
- [ ] T089 [US5] Verify each JAR contains common module classes
- [ ] T090 [US5] Add modification to `common/` class, rebuild, verify change in all JARs

### Documentation

- [ ] T091 [P] [US5] Create `docs/MULTI-VERSION-ARCHITECTURE.md` explaining module structure
- [ ] T092 [P] [US5] Update `CONTRIBUTING.md` with multi-version development guidelines
- [ ] T093 [US5] Document mixin organization strategy in `docs/MIXIN-STRATEGY.md`

**Checkpoint**: User Story 5 complete - Maintainer workflow documented and verified

---

## Phase 8: User Story 6 - Release Manager (Priority: P3)

**Goal**: Automated release workflow producing all 5 JARs with correct metadata

**Independent Test**: Run release workflow and verify all artifacts generated correctly

### CI/CD Updates

- [ ] T094 [US6] Update `.github/workflows/ci.yml` with build matrix: MC [1.19.4, 1.20.6, 1.21.11] × Loader [fabric, neoforge] (exclude 1.19.4+neoforge)
- [ ] T095 [US6] Configure Gradle caching in CI for < 15 minute builds
- [ ] T096 [US6] Update `.github/workflows/release.yml` to build and attach all 5 JARs on version tag

### Artifact Naming & Metadata

- [ ] T097 [US6] Verify CI artifact names match convention: `narrativecraft-{version}-mc{mc}-{loader}.jar`
- [ ] T098 [US6] Add version compatibility table to GitHub Release description template

### Distribution Preparation

- [ ] T099 [P] [US6] Create CurseForge metadata template for multi-version release
- [ ] T100 [P] [US6] Create Modrinth metadata template for multi-version release

**Checkpoint**: User Story 6 complete - Release workflow produces all artifacts

---

## Phase 9: User Story 7 - QA/Reviewer (Priority: P3)

**Goal**: QA can verify all versions pass tests before release

**Independent Test**: Run CI, verify matrix shows pass/fail per version clearly

### CI Matrix Verification

- [ ] T101 [US7] Verify CI matrix runs all 5 target builds in parallel
- [ ] T102 [US7] Verify CI job naming clearly shows MC version and loader
- [ ] T103 [US7] Test PR blocking: Fail one build, verify PR blocked

### Smoke Test Documentation

- [ ] T104 [US7] Create `docs/SMOKE_TESTS.md` with manual test checklist per version
- [ ] T105 [US7] Document core test scenarios: mod load, dialog, choices, cutscene, camera, audio
- [ ] T106 [P] [US7] Create test result template for tracking smoke test results

**Checkpoint**: User Story 7 complete - QA workflow defined and CI matrix functional

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: Final documentation, cleanup, and release preparation

### Documentation Updates

- [ ] T107 [P] Update README.md with version compatibility table and JAR selection guide
- [ ] T108 [P] Update `docs/TAG_REFERENCE.md` with version support notes per tag
- [ ] T109 [P] Update `docs/TROUBLESHOOTING.md` with version-specific issues and solutions
- [ ] T110 Update CHANGELOG.md with multi-version support release notes

### Final Verification

- [ ] T111 Run full smoke test suite on all 5 targets per `docs/SMOKE_TESTS.md`
- [ ] T112 Verify no warnings or errors in Gradle build output
- [ ] T113 Create release candidate tag `v1.2.0-rc1` and verify CI produces artifacts

### Cleanup

- [ ] T114 Remove deprecated code from original `fabric/` and `neoforge/` directories
- [ ] T115 Update `.gitignore` with new build output directories
- [ ] T116 Final code review: verify no hardcoded version strings, all use gradle.properties

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1 (Setup) ─────────────────────────────────────────┐
                                                         │
Phase 2 (Foundational) ←─────────────────────────────────┤ BLOCKS ALL
                                                         │
    ┌────────────────────────────────────────────────────┘
    │
    ├── Phase 3 (US1: 1.21.x) ←── MVP - Can ship here!
    │
    ├── Phase 4 (US2: 1.20.x) ←── Can run parallel with US1
    │
    └── Phase 5 (US3: 1.19.x) ←── Can run parallel with US1/US2

    After US1, US2, US3 complete:

    Phase 6 (US4: Cross-Version) ←── Depends on US1, US2, US3

    Phase 7 (US5: Maintainer) ←── Can run parallel with US4

    Phase 8 (US6: Release) ←── Depends on US4, US5

    Phase 9 (US7: QA) ←── Depends on US6

    Phase 10 (Polish) ←── Depends on all user stories
```

### User Story Dependencies

| Story | Depends On | Can Parallel With |
|-------|------------|-------------------|
| US1 (1.21.x) | Phase 2 only | US2, US3 |
| US2 (1.20.x) | Phase 2 only | US1, US3 |
| US3 (1.19.x) | Phase 2 only | US1, US2 |
| US4 (Cross-Version) | US1, US2, US3 | US5 |
| US5 (Maintainer) | Phase 2 only | US4 |
| US6 (Release) | US4, US5 | US7 |
| US7 (QA) | US6 | - |

### Parallel Opportunities per Phase

**Phase 1**: T003, T004, T006 can run parallel
**Phase 2**: T009-T011, T013, T015-T017 can run parallel
**Phase 3**: T024-T026, T029-T030, T033, T035 can run parallel
**Phase 4**: T038-T040, T044-T046, T050-T051, T055, T057 can run parallel
**Phase 5**: T061-T063, T067-T069 can run parallel
**Phase 6**: T085-T087 can run parallel
**Phase 7**: T091-T092 can run parallel
**Phase 8**: T099-T100 can run parallel
**Phase 9**: T106 can run parallel
**Phase 10**: T107-T109 can run parallel

---

## Parallel Example: Phase 2 Foundation

```bash
# Launch foundational interface creation in parallel:
Task: "Create compat/api/IGuiRenderCompat.java interface"
Task: "Create compat/api/ICameraCompat.java interface"
Task: "Create compat/api/IAudioCompat.java interface"
Task: "Create compat/api/VersionCapability.java record"

# Then launch 1.21.x implementations in parallel:
Task: "Create compat/mc121x/Mc121xGuiRenderCompat.java"
Task: "Create compat/mc121x/Mc121xCameraCompat.java"
Task: "Create compat/mc121x/Mc121xAudioCompat.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1 (1.21.x)
4. **STOP and VALIDATE**: Test 1.21.11 Fabric/NeoForge independently
5. Deploy if 1.21.x-only release acceptable

### Incremental Delivery

1. Setup + Foundational → Foundation ready
2. Add US1 (1.21.x) → Test → Release as "1.21.x only" if needed
3. Add US2 (1.20.x) → Test → Release as "1.20.x + 1.21.x"
4. Add US3 (1.19.x) → Test → Release as full multi-version
5. Each version adds users without breaking existing support

### Parallel Team Strategy

With 2-3 developers after Phase 2:

```
Developer A: US1 (1.21.x) + US4 (Cross-Version)
Developer B: US2 (1.20.x) + US5 (Maintainer)
Developer C: US3 (1.19.x) + US6/US7 (Release/QA)
```

---

## Notes

- **5 Target JARs**: fabric-1.19.4, fabric-1.20.6, neoforge-1.20.6, fabric-1.21.11, neoforge-1.21.11
- **[P] tasks**: Different files, no dependencies - safe to parallelize
- **[Story] label**: Maps task to user story for traceability
- **Commit after each task** or logical group for easy rollback
- **Stop at any checkpoint** to validate independently
- **No NeoForge for 1.19.x**: NeoForge didn't exist until 1.20.4
- **Java versions**: 1.19.x/1.20.x target Java 17 runtime, 1.21.x targets Java 21
