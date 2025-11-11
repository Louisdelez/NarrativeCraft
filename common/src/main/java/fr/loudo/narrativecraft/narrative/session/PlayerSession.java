/*
 * NarrativeCraft - Create your own stories, easily, and freely in Minecraft.
 * Copyright (c) 2025 LOUDO and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package fr.loudo.narrativecraft.narrative.session;

import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.controllers.AbstractController;
import fr.loudo.narrativecraft.controllers.interaction.InteractionController;
import fr.loudo.narrativecraft.gui.StorySaveIconGui;
import fr.loudo.narrativecraft.managers.PlaybackManager;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.AreaTrigger;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.interaction.StitchInteraction;
import fr.loudo.narrativecraft.narrative.character.CharacterRuntime;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.dialog.DialogRenderer;
import fr.loudo.narrativecraft.narrative.inkTag.InkTagHandler;
import fr.loudo.narrativecraft.narrative.keyframes.KeyframeLocation;
import fr.loudo.narrativecraft.narrative.recording.Location;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.inkAction.GameplayInkAction;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public class PlayerSession {

    private final ServerPlayer player;
    private final PlaybackManager playbackManager = new PlaybackManager();
    private final List<InkAction> inkActions = new ArrayList<>();
    private final List<CharacterRuntime> characterRuntimes = new ArrayList<>();
    private final InkTagHandler inkTagHandler;
    private final StorySaveIconGui storySaveIconGui = new StorySaveIconGui(0.2, 0.9, 0.2);
    private final List<InteractionController> interactionControllers = new ArrayList<>();
    private final List<AreaTrigger> areaTriggersEntered = new ArrayList<>();
    private AbstractController controller;
    private DialogRenderer dialogRenderer;
    private KeyframeLocation currentCamera;
    private StoryHandler storyHandler;
    private AreaTrigger lastAreaTriggerEntered;
    private StitchInteraction lastInteraction;
    private Chapter chapter;
    private Scene scene;
    private String stitch;
    private GameType lastGameType;
    private int lookingAtEntityId;
    private boolean showDebugHud;

    public PlayerSession(ServerPlayer player) {
        this.player = player;
        inkTagHandler = new InkTagHandler(this);
    }

    public PlayerSession(ServerPlayer player, Chapter chapter, Scene scene) {
        this.player = player;
        this.chapter = chapter;
        this.scene = scene;
        inkTagHandler = new InkTagHandler(this);
    }

    public void addInkAction(InkAction inkAction) {
        if (inkActions.contains(inkAction)) return;
        inkActions.add(inkAction);
    }

    public void removeInkAction(InkAction inkAction) {
        inkActions.remove(inkAction);
    }

    public boolean isSamePlayer(Player player) {
        if (player == null) return false;
        return this.player.getUUID().equals(player.getUUID());
    }

    public boolean isSessionSet() {
        return chapter != null && scene != null;
    }

    public void reset() {
        chapter = null;
        scene = null;
    }

    public Location getPlayerPosition() {
        return new Location(
                player.getX(), player.getY(), player.getZ(), player.getXRot(), player.getYRot(), player.onGround());
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public PlaybackManager getPlaybackManager() {
        return playbackManager;
    }

    public List<InkAction> getInkActions() {
        return inkActions;
    }

    // I know this code sucks, but I don't know why it sometimes does ConcurrentModificationException bruh...

    public List<InkAction> getClientSideInkActions() {
        try {
            return inkActions.stream()
                    .filter(inkAction -> inkAction.getSide() != null && inkAction.getSide() == InkAction.Side.CLIENT)
                    .toList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<InkAction> getServerSideInkActions() {
        try {
            return inkActions.stream()
                    .filter(inkAction -> inkAction.getSide() != null && inkAction.getSide() == InkAction.Side.SERVER)
                    .toList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public InkTagHandler getInkTagHandler() {
        return inkTagHandler;
    }

    public List<CharacterRuntime> getCharacterRuntimesByCharacter(CharacterStory characterStory) {
        clearKilledCharacters();
        List<CharacterRuntime> characterRuntimes1 = new ArrayList<>();
        for (CharacterRuntime characterRuntime : characterRuntimes) {
            if (characterRuntime.getEntity() == null
                    || characterRuntime.getEntity().isRemoved()) continue;
            if (characterRuntime.getCharacterStory().getName().equalsIgnoreCase(characterStory.getName())) {
                characterRuntimes1.add(characterRuntime);
            }
        }
        return characterRuntimes1;
    }

    public void clearKilledCharacters() {
        characterRuntimes.removeIf(characterRuntime -> characterRuntime.getEntity() == null
                || characterRuntime.getEntity().isRemoved());
    }

    public void clearPlaybacksNotPlaying() {
        playbackManager
                .getPlaybacks()
                .removeIf(playback -> playback.hasEnded()
                        || playback.getCharacterRuntime().getEntity() == null
                        || playback.getCharacterRuntime().getEntity().isRemoved());
    }

    public boolean isOnGameplay() {
        List<InkAction> inkActions1 = new ArrayList<>(inkActions);
        return !inkActions1.stream()
                .filter(inkAction -> inkAction instanceof GameplayInkAction)
                .toList()
                .isEmpty();
    }

    public List<AreaTrigger> getAreaTriggersEntered() {
        return areaTriggersEntered;
    }

    public void addAreaTriggerEntered(AreaTrigger areaTrigger) {
        if (areaTriggersEntered.contains(areaTrigger)) return;
        areaTriggersEntered.add(areaTrigger);
    }

    public void removeAreaTriggerEntered(AreaTrigger areaTrigger) {
        areaTriggersEntered.remove(areaTrigger);
    }

    public AreaTrigger getLastAreaTriggerEntered() {
        return lastAreaTriggerEntered;
    }

    public StitchInteraction getLastInteraction() {
        return lastInteraction;
    }

    public void setLastInteraction(StitchInteraction lastInteraction) {
        this.lastInteraction = lastInteraction;
    }

    public void setLastAreaTriggerEntered(AreaTrigger lastAreaTriggerEntered) {
        this.lastAreaTriggerEntered = lastAreaTriggerEntered;
    }

    public List<CharacterRuntime> getCharacterRuntimes() {
        return characterRuntimes;
    }

    public DialogRenderer getDialogRenderer() {
        return dialogRenderer;
    }

    public void setDialogRenderer(DialogRenderer dialogRenderer) {
        this.dialogRenderer = dialogRenderer;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public String getStitch() {
        return stitch;
    }

    public void setStitch(String stitch) {
        this.stitch = stitch;
    }

    public int getLookingAtEntityId() {
        return lookingAtEntityId;
    }

    public void setLookingAtEntityId(int lookingAtEntityId) {
        this.lookingAtEntityId = lookingAtEntityId;
    }

    public AbstractController getController() {
        return controller;
    }

    public void setController(AbstractController controller) {
        this.controller = controller;
    }

    public KeyframeLocation getCurrentCamera() {
        return currentCamera;
    }

    public StoryHandler getStoryHandler() {
        return storyHandler;
    }

    public void setStoryHandler(StoryHandler storyHandler) {
        this.storyHandler = storyHandler;
    }

    public void setCurrentCamera(KeyframeLocation currentCamera) {
        this.currentCamera = currentCamera;
        if (currentCamera == null) {
            if (lastGameType != null) {
                player.setGameMode(lastGameType);
            }
            return;
        }
        if (player.gameMode() != GameType.SPECTATOR) {
            this.lastGameType = player.gameMode();
        }
        player.setGameMode(GameType.SPECTATOR);
    }

    public StorySaveIconGui getStorySaveIconGui() {
        return storySaveIconGui;
    }

    public boolean isShowDebugHud() {
        return showDebugHud;
    }

    public void setShowDebugHud(boolean showDebugHud) {
        this.showDebugHud = showDebugHud;
    }

    public List<InteractionController> getInteractionControllers() {
        return interactionControllers;
    }
}
