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

package fr.loudo.narrativecraft.screens.options;

import com.mojang.authlib.GameProfile;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.character.CharacterRuntime;
import fr.loudo.narrativecraft.narrative.dialog.DialogData;
import fr.loudo.narrativecraft.narrative.dialog.DialogEntityBobbing;
import fr.loudo.narrativecraft.narrative.dialog.DialogRenderer3D;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.util.FakePlayer;
import fr.loudo.narrativecraft.util.ScreenUtils;
import fr.loudo.narrativecraft.util.Translation;
import fr.loudo.narrativecraft.util.Util;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class DialogCustomOptionsScreen extends Screen {

    private final Screen lastScreen;
    private final PlayerSession playerSession;
    private final DialogData dialogData = new DialogData(DialogData.globalDialogData);
    private FakePlayer fakePlayer;
    private Vec3 lastPos;
    private float lastXRot;
    private float lastYRot;

    private ScreenUtils.LabelBox paddingXBox;
    private ScreenUtils.LabelBox paddingYBox;
    private ScreenUtils.LabelBox scaleBox;
    private ScreenUtils.LabelBox letterSpacingBox;
    private ScreenUtils.LabelBox gapBox;
    private ScreenUtils.LabelBox widthBox;
    private ScreenUtils.LabelBox bcColorBox;
    private ScreenUtils.LabelBox textColorBox;
    private ScreenUtils.LabelBox bobbingSpeed;
    private ScreenUtils.LabelBox bobbingStrength;
    private StringWidget errorWidget;

    public DialogCustomOptionsScreen(Screen lastScreen, PlayerSession playerSession) {
        super(Component.literal("Character Custom Dialog Screen"));
        this.lastScreen = lastScreen;
        this.playerSession = playerSession;
    }

    @Override
    public void onClose() {
        LocalPlayer player = minecraft.player;
        fakePlayer.remove(Entity.RemovalReason.KILLED);
        player.setPos(lastPos);
        player.setXRot(lastXRot);
        player.setYRot(lastYRot);
        player.setYHeadRot(lastYRot);
        playerSession.getPlayer().connection.send(new ClientboundPlayerInfoRemovePacket(List.of(fakePlayer.getUUID())));
        playerSession.getPlayer().connection.send(new ClientboundRemoveEntitiesPacket(fakePlayer.getId()));
        playerSession.setDialogRenderer(null);
        minecraft.setScreen(lastScreen);
        minecraft.options.hideGui = false;
    }

    @Override
    protected void init() {
        minecraft.options.hideGui = true;
        if (fakePlayer == null) {
            LocalPlayer player = minecraft.player;
            Vec3 localPos = player.position();
            lastPos = localPos;
            lastXRot = player.getXRot();
            lastYRot = player.getYRot();
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
            player.setDeltaMovement(0, 0, 0);
            player.setPos(localPos.x, localPos.y + player.getEyeHeight() + 5, localPos.z);
            player.setXRot(0);
            player.setYRot(0);
            player.setYHeadRot(0);
            fakePlayer = new FakePlayer(playerSession.getPlayer().level(), new GameProfile(UUID.randomUUID(), ""));
            fakePlayer.setXRot(0);
            fakePlayer.setYHeadRot(180);
            fakePlayer.setYRot(180);
            fakePlayer.setPos(localPos.x, localPos.y + player.getEyeHeight() + 4.2, localPos.z + 2);
            playerSession
                    .getPlayer()
                    .connection
                    .send(new ClientboundPlayerInfoUpdatePacket(
                            ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
            playerSession.getPlayer().level().addFreshEntity(fakePlayer);
            DialogRenderer3D dialog = new DialogRenderer3D(
                    "Lorem ipsum dolor sit amet consectetur adipiscing elit\n",
                    "",
                    dialogData,
                    new CharacterRuntime(null, null, fakePlayer));
            dialog.setDialogEntityBobbing(new DialogEntityBobbing(
                    dialog, dialogData.getNoiseShakeSpeed(), dialogData.getNoiseShakeStrength()));
            dialog.setNoSkip(true);
            dialog.start();
            playerSession.setDialogRenderer(dialog);
        }
        int gap = 5;
        int labelHeight = 20;
        int labelWidth = 50;
        int currentY;
        int startX = this.width - labelWidth - 120;

        errorWidget = ScreenUtils.text(Component.literal(""), minecraft.font, this.width / 2, 10);
        this.addRenderableWidget(errorWidget);
        currentY = this.height / 2 - ((labelHeight + gap) * 12) / 2;
        paddingXBox = new ScreenUtils.LabelBox(
                Component.literal("Padding X"),
                minecraft.font,
                labelWidth,
                labelHeight,
                startX,
                currentY,
                ScreenUtils.Align.HORIZONTAL);
        paddingXBox.getEditBox().setValue(String.valueOf(dialogData.getPaddingX()));
        this.addRenderableWidget(paddingXBox.getStringWidget());
        this.addRenderableWidget(paddingXBox.getEditBox());
        currentY += paddingXBox.getEditBox().getHeight() + gap;

        paddingYBox = new ScreenUtils.LabelBox(
                Component.literal("Padding Y"),
                minecraft.font,
                labelWidth,
                labelHeight,
                startX,
                currentY,
                ScreenUtils.Align.HORIZONTAL);
        paddingYBox.getEditBox().setValue(String.valueOf(dialogData.getPaddingY()));
        this.addRenderableWidget(paddingYBox.getStringWidget());
        this.addRenderableWidget(paddingYBox.getEditBox());
        currentY += paddingYBox.getEditBox().getHeight() + gap;

        scaleBox = new ScreenUtils.LabelBox(
                Component.literal("Scale"),
                minecraft.font,
                labelWidth,
                labelHeight,
                startX,
                currentY,
                ScreenUtils.Align.HORIZONTAL);
        scaleBox.getEditBox().setValue(String.valueOf(dialogData.getScale()));
        this.addRenderableWidget(scaleBox.getStringWidget());
        this.addRenderableWidget(scaleBox.getEditBox());
        currentY += scaleBox.getEditBox().getHeight() + gap;

        letterSpacingBox = new ScreenUtils.LabelBox(
                Component.literal("Letter Spacing"),
                minecraft.font,
                labelWidth,
                labelHeight,
                startX,
                currentY,
                ScreenUtils.Align.HORIZONTAL);
        letterSpacingBox.getEditBox().setValue(String.valueOf(dialogData.getLetterSpacing()));
        this.addRenderableWidget(letterSpacingBox.getStringWidget());
        this.addRenderableWidget(letterSpacingBox.getEditBox());
        currentY += letterSpacingBox.getEditBox().getHeight() + gap;

        gapBox = new ScreenUtils.LabelBox(
                Component.literal("Gap"),
                minecraft.font,
                labelWidth,
                labelHeight,
                startX,
                currentY,
                ScreenUtils.Align.HORIZONTAL);
        gapBox.getEditBox().setValue(String.valueOf(dialogData.getGap()));
        this.addRenderableWidget(gapBox.getStringWidget());
        this.addRenderableWidget(gapBox.getEditBox());
        currentY += gapBox.getEditBox().getHeight() + gap;

        widthBox = new ScreenUtils.LabelBox(
                Component.literal("Width"),
                minecraft.font,
                labelWidth,
                labelHeight,
                startX,
                currentY,
                ScreenUtils.Align.HORIZONTAL);
        widthBox.getEditBox().setValue(String.valueOf(dialogData.getWidth()));
        this.addRenderableWidget(widthBox.getStringWidget());
        this.addRenderableWidget(widthBox.getEditBox());
        currentY += widthBox.getEditBox().getHeight() + gap;

        bcColorBox = new ScreenUtils.LabelBox(
                Component.literal("Background Color"),
                minecraft.font,
                labelWidth,
                labelHeight,
                startX,
                currentY,
                ScreenUtils.Align.HORIZONTAL);
        bcColorBox.getEditBox().setValue(Integer.toHexString(ARGB.color(0, dialogData.getBackgroundColor())));
        this.addRenderableWidget(bcColorBox.getStringWidget());
        this.addRenderableWidget(bcColorBox.getEditBox());
        currentY += bcColorBox.getEditBox().getHeight() + gap;

        textColorBox = new ScreenUtils.LabelBox(
                Component.literal("Text Color"),
                minecraft.font,
                labelWidth,
                labelHeight,
                startX,
                currentY,
                ScreenUtils.Align.HORIZONTAL);
        textColorBox.getEditBox().setValue(Integer.toHexString(ARGB.color(0, dialogData.getTextColor())));
        this.addRenderableWidget(textColorBox.getStringWidget());
        this.addRenderableWidget(textColorBox.getEditBox());
        currentY += textColorBox.getEditBox().getHeight() + gap;

        bobbingSpeed = new ScreenUtils.LabelBox(
                Component.literal("Bobbing Speed"),
                minecraft.font,
                labelWidth,
                labelHeight,
                startX,
                currentY,
                ScreenUtils.Align.HORIZONTAL);
        bobbingSpeed.getEditBox().setValue(String.valueOf(dialogData.getNoiseShakeSpeed()));
        this.addRenderableWidget(bobbingSpeed.getStringWidget());
        this.addRenderableWidget(bobbingSpeed.getEditBox());
        currentY += bobbingSpeed.getEditBox().getHeight() + gap;

        bobbingStrength = new ScreenUtils.LabelBox(
                Component.literal("Bobbing Strength"),
                minecraft.font,
                labelWidth,
                labelHeight,
                startX,
                currentY,
                ScreenUtils.Align.HORIZONTAL);
        bobbingStrength.getEditBox().setValue(String.valueOf(dialogData.getNoiseShakeStrength()));
        this.addRenderableWidget(bobbingStrength.getStringWidget());
        this.addRenderableWidget(bobbingStrength.getEditBox());
        currentY += bobbingStrength.getEditBox().getHeight() + gap;

        Button updateButton = Button.builder(Translation.message("global.update"), button -> {
                    updateValues();
                })
                .pos(startX, currentY)
                .width(70)
                .build();
        this.addRenderableWidget(updateButton);

        Button doneButton = Button.builder(CommonComponents.GUI_DONE, button -> {
                    try {
                        updateValues();
                        NarrativeCraftFile.updateGlobalDialogValues(dialogData);
                        DialogData.globalDialogData = dialogData;
                    } catch (IOException e) {
                        Util.sendCrashMessage(minecraft.player, e);
                    }
                    onClose();
                })
                .pos(updateButton.getX() + updateButton.getWidth() + 5, currentY)
                .width(70)
                .build();
        this.addRenderableWidget(doneButton);

        currentY += updateButton.getHeight() + gap;

        Button closeButton = Button.builder(Translation.message("global.close"), button -> {
                    updateValues();
                    onClose();
                })
                .pos(startX, currentY)
                .width(70)
                .build();
        this.addRenderableWidget(closeButton);
    }

    private void updateValues() {
        try {
            dialogData.setPaddingX(Float.parseFloat(paddingXBox.getEditBox().getValue()));
            dialogData.setPaddingY(Float.parseFloat(paddingYBox.getEditBox().getValue()));
            dialogData.setScale(Float.parseFloat(scaleBox.getEditBox().getValue()));
            dialogData.setLetterSpacing(
                    Float.parseFloat(letterSpacingBox.getEditBox().getValue()));
            dialogData.setGap(Float.parseFloat(gapBox.getEditBox().getValue()));
            dialogData.setWidth(Float.parseFloat(widthBox.getEditBox().getValue()));
            int backgroundColor = Integer.parseInt(bcColorBox.getEditBox().getValue(), 16);
            dialogData.setBackgroundColor(ARGB.color(255, backgroundColor));
            dialogData.setTextColor(Integer.parseInt(textColorBox.getEditBox().getValue(), 16));
            dialogData.setNoiseShakeSpeed(
                    Float.parseFloat(bobbingSpeed.getEditBox().getValue()));
            dialogData.setNoiseShakeStrength(
                    Float.parseFloat(bobbingStrength.getEditBox().getValue()));
            DialogRenderer3D dialog = (DialogRenderer3D) playerSession.getDialogRenderer();
            dialog.setText(dialog.getText());
            dialog.setPaddingX(dialogData.getPaddingX());
            dialog.setPaddingY(dialogData.getPaddingY());
            dialog.setScale(dialogData.getScale());
            dialog.setLetterSpacing(dialogData.getLetterSpacing());
            dialog.setGap(dialogData.getGap());
            dialog.setWidth(dialogData.getWidth());
            dialog.setBackgroundColor(dialogData.getBackgroundColor());
            dialog.setTextColor(dialogData.getTextColor());
            dialog.getDialogEntityBobbing().setNoiseShakeSpeed(dialogData.getNoiseShakeSpeed());
            dialog.getDialogEntityBobbing().setNoiseShakeStrength(dialogData.getNoiseShakeStrength());
            dialog.update();
            errorWidget.setMessage(Component.empty());
        } catch (NumberFormatException e) {
            Component message = Component.literal(e.getMessage()).withStyle(ChatFormatting.RED);
            errorWidget.setMessage(message);
            errorWidget.setPosition(this.width / 2 - minecraft.font.width(message) / 2, 10);
            errorWidget.setWidth(minecraft.font.width(message));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    protected void renderBlurredBackground(GuiGraphics guiGraphics) {}
}
