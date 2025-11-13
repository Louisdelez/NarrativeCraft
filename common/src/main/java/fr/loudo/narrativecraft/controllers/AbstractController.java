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

package fr.loudo.narrativecraft.controllers;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Player;

public abstract class AbstractController {
    protected final Environment environment;
    protected final PlayerSession playerSession;
    protected String hudMessage;

    public AbstractController(Environment environment, Player player) {
        this.environment = environment;
        this.playerSession =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getSessionByPlayer(player);
    }

    public abstract void startSession();

    public abstract void stopSession(boolean save);

    public abstract Screen getControllerScreen();

    protected void stopCurrentSession() {
        AbstractController controller = playerSession.getController();
        if (controller == null) return;
        controller.stopSession(false);
    }

    public void renderHUDInfo(GuiGraphics guiGraphics) {
        if (playerSession.getCurrentCamera() != null || environment != Environment.DEVELOPMENT) return;
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int width = minecraft.getWindow().getGuiScaledWidth();
        guiGraphics.drawString(
                font,
                hudMessage,
                width / 2 - font.width(hudMessage) / 2,
                10,
                FastColor.ARGB32.colorFromFloat(1, 1, 1, 1));
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

    public CharacterRuntime getCharacterFromEntity(Entity entity) {
        for (CharacterRuntime characterRuntime : playerSession.getCharacterRuntimes()) {
            if (Util.isSameEntity(entity, characterRuntime.getEntity())) {
                return characterRuntime;
            }
        }
        return null;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public PlayerSession getPlayerSession() {
        return playerSession;
    }

    public List<CharacterStoryData> getCharacterStoryDataList() {
        return characterStoryDataList;
    }

    public void removeCharacterStoryData(CharacterStoryData characterStoryData) {
        characterStoryDataList.remove(characterStoryData);
    }
}
