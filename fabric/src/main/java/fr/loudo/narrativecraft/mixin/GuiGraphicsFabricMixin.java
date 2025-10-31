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

package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.gui.Fill2dGui;
import fr.loudo.narrativecraft.gui.ICustomGuiRender;
import fr.loudo.narrativecraft.gui.IGuiTextAccessor;
import fr.loudo.narrativecraft.gui.SkipArrow2dGui;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.GuiTextRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsFabricMixin implements ICustomGuiRender {

    @Shadow
    @Final
    private GuiRenderState guiRenderState;

    @Shadow
    @Final
    private Matrix3x2fStack pose;

    @Shadow
    @Final
    private GuiGraphics.ScissorStack scissorStack;

    @Override
    public void narrativecraft$drawDialogSkip(float width, float height, int color) {
        this.guiRenderState.submitGuiElement(new SkipArrow2dGui(
                RenderPipelines.GUI,
                TextureSetup.noTexture(),
                new Matrix3x2f(pose),
                width,
                height,
                color,
                this.scissorStack.peek()));
    }

    @Override
    public void narrativecraft$fill(float x1, float y1, float x2, float y2, int color) {
        this.guiRenderState.submitGuiElement(new Fill2dGui(
                RenderPipelines.GUI,
                TextureSetup.noTexture(),
                new Matrix3x2f(pose),
                x1,
                y1,
                x2,
                y2,
                color,
                this.scissorStack.peek()));
    }

    @Override
    public void narrativecraft$drawStringFloat(
            String text, Font font, float x, float y, int color, boolean drawShadow) {
        if (ARGB.alpha(color) != 0) {
            GuiTextRenderState guiTextRenderState = new GuiTextRenderState(
                    font,
                    Language.getInstance().getVisualOrder(FormattedText.of(text)),
                    new Matrix3x2f(this.pose),
                    (int) x,
                    (int) y,
                    color,
                    0,
                    drawShadow,
                    this.scissorStack.peek());
            ((IGuiTextAccessor) (Object) guiTextRenderState).narrativecraft$setFloatX(x);
            ((IGuiTextAccessor) (Object) guiTextRenderState).narrativecraft$setFloatY(y);
            this.guiRenderState.submitText(guiTextRenderState);
        }
    }

    @Override
    public void narrativecraft$drawStringFloat(
            Component text, Font font, float x, float y, int color, boolean drawShadow) {
        if (ARGB.alpha(color) != 0) {
            GuiTextRenderState guiTextRenderState = new GuiTextRenderState(
                    font,
                    text.getVisualOrderText(),
                    new Matrix3x2f(this.pose),
                    (int) x,
                    (int) y,
                    color,
                    0,
                    drawShadow,
                    this.scissorStack.peek());
            ((IGuiTextAccessor) (Object) guiTextRenderState).narrativecraft$setFloatX(x);
            ((IGuiTextAccessor) (Object) guiTextRenderState).narrativecraft$setFloatY(y);
            this.guiRenderState.submitText(guiTextRenderState);
        }
    }
}
