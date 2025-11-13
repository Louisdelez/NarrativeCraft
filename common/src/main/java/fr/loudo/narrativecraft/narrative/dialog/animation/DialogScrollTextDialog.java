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
import fr.loudo.narrativecraft.narrative.dialog.DialogRenderer;
import fr.loudo.narrativecraft.narrative.dialog.DialogRenderer2D;
import fr.loudo.narrativecraft.narrative.dialog.DialogRenderer3D;
import fr.loudo.narrativecraft.options.NarrativeClientOption;
import fr.loudo.narrativecraft.util.Util;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;
import org.joml.Vector2f;

public class DialogScrollTextDialog extends AbstractDialogScrollText {
    private final DialogRenderer dialogRenderer;

    public DialogScrollTextDialog(DialogRenderer dialogRenderer, Minecraft minecraft) {
        super(minecraft);
        this.dialogRenderer = dialogRenderer;
        setText(dialogRenderer.getText());
        NarrativeClientOption clientOption = NarrativeCraftMod.getInstance().getNarrativeClientOptions();
        this.textSpeed = clientOption.textSpeed;
        this.letterSpacing = dialogRenderer.getLetterSpacing();
        this.gap = dialogRenderer.getGap();
        this.textColor = dialogRenderer.getTextColor();
    }

    @Override
    protected List<String> splitTextIntoLines(String text) {
        return Util.splitText(text, minecraft.font, (int) dialogRenderer.getWidth());
    }

    @Override
    protected float getInitialX() {
        return -dialogRenderer.getTotalWidth() + dialogRenderer.getPaddingX() * 2;
    }

    @Override
    protected float getInitialY() {
        if (dialogRenderer instanceof DialogRenderer2D) {
            if (lines.size() > 1) {
                return -(((minecraft.font.lineHeight + dialogRenderer.getGap()) * (lines.size() - 1)
                                        + minecraft.font.lineHeight)
                                / 2.0F
                        + 0.7F);
            } else {
                return -minecraft.font.lineHeight / 2.0F;
            }
        } else {
            return -dialogRenderer.getTotalHeight() + dialogRenderer.getPaddingY() + 0.7F;
        }
    }

    @Override
    protected boolean canScroll() {
        return !dialogRenderer.isAnimating();
    }

    public void render(PoseStack poseStack, MultiBufferSource.BufferSource source, float partialTick) {
        Map<Integer, Vector2f> offsets = getTextEffectOffsets(partialTick);
        for (int i = 0; i < lettersRenderer.size(); i++) {
            LetterLocation letter = lettersRenderer.get(i);
            if (!letter.render()) continue;
            float x = letter.x();
            float y = letter.y();
            if (offsets.containsKey(i)) {
                x += offsets.get(i).x;
                y += offsets.get(i).y;
            }
            minecraft.font.drawInBatch(
                    String.valueOf(letter.letter()),
                    x,
                    y,
                    FastColor.ARGB32.color(255, textColor),
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
        PoseStack poseStack = guiGraphics.pose();
        Map<Integer, Vector2f> offsets = getTextEffectOffsets(partialTick);
        for (int i = 0; i < lettersRenderer.size(); i++) {
            LetterLocation letter = lettersRenderer.get(i);
            if (!letter.render()) continue;
            float x = letter.x();
            float y = letter.y();
            if (dialogRenderer instanceof DialogRenderer3D dialogRenderer3D) {
                if (dialogRenderer3D.getDialogOffset().y < 0) {
                    y += dialogRenderer3D.getTotalHeight() - dialogRenderer3D.getPaddingY();
                }
            }
            if (offsets.containsKey(i)) {
                x += offsets.get(i).x;
                y += offsets.get(i).y;
            }
            minecraft.font.drawInBatch(
                    String.valueOf(letter.letter()),
                    x,
                    y,
                    FastColor.ARGB32.color(255, textColor),
                    false,
                    poseStack.last().pose(),
                    minecraft.renderBuffers().bufferSource(),
                    Font.DisplayMode.SEE_THROUGH,
                    0,
                    LightTexture.FULL_BRIGHT);
        }
    }
}
