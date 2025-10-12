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

package fr.loudo.narrativecraft.controllers.keyframe;

import fr.loudo.narrativecraft.controllers.AbstractController;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.character.CharacterRuntime;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.keyframes.KeyframeLocation;
import fr.loudo.narrativecraft.narrative.keyframes.keyframeTrigger.KeyframeTrigger;
import fr.loudo.narrativecraft.util.Util;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

/**
 * Base controller providing common functionality for keyframes,
 * including camera management through the {@link #setCamera(Keyframe)} method.
 *
 * @param <T> the type of keyframe, extending {@link Keyframe}
 */
public abstract class AbstractKeyframeController<T extends Keyframe> extends AbstractController
        implements KeyframeControllerInterface<T> {

    protected final List<KeyframeTrigger> keyframeTriggers = new ArrayList<>();
    protected final List<CharacterStoryData> characterStoryDataList = new ArrayList<>();
    protected final AtomicInteger keyframesCounter = new AtomicInteger();
    protected GameType lastGameType;

    public AbstractKeyframeController(Environment environment, Player player) {
        super(environment, player);
    }

    protected KeyframeLocation getKeyframeLocationFromPlayer() {
        ServerPlayer player = playerSession.getPlayer();
        return new KeyframeLocation(
                player.position().add(0, player.getEyeHeight(), 0), player.getXRot(), player.getYRot(), 0, 85.0f);
    }

    @Override
    public void setCamera(Keyframe keyframe) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.options.setCameraType(CameraType.FIRST_PERSON);
        if (playerSession.getCurrentCamera() != null && keyframe == null) {
            playerSession.setCurrentCamera(null);
            playerSession.getPlayer().setGameMode(lastGameType);
            minecraft.options.hideGui = false;
            showKeyframes(playerSession.getPlayer());
            Vec3 pos = playerSession.getPlayer().position();
            playerSession.getPlayer().teleportTo(pos.x, pos.y - 5.0 - playerSession.getPlayer().getEyeHeight(), pos.z);
            return;
        }
        if (keyframe == null) return;
        if (playerSession.getCurrentCamera() == null) {
            lastGameType = playerSession.getPlayer().gameMode();
        }
        playerSession.getPlayer().setGameMode(GameType.SPECTATOR);
        playerSession.setCurrentCamera(keyframe.getKeyframeLocation());
        hideKeyframes(playerSession.getPlayer());
        minecraft.options.hideGui = true;
        if (environment == Environment.DEVELOPMENT) {
            minecraft.execute(() -> minecraft.setScreen(keyframeOptionScreen(keyframe, false)));
        }
    }

    public KeyframeTrigger getKeyframeTriggerByEntity(Entity entity) {
        for (KeyframeTrigger keyframeTrigger : keyframeTriggers) {
            if (Util.isSameEntity(entity, keyframeTrigger.getCamera())) {
                return keyframeTrigger;
            }
        }
        return null;
    }

    @Override
    public void addKeyframeTrigger(int tick, String commands) {
        KeyframeTrigger keyframeTrigger = new KeyframeTrigger(
                keyframesCounter.incrementAndGet(), tick, commands, getKeyframeLocationFromPlayer());
        keyframeTrigger.showKeyframe(playerSession.getPlayer());
        keyframeTriggers.add(keyframeTrigger);
    }

    @Override
    public void removeKeyframeTrigger(KeyframeTrigger keyframeTrigger) {
        keyframeTriggers.remove(keyframeTrigger);
        keyframeTrigger.hideKeyframe(playerSession.getPlayer());
    }

    public abstract Screen keyframeOptionScreen(Keyframe keyframe, boolean hide);

    public List<KeyframeTrigger> getKeyframeTriggers() {
        return keyframeTriggers;
    }

    public CharacterRuntime getCharacterFromEntity(Entity entity) {
        for (CharacterRuntime characterRuntime : playerSession.getCharacterRuntimes()) {
            if (Util.isSameEntity(entity, characterRuntime.getEntity())) {
                return characterRuntime;
            }
        }
        return null;
    }

    public CharacterStoryData getCharacterStoryDataFromEntity(Entity entity) {
        for (CharacterStoryData characterStoryData : characterStoryDataList) {
            if (Util.isSameEntity(
                    entity, characterStoryData.getCharacterRuntime().getEntity())) {
                return characterStoryData;
            }
        }
        return null;
    }

    public List<CharacterStoryData> getCharacterStoryDataList() {
        return characterStoryDataList;
    }
}
