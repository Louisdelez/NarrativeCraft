# HIGH Risk Override Action Plan

> Analysis and remediation plan for the 10 HIGH risk override files

_Generated: 2026-01-10_

## Summary

These 10 files contain duplicated business logic and/or complex rendering code that represents the highest maintenance burden. They require special attention during any changes.

## HIGH Risk Files

### 1. CutsceneController.java
**Path**: `controllers/cutscene/CutsceneController.java`

**Why HIGH**:
- Uses Camera position/vector methods (`getPosition()` vs `position()`)
- Uses VertexConsumer chain API which differs significantly between versions
- Contains cutscene camera control business logic that must stay synchronized

**Root Cause**: VertexConsumer API change and Camera accessor renames

**Proposed Solution**: **Keep Override + Document**
- The VertexConsumer chain is too different to bridge efficiently
- Camera vectors now bridged via ICameraCompat (Phase 9 addition)
- Add extensive comments documenting why override exists

**Priority**: P1 - Document

---

### 2. OnServerTick.java
**Path**: `events/OnServerTick.java`

**Why HIGH**:
- Contains custom `isPlayerLookingAt()` implementation
- 1.21.x has `LivingEntity.isLookingAtMe()`, 1.20.x doesn't
- Business logic for interaction detection duplicated

**Root Cause**: Missing method in 1.20.x API

**Proposed Solution**: **Extract to IUtilCompat Bridge**
- Add `isEntityLookingAtPlayer(entity, player)` to IUtilCompat
- 1.20.x: Use existing custom implementation
- 1.21.x: Delegate to `LivingEntity.isLookingAtMe()`
- Eliminate override by using bridge

**Priority**: P0 - Fix Now

---

### 3. AreaTrigger.java
**Path**: `narrative/chapter/scene/data/AreaTrigger.java`

**Why HIGH**:
- Uses Camera getters (now bridged)
- Uses VertexConsumer chain for debug rendering
- Contains trigger detection logic

**Root Cause**: VertexConsumer API change and Camera accessor renames

**Proposed Solution**: **Keep Override + Use Camera Bridge**
- Camera methods now bridged via ICameraCompat
- VertexConsumer debug rendering is complex to bridge
- Consider extracting debug rendering to separate utility

**Priority**: P1 - Partial Fix (use camera bridge)

---

### 4. DialogArrowSkip.java
**Path**: `narrative/dialog/animation/DialogArrowSkip.java`

**Why HIGH**:
- Full VertexConsumer chain for arrow animation rendering
- Business logic for skip arrow animation

**Root Cause**: VertexConsumer API change

**Proposed Solution**: **Keep Override + Document**
- VertexConsumer chain too different to bridge
- Animation logic is tightly coupled to rendering
- Add comments explaining the API differences

**Priority**: P2 - Document

---

### 5. DialogRenderer3D.java
**Path**: `narrative/dialog/DialogRenderer3D.java`

**Why HIGH**:
- PoseStack methods differ (`pose.last().pose()` vs `pose.pose()`)
- 3D dialog rendering logic duplicated
- Complex matrix transformations

**Root Cause**: PoseStack API changes

**Proposed Solution**: **Investigate PoseStack Bridge**
- Could add `getLastPose(PoseStack)` to IGuiRenderCompat
- Would allow single implementation
- Medium complexity, medium benefit

**Priority**: P1 - Investigate

---

### 6. DialogTail.java
**Path**: `narrative/dialog/geometric/DialogTail.java`

**Why HIGH**:
- Camera vectors (now bridged)
- VertexConsumer chain for tail geometry
- Dialog tail positioning logic

**Root Cause**: VertexConsumer API change and Camera accessor renames

**Proposed Solution**: **Keep Override + Use Camera Bridge**
- Camera methods now bridged via ICameraCompat
- VertexConsumer chain is specialized for tail geometry
- Update to use camera bridge methods

**Priority**: P1 - Partial Fix (use camera bridge)

---

### 7. InteractionEyeRenderer.java
**Path**: `narrative/interaction/InteractionEyeRenderer.java`

**Why HIGH**:
- Camera position access (now bridged)
- VertexConsumer chain for eye icon rendering
- Interaction indicator logic duplicated

**Root Cause**: VertexConsumer API change and Camera accessor renames

**Proposed Solution**: **Keep Override + Use Camera Bridge**
- Camera methods now bridged via ICameraCompat
- Eye icon rendering uses specialized VertexConsumer calls
- Update to use camera bridge methods

**Priority**: P1 - Partial Fix (use camera bridge)

---

### 8. ActionDifferenceListener.java
**Path**: `narrative/recording/actions/manager/ActionDifferenceListener.java`

**Why HIGH**:
- Logic differences for `EquipmentSlot.SADDLE`/`BODY` (don't exist in 1.20.x)
- `Boat` vs `AbstractBoat` class references
- Complex recording difference detection logic duplicated

**Root Cause**: Entity class renames and EquipmentSlot additions

**Proposed Solution**: **Extract to IEntityCompat Bridge**
- Add `hasSaddleSlot()` and `hasBodySlot()` to check availability
- Add `isBoatType(Entity)` helper
- Would reduce logic duplication significantly

**Priority**: P0 - Fix Now

---

### 9. ChoiceButtonWidget.java
**Path**: `screens/components/ChoiceButtonWidget.java`

**Why HIGH**:
- VertexConsumer chain for button background rendering
- PoseStack methods for text positioning
- Choice button interaction logic

**Root Cause**: VertexConsumer API change and PoseStack changes

**Proposed Solution**: **Keep Override + Document**
- Widget rendering is complex and specialized
- Not worth the bridge complexity
- Document the differences clearly

**Priority**: P2 - Document

---

### 10. CutscenePlayback.java (Additional)
**Path**: `controllers/cutscene/CutscenePlayback.java`

**Why HIGH** (MEDIUM in inventory but HIGH impact):
- `Mth.lerp(double, Vec3, Vec3)` doesn't exist in 1.20.x
- Uses component-wise lerp helper
- Core cutscene interpolation logic duplicated

**Root Cause**: Missing Vec3 lerp overload in 1.20.x

**Proposed Solution**: **Extract to IMathCompat Bridge**
- Add `lerpVec3(double, Vec3, Vec3)` to new IMathCompat interface
- 1.20.x: Component-wise implementation
- 1.21.x: Delegate to `Mth.lerp()`

**Priority**: P1 - Investigate

---

## Action Plan Summary

### P0 - Fix Now (3 files)
1. **OnServerTick.java** - Add `isEntityLookingAtPlayer()` to IUtilCompat
2. **ActionDifferenceListener.java** - Add entity type helpers to IUtilCompat

### P1 - Partial Fix / Investigate (5 files)
3. **CutsceneController.java** - Document + use camera bridge
4. **AreaTrigger.java** - Use camera bridge
5. **DialogRenderer3D.java** - Investigate PoseStack bridge
6. **DialogTail.java** - Use camera bridge
7. **InteractionEyeRenderer.java** - Use camera bridge

### P2 - Document Only (3 files)
8. **DialogArrowSkip.java** - Document VertexConsumer differences
9. **ChoiceButtonWidget.java** - Document rendering differences
10. **CutscenePlayback.java** - Document Vec3 lerp workaround

---

## Immediate Implementation Tasks

### Task 1: Add `isEntityLookingAtPlayer()` to IUtilCompat
```java
/**
 * Check if an entity is looking at a player.
 * In 1.21.x uses LivingEntity.isLookingAtMe().
 * In 1.20.x uses custom ray-cast implementation.
 *
 * @param entity The entity to check
 * @param player The player to check against
 * @return true if the entity is looking at the player
 */
boolean isEntityLookingAtPlayer(Object entity, Object player);
```

### Task 2: Add entity type helpers to IUtilCompat
```java
/**
 * Check if entity is a boat type (Boat in 1.20.x, AbstractBoat in 1.21.x).
 */
boolean isBoatType(Object entity);

/**
 * Check if EquipmentSlot.SADDLE exists in this version.
 */
boolean hasSaddleSlot();

/**
 * Check if EquipmentSlot.BODY exists in this version.
 */
boolean hasBodySlot();
```

### Task 3: Update HIGH risk files to use camera bridge
Files to update:
- `AreaTrigger.java`
- `DialogTail.java`
- `InteractionEyeRenderer.java`
- `CutsceneController.java`

---

## Expected Impact

After implementing P0 and P1 tasks:
- **2 overrides potentially eliminated** (OnServerTick, ActionDifferenceListener)
- **4 overrides simplified** (camera bridge usage)
- **4 overrides documented** (no code change)

This reduces HIGH risk maintenance burden by ~60%.
