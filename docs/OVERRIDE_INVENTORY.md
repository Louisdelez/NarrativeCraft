# MC 1.20.x Override Files Inventory

> Complete inventory of files in `common-mc120/` that override `common/` for 1.20.x compatibility.

_Generated: 2026-01-10_

## Summary Statistics

| Risk Level | Count | Percentage | Description |
|------------|-------|------------|-------------|
| **LOW** | 44 | 55% | Pure API adaptation (import, method rename) |
| **MEDIUM** | 26 | 32.5% | Signature adaptation (params added/removed) |
| **HIGH** | 10 | 12.5% | Logic duplication (business logic in override) |
| **Total** | 80 | 100% | |

**Bridge Candidates**: 42 files (52.5%) - can potentially be eliminated via compat-api abstractions

---

## Full Inventory

### Commands (6 files)

| File | Why Override | Risk | Bridge? |
|------|--------------|------|---------|
| `commands/LinkCommand.java` | Package diff: `net.minecraft.Util` vs `net.minecraft.util.Util` | LOW | YES |
| `commands/OpenScreenCommand.java` | Package diff: `net.minecraft.Util` | LOW | YES |
| `commands/PlaybackCommand.java` | Package diff: `net.minecraft.Util`, API: `openFile()` vs `openPath()` | LOW | YES |
| `commands/PlayerSessionCommand.java` | API diff: `player.gameMode.getGameModeForPlayer()` vs `gameMode()` | MEDIUM | YES |
| `commands/RecordCommand.java` | API diff: `player.gameMode.getGameModeForPlayer()` vs `gameMode()` | MEDIUM | YES |
| `commands/StoryCommand.java` | Signature diff: permission level int vs PermissionSet | MEDIUM | NO |

### Controllers (4 files)

| File | Why Override | Risk | Bridge? |
|------|--------------|------|---------|
| `controllers/cutscene/CutsceneController.java` | API diff: `Camera.getPosition()` vs `position()`, VertexConsumer chain | HIGH | NO |
| `controllers/cutscene/CutscenePlayback.java` | API diff: `Mth.lerp(double, Vec3, Vec3)` missing - component-wise lerp | MEDIUM | NO |

### Events (2 files)

| File | Why Override | Risk | Bridge? |
|------|--------------|------|---------|
| `events/OnPlayerServerConnection.java` | Minimal differences, session initialization | LOW | YES |
| `events/OnServerTick.java` | API diff: `LivingEntity.isLookingAtMe()` missing - custom helper | HIGH | NO |

### Files (1 file)

| File | Why Override | Risk | Bridge? |
|------|--------------|------|---------|
| `files/NarrativeCraftFile.java` | API diff: `DefaultPlayerSkin.get()` returns different types | MEDIUM | NO |

### Keys (2 files)

| File | Why Override | Risk | Bridge? |
|------|--------------|------|---------|
| `keys/ModKeys.java` | API diff: String category instead of `KeyMapping.Category` enum | MEDIUM | NO |
| `keys/PressKeyListener.java` | Minimal API differences | LOW | YES |

### Mixins (8 files)

| File | Why Override | Risk | Bridge? |
|------|--------------|------|---------|
| `mixin/AbstractBoatMixin.java` | Package diff: `Boat` class (no `AbstractBoat` in 1.20.x) | MEDIUM | NO |
| `mixin/AbstractHorseMixin.java` | Package diff: `animal.horse.AbstractHorse` vs `animal.equine.AbstractHorse` | LOW | NO |
| `mixin/AvatarRendererMixin.java` | Signature diff: Different render method parameters | MEDIUM | NO |
| `mixin/PlayerInfoMixin.java` | API diff: PlayerSkin record with `texture()` method | MEDIUM | NO |
| `mixin/WorldSelectionListMixin.java` | API diff: `SharedConstants.getCurrentVersion().getName()` vs `.name()` | LOW | YES |
| `mixin/accessor/AbstractBoatAccessor.java` | Package diff: Targets `Boat` class | LOW | NO |
| `mixin/accessor/AbstractHorseAccessor.java` | Package diff: `animal.horse.AbstractHorse` | LOW | NO |
| `mixin/accessor/AvatarAccessor.java` | Minimal differences | LOW | YES |

### Narrative - Core (5 files)

| File | Why Override | Risk | Bridge? |
|------|--------------|------|---------|
| `narrative/chapter/scene/data/AreaTrigger.java` | API diff: Camera getters, VertexConsumer API | HIGH | NO |
| `narrative/chapter/scene/data/interaction/EntityInteraction.java` | API diff: `setPos(x, y, z)` instead of `snapTo(Vec3)` | LOW | YES |
| `narrative/character/CharacterSkinController.java` | API diff: DynamicTexture constructor params | MEDIUM | NO |
| `narrative/cleanup/handlers/AudioCleanupHandler.java` | API diff: `musicInstance` field vs getter | LOW | YES |
| `narrative/session/PlayerSession.java` | API diff: `player.gameMode.getGameModeForPlayer()` vs `gameMode()` | MEDIUM | YES |

### Narrative - Dialog (4 files)

| File | Why Override | Risk | Bridge? |
|------|--------------|------|---------|
| `narrative/dialog/animation/DialogArrowSkip.java` | API diff: VertexConsumer chain API | HIGH | NO |
| `narrative/dialog/DialogRenderer2D.java` | API diff: PoseStack methods, component-wise Vec3 lerp | MEDIUM | NO |
| `narrative/dialog/DialogRenderer3D.java` | API diff: PoseStack methods, `pose.last().pose()` vs `pose.pose()` | HIGH | NO |
| `narrative/dialog/geometric/DialogTail.java` | API diff: Camera getters, VertexConsumer API | HIGH | NO |

### Narrative - Interaction (1 file)

| File | Why Override | Risk | Bridge? |
|------|--------------|------|---------|
| `narrative/interaction/InteractionEyeRenderer.java` | API diff: `Camera.getPosition()`, VertexConsumer API | HIGH | NO |

### Narrative - Keyframes (1 file)

| File | Why Override | Risk | Bridge? |
|------|--------------|------|---------|
| `narrative/keyframes/Keyframe.java` | API diff: `Mth.lerp` component-wise, no `Vec3` overload | MEDIUM | NO |

### Narrative - Recording (7 files)

| File | Why Override | Risk | Bridge? |
|------|--------------|------|---------|
| `narrative/recording/Recording.java` | Package diff: `Boat` vs `AbstractBoat`, no `ProjectileItem` | MEDIUM | NO |
| `narrative/recording/actions/AbstractBoatBubbleAction.java` | Package diff: `Boat` class | LOW | NO |
| `narrative/recording/actions/AbstractBoatPaddleAction.java` | Package diff: `Boat` class | LOW | NO |
| `narrative/recording/actions/AbstractHorseByteAction.java` | Package diff: `animal.horse.AbstractHorse` | LOW | NO |
| `narrative/recording/actions/RidingAction.java` | Signature diff: `startRiding(entity, true)` 2 vs 3 params | MEDIUM | YES |
| `narrative/recording/actions/StopRidingAction.java` | Signature diff: `startRiding(entity, true)` 2 vs 3 params | MEDIUM | YES |
| `narrative/recording/actions/manager/ActionDifferenceListener.java` | Logic diff: No `EquipmentSlot.SADDLE`/`BODY`, `Boat` vs `AbstractBoat` | HIGH | NO |

### Narrative - Story (3 files)

| File | Why Override | Risk | Bridge? |
|------|--------------|------|---------|
| `narrative/story/inkAction/MinecraftCommandInkAction.java` | Signature diff: Permission level int vs PermissionSet | MEDIUM | NO |
| `narrative/story/inkAction/WeatherInkAction.java` | Minimal differences | LOW | YES |
| `narrative/story/inkAction/text/DialogScrollTextInkAction.java` | Minimal differences | LOW | YES |

### Screens - Characters (2 files)

| File | Why Override | Risk | Bridge? |
|------|--------------|------|---------|
| `screens/characters/CharacterChangePoseScreen.java` | API diff: `renderBlurredBackground()` override | LOW | YES |
| `screens/characters/CharacterEntityTypeScreen.java` | API diff: OptionsSubScreen, primitive event params | MEDIUM | NO |

### Screens - Components (10 files)

| File | Why Override | Risk | Bridge? |
|------|--------------|------|---------|
| `screens/components/AbstractTextBoxScreen.java` | API diff: `ResourceLocation` constructor vs `withDefaultNamespace()` | LOW | YES |
| `screens/components/ChangeSkinLinkScreen.java` | Minimal differences | LOW | YES |
| `screens/components/ChoiceButtonWidget.java` | VertexConsumer API, PoseStack methods | HIGH | NO |
| `screens/components/FinishedStoryScreen.java` | API diff: `ResourceLocation` constructor | LOW | YES |
| `screens/components/GenericSelectionScreen.java` | API diff: Entry.render() 10-param vs renderContent() | MEDIUM | NO |
| `screens/components/KeyframeOptionScreen.java` | API diff: `renderBlurredBackground()` override | LOW | YES |
| `screens/components/NarrativeCraftLogoRenderer.java` | API diff: NcId/ResourceLocation conversion | LOW | YES |
| `screens/components/ObjectListScreen.java` | API diff: Entry.render() signature, no `getContentY()` | MEDIUM | NO |
| `screens/components/PickElementScreen.java` | API diff: `refreshScrollAmount()` missing | MEDIUM | NO |
| `screens/components/StoryElementList.java` | API diff: Entry.render() signature, no `getContentY()` | MEDIUM | NO |

### Screens - Controllers (7 files)

| File | Why Override | Risk | Bridge? |
|------|--------------|------|---------|
| `screens/controller/areaTrigger/AreaTriggerControllerScreen.java` | API diff: `renderBlurredBackground()` override | LOW | YES |
| `screens/controller/cameraAngle/CameraAngleControllerScreen.java` | API diff: `renderBlurredBackground()` override | LOW | YES |
| `screens/controller/cutscene/CutsceneControllerScreen.java` | API diff: `renderBlurredBackground()` override | LOW | YES |
| `screens/controller/cutscene/CutsceneKeyframeAdvancedSettings.java` | Logic diff: Direct screen instantiation | LOW | YES |
| `screens/controller/cutscene/CutsceneKeyframeEasingsScreen.java` | API diff: OptionsSubScreen usage | MEDIUM | NO |
| `screens/controller/interaction/InteractionControllerScreen.java` | API diff: `renderBlurredBackground()` override | LOW | YES |
| `screens/controller/mainScreen/MainScreenControllerScreen.java` | API diff: `renderBlurredBackground()` override | LOW | YES |

### Screens - Credits (1 file)

| File | Why Override | Risk | Bridge? |
|------|--------------|------|---------|
| `screens/credits/CreditScreen.java` | API diff: `ResourceLocation` constructor | LOW | YES |

### Screens - Main (2 files)

| File | Why Override | Risk | Bridge? |
|------|--------------|------|---------|
| `screens/mainScreen/MainScreen.java` | API diff: `ResourceLocation` constructor, SimpleSoundInstance | MEDIUM | NO |
| `screens/mainScreen/MainScreenOptionsScreen.java` | API diff: Checkbox.builder() API | MEDIUM | NO |

### Screens - Options (2 files)

| File | Why Override | Risk | Bridge? |
|------|--------------|------|---------|
| `screens/options/DialogCustomOptionsScreen.java` | API diff: `renderBlurredBackground()` override | LOW | YES |
| `screens/options/StoryOptionsScreen.java` | Package diff: `net.minecraft.Util`, API: `openFile()` vs `openPath()` | LOW | YES |

### Screens - Story (1 file)

| File | Why Override | Risk | Bridge? |
|------|--------------|------|---------|
| `screens/story/StoryChoicesScreen.java` | API diff: `renderBlurredBackground()` missing, PoseStack translate | MEDIUM | NO |

### Screens - Story Manager (12 files)

| File | Why Override | Risk | Bridge? |
|------|--------------|------|---------|
| `screens/storyManager/animations/AnimationsScreen.java` | Package diff: `net.minecraft.Util`, API: `openFile()` | LOW | YES |
| `screens/storyManager/cameraAngle/CameraAngleScreen.java` | Package diff: `net.minecraft.Util`, API: `openFile()` | LOW | YES |
| `screens/storyManager/chapter/ChaptersScreen.java` | Package diff: `net.minecraft.Util`, API: `openFile()` | LOW | YES |
| `screens/storyManager/character/CharactersScreen.java` | Package diff: `net.minecraft.Util`, API: `openFile()` | LOW | YES |
| `screens/storyManager/character/EditScreenCharacterAdapter.java` | API diff: `Minecraft.getInstance().font` vs `screen.getFont()` | LOW | YES |
| `screens/storyManager/cutscene/CutscenesScreen.java` | Package diff: `net.minecraft.Util`, API: `openFile()` | LOW | YES |
| `screens/storyManager/interaction/InteractionsScreen.java` | Package diff: `net.minecraft.Util`, API: `openFile()` | LOW | YES |
| `screens/storyManager/scene/EditScreenSceneAdapter.java` | API diff: `Minecraft.getInstance().font` vs `screen.getFont()` | LOW | YES |
| `screens/storyManager/scene/ScenesMenuScreen.java` | Package diff: `net.minecraft.Util`, API: `openFile()` | LOW | YES |
| `screens/storyManager/scene/ScenesScreen.java` | Package diff: `net.minecraft.Util`, API: `openFile()` | LOW | YES |
| `screens/storyManager/subscene/SubscenesScreen.java` | Package diff: `net.minecraft.Util`, API: `openFile()` | LOW | YES |

### Util (2 files)

| File | Why Override | Risk | Bridge? |
|------|--------------|------|---------|
| `util/ScreenUtils.java` | API diff: `getToasts()` vs `getToastManager()`, MultiLineEditBox | MEDIUM | NO |
| `util/Util.java` | Package diff: `net.minecraft.Util` vs `net.minecraft.util.Util` | LOW | YES |

---

## Key API Differences

### Package Renames

| 1.20.x | 1.21.x |
|--------|--------|
| `net.minecraft.Util` | `net.minecraft.util.Util` |
| `animal.horse.AbstractHorse` | `animal.equine.AbstractHorse` |
| `vehicle.Boat` | `vehicle.boat.AbstractBoat` |

### Rendering API (HIGH complexity)

| 1.20.x | 1.21.x |
|--------|--------|
| `.vertex().color().uv().overlayCoords().uv2().normal().endVertex()` | `.addVertex().setColor().setLight()` |
| `camera.getPosition()` | `camera.position()` |
| `camera.getLeftVector()` | `camera.leftVector()` |
| `camera.getUpVector()` | `camera.upVector()` |
| `camera.getEntity()` | `camera.entity()` |

### Screen/GUI API

| 1.20.x | 1.21.x |
|--------|--------|
| `new ResourceLocation(path)` | `ResourceLocation.withDefaultNamespace(path)` |
| `getToasts()` | `getToastManager()` |
| `Entry.render(10 params)` | `renderContent()` |
| `Util.getPlatform().openFile(file)` | `Util.getPlatform().openPath(path)` |
| `screen.font` | `screen.getFont()` |
| _(no method)_ | `renderBlurredBackground()` |

### Entity API

| 1.20.x | 1.21.x |
|--------|--------|
| `startRiding(entity, force)` | `startRiding(entity, force, skipCheck)` |
| `setPos(x, y, z)` | `snapTo(Vec3)` |
| `player.gameMode.getGameModeForPlayer()` | `player.gameMode()` |

### Missing Methods (require helpers)

| Method | 1.20.x Alternative |
|--------|-------------------|
| `Mth.lerp(double, Vec3, Vec3)` | Component-wise lerp |
| `LivingEntity.isLookingAtMe()` | Custom `isPlayerLookingAt()` helper |
| `Entity.snapTo(Vec3)` | `setPos(x, y, z)` |
| `refreshScrollAmount()` | `setScrollAmount(0)` |

---

## Recommended Bridge Abstractions

Based on usage frequency and reduction potential:

| Bridge | Files Affected | Priority |
|--------|---------------|----------|
| **UtilBridge** | 15+ | HIGH |
| **CameraBridge** | 5 | HIGH |
| **VertexConsumerBridge** | 8 | MEDIUM (complex) |
| **EntityRidingBridge** | 2 | LOW |
| **GameModeBridge** | 4 | MEDIUM |
| **ScreenFontBridge** | 3 | LOW |
| **ResourceLocationBridge** | Already exists (NcId) | DONE |

---

## Maintenance Notes

1. **When adding features to `common/`**: Check if mc120 override exists and mirror changes
2. **When fixing bugs**: Fix in `common/` first, mirror to mc120 override if exists
3. **When creating new screens**: Consider if `renderBlurredBackground()` override is needed
4. **HIGH risk files**: Extra review needed - contain duplicated business logic
