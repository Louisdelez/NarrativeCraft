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
import fr.loudo.narrativecraft.managers.PlaybackManager;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.dialog.DialogRenderer;
import fr.loudo.narrativecraft.narrative.keyframes.KeyframeLocation;
import fr.loudo.narrativecraft.narrative.recording.Location;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PlayerSession {

    private final ServerPlayer player;
    private final PlaybackManager playbackManager = new PlaybackManager();
    private final List<InkAction> inkActions = new ArrayList<>();
    private AbstractController controller;
    private DialogRenderer dialogRenderer;
    private KeyframeLocation currentCamera;
    private Chapter chapter;
    private Scene scene;

    public PlayerSession(ServerPlayer player) {
        this.player = player;
    }

    public PlayerSession(ServerPlayer player, Chapter chapter, Scene scene) {
        this.player = player;
        this.chapter = chapter;
        this.scene = scene;
    }

    public void addInkAction(InkAction inkAction) {
        if (inkActions.contains(inkAction)) return;
        inkActions.add(inkAction);
    }

    public void removeInkAction(InkAction inkAction) {
        inkActions.remove(inkAction);
    }

    public boolean isSamePlayer(Player player) {
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

    public AbstractController getController() {
        return controller;
    }

    public void setController(AbstractController controller) {
        this.controller = controller;
    }

    public KeyframeLocation getCurrentCamera() {
        return currentCamera;
    }

    public void setCurrentCamera(KeyframeLocation currentCamera) {
        this.currentCamera = currentCamera;
    }
}
