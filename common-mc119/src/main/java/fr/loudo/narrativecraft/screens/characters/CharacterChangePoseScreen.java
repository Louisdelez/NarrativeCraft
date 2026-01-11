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

package fr.loudo.narrativecraft.screens.characters;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.mixin.accessor.EntityAccessor;
import fr.loudo.narrativecraft.mixin.accessor.LivingEntityAccessor;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

public class CharacterChangePoseScreen extends Screen {
    private final Screen lastScreen;
    private final LivingEntity livingEntity;
    private final CharacterStoryData characterStoryData;

    public CharacterChangePoseScreen(Screen lastScreen, CharacterStoryData characterStoryData) {
        super(Component.literal("Change pose camera angle screen"));
        this.livingEntity = characterStoryData.getCharacterRuntime().getEntity();
        this.characterStoryData = characterStoryData;
        this.lastScreen = lastScreen;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
    }

    @Override
    protected void init() {
        // 1.19.x: Pose.SHOOTING doesn't exist, only added in later versions
        List<Pose> poseList = List.of(Pose.STANDING, Pose.CROUCHING, Pose.SLEEPING, Pose.FALL_FLYING);
        int gap = 3;
        int startY = this.height / 2 - gap - 5 * poseList.size() - 20;
        int startX = this.width - 80 - 10;
        for (Pose pose : poseList) {
            Button poseButton = Button.builder(Component.literal(pose.name()), button -> {
                        livingEntity.setPose(pose);
                        characterStoryData.setPose(pose);
                        SynchedEntityData entityData = livingEntity.getEntityData();
                        byte currentMask = entityData.get(EntityAccessor.getDATA_SHARED_FLAGS_ID());
                        byte currentLivingEntityByte =
                                entityData.get(LivingEntityAccessor.getDATA_LIVING_ENTITY_FLAGS());
                        if (pose == Pose.CROUCHING) {
                            entityData.set(EntityAccessor.getDATA_SHARED_FLAGS_ID(), (byte) (currentMask | 0x02));
                            characterStoryData.setEntityByte((byte) (currentMask | 0x02));
                        } else {
                            entityData.set(EntityAccessor.getDATA_SHARED_FLAGS_ID(), (byte) (currentMask & ~0x02));
                            characterStoryData.setEntityByte((byte) (currentMask & ~0x02));
                        }
                        // 1.19.x: Pose.SHOOTING check removed as it doesn't exist in this version
                    })
                    .width(80)
                    .pos(startX, startY)
                    .build();
            startY += poseButton.getHeight() + gap;
            this.addRenderableWidget(poseButton);
        }
        Button closeBtn = Button.builder(Translation.message("global.close"), button -> {
                    this.onClose();
                })
                .width(80)
                .pos(startX, startY)
                .build();
        this.addRenderableWidget(closeBtn);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(PoseStack poseStack) {
        // Empty - no background rendering
    }

    // MC 1.19.x: renderBlurredBackground doesn't exist
}
