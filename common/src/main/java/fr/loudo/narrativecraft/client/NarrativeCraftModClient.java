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

package fr.loudo.narrativecraft.client;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.client.player.PlayerSessionClient;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.managers.CharacterManager;
import fr.loudo.narrativecraft.options.NarrativeClientOption;
import fr.loudo.narrativecraft.screens.components.NarrativeCraftLogoRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class NarrativeCraftModClient {

    private static final NarrativeCraftModClient INSTANCE = new NarrativeCraftModClient();
    private final RenderType dialogBackgroundRenderType = RenderTypes.textBackgroundSeeThrough();
    private final NarrativeCraftLogoRenderer narrativeCraftLogoRenderer = new NarrativeCraftLogoRenderer(
            Identifier.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "textures/logo.png"));
    private final PlayerSessionClient playerSessionClient = new PlayerSessionClient();
    private final ChapterManager chapterManager = new ChapterManager();
    private final CharacterManager characterManager = new CharacterManager();

    private NarrativeClientOption narrativeClientOptions = new NarrativeClientOption();

    public static NarrativeCraftModClient getInstance() {
        return INSTANCE;
    }

    public RenderType dialogBackgroundRenderType() {
        return dialogBackgroundRenderType;
    }

    public NarrativeCraftLogoRenderer getNarrativeCraftLogoRenderer() {
        return narrativeCraftLogoRenderer;
    }

    public ChapterManager getChapterManager() {
        return chapterManager;
    }

    public CharacterManager getCharacterManager() {
        return characterManager;
    }

    public NarrativeClientOption getNarrativeClientOptions() {
        return narrativeClientOptions;
    }

    public void setNarrativeClientOptions(NarrativeClientOption narrativeClientOptions) {
        this.narrativeClientOptions = narrativeClientOptions;
    }

    public PlayerSessionClient getPlayerSessionClient() {
        return playerSessionClient;
    }
}
