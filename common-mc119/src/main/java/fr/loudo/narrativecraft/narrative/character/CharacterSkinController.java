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

package fr.loudo.narrativecraft.narrative.character;

import com.google.common.io.Files;
import com.mojang.blaze3d.platform.NativeImage;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.compat.api.NcId;
import fr.loudo.narrativecraft.compat.api.VersionAdapterLoader;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.util.Util;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;

/**
 * MC 1.20.x version of CharacterSkinController.
 * Key differences from 1.21.x:
 * - DynamicTexture constructor takes only NativeImage (no lambda/name parameter)
 */
public class CharacterSkinController {

    private final Scene scene;
    private final CharacterRuntime characterRuntime;
    private final List<String> cachedSkins;
    private String skinName;
    private List<File> skins;
    private File currentSkin;

    public CharacterSkinController(CharacterRuntime characterRuntime, String skinName, Scene scene) {
        this.characterRuntime = characterRuntime;
        this.skinName = skinName;
        this.scene = scene;
        initSkins();
        cachedSkins = new ArrayList<>();
    }

    private void initSkins() {
        if (characterRuntime.getCharacterStory() != null) {
            if (characterRuntime.getCharacterStory().getCharacterType() == CharacterType.MAIN) {
                skins = NarrativeCraftFile.getSkinFiles(characterRuntime.getCharacterStory());
            } else if (characterRuntime.getCharacterStory().getCharacterType() == CharacterType.NPC) {
                File mainSkin = NarrativeCraftFile.getSkinFile(characterRuntime.getCharacterStory(), scene);
                skins = new ArrayList<>();
                if (mainSkin.exists()) {
                    skins.add(mainSkin);
                }
            }
        }
    }

    /**
     * MC 1.20.x: DynamicTexture constructor takes only NativeImage.
     * No lambda or name parameter.
     */
    public void cacheSkins() {
        if (characterRuntime.getCharacterStory() == null) return;
        Minecraft minecraft = Minecraft.getInstance();
        unCacheSkins();
        initSkins();
        for (File skin : skins) {
            String path = "character/"
                    + Util.snakeCase(characterRuntime.getCharacterStory().getName()) + "/"
                    + Util.snakeCase(skin.getName());
            minecraft.execute(() -> {
                try {
                    byte[] array = Files.toByteArray(skin);
                    NativeImage nativeImage = NativeImage.read(array);
                    // MC 1.20.x: DynamicTexture constructor takes only NativeImage
                    DynamicTexture texture = new DynamicTexture(nativeImage);
                    NcId ncId = NcId.of(NarrativeCraftMod.MOD_ID, path);
                    VersionAdapterLoader.getAdapter().getUtilCompat().registerTexture(ncId, texture);
                } catch (IOException ignored) {
                }
            });
            cachedSkins.add(path);
        }
        currentSkin = skinName == null || skinName.isEmpty() ? getMainSkinFile() : getSkinFile(skinName);
    }

    public void unCacheSkins() {
        Minecraft minecraft = Minecraft.getInstance();
        for (String path : cachedSkins) {
            minecraft.execute(() -> {
                NcId ncId = NcId.of(NarrativeCraftMod.MOD_ID, path);
                VersionAdapterLoader.getAdapter().getUtilCompat().releaseTexture(ncId);
            });
        }
        cachedSkins.clear();
    }

    public File getMainSkinFile() {
        for (File skin : skins) {
            if (skin.getName().equals("main.png")) {
                return skin;
            }
        }
        return null;
    }

    public File getSkinFile(String name) {
        for (File skin : skins) {
            if (skin.getName().equals(name)) {
                return skin;
            }
        }
        return null;
    }

    public List<File> getSkins() {
        return skins;
    }

    public File getCurrentSkin() {
        return currentSkin;
    }

    public List<String> getCachedSkins() {
        return cachedSkins;
    }

    public void setCurrentSkin(File skinFile) {
        this.currentSkin = skinFile;
    }

    public String getSkinName() {
        return skinName;
    }

    public void setSkinName(String skinName) {
        this.skinName = skinName;
    }
}
