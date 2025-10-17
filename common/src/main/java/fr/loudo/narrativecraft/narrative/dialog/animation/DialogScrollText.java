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
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.gui.ICustomGuiRender;
import fr.loudo.narrativecraft.narrative.dialog.DialogRenderer;
import fr.loudo.narrativecraft.narrative.dialog.DialogRenderer2D;
import fr.loudo.narrativecraft.narrative.dialog.DialogRenderer3D;
import fr.loudo.narrativecraft.narrative.story.text.ParsedDialog;
import fr.loudo.narrativecraft.narrative.story.text.TextEffectAnimation;
import fr.loudo.narrativecraft.options.NarrativeClientOption;
import fr.loudo.narrativecraft.util.Util;
import java.util.*;
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
import org.joml.Vector2f;

public class DialogScrollText {
    private final NarrativeClientOption clientOption =
            NarrativeCraftMod.getInstance().getNarrativeClientOptions();
    private final DialogRenderer dialogRenderer;
    private final Minecraft minecraft;
    private final List<LetterLocation> lettersRenderer = new ArrayList<>();
    private TextEffectAnimation textEffectAnimation;
    private List<String> lines = new ArrayList<>();
    private int currentLine, currentCharIndex;
    private float currentX, currentY;

    private float tickAccumulator = 0.0f;

    public DialogScrollText(DialogRenderer dialogRenderer, Minecraft minecraft) {
        this.dialogRenderer = dialogRenderer;
        this.minecraft = minecraft;
        ParsedDialog parsedDialog = ParsedDialog.parse(dialogRenderer.getText());
        setText(parsedDialog.cleanedText());
    }

    public void reset() {
        currentLine = 0;
        currentCharIndex = 0;
        tickAccumulator = 0.0f;
        currentY = 0;
        if (dialogRenderer instanceof DialogRenderer2D) {
            if (lines.size() > 1) {
                currentY -= ((minecraft.font.lineHeight + dialogRenderer.getGap()) * (lines.size() - 1)
                                        + minecraft.font.lineHeight)
                                / 2.0F
                        + 0.7F;
            } else {
                currentY -= minecraft.font.lineHeight / 2.0F;
            }
        } else {
            currentY = -dialogRenderer.getTotalHeight() + dialogRenderer.getPaddingY() + 0.7F;
        }
        currentX = -dialogRenderer.getTotalWidth() + dialogRenderer.getPaddingX() * 2;
        lettersRenderer.clear();
        textEffectAnimation = new TextEffectAnimation(dialogRenderer.getText());
    }

    public void forceFinish() {
        while (!isFinished()) {
            addLetter();
        }
    }

    public void tick() {
        if (!isFinished() && !dialogRenderer.isAnimating()) {
            tickAccumulator += 1.0f;
            populateLetters();
        }
        textEffectAnimation.tick();
    }

    public void render(PoseStack poseStack, MultiBufferSource.BufferSource source, float partialTick) {
        Map<Integer, Vector2f> offsets = textEffectAnimation.getOffsets(partialTick);
        for (int i = 0; i < lettersRenderer.size(); i++) {
            LetterLocation letter = lettersRenderer.get(i);
            if (!letter.render()) continue;
            float x = letter.x;
            float y = letter.y;
            if (offsets.containsKey(i)) {
                x += offsets.get(i).x;
                y += offsets.get(i).y;
            }
            minecraft.font.drawInBatch(
                    String.valueOf(letter.letter),
                    x,
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

    public void render(GuiGraphics guiGraphics, float partialTick) {
        Map<Integer, Vector2f> offsets = textEffectAnimation.getOffsets(partialTick);
        for (int i = 0; i < lettersRenderer.size(); i++) {
            LetterLocation letter = lettersRenderer.get(i);
            if (!letter.render()) continue;
            float x = letter.x;
            float y = letter.y;
            if (dialogRenderer instanceof DialogRenderer3D dialogRenderer3D) {
                if (dialogRenderer3D.getDialogOffset().y < 0) {
                    y += dialogRenderer3D.getTotalHeight() - dialogRenderer3D.getPaddingY();
                }
            }
            if (offsets.containsKey(i)) {
                x += offsets.get(i).x;
                y += offsets.get(i).y;
            }
            ((ICustomGuiRender) guiGraphics)
                    .narrativecraft$drawStringFloat(
                            String.valueOf(letter.letter),
                            minecraft.font,
                            x,
                            y,
                            ARGB.color(255, dialogRenderer.getTextColor()),
                            false);
        }
    }

    public boolean isFinished() {
        return currentLine == lines.size() - 1
                && currentCharIndex == lines.getLast().length();
    }

    public String getLongerTextLine() {
        float longerSentenceWidth = 0;
        String longerText = "";
        for (String line : lines) {
            float width = 0;
            for (int i = 0; i < line.length(); i++) {
                width += Util.getLetterWidth(line.codePointAt(i), minecraft);
            }
            if (width > longerSentenceWidth) {
                longerSentenceWidth = width;
                longerText = line;
            }
        }
        return longerText;
    }

    public void setText(String text) {
        text = text.replace("\n", "");
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
        boolean playSound = false;
        while (tickAccumulator >= currentLine && currentLine < lines.size()) {
            if (addLetter() != ' ') {
                playSound = true;
            }
            tickAccumulator -= clientOption.textSpeed;
        }
        if (playSound) {
            playLetterSound();
        }
    }

    private char addLetter() {
        if (currentLine >= lines.size()) {
            return ' ';
        }

        String currentLineText = lines.get(currentLine);

        if (currentLineText.isEmpty() || currentCharIndex >= currentLineText.length()) {
            if (lines.size() > 1 && currentLine < lines.size() - 1) {
                moveToNextLine();
            }
            return ' ';
        }

        char letter = currentLineText.charAt(currentCharIndex);
        lettersRenderer.add(new LetterLocation(letter, currentX, currentY, true));
        currentX += Util.getLetterWidth(letter, minecraft) + dialogRenderer.getLetterSpacing();
        currentCharIndex++;

        if (currentCharIndex >= currentLineText.length() && lines.size() > 1 && currentLine < lines.size() - 1) {
            moveToNextLine();
        }

        return letter;
    }

    private void moveToNextLine() {
        lettersRenderer.add(new LetterLocation(' ', currentX, currentY, false));
        currentLine++;
        currentCharIndex = 0;
        currentX = -dialogRenderer.getTotalWidth() + dialogRenderer.getPaddingX() * 2;
        currentY += minecraft.font.lineHeight + dialogRenderer.getGap();
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

    private record LetterLocation(char letter, float x, float y, boolean render) {}
}
