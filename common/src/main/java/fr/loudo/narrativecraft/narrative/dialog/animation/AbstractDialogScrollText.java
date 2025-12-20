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

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.story.text.ParsedDialog;
import fr.loudo.narrativecraft.narrative.story.text.TextEffectAnimation;
import fr.loudo.narrativecraft.util.Util;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import org.joml.Vector2f;

public abstract class AbstractDialogScrollText {
    protected final Minecraft minecraft;
    protected final List<LetterLocation> lettersRenderer = new ArrayList<>();
    protected TextEffectAnimation textEffectAnimation;
    protected List<String> lines = new ArrayList<>();
    protected int currentLine;
    protected int currentCharIndex;
    protected int index;
    protected float currentX;
    protected float currentY;
    protected float tickAccumulator = 0.0f;
    protected Identifier letterSound =
            Identifier.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "sfx.dialog_sound");

    protected int textColor = 0xFFFFFF;
    protected float textSpeed = 1.0f;
    protected float letterSpacing = 0.0f;
    protected float gap = 0.0f;
    protected boolean muteSound;

    public AbstractDialogScrollText(Minecraft minecraft) {
        this.minecraft = minecraft;
        textSpeed = NarrativeCraftMod.getInstance().getNarrativeClientOptions().textSpeed;
    }

    public void setText(String text) {
        text = text.replace("\n", "").trim();
        ParsedDialog parsedDialog = ParsedDialog.parse(text);
        lines = splitTextIntoLines(parsedDialog.cleanedText());
        textEffectAnimation = new TextEffectAnimation(parsedDialog);
    }

    protected abstract List<String> splitTextIntoLines(String text);

    protected abstract float getInitialX();

    protected abstract float getInitialY();

    public void reset() {
        currentLine = 0;
        currentCharIndex = 0;
        index = 0;
        tickAccumulator = 0.0f;
        currentX = getInitialX();
        currentY = getInitialY();
        lettersRenderer.clear();
    }

    public void forceFinish() {
        if (lines.isEmpty()) return;
        while (!isFinished()) {
            addLetter();
        }
    }

    public void tick() {
        if (lines.isEmpty()) return;
        if (textEffectAnimation != null) {
            textEffectAnimation.tick();
            if (!textEffectAnimation.canTick(index)) return;
        }

        if (!isFinished() && canScroll()) {
            tickAccumulator += 1.0f;
            populateLetters();
        }
    }

    protected abstract boolean canScroll();

    protected void populateLetters() {
        boolean playSound = false;
        while (tickAccumulator >= textSpeed && currentLine < lines.size()) {
            char letter = addLetter();
            if (letter != ' ') {
                playSound = true;
            }
            tickAccumulator -= textSpeed;
        }
        if (playSound && !muteSound) {
            playLetterSound();
        }
    }

    protected char addLetter() {
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
        currentX += Util.getLetterWidth(letter, minecraft) + letterSpacing;
        currentCharIndex++;

        if (currentCharIndex >= currentLineText.length() && lines.size() > 1 && currentLine < lines.size() - 1) {
            moveToNextLine();
        }
        index++;

        return letter;
    }

    protected void moveToNextLine() {
        lettersRenderer.add(new LetterLocation(' ', currentX, currentY, false));
        currentLine++;
        currentCharIndex = 0;
        currentX = getInitialX();
        currentY += minecraft.font.lineHeight + gap;
    }

    protected void playLetterSound() {
        float pitch = 0.8F + new Random().nextFloat() * 0.4F;
        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(letterSound);
        minecraft.player.playSound(soundEvent, 1.0F, pitch);
    }

    public boolean isFinished() {
        return currentLine == lines.size() - 1
                && currentCharIndex == lines.getLast().length();
    }

    public String getLongerTextLine() {
        return Util.getLongerTextLine(lines, minecraft);
    }

    public List<String> getLines() {
        return lines;
    }

    public Map<Integer, Vector2f> getTextEffectOffsets(float partialTick) {
        if (textEffectAnimation != null) {
            return textEffectAnimation.getOffsets(partialTick);
        }
        return Collections.emptyMap();
    }

    public List<LetterLocation> getLettersRenderer() {
        return lettersRenderer;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public float getTextSpeed() {
        return textSpeed;
    }

    public void setTextSpeed(float textSpeed) {
        this.textSpeed = textSpeed;
    }

    public void setLetterSpacing(float letterSpacing) {
        this.letterSpacing = letterSpacing;
    }

    public void setGap(float gap) {
        this.gap = gap;
    }

    public boolean isMuteSound() {
        return muteSound;
    }

    public void setMuteSound(boolean muteSound) {
        this.muteSound = muteSound;
    }

    public Identifier getLetterSound() {
        return letterSound;
    }

    public void setLetterSound(Identifier letterSound) {
        this.letterSound = letterSound;
    }

    public record LetterLocation(char letter, float x, float y, boolean render) {}
}
