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

package fr.loudo.narrativecraft.narrative.dialog.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.narrative.dialog.DialogRenderer;
import fr.loudo.narrativecraft.narrative.dialog.DialogRenderer3D;
import fr.loudo.narrativecraft.util.Util;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;

public class DialogScrollText {
    private final DialogRenderer dialogRenderer;
    private final Minecraft minecraft;
    private final List<LetterLocation> lettersRenderer = new ArrayList<>();
    private List<String> lines = new ArrayList<>();
    private int currentTick, currentLine, currentCharIndex;
    private float currentX, currentY;

    public DialogScrollText(DialogRenderer dialogRenderer, Minecraft minecraft) {
        this.dialogRenderer = dialogRenderer;
        this.minecraft = minecraft;
        setText(dialogRenderer.getText());
    }

    public void reset() {
        currentTick = 0;
        currentLine = 0;
        currentCharIndex = 0;
        currentY = -dialogRenderer.getTotalHeight() + dialogRenderer.getPaddingY() * 2;
        currentX = -dialogRenderer.getTotalWidth() + dialogRenderer.getPaddingX() * 2;
        lettersRenderer.clear();
    }

    public void forceFinish() {
        while (!isFinished()) {
            addLetter();
        }
    }

    public void tick() {
        if (!isFinished() && !dialogRenderer.isAnimating()) {
            currentTick++;
            populateLetters();
        }
    }

    public void render(PoseStack poseStack, MultiBufferSource.BufferSource source) {
        for (LetterLocation letter : lettersRenderer) {
            float y = letter.y;
            if (dialogRenderer instanceof DialogRenderer3D dialogRenderer3D) {
                if (dialogRenderer3D.getDialogOffset().y < 0) {
                    y += dialogRenderer3D.getTotalHeight() - dialogRenderer3D.getPaddingY();
                }
            }
            minecraft.font.drawInBatch(
                    String.valueOf(letter.letter),
                    letter.x,
                    y,
                    ARGB.color(255, dialogRenderer.getTextColor()),
                    false,
                    poseStack.last().pose(),
                    minecraft.renderBuffers().bufferSource(),
                    Font.DisplayMode.SEE_THROUGH,
                    0,
                    LightTexture.FULL_BRIGHT);
        }
        source.endBatch();
    }

    public void render(GuiGraphics guiGraphics, float partialTick) {}

    public boolean isFinished() {
        return currentLine == lines.size() - 1
                && currentCharIndex == lines.getLast().length();
    }

    public String getLongerTextLine() {
        return lines.stream().max(Comparator.comparingInt(String::length)).orElse("");
    }

    public void setText(String text) {
        lines = splitText(text);
    }

    private List<String> splitText(String text) {

        List<String> finalString = new ArrayList<>();
        Minecraft client = Minecraft.getInstance();
        List<FormattedCharSequence> charSequences =
                client.font.split(FormattedText.of(text), (int) dialogRenderer.getWidth());
        for (FormattedCharSequence chara : charSequences) {
            StringBuilder stringBuilder = new StringBuilder();
            chara.accept((i, style, i1) -> {
                stringBuilder.appendCodePoint(i1);
                return true;
            });
            finalString.add(stringBuilder.toString());
        }
        return finalString;
    }

    private void populateLetters() {
        if (currentTick >= 1 && currentLine < lines.size()) {
            if (addLetter() != ' ') {
                playLetterSound();
            }
        }
    }

    private char addLetter() {
        char letter = lines.get(currentLine).charAt(currentCharIndex);
        lettersRenderer.add(new LetterLocation(letter, currentX, currentY));
        currentTick = 0;
        currentX += Util.getLetterWidth(letter, minecraft) + dialogRenderer.getLetterSpacing();
        currentCharIndex++;
        if (currentCharIndex >= lines.get(currentLine).length()) {
            currentLine++;
            currentCharIndex = 0;
            currentX = -dialogRenderer.getTotalWidth() + dialogRenderer.getPaddingX() * 2;
            currentY += minecraft.font.lineHeight + dialogRenderer.getGap();
        }
        return letter;
    }

    private void playLetterSound() {
        float pitch = 0.8F + new Random().nextFloat() * 0.4F;
        ResourceLocation soundRes = ResourceLocation.withDefaultNamespace("sfx.dialog_sound");
        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(soundRes);
        minecraft.player.playSound(soundEvent, 1.0F, pitch);
    }

    public List<String> getLines() {
        return lines;
    }

    private record LetterLocation(char letter, float x, float y) {}
}
