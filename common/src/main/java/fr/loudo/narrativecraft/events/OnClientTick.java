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

package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.controllers.cutscene.CutsceneController;
import fr.loudo.narrativecraft.narrative.interaction.InteractionEyeRenderer;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.util.NarrativeProfiler;
import java.util.Iterator;
import net.minecraft.client.Minecraft;

public class OnClientTick {

    public static void clientTick(Minecraft minecraft) {
        NarrativeProfiler.start(NarrativeProfiler.TICK_CLIENT);
        try {
            clientTickInternal(minecraft);
        } finally {
            NarrativeProfiler.stop(NarrativeProfiler.TICK_CLIENT);
        }
    }

    private static void clientTickInternal(Minecraft minecraft) {
        if (minecraft.isPaused() && minecraft.isSingleplayer()) return;
        PlayerSession playerSession =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getSessionByPlayer(minecraft.player);
        if (playerSession == null) return;
        if (playerSession.getController() instanceof CutsceneController controller) {
            controller.getCutscenePlayback().tick();
        }
        if (playerSession.getDialogRenderer() != null) {
            NarrativeProfiler.start(NarrativeProfiler.DIALOG);
            playerSession.getDialogRenderer().tick();
            NarrativeProfiler.stop(NarrativeProfiler.DIALOG);
        }

        // T095: Use Iterator to avoid ArrayList allocation for removal
        // Before: List<InkAction> toRemove = new ArrayList<>(); + removeAll()
        // After: Iterator with remove() - zero allocations
        NarrativeProfiler.start(NarrativeProfiler.INK_ACTIONS);
        Iterator<InkAction> iterator = playerSession.getClientSideInkActions().iterator();
        while (iterator.hasNext()) {
            InkAction inkAction = iterator.next();
            inkAction.tick();
            if (!inkAction.isRunning()) {
                iterator.remove();
            }
        }
        NarrativeProfiler.stop(NarrativeProfiler.INK_ACTIONS);

        // Renderer
        playerSession.getStorySaveIconGui().tick();
        InteractionEyeRenderer.tick();
    }
}
