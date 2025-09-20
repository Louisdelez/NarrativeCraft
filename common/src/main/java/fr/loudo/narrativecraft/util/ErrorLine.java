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

package fr.loudo.narrativecraft.util;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

public class ErrorLine {
    private final boolean isWarn;
    private final int line;
    private final Scene scene;
    private final Chapter chapter;
    private String fileName;
    private final String message;
    private final String lineText;

    public ErrorLine(int line, Chapter chapter, Scene scene, String message, String lineText, boolean isWarn) {
        this.line = line;
        this.chapter = chapter;
        this.scene = scene;

        if (scene != null) {
            this.fileName = scene.knotName() + NarrativeCraftFile.EXTENSION_SCRIPT_FILE;
        } else if (chapter != null) {
            this.fileName = chapter.knotName() + NarrativeCraftFile.EXTENSION_SCRIPT_FILE;
        }

        this.message = message;
        this.lineText = lineText;
        this.isWarn = isWarn;
    }

    public Component toMessage() {
        if (scene == null && chapter == null) {
            return Component.empty()
                    .append(Component.literal(lineText)
                            .withStyle(ChatFormatting.GRAY)
                            .withStyle(style -> style.withBold(false)))
                    .append("\n")
                    .append(Component.literal("'" + message + "'")
                            .withStyle(ChatFormatting.RED)
                            .withStyle(style -> style.withBold(false)))
                    .append("\n");
        } else {
            Component result = Component.empty().append("\n");

            Chapter chapterToDisplay = scene != null ? scene.getChapter() : chapter;
            if (chapterToDisplay != null) {
                result = result.copy()
                        .append(Component.literal("Chapter: ")
                                .withStyle(ChatFormatting.RED)
                                .withStyle(ChatFormatting.BOLD))
                        .append(Component.literal(String.valueOf(chapterToDisplay.getIndex()))
                                .withStyle(ChatFormatting.RED)
                                .withStyle(style -> style.withBold(false)))
                        .append("\n");
            }

            if (scene != null) {
                result = result.copy()
                        .append(Component.literal("Scene: ")
                                .withStyle(ChatFormatting.RED)
                                .withStyle(ChatFormatting.BOLD))
                        .append(Component.literal(scene.getName() + " ")
                                .withStyle(ChatFormatting.RED)
                                .withStyle(style -> style.withBold(false)));
            }

            if (fileName != null) {
                ClickEvent clickEvent;
                if (scene != null) {
                    clickEvent = new ClickEvent.OpenFile(NarrativeCraftFile.getScriptFile(scene));
                } else {
                    clickEvent = new ClickEvent.OpenFile(NarrativeCraftFile.getScriptFile(chapter));
                }

                result = result.copy()
                        .append(Component.literal("(" + fileName + ")")
                                .withStyle(ChatFormatting.GRAY)
                                .withStyle(style -> style.withBold(false)
                                        .withClickEvent(clickEvent)
                                        .withHoverEvent(
                                                new HoverEvent.ShowText(Translation.message("validation.quick_edit")))))
                        .append("\n");
            }

            result = result.copy()
                    .append(Translation.message("global.line").withStyle(ChatFormatting.GOLD))
                    .append(" " + line + ": ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(lineText)
                            .withStyle(ChatFormatting.GRAY)
                            .withStyle(style -> style.withBold(false)))
                    .append("\n")
                    .append(Component.literal("'" + message + "'")
                            .withStyle(isWarn ? ChatFormatting.YELLOW : ChatFormatting.RED)
                            .withStyle(style -> style.withBold(false)))
                    .append("\n");

            return result;
        }
    }

    public int getLine() {
        return line;
    }

    public Scene getScene() {
        return scene;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMessage() {
        return message;
    }

    public String getLineText() {
        return lineText;
    }

    public boolean isWarn() {
        return isWarn;
    }
}
