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

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.options.NarrativeWorldOption;
import fr.loudo.narrativecraft.util.Translation;
import java.io.File;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldSelectionList.WorldListEntry.class)
public abstract class WorldSelectionListMixin {

    @Shadow
    @Final
    private LevelSummary summary;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    public abstract void joinWorld();

    @Shadow
    @Final
    private Screen screen;

    @Inject(method = "joinWorld", at = @At("HEAD"), cancellable = true)
    private void narrativecraft$joinWorld(CallbackInfo ci) {
        NarrativeWorldOption worldOption = NarrativeCraftFile.getNarrativeCraftWorldVersion(
                this.summary.getLevelId(), SharedConstants.getCurrentVersion().name());
        if (worldOption == null) return;
        if (worldOption.stringMcVersion == null) return;
        if (!SharedConstants.getCurrentVersion().name().equals(worldOption.stringMcVersion)) {
            ConfirmScreen confirmScreen = new ConfirmScreen(
                    b -> {
                        if (b) {
                            File worldOptionFile = NarrativeCraftFile.getWorldOptionFile(this.summary.getLevelId());
                            worldOption.stringMcVersion =
                                    SharedConstants.getCurrentVersion().name();
                            NarrativeCraftFile.updateWorldOptions(worldOptionFile, worldOption);
                            this.joinWorld();
                        } else {
                            minecraft.setScreen(this.screen);
                        }
                    },
                    Component.literal(""),
                    Translation.message("screen.nc_mc_version_incompatible"));
            minecraft.setScreen(confirmScreen);
            ci.cancel();
        }
        if (!NarrativeCraftMod.MAJOR_VERSION.equals(worldOption.ncVersion)) {
            ConfirmScreen confirmScreen = new ConfirmScreen(
                    b -> {
                        if (b) {
                            File worldOptionFile = NarrativeCraftFile.getWorldOptionFile(this.summary.getLevelId());
                            worldOption.ncVersion = NarrativeCraftMod.MAJOR_VERSION;
                            NarrativeCraftFile.updateWorldOptions(worldOptionFile, worldOption);
                            this.joinWorld();
                        } else {
                            minecraft.setScreen(this.screen);
                        }
                    },
                    Component.literal(""),
                    Translation.message(
                            "screen.nc_version_incompatible", worldOption.ncVersion, NarrativeCraftMod.MAJOR_VERSION));
            minecraft.setScreen(confirmScreen);
            ci.cancel();
        }
    }
}
