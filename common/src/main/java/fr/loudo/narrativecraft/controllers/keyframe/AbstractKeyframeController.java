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
import fr.loudo.narrativecraft.narrative.keyframes.Keyframe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

/**
 * Base controller providing common functionality for keyframes,
 * including camera management through the {@link #setCamera(Keyframe)} method.
 *
 * @param <T> the type of keyframe, extending {@link Keyframe}
 */
public abstract class AbstractKeyframeController<T extends Keyframe> extends AbstractController
        implements KeyframeControllerInterface<T> {

    protected GameType lastGameType;

    public AbstractKeyframeController(Environment environment, Player player) {
        super(environment, player);
    }

    @Override
    public void setCamera(Keyframe keyframe) {
        Minecraft minecraft = Minecraft.getInstance();
        if (playerSession.getCurrentCamera() != null && keyframe == null) {
            playerSession.setCurrentCamera(null);
            playerSession.getPlayer().setGameMode(lastGameType);
            minecraft.options.hideGui = false;
            showKeyframes(playerSession.getPlayer());
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
            minecraft.execute(() -> minecraft.setScreen(keyframeOptionScreen(keyframe)));
        }
    }

    public abstract Screen keyframeOptionScreen(Keyframe keyframe);
}
