package fr.loudo.narrativecraft.narrative.dialog.animations;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.gui.ICustomGuiRender;
import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import fr.loudo.narrativecraft.narrative.dialog.Dialog2d;
import fr.loudo.narrativecraft.narrative.story.text.TextEffectAnimation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.DeltaTracker;
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
import org.joml.Matrix3x2fStack;
import org.joml.Random;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DialogAnimationScrollText {

    private Dialog dialog;
    private Dialog2d dialog2d;

    private int maxWidth;
    private List<String> lines;
    private int currentLetter, totalLetters;
    private String text;
    private float letterSpacing;
    private float gap, totalHeight, maxLineWidth;
    private TextEffectAnimation textEffectAnimation;
    private long lastTimeChar;

    public DialogAnimationScrollText(String text, float letterSpacing, float gap, int maxWidth, Dialog dialog) {
        this.dialog = dialog;
        commonInit(text, letterSpacing, gap, maxWidth);
    }

    public DialogAnimationScrollText(String text, float letterSpacing, float gap, int maxWidth, Dialog2d dialog2d) {
        this.dialog2d = dialog2d;
        commonInit(text, letterSpacing, gap, maxWidth);
    }

    private void commonInit(String text, float letterSpacing, float gap, int maxWidth) {
        this.text = text;
        this.letterSpacing = letterSpacing;
        this.gap = gap;
        this.maxWidth = maxWidth;
        this.lines = splitText(text);
        this.currentLetter = 0;
        totalLetters = lines.stream().mapToInt(String::length).sum();
        init();
    }

    public void init() {
        Minecraft client = Minecraft.getInstance();
        maxLineWidth = 0;
        for (String line : lines) {
            float lineWidth = client.font.width(line) + (line.length() - 1) * letterSpacing;
            maxLineWidth = Math.max(maxLineWidth, lineWidth);
        }
        totalHeight = 0;
        for (String text : lines) {
            if(lines.size() > 1) {
                totalHeight += gap;
            } else {
                totalHeight += client.font.lineHeight;
            }
        }
        if(lines.size() > 1) {
            totalHeight -= gap - client.font.lineHeight;
        }
    }

    public void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource) {
        Minecraft client = Minecraft.getInstance();
        int shownLetters = 0;
        totalLetters = lines.stream().mapToInt(String::length).sum();
        float currentY = -getTotalHeight() + dialog.getPaddingY() * 2 + 0.7f;

        int globalCharIndex = 0;

        for (int i = 0; i < lines.size(); i++) {
            String text = lines.get(i);
            int lineLength = text.length();
            int lineVisibleLetters = Math.max(0, Math.min(lineLength, currentLetter - shownLetters));
            shownLetters += lineLength;

            float textWidth = client.font.width(text) + letterSpacing * (lineLength - 1);
            float startX = textWidth == maxLineWidth ? -textWidth / 2.0F : -maxLineWidth / 2.0F;

            Map<Integer, Vector2f> letterOffsets = textEffectAnimation.getOffsets();

            for (int j = 0; j < lineVisibleLetters; j++) {
                Vector2f offset = letterOffsets.getOrDefault(globalCharIndex, new Vector2f(0, 0));
                String character = String.valueOf(text.charAt(j));
                drawStringPoseStack(character, poseStack, bufferSource, startX + offset.x, currentY + offset.y);

                startX += Utils.getLetterWidth(text.codePointAt(j)) + letterSpacing;

                globalCharIndex++;
            }

            globalCharIndex++;
            currentY += lines.size() > 1 ? gap : client.font.lineHeight;
        }

        playScrollSound();

        dialog.getDialogEntityBobbing().updateLookDirection(client.getDeltaTracker().getGameTimeDeltaPartialTick(true));

        bufferSource.endBatch();
    }

    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, float scale) {
        Minecraft client = Minecraft.getInstance();
        int shownLetters = 0;
        totalLetters = lines.stream().mapToInt(String::length).sum();
        int windowWidth = client.getWindow().getGuiScaledWidth();
        int windowHeight = client.getWindow().getGuiScaledHeight();

        int offsetDialog = dialog2d.getOffset();
        int guiScale = client.options.guiScale().get();
        switch (guiScale) {
            case 1: offsetDialog *= 4;
            case 2: offsetDialog *= 2;
        }
        float maxY = windowHeight - offsetDialog;
        float rectHeight = dialog2d.getHeight() + dialog2d.getPaddingY() * 2;
        float minY = maxY - rectHeight;

        Font font = client.font;
        float textHeight = lines.size() * (font.lineHeight * scale);
        float currentY = minY + (rectHeight - textHeight) / 2f;

        int globalCharIndex = 0;

        Matrix3x2fStack poseStack = guiGraphics.pose();

        for (int i = 0; i < lines.size(); i++) {
            String text = lines.get(i);
            int lineLength = text.length();
            int lineVisibleLetters = Math.max(0, Math.min(lineLength, currentLetter - shownLetters));
            shownLetters += lineLength;

            float lineWidth = font.width(text) * scale;
            float startX = (windowWidth - lineWidth) / 2f;

            Map<Integer, Vector2f> letterOffsets = textEffectAnimation.getOffsets();

            for (int j = 0; j < lineVisibleLetters; j++) {
                Vector2f offset = letterOffsets.getOrDefault(globalCharIndex, new Vector2f(0, 0));
                String character = String.valueOf(text.charAt(j));
                poseStack.pushMatrix();
                poseStack.scale(scale, scale);
                drawStringGui(
                        guiGraphics,
                        deltaTracker,
                        character,
                        (startX + offset.x) / scale,
                        (currentY + offset.y) / scale,
                        dialog2d.getTextColor()
                );
                poseStack.popMatrix();
                startX += (Utils.getLetterWidth(text.codePointAt(j)) + letterSpacing) * scale;

                globalCharIndex++;
            }

            currentY += lines.size() > 1 ? gap * scale : client.font.lineHeight * scale;
        }
        playScrollSound();

    }

    private void playScrollSound() {
        long now = System.currentTimeMillis();
        Minecraft client = Minecraft.getInstance();
        if (currentLetter < totalLetters && now - lastTimeChar >= NarrativeCraftMod.getInstance().getNarrativeClientOptions().textSpeed && !client.isPaused()) {
            char nextChar = getCharAtIndex(currentLetter);
            if (nextChar != ' ') {
                ResourceLocation soundRes = ResourceLocation.withDefaultNamespace("sfx.dialog_sound");
                SoundEvent sound = SoundEvent.createVariableRangeEvent(soundRes);
                float pitch = 0.8F + new Random().nextFloat() * 0.4F;
                client.player.playSound(sound, 1.0F, pitch);
            }
            currentLetter++;
            lastTimeChar = now;
        }
    }

    private char getCharAtIndex(int index) {
        int count = 0;
        for (String line : lines) {
            if (index < count + line.length()) {
                return line.charAt(index - count);
            }
            count += line.length();
        }
        return ' ';
    }

    private void drawStringPoseStack(String character, PoseStack poseStack, MultiBufferSource bufferSource, float x, float y) {
        Minecraft client = Minecraft.getInstance();

        int color = dialog.getTextDialogColor();

        client.font.drawInBatch(
                character,
                x,
                y,
                ARGB.color(255, color),
                false,
                poseStack.last().pose(),
                bufferSource,
                Font.DisplayMode.SEE_THROUGH,
                0,
                LightTexture.FULL_BRIGHT
        );

    }

    private void drawStringGui(GuiGraphics guiGraphics, DeltaTracker deltaTracker, String text, float x, float y, int color) {
        Minecraft client = Minecraft.getInstance();
        ((ICustomGuiRender)guiGraphics).drawStringFloat(
                text,
                client.font,
                x,
                y,
                ARGB.color(255, color),
                false
        );
    }

    private List<String> splitText(String text) {

        List<String> finalString = new ArrayList<>();
        Minecraft client = Minecraft.getInstance();
        List<FormattedCharSequence> charSequences = client.font.split(FormattedText.of(text), maxWidth);
        for(FormattedCharSequence chara : charSequences) {
            StringBuilder stringBuilder = new StringBuilder();
            chara.accept((i, style, i1) -> {
                stringBuilder.appendCodePoint(i1);
                return true;
            });
            finalString.add(stringBuilder.toString());
        }
        return finalString;
    }

    public boolean isFinished() {
        return currentLetter == lines.stream().mapToInt(String::length).sum();
    }

    public void forceFinish() {
        currentLetter = lines.stream().mapToInt(String::length).sum();
    }

    public float getMaxWidthLine() {
        return maxLineWidth;
    }

    public void setMaxLineWidth(float maxLineWidth) {
        this.maxLineWidth = maxLineWidth;
    }

    public void reset() {
        currentLetter = 0;
        init();
    }

    public List<String> getLines() {
        return lines;
    }

    public void setText(String text) {
        this.text = text;
        this.lines = splitText(text);
    }

    public float getLetterSpacing() {
        return letterSpacing;
    }

    public void setLetterSpacing(float letterSpacing) {
        this.letterSpacing = letterSpacing;
    }

    public float getGap() {
        return gap;
    }

    public void setGap(float gap) {
        this.gap = gap;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public float getTotalHeight() {
        return totalHeight + 4 * dialog.getPaddingY();
    }

    public void setTotalHeight(float totalHeight) {
        this.totalHeight = totalHeight;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public void loadTextEffects(String rawText) {
        textEffectAnimation = new TextEffectAnimation(rawText);
    }
}
