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
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.StoryValidation;
import fr.loudo.narrativecraft.util.ErrorLine;
import fr.loudo.narrativecraft.util.Translation;
import fr.loudo.narrativecraft.util.Util;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class StoryCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                .then(Commands.literal("story")
                        .then(Commands.literal("validate").executes(StoryCommand::validateStory))
                        .then(Commands.literal("play")
                                .then(Commands.argument("chapter_index", IntegerArgumentType.integer())
                                        .suggests(NarrativeCraftMod.getInstance()
                                                .getChapterManager()
                                                .getChapterSuggestions())
                                        .then(Commands.argument("scene_name", StringArgumentType.string())
                                                .suggests(NarrativeCraftMod.getInstance()
                                                        .getChapterManager()
                                                        .getSceneSuggestionsByChapter())
                                                .then(Commands.argument("debug", BoolArgumentType.bool())
                                                        .executes(context -> playStoryChapterStory(
                                                                context,
                                                                IntegerArgumentType.getInteger(
                                                                        context, "chapter_index"),
                                                                StringArgumentType.getString(context, "scene_name"),
                                                                BoolArgumentType.getBool(context, "debug")))))))
                        .then(Commands.literal("stop").executes(StoryCommand::stopStory))));
    }

    private static int validateStory(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(Translation.message("validation.validating").withStyle(ChatFormatting.YELLOW));
        try {
            List<ErrorLine> results = StoryValidation.validate();
            List<ErrorLine> warnLines =
                    results.stream().filter(ErrorLine::isWarn).toList();
            List<ErrorLine> errorLines =
                    results.stream().filter(errorLine -> !errorLine.isWarn()).toList();
            if (errorLines.isEmpty() && warnLines.isEmpty()) {
                player.sendSystemMessage(
                        Translation.message("validation.validated").withStyle(ChatFormatting.GREEN));
                player.sendSystemMessage(Component.empty());
                return Command.SINGLE_SUCCESS;
            }
            for (ErrorLine errorLine : results) {
                player.sendSystemMessage(errorLine.toMessage());
            }
            if (!errorLines.isEmpty()) {
                player.sendSystemMessage(Translation.message(
                                "validation.found_errors",
                                Component.literal(String.valueOf(errorLines.size()))
                                        .withStyle(ChatFormatting.GOLD))
                        .withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.empty());
            }
            if (!warnLines.isEmpty()) {
                player.sendSystemMessage(Translation.message(
                                "validation.found_warns",
                                Component.literal(String.valueOf(warnLines.size()))
                                        .withStyle(ChatFormatting.GOLD))
                        .withStyle(ChatFormatting.YELLOW));
            }
            return errorLines.isEmpty() ? 1 : 0;
        } catch (Exception e) {
            Util.sendCrashMessage(player, e);
        }
        player.sendSystemMessage(Component.empty());

        return Command.SINGLE_SUCCESS;
    }

    private static int playStoryChapterStory(
            CommandContext<CommandSourceStack> context, int chapterIndex, String sceneName, boolean debug) {

        PlayerSession playerSession = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getSessionByPlayer(context.getSource().getPlayer());
        if (playerSession == null) return 0;
        if (!NarrativeCraftFile.getStoryFile().exists()) {
            context.getSource().sendFailure(Translation.message("story.no_exists"));
            return 0;
        }
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getChapterByIndex(chapterIndex);
        if (chapter == null) {
            context.getSource().sendFailure(Translation.message("chapter.no_exists", chapterIndex));
            return 0;
        }
        Scene scene = chapter.getSceneByName(sceneName);
        if (scene == null) {
            context.getSource().sendFailure(Translation.message("scene.no_exists", sceneName, chapterIndex));
            return 0;
        }
        if (validateStory(context) == 0) return 0;
        if (playerSession.getStoryHandler() != null) {
            playerSession.getStoryHandler().stop();
        }
        StoryHandler storyHandler = new StoryHandler(chapter, scene, playerSession);
        storyHandler.setDebugMode(debug);
        storyHandler.start();

        return Command.SINGLE_SUCCESS;
    }

    private static int stopStory(CommandContext<CommandSourceStack> context) {
        if (!context.getSource().hasPermission(2)) return 0;

        PlayerSession playerSession = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getSessionByPlayer(context.getSource().getPlayer());
        if (playerSession == null) return 0;
        StoryHandler storyHandler = playerSession.getStoryHandler();
        if (storyHandler == null) {
            context.getSource().sendFailure(Translation.message("story.not_playing"));
            return 0;
        }

        storyHandler.stop();
        context.getSource().sendSuccess(() -> Translation.message("story.stopped"), false);

        return Command.SINGLE_SUCCESS;
    }
}
