# Data Model: NarrativeCraft Production Hardening

**Date**: 2026-01-09
**Branch**: `001-production-hardening`

## Overview

This document defines the key entities and state models for the production-hardened
NarrativeCraft system. Focus is on state management, cleanup guarantees, and
validation structures.

---

## Core Entities

### 1. NarrativeState (Enum)

The central state that governs player experience mode.

```
┌─────────────────────────────────────────────────────────────┐
│                      NarrativeState                         │
├─────────────────────────────────────────────────────────────┤
│ GAMEPLAY    │ Default state, normal player control          │
│ DIALOGUE    │ Dialog UI visible, input captured             │
│ CUTSCENE    │ Camera locked, playback running               │
│ RECORDING   │ Recording player actions                      │
│ PLAYBACK    │ Playing back recorded sequence                │
└─────────────────────────────────────────────────────────────┘
```

**State Transitions**:
```
                    ┌──────────────┐
          ┌────────►│   DIALOGUE   │◄────────┐
          │         └──────────────┘         │
          │                                  │
┌─────────┴─────────┐              ┌─────────┴─────────┐
│     GAMEPLAY      │◄────────────►│    CUTSCENE       │
└─────────┬─────────┘              └─────────┬─────────┘
          │                                  │
          │         ┌──────────────┐         │
          ├────────►│  RECORDING   │◄────────┤
          │         └──────────────┘         │
          │         ┌──────────────┐         │
          └────────►│   PLAYBACK   │◄────────┘
                    └──────────────┘
```

**Invariants**:
- Only one state active at a time per player
- All non-GAMEPLAY states must return to GAMEPLAY on exit
- State transitions trigger cleanup handlers

---

### 2. PlayerSession

Tracks a player's narrative engagement within a game session.

```
┌─────────────────────────────────────────────────────────────┐
│                      PlayerSession                          │
├─────────────────────────────────────────────────────────────┤
│ playerId        │ UUID (hashed for logs)                    │
│ currentState    │ NarrativeState                            │
│ activeStory     │ Story? (nullable)                         │
│ activeChapter   │ Chapter? (nullable)                       │
│ activeScene     │ Scene? (nullable)                         │
│ hudModifications│ List<HudModification>                     │
│ cameraState     │ CameraState                               │
│ inputCapture    │ InputCaptureState                         │
│ cleanupHandlers │ PriorityQueue<CleanupHandler>             │
│ createdAt       │ Instant                                   │
│ lastActivity    │ Instant                                   │
└─────────────────────────────────────────────────────────────┘
```

**Lifecycle**:
1. Created when player joins world
2. Updated on state transitions
3. Cleaned up on disconnect or state exit
4. Destroyed when player leaves world

**Validation Rules**:
- `playerId` must be non-null UUID
- `currentState` defaults to GAMEPLAY
- `cleanupHandlers` must never be null (empty list OK)

---

### 3. CleanupHandler

Registered callback for state restoration.

```
┌─────────────────────────────────────────────────────────────┐
│                     CleanupHandler                          │
├─────────────────────────────────────────────────────────────┤
│ id              │ String (unique identifier)                │
│ priority        │ int (lower = runs first)                  │
│ cleanupAction   │ Runnable                                  │
│ registeredAt    │ Instant                                   │
│ source          │ String (component that registered)        │
└─────────────────────────────────────────────────────────────┘
```

**Priority Conventions**:
| Priority | Component | Description |
|----------|-----------|-------------|
| 10 | HUD | Reset HUD overlays |
| 20 | Camera | Release camera lock |
| 30 | Input | Release input capture |
| 50 | Audio | Stop narrative audio |
| 100 | Session | Clear session data |

**Invariants**:
- Must be registered BEFORE the modification it cleans up
- Cleanup action must be idempotent (safe to call multiple times)
- Must handle exceptions internally (log, don't propagate)

---

### 4. Story

A narrative content unit containing chapters.

```
┌─────────────────────────────────────────────────────────────┐
│                         Story                               │
├─────────────────────────────────────────────────────────────┤
│ id              │ String (unique identifier)                │
│ name            │ String (display name)                     │
│ filePath        │ Path (to .ink file)                       │
│ chapters        │ List<Chapter>                             │
│ characters      │ List<Character>                           │
│ metadata        │ StoryMetadata                             │
│ loadedAt        │ Instant?                                  │
│ validationState │ ValidationState                           │
└─────────────────────────────────────────────────────────────┘
```

**Validation States**:
- `UNVALIDATED` - Not yet checked
- `VALID` - Passed all validation
- `INVALID` - Failed validation (errors stored)
- `VALIDATING` - Currently being validated

---

### 5. InkTag

A narrative script command with validation metadata.

```
┌─────────────────────────────────────────────────────────────┐
│                        InkTag                               │
├─────────────────────────────────────────────────────────────┤
│ name            │ String (tag identifier)                   │
│ parameters      │ List<TagParameter>                        │
│ handler         │ InkAction                                 │
│ documentation   │ TagDocumentation                          │
│ isWhitelisted   │ boolean                                   │
│ requiredPermission │ Permission?                            │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                     TagParameter                            │
├─────────────────────────────────────────────────────────────┤
│ name            │ String                                    │
│ type            │ ParameterType (STRING, INT, FLOAT, BOOL)  │
│ required        │ boolean                                   │
│ defaultValue    │ Object?                                   │
│ validationRegex │ Pattern?                                  │
│ description     │ String                                    │
└─────────────────────────────────────────────────────────────┘
```

**Built-in Tags** (whitelist):
- `dialog` - Display dialog text
- `choice` - Present player choice
- `camera` - Control camera
- `sound` - Play audio
- `wait` - Pause execution
- `command` - Execute Minecraft command (restricted)
- `emotion` - Trigger emote animation
- `fade` - Screen fade effect

---

### 6. ValidationResult

Result of script/tag validation.

```
┌─────────────────────────────────────────────────────────────┐
│                    ValidationResult                         │
├─────────────────────────────────────────────────────────────┤
│ isValid         │ boolean                                   │
│ errors          │ List<ValidationError>                     │
│ warnings        │ List<ValidationWarning>                   │
│ validatedAt     │ Instant                                   │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    ValidationError                          │
├─────────────────────────────────────────────────────────────┤
│ code            │ String (e.g., "INVALID_TAG")              │
│ message         │ String (human-readable)                   │
│ file            │ String (source file)                      │
│ line            │ int                                       │
│ column          │ int?                                      │
│ context         │ String (surrounding code)                 │
│ suggestion      │ String? (how to fix)                      │
└─────────────────────────────────────────────────────────────┘
```

**Error Codes**:
| Code | Description |
|------|-------------|
| `INVALID_TAG` | Tag not in whitelist |
| `MISSING_PARAM` | Required parameter missing |
| `INVALID_PARAM_TYPE` | Parameter type mismatch |
| `PERMISSION_DENIED` | Insufficient permissions |
| `PARSE_ERROR` | Ink syntax error |
| `ASSET_NOT_FOUND` | Referenced asset missing |

---

### 7. AuditFinding

Discovered issue during code audit.

```
┌─────────────────────────────────────────────────────────────┐
│                     AuditFinding                            │
├─────────────────────────────────────────────────────────────┤
│ id              │ String (e.g., "BUG-001")                  │
│ title           │ String                                    │
│ severity        │ Severity (CRITICAL, MAJOR, MINOR)         │
│ category        │ Category (BUG, DEBT, PERF, SECURITY)      │
│ location        │ CodeLocation                              │
│ description     │ String                                    │
│ impact          │ String                                    │
│ reproduction    │ String?                                   │
│ remediation     │ String                                    │
│ status          │ Status (OPEN, IN_PROGRESS, FIXED)         │
│ fixCommit       │ String? (commit hash when fixed)          │
│ testId          │ String? (regression test ID)              │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                     CodeLocation                            │
├─────────────────────────────────────────────────────────────┤
│ file            │ String (relative path)                    │
│ startLine       │ int                                       │
│ endLine         │ int?                                      │
│ className       │ String?                                   │
│ methodName      │ String?                                   │
└─────────────────────────────────────────────────────────────┘
```

---

### 8. QualityGate

CI check configuration.

```
┌─────────────────────────────────────────────────────────────┐
│                      QualityGate                            │
├─────────────────────────────────────────────────────────────┤
│ name            │ String (e.g., "build-fabric")             │
│ type            │ GateType (BUILD, TEST, LINT, CUSTOM)      │
│ command         │ String (Gradle task)                      │
│ required        │ boolean (blocks merge if true)            │
│ timeout         │ Duration                                  │
│ loader          │ Loader? (FABRIC, NEOFORGE, null=all)      │
└─────────────────────────────────────────────────────────────┘
```

**Configured Gates**:
| Name | Type | Command | Required |
|------|------|---------|----------|
| build-fabric | BUILD | `:fabric:build` | Yes |
| build-neoforge | BUILD | `:neoforge:build` | Yes |
| test | TEST | `:common:test` | Yes |
| spotless | LINT | `spotlessCheck` | Yes |

---

## State Diagrams

### Player Session Lifecycle

```
┌─────────┐     join world     ┌──────────────┐
│  NULL   │ ─────────────────► │   ACTIVE     │
└─────────┘                    │  (GAMEPLAY)  │
                               └──────┬───────┘
                                      │
                        ┌─────────────┼─────────────┐
                        │             │             │
                        ▼             ▼             ▼
                 ┌──────────┐  ┌──────────┐  ┌──────────┐
                 │ DIALOGUE │  │ CUTSCENE │  │RECORDING │
                 └────┬─────┘  └────┬─────┘  └────┬─────┘
                      │             │             │
                      └─────────────┼─────────────┘
                                    │
                        exit/error/disconnect
                                    │
                                    ▼
                               ┌──────────────┐
                               │   CLEANUP    │
                               │ (handlers)   │
                               └──────┬───────┘
                                      │
                      ┌───────────────┴───────────────┐
                      │                               │
                      ▼                               ▼
               ┌──────────────┐               ┌─────────────┐
               │   GAMEPLAY   │               │  DESTROYED  │
               │  (restored)  │               │  (leave)    │
               └──────────────┘               └─────────────┘
```

### Cleanup Handler Execution

```
┌─────────────────────────────────────────────────────────────┐
│                  State Exit Triggered                       │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│              Sort handlers by priority (ASC)                │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│              For each handler in order:                     │
│              ┌────────────────────────────────────────┐     │
│              │  try { handler.cleanup() }             │     │
│              │  catch (Exception e) { log.error(e) }  │     │
│              │  // Continue to next handler           │     │
│              └────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│              Clear handler queue                            │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│              Set state = GAMEPLAY                           │
└─────────────────────────────────────────────────────────────┘
```

---

## Relationships

```
┌─────────────┐       1:N       ┌─────────────┐
│   Story     │────────────────►│   Chapter   │
└─────────────┘                 └──────┬──────┘
       │                               │
       │ 1:N                           │ 1:N
       ▼                               ▼
┌─────────────┐                 ┌─────────────┐
│  Character  │                 │    Scene    │
└─────────────┘                 └─────────────┘

┌─────────────┐       1:1       ┌───────────────────┐
│   Player    │────────────────►│  PlayerSession    │
└─────────────┘                 └─────────┬─────────┘
                                          │
                                          │ 1:N
                                          ▼
                                ┌───────────────────┐
                                │  CleanupHandler   │
                                └───────────────────┘

┌─────────────┐       N:1       ┌───────────────────┐
│   InkTag    │────────────────►│   InkAction       │
└─────────────┘                 │   (handler)       │
       │                        └───────────────────┘
       │ validation
       ▼
┌─────────────────────┐
│  ValidationResult   │
└─────────────────────┘
```

---

## Data Persistence

### Runtime Only (No Persistence)
- `PlayerSession` - Recreated each session
- `CleanupHandler` - Transient registration
- `ValidationResult` - Cached briefly

### File-Based Persistence
- `Story` - .ink files in data packs
- `Chapter` - Embedded in story files
- `Character` - JSON in world save
- `AuditFinding` - Markdown in specs/

### Configuration
- Tag whitelist - TOML config file
- Command whitelist - TOML config file
- Performance flags - TOML config file
