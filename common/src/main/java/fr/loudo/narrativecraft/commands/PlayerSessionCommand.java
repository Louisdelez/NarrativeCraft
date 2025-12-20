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

package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

public class PlayerSessionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .requires(commandSourceStack ->
                        commandSourceStack.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                .then(Commands.literal("session")
                        .then(Commands.literal("clear").executes(PlayerSessionCommand::clearSession))
                        .then(Commands.literal("set")
                                .then(Commands.argument("chapter_index", IntegerArgumentType.integer())
                                        .suggests(NarrativeCraftMod.getInstance()
                                                .getChapterManager()
                                                .getChapterSuggestions())
                                        .then(Commands.argument("scene_name", StringArgumentType.string())
                                                .suggests(NarrativeCraftMod.getInstance()
                                                        .getChapterManager()
                                                        .getSceneSuggestionsByChapter())
                                                .executes(context -> setSession(
                                                        context,
                                                        IntegerArgumentType.getInteger(context, "chapter_index"),
                                                        StringArgumentType.getString(context, "scene_name"))))))));
    }

    private static int setSession(CommandContext<CommandSourceStack> context, int chapterIndex, String sceneName) {

        if (!NarrativeCraftMod.getInstance().getChapterManager().chapterExists(chapterIndex)) {
            context.getSource().sendFailure(Translation.message("chapter.no_exists", chapterIndex));
            return 0;
        }

        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getChapterByIndex(chapterIndex);

        if (!chapter.sceneExists(sceneName)) {
            context.getSource().sendFailure(Translation.message("scene.no_exists", sceneName, chapterIndex));
            return 0;
        }

        Scene scene = chapter.getSceneByName(sceneName);
        PlayerSession playerSession = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getSessionByPlayer(context.getSource().getPlayer());
        playerSession.setChapter(chapter);
        playerSession.setScene(scene);
        context.getSource()
                .sendSuccess(() -> Translation.message("session.set", chapter.getIndex(), scene.getName()), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int clearSession(CommandContext<CommandSourceStack> context) {

        ServerPlayer player = context.getSource().getPlayer();

        PlayerSession playerSession =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getSessionByPlayer(player);
        playerSession.reset();
        context.getSource().sendSuccess(() -> Translation.message("session.cleared"), false);

        return Command.SINGLE_SUCCESS;
    }
}
