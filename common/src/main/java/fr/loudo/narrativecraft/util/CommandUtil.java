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

package fr.loudo.narrativecraft.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class CommandUtil {
    public static PlayerSession getSession(CommandContext<CommandSourceStack> context, ServerPlayer player) {

        PlayerSession playerSession =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getSessionByPlayer(player);
        if (!playerSession.isSessionSet()) {
            if (Util.isSamePlayer(context.getSource().getPlayer(), player)) {
                context.getSource().sendFailure(Translation.message("session.not_set"));
            } else {
                context.getSource().sendFailure(Translation.message("session.player_not_set", player.getName()));
            }
            return null;
        }
        return playerSession;
    }

    public static CompletableFuture<Suggestions> getNamesNarrativeEntrySuggestion(
            SuggestionsBuilder builder, List<? extends NarrativeEntry> narrativeEntries) {
        for (NarrativeEntry narrativeEntry : narrativeEntries) {
            if (narrativeEntry.getName().split(" ").length > 1) {
                builder.suggest("\"" + narrativeEntry.getName() + "\"");
            } else {
                builder.suggest(narrativeEntry.getName());
            }
        }
        return builder.buildFuture();
    }
}
