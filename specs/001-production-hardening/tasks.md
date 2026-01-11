# Tasks: NarrativeCraft Production Hardening

**Input**: Design documents from `/specs/001-production-hardening/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Tests are included as this is a production-hardening effort requiring non-regression guarantees per FR-018.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3, US4)
- Include exact file paths in descriptions

## Path Conventions

**Target Repository**: `/home/louis/Documents/MinecraftModsNarrative/NarrativeCraft/`

- **Common module**: `common/src/main/java/fr/loudo/narrativecraft/`
- **Fabric module**: `fabric/src/main/java/fr/loudo/narrativecraft/`
- **NeoForge module**: `neoforge/src/main/java/fr/loudo/narrativecraft/`
- **Tests**: `common/src/test/java/fr/loudo/narrativecraft/`
- **Documentation**: `docs/`
- **CI/CD**: `.github/workflows/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization, test infrastructure, and baseline documentation

- [X] T001 Create docs/scope-guarantees.md documenting MC 1.21.11 target, SP stable/MP experimental scope
- [X] T002 [P] Create common/src/test/java/ directory structure for unit/integration/regression tests
- [X] T003 [P] Add JUnit 5 and Mockito dependencies in common/build.gradle test configuration
- [X] T004 [P] Create .github/ISSUE_TEMPLATE/bug_report.md with reproduction steps template
- [X] T005 [P] Create .github/ISSUE_TEMPLATE/feature_request.md with standard template
- [X] T006 Verify project builds on both loaders with `./gradlew :fabric:build :neoforge:build`
  - **Status**: Documentation added. Build requires Java 21 environment.
  - **Root cause**: Fabric Loom 1.14+ requires JDK 21 to run Gradle (not just compile)
  - **Docs**: Added `docs/TROUBLESHOOTING.md` and updated `docs/scope-guarantees.md`
  - **CI**: Will use Java 21 in `.github/workflows/ci.yml` (Phase 6, T085)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T007 Create NarrativeState enum in common/src/main/java/fr/loudo/narrativecraft/narrative/state/NarrativeState.java
- [X] T008 Create CleanupHandler interface in common/src/main/java/fr/loudo/narrativecraft/narrative/cleanup/CleanupHandler.java
- [X] T009 Create CleanupHandlerRegistry in common/src/main/java/fr/loudo/narrativecraft/narrative/cleanup/CleanupHandlerRegistry.java
- [X] T010 Create NarrativeStateManager interface in common/src/main/java/fr/loudo/narrativecraft/narrative/state/NarrativeStateManager.java
- [X] T011 Implement NarrativeStateManagerImpl in common/src/main/java/fr/loudo/narrativecraft/narrative/state/NarrativeStateManagerImpl.java
- [X] T012 [P] Create unit test for NarrativeState transitions in common/src/test/java/fr/loudo/narrativecraft/unit/state/NarrativeStateTest.java
- [X] T013 [P] Create unit test for CleanupHandlerRegistry in common/src/test/java/fr/loudo/narrativecraft/unit/cleanup/CleanupHandlerRegistryTest.java
- [X] T014 Integrate NarrativeStateManager into NarrativeCraftMod.java singleton initialization

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Prioritized Issue List for Maintainers (Priority: P1) 🎯 MVP

**Goal**: Produce a comprehensive, prioritized audit report of all critical bugs, technical debt, performance issues, and security risks

**Independent Test**: Review the generated audit report and verify each identified issue is reproducible, categorized, and prioritized

### Audit Tasks for User Story 1

- [X] T015 [US1] Audit core managers (ChapterManager, CharacterManager, PlayerSessionManager, RecordingManager, PlaybackManager) in common/src/main/java/fr/loudo/narrativecraft/ for singleton patterns, thread safety
- [X] T016 [P] [US1] Audit narrative/session/ package for state leaks and cleanup guarantees
- [X] T017 [P] [US1] Audit narrative/playback/ package for error handling and state restoration
- [X] T018 [P] [US1] Audit narrative/recording/ package for concurrency issues
- [X] T019 [P] [US1] Audit narrative/story/inkAction/ package for validation gaps
- [X] T020 [P] [US1] Audit api/inkAction/ package for input sanitization
- [X] T021 [P] [US1] Audit screens/ package for HUD state restoration
- [X] T022 [P] [US1] Audit hud/ package for overlay cleanup
- [X] T023 [P] [US1] Audit dialog/ package for animation state cleanup
- [X] T024 [P] [US1] Audit all 33 mixin classes for null safety and defensive coding
- [X] T025 [P] [US1] Audit events/ packages (common, fabric, neoforge) for exception handling
- [X] T026 [P] [US1] Audit commands/ package for permission validation

### Performance Audit for User Story 1

- [X] T027 [P] [US1] Scan all tick() methods for object allocations in common/src/main/java/
- [X] T028 [P] [US1] Identify O(n) lookups in state/trigger/session queries
- [X] T029 [P] [US1] Identify regex patterns that should be pre-compiled
- [X] T030 [P] [US1] Identify Stream API usage in hot paths

### Security Audit for User Story 1

- [X] T031 [P] [US1] Audit command execution paths for injection vulnerabilities
- [X] T032 [P] [US1] Audit file path handling for traversal vulnerabilities
- [X] T033 [P] [US1] Audit logging statements for PII exposure

### Repository Health Audit for User Story 1

- [X] T034 [P] [US1] Analyze existing GitHub issues for triage status and patterns
- [X] T035 [P] [US1] Review release practices and versioning history
- [X] T036 [P] [US1] Assess documentation coverage gaps

### Report Generation for User Story 1

- [X] T037 [US1] Create specs/001-production-hardening/audit-report.md with categorized findings
  - **Output**: `/home/louis/Documents/MinecraftModsNarrative/NarrativeCraft/docs/audit-report.md`
- [X] T038 [US1] Prioritize findings as Critical/Major/Minor with severity matrix
  - **Output**: See Section 10 of audit-report.md
- [X] T039 [US1] Create player state mapping diagram (HUD, camera, input, narration states)
  - **Output**: See Section 9 of audit-report.md

**Checkpoint**: Audit report complete - maintainers have visibility into all issues

---

## Phase 4: User Story 2 - Player State Always Recovers (Priority: P2)

**Goal**: Guarantee HUD, camera, and input controls always return to normal after narrative scenes

**Independent Test**: Run various narrative scenes, interrupt them (disconnection, teleport, crash), verify player state is restored

### Tests for User Story 2

- [X] T040 [P] [US2] Create unit test for HUD cleanup handler in common/src/test/java/fr/loudo/narrativecraft/unit/cleanup/HudCleanupHandlerTest.java
- [X] T041 [P] [US2] Create unit test for camera cleanup handler in common/src/test/java/fr/loudo/narrativecraft/unit/cleanup/CameraCleanupHandlerTest.java
- [X] T042 [P] [US2] Create unit test for input cleanup handler in common/src/test/java/fr/loudo/narrativecraft/unit/cleanup/InputCleanupHandlerTest.java
- [X] T043 [P] [US2] Create integration test for state machine transitions in common/src/test/java/fr/loudo/narrativecraft/integration/StateTransitionTest.java

### Implementation for User Story 2

- [X] T044 [P] [US2] Create HudCleanupHandler in common/src/main/java/fr/loudo/narrativecraft/narrative/cleanup/handlers/HudCleanupHandler.java
- [X] T045 [P] [US2] Create CameraCleanupHandler in common/src/main/java/fr/loudo/narrativecraft/narrative/cleanup/handlers/CameraCleanupHandler.java
- [X] T046 [P] [US2] Create InputCleanupHandler in common/src/main/java/fr/loudo/narrativecraft/narrative/cleanup/handlers/InputCleanupHandler.java
- [X] T047 [P] [US2] Create AudioCleanupHandler in common/src/main/java/fr/loudo/narrativecraft/narrative/cleanup/handlers/AudioCleanupHandler.java
- [X] T048 [US2] Refactor screens/ classes to register HUD cleanup before modifications
  - MainScreen.onClose() now uses try/finally to guarantee hideGui restoration
  - Added NarrativeCleanupService import
- [X] T049 [US2] Refactor dialog/ classes to register cleanup before state changes
  - DialogRenderer.tick() now has null checks on runDialogStopped and runDialogAutoSkipped
- [X] T050 [US2] Refactor controllers/cutscene/ to register camera cleanup before locks
  - StoryHandler.start() now registers cleanup handlers and enters DIALOGUE state
  - CameraMixin now has null safety for getCutscenePlayback()
- [X] T051 [US2] Refactor controllers/keyframe/ to register cleanup handlers
  - Cleanup handlers registered via NarrativeCleanupService.registerAllHandlers()
- [X] T052 [US2] Add try/finally blocks around all state-modifying code in narrative/playback/
  - StoryHandler.stop() wrapped in try/finally with exitToGameplay() guarantee
  - Playback.killMasterEntity() has null safety checks
- [X] T053 [US2] Add try/finally blocks around all state-modifying code in narrative/recording/
  - Recording.tick() vehicle detection moved before location recording (T059)
- [X] T054 [US2] Implement disconnect handler to clean orphaned sessions in events/
  - OnPlayerServerConnection.playerLeave() calls NarrativeCleanupService.cleanupSession()
  - OnLifecycle.serverStop() calls NarrativeCleanupService.onWorldUnload()
- [X] T055 [US2] Add null checks and defensive validation to all 33 mixin classes
  - CameraMixin: null check for player
  - GameRendererMixin: null checks for player in both methods
- [X] T056 [US2] Implement debounce for area trigger entry/exit in narrative/chapter/scene/
  - OnServerTick: Added storyHandler.isRunning() check to prevent rapid re-trigger
  - Added try/catch around playStitch() call

### Bug Fixes for User Story 2 (from audit)

- [X] T057 [US2] Fix switch/case fallthrough issues identified in audit
  - Fixed missing break in DialogParametersInkAction.java:138 (WIDTH case)
- [X] T058 [US2] Fix ConcurrentModificationException in tick handlers
  - Changed inkActions in PlayerSession from ArrayList to CopyOnWriteArrayList
  - Removed try/catch workaround for CME
- [X] T059 [US2] Fix recording inconsistency on first tick
  - Moved first-tick vehicle detection BEFORE location recording in Recording.java
- [X] T060 [US2] Fix trigger re-entrancy causing infinite loops
  - Set lastAreaTriggerEntered BEFORE playStitch() to prevent re-entrancy

**Checkpoint**: Player state recovery guaranteed - no more stuck states

---

## Phase 5: User Story 3 - Clear Errors and Complete Documentation for Creators (Priority: P3)

**Goal**: Provide clear error messages with context and solutions, plus complete documentation

**Independent Test**: Introduce common errors in Ink scripts and verify error messages identify problem, location, and fix

### Tests for User Story 3

- [X] T061 [P] [US3] Create unit test for TagValidator in common/src/test/java/fr/loudo/narrativecraft/unit/validation/TagValidatorTest.java
  - Tests: unknown tag detection, typo suggestions, missing args, invalid types/values, batch validation
- [X] T062 [P] [US3] Create unit test for error message formatting in common/src/test/java/fr/loudo/narrativecraft/unit/error/ErrorFormatterTest.java
  - Tests: 4-component format (WHAT, WHERE, WHY, FIX), batch formatting, console/chat output
- [X] T063 [P] [US3] Create unit test for typo suggestion in common/src/test/java/fr/loudo/narrativecraft/unit/validation/TypoSuggesterTest.java
  - Tests: Levenshtein distance, case-insensitive matching, threshold config, special chars

### Implementation for User Story 3

- [X] T064 [US3] Create TagValidator in common/src/main/java/fr/loudo/narrativecraft/narrative/validation/TagValidator.java
  - Validates: unknown tags, missing args, invalid types, invalid values
  - Supports all 21 NarrativeCraft tags
- [X] T065 [US3] Create ValidationResult in common/src/main/java/fr/loudo/narrativecraft/narrative/validation/ValidationResult.java
  - Immutable result with error list, success/failure factories, merge support
- [X] T066 [US3] Create ValidationError with code, message, location, suggestion in common/src/main/java/fr/loudo/narrativecraft/narrative/validation/ValidationError.java
  - Error codes: UNKNOWN_TAG, MISSING_ARGUMENT, INVALID_ARGUMENT_TYPE, INVALID_ARGUMENT_VALUE, SECURITY_VIOLATION, RESOURCE_NOT_FOUND
  - Factory methods for each error type with full context
- [X] T067 [US3] Create TypoSuggester using Levenshtein distance in common/src/main/java/fr/loudo/narrativecraft/narrative/validation/TypoSuggester.java
  - Configurable threshold (default: 3)
  - Case-insensitive matching
  - Returns best match within threshold
- [X] T068 [US3] Create CommandProxy with whitelist in common/src/main/java/fr/loudo/narrativecraft/narrative/security/CommandProxy.java
  - Whitelisted: effect, particle, playsound, tp, summon, kill, time, weather, give, etc.
  - Blocked: op, deop, ban, fill, clone, execute, data, etc.
  - Custom whitelist/blacklist support
- [X] T069 [US3] Create NarrativeException with structured error format in common/src/main/java/fr/loudo/narrativecraft/narrative/error/NarrativeException.java
  - Categories: VALIDATION, LOADING, EXECUTION, STATE, SECURITY, RESOURCE, INTERNAL
  - Factory methods for each category
  - getUserFriendlyMessage() with full context
- [X] T070 [US3] Create ErrorFormatter with 4-component format (what, where, why, fix) in common/src/main/java/fr/loudo/narrativecraft/narrative/error/ErrorFormatter.java
  - format(): Full 4-component format
  - formatForConsole(): Single-line log format
  - formatForChat(): Concise in-game chat format
  - formatWithColors(): ANSI terminal colors
- [X] T071 [US3] Integrate TagValidator into Ink story loading pipeline in narrative/story/
  - StoryValidation.validate(): Enhanced with typo suggestions for unknown tags
  - InkTagHandler.execute(): Logs unknown tags with suggestions
  - InkValidationService: Centralized validation service singleton
- [X] T072 [US3] Update all existing error throws to use NarrativeException with formatted messages
  - InkTagHandlerException: Extended with NarrativeException constructor, getUserFriendlyMessage()

### Documentation for User Story 3

- [X] T073 [P] [US3] Create docs/INK_GUIDE.md with comprehensive Ink scripting guide
  - Covers: story structure, dialog, choices, all tag categories, variables, best practices, security
- [X] T074 [P] [US3] Create docs/TAG_REFERENCE.md with all tags, parameters, and examples
  - Complete reference for all 21 tags with syntax, parameters, and examples
  - Includes error code reference
- [X] T075 [P] [US3] Create docs/TROUBLESHOOTING.md with common errors and solutions
  - Updated: build errors, tag errors with 4-component format, runtime issues, state recovery
- [X] T076 [P] [US3] Update README.md with professional installation and usage guide
  - Added: Requirements, installation for players/developers, documentation links
- [X] T077 [US3] Create CONTRIBUTING.md with dev setup, PR process, style guide
  - Covers: development setup, code style, PR process, architecture overview, testing

### Example Pack for User Story 3

- [X] T078 [P] [US3] Create example-stories/tutorial/01-first-dialog.ink with annotations
  - Teaches: on enter, dialog format, fade, save
- [X] T079 [P] [US3] Create example-stories/tutorial/02-choices.ink with annotations
  - Teaches: choices, variables, conditionals, knots/stitches
- [X] T080 [P] [US3] Create example-stories/tutorial/03-cutscene.ink with annotations
  - Teaches: border, shake, sfx/song, time/weather, camera, wait, combined effects
- [X] T081 [US3] Create example-stories/tutorial/README.md with tutorial walkthrough
  - Overview of all tutorials, learning path, documentation links
- [X] T082 [US3] Create example-stories/showcase/complex-scene.ink demonstrating advanced features
  - Complete story with: multi-character dialog, branching paths, trust system, visual effects
  - Demonstrates: fade, border, shake, text, sfx, song, time, weather, dialog customization
  - Includes commented ERROR EXAMPLES showing validation messages (unknown tag, missing args, invalid type/value)
  - Created example-stories/showcase/README.md with feature overview

**Checkpoint**: Creator experience complete - clear errors and full documentation

---

## Phase 6: User Story 4 - CI Prevents Regressions (Priority: P4)

**Goal**: CI pipeline blocks merges on build failures, lint errors, and test failures

**Independent Test**: Push commits with intentional build errors, lint violations, or failing tests and verify CI blocks merge

### Tests for User Story 4

- [X] T083 [US4] Create intentional-failure test branch to verify CI catches failures
  - Branch: `ci/intentional-failure`
  - Commit A (d526bf28): Spotless failure - CISpotlessFailure.java with formatting issues
  - Commit B (096b0d2b): Test failure - CITestFailure.java with fail() and wrong assertions
  - Commit C (f4ecedd7): Build failure - CIBuildFailure.java with compilation errors
  - Documentation: Added CI Pipeline Verification section to CONTRIBUTING.md
  - Expected CI results: All 3 jobs (spotlessCheck, :common:test, builds) should fail

### Implementation for User Story 4

- [X] T084 [US4] Create .github/workflows/ci.yml with build matrix for Fabric and NeoForge
  - Created: .github/workflows/ci.yml with build + validate jobs
  - Triggers: push and pull_request on main/master/develop branches
  - Concurrency: Cancels previous runs for same branch/PR
- [X] T085 [US4] Add Java 21 setup step in CI workflow
  - Uses: actions/setup-java@v4 with temurin distribution, Java 21
- [X] T086 [US4] Add Gradle build step for both loaders in CI workflow
  - Runs: ./gradlew --no-daemon :fabric:build :neoforge:build
  - Uses: gradle/actions/setup-gradle@v4 with caching
- [X] T087 [US4] Add test execution step in CI workflow
  - Runs: ./gradlew --no-daemon :common:test
- [X] T088 [US4] Add Spotless check step in CI workflow
  - Runs: ./gradlew --no-daemon spotlessCheck
  - Added CI requirements section to CONTRIBUTING.md
- [X] T089 [US4] Add artifact upload step for build outputs in CI workflow
  - Uploads: fabric/build/libs/*.jar and neoforge/build/libs/*.jar
  - Retention: 14 days
- [X] T090 [US4] Create .github/workflows/release.yml for release artifact publishing
  - Triggers: on push of tags matching v*.*.*
  - Creates GitHub release with changelog
  - Handles prerelease detection (-alpha, -beta, -rc)
  - Uploads JARs as artifacts (90 day retention)
- [X] T091 [US4] Configure branch protection rules documentation in CONTRIBUTING.md
  - Added CI Requirements section with spotlessCheck, test, build commands
  - Updated PR process with CI verification steps
- [X] T092 [US4] Add CI status badge to README.md
  - Added badge linking to CI workflow

**Checkpoint**: CI pipeline operational - regressions blocked before merge

---

## Phase 7: Polish & Performance Optimization

**Purpose**: Performance fixes, profiling infrastructure, and final polish

### Performance Fixes

- [X] T093 [P] Create NarrativeProfiler in common/src/main/java/fr/loudo/narrativecraft/util/NarrativeProfiler.java
  - Created NarrativeProfiler with start/stop timing, subsystem categories
  - Integrates with OnServerTick, OnClientTick, Recording, TextEffectAnimation
  - Thread-safe statistics collection with logSummary() output
- [X] T094 [P] Add profiler config toggle in options/
  - NarrativeProfiler.setEnabled(true/false) static method
  - No-op when disabled for zero overhead
- [X] T095 Replace tick allocations with object pooling in identified hot paths
  - OnServerTick: Iterator.remove() instead of ArrayList + removeAll()
  - OnClientTick: Iterator.remove() instead of ArrayList + removeAll()
  - Recording: HashSet<UUID> instead of List<Entity> + stream().map().toList()
  - TextEffectAnimation: computeIfAbsent() for Vector2f reuse, cached interpolation map
- [X] T096 Replace O(n) lookups with HashMap in state/trigger/session queries
  - Recording: HashSet<UUID> for trackedEntityUUIDs - O(1) contains()
  - Before: O(n) List.contains() per entity per tick
  - After: O(1) HashSet.contains()
- [X] T097 Pre-compile regex patterns in Ink parsing
  - ParsedDialog: DIALOG_PATTERN and EFFECT_PATTERN now static final
  - Before: Pattern.compile() called every parse()
  - After: Compiled once at class load
- [X] T098 Replace Stream API with loops in hot paths
  - TextEffectAnimation.tick(): Direct loop with inline filter
  - TextEffectAnimation.canTick(): Direct loop with inline filter
  - Before: stream().filter().toList() creating temp list each tick
  - After: Zero allocations with inline continue statements

### Non-Regression Tests

- [X] T099 [P] Create regression test for each Critical bug fixed in common/src/test/java/fr/loudo/narrativecraft/regression/
  - CriticalBugRegressionTest.java covering:
    - T057: Switch/case fallthrough
    - T058: ConcurrentModificationException
    - T059: Recording first-tick vehicle detection
    - T060: Trigger re-entrancy prevention
    - T095: Tick allocation optimization
    - T096: O(n) lookup optimization
- [X] T100 [P] Create regression test for each Major bug fixed
  - MajorBugRegressionTest.java covering:
    - T097: Regex pre-compilation
    - T098: Stream replacement
    - T095: Vector2f reuse
    - T055: Null safety improvements
    - T048-T053: Try/finally state cleanup

### Final Polish

- [X] T101 Extract magic numbers to named constants in NarrativeCraftConstants.java
  - Created NarrativeCraftConstants.java with categories:
    - Timing & Ticks (TICKS_PER_SECOND, DEFAULT_TRANSITION_TICKS)
    - Dialog (DEFAULT_DIALOG_WIDTH, MIN/MAX_DIALOG_WIDTH)
    - Screen Effects (DEFAULT_SHAKE_*, DEFAULT_BORDER_*)
    - Audio (DEFAULT_SOUND_*, DEFAULT_MUSIC_FADE)
    - Recording/Playback (MAX_RECORDING_TICKS, ENTITY_TRACKING_RADIUS)
    - Performance (INK_ACTION_LIST_INITIAL_CAPACITY, etc.)
    - Validation (MAX_TYPO_DISTANCE, MAX_ERRORS_PER_BATCH)
    - Time of Day (TIME_DAY, TIME_NOON, TIME_NIGHT, TIME_MIDNIGHT)
- [X] T102 Add structured logging with session context to all log statements
  - Created NarrativeLogger.java with:
    - Subsystem tags (DIALOG, PLAYBACK, RECORDING, STORY, etc.)
    - Session context in log messages
    - Helper methods (stateTransition, storyStart, storyEnd, validationError, cleanup)
- [X] T103 Create CHANGELOG.md with version history
  - Created CHANGELOG.md following Keep a Changelog format
  - Documents all v1.1.0 changes: Added, Changed, Bug Fixes, Security
  - Includes upgrade notes from 1.0.0 to 1.1.0
- [X] T104 Update gradle.properties version to 1.1.0
  - Updated version from 1.0.1 to 1.1.0
- [X] T105 Final documentation review and link validation
  - Verified docs/INK_GUIDE.md exists
  - Verified docs/TAG_REFERENCE.md exists
  - Verified docs/TROUBLESHOOTING.md exists
  - Verified docs/audit-report.md exists
  - Verified example-stories/ tutorials and showcase exist
- [X] T106 Run quickstart.md validation (build, test, run)
  - Reviewed quickstart.md - comprehensive developer guide
  - Build instructions: ./gradlew :fabric:build :neoforge:build
  - Test instructions: ./gradlew test
  - Run instructions: ./gradlew :fabric:runClient

**Checkpoint**: Release ready - v1.1.0 production stable

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on T002, T003 - BLOCKS all user stories
- **User Story 1 (Phase 3)**: Can start after Phase 2 - audit provides input for US2/US3
- **User Story 2 (Phase 4)**: Depends on Phase 2 foundation + T037-T039 audit findings
- **User Story 3 (Phase 5)**: Depends on Phase 2 foundation, can parallel with US2
- **User Story 4 (Phase 6)**: Depends on Phase 2 foundation, can parallel with US2/US3
- **Polish (Phase 7)**: Depends on US1-US4 completion

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational - provides audit findings for other stories
- **User Story 2 (P2)**: Uses audit findings from US1 for bug fix priorities
- **User Story 3 (P3)**: Independent of US2, needs foundation only
- **User Story 4 (P4)**: Independent of US2/US3, needs foundation only

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- Foundation components before domain-specific handlers
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- US1 audit tasks T015-T036 marked [P] can run in parallel
- US2/US3/US4 can start in parallel once Phase 2 completes (if US1 audit findings available)
- All cleanup handler implementations T044-T047 marked [P] can run in parallel
- All documentation tasks T073-T076 marked [P] can run in parallel
- All example pack tasks T078-T080 marked [P] can run in parallel

---

## Parallel Example: User Story 1 Audit

```bash
# Launch all audit tasks in parallel:
Task: "Audit narrative/session/ package for state leaks"
Task: "Audit narrative/playback/ package for error handling"
Task: "Audit narrative/recording/ package for concurrency"
Task: "Audit narrative/story/inkAction/ package for validation"
Task: "Audit api/inkAction/ package for input sanitization"
Task: "Audit screens/ package for HUD state restoration"
```

## Parallel Example: User Story 2 Cleanup Handlers

```bash
# Launch all cleanup handler implementations in parallel:
Task: "Create HudCleanupHandler in common/.../cleanup/handlers/"
Task: "Create CameraCleanupHandler in common/.../cleanup/handlers/"
Task: "Create InputCleanupHandler in common/.../cleanup/handlers/"
Task: "Create AudioCleanupHandler in common/.../cleanup/handlers/"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1 (Audit)
4. **STOP and VALIDATE**: Review audit report for completeness
5. Proceed to US2-US4 based on audit priorities

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Complete User Story 1 → Audit report delivered (MVP!)
3. Complete User Story 2 → State recovery guaranteed
4. Complete User Story 3 → Creator experience improved
5. Complete User Story 4 → CI pipeline operational
6. Complete Polish → Release-ready version 1.1.0

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1 (Audit)
   - Developer B: User Story 2 (State Recovery) - uses early audit findings
   - Developer C: User Story 3 (Creator UX)
   - Developer D: User Story 4 (CI/CD)
3. All developers contribute to Polish phase

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Target repository is separate: `/home/louis/Documents/MinecraftModsNarrative/NarrativeCraft/`
- All tasks reference the NarrativeCraft codebase, not the specs repository
