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

package fr.loudo.narrativecraft.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.narrative.character.CharacterRuntime;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;

/**
 * MC 1.19.x version of StoryDebugHud.
 * Uses PoseStack and font.draw() instead of GuiGraphics.drawString().
 */
public class StoryDebugHud {

    private final Minecraft minecraft = Minecraft.getInstance();

    private final PlayerSession playerSession;

    public StoryDebugHud(PlayerSession playerSession) {
        this.playerSession = playerSession;
    }

    public void render(PoseStack poseStack, float partialTick) {
        if (playerSession.getStoryHandler() == null) return;
        List<InkAction> inkActions = new ArrayList<>(playerSession.getInkActions());
        int x = 5;
        int y = 5;
        int color = NarrativeCraftMod.getColorCompat().color(255, 255, 255, 255);

        // 1.19.x: Use font.draw() with PoseStack instead of guiGraphics.drawString()
        minecraft.font.draw(poseStack, "Ink tag running:", x, y, color);
        y += minecraft.font.lineHeight + 5;

        for (InkAction inkAction : inkActions) {
            String command = inkAction.getCommand();
            if (command.length() > 25) {
                command = command.substring(0, 25) + "...";
            }
            minecraft.font.draw(poseStack, "\"" + command + "\"", x, y, color);
            y += minecraft.font.lineHeight + 5;
        }
        y = 5;
        String headerTag = "Ink tag running:";
        String longestInkCommand = inkActions.stream()
                .map(inkAction -> {
                    String command = inkAction.getCommand();
                    if (command.length() > 25) {
                        command = command.substring(0, 25) + "...";
                    }
                    return "\"" + command + "\"";
                })
                .max(Comparator.comparingInt(String::length))
                .orElse(headerTag);

        x += minecraft.font.width(headerTag.length() > longestInkCommand.length() ? headerTag : longestInkCommand) + 10;

        minecraft.font.draw(poseStack, "Characters in the world:", x, y, color);
        y += minecraft.font.lineHeight + 5;

        List<CharacterRuntime> characterRuntimes = playerSession.getCharacterRuntimes();
        for (CharacterRuntime characterRuntime : characterRuntimes) {
            minecraft.font.draw(poseStack, characterRuntime.getCharacterStory().getName(), x, y, color);
            y += minecraft.font.lineHeight + 5;
        }
        String headerChara = "Characters in the world:";
        String longestCharaName = characterRuntimes.stream()
                .map(characterRuntime -> characterRuntime.getCharacterStory().getName())
                .max(Comparator.comparingInt(String::length))
                .orElse(headerChara);
        x += minecraft.font.width(headerChara.length() > longestCharaName.length() ? headerChara : longestCharaName)
                + 10;

        minecraft.font.draw(poseStack, "Session:", x, 5, color);

        String chapterText = "Chapter " + playerSession.getChapter().getIndex();
        minecraft.font.draw(poseStack, chapterText, x, minecraft.font.lineHeight + 10, color);

        String sceneName = playerSession.getScene().getName();
        minecraft.font.draw(poseStack, sceneName, x, minecraft.font.lineHeight + 23, color);

        String stitchName = playerSession.getStitch();
        if (stitchName != null) {
            minecraft.font.draw(poseStack, stitchName, x, minecraft.font.lineHeight + 35, color);
        }
    }
}
