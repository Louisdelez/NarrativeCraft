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
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class StoryCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                .then(Commands.literal("story")
                        .then(Commands.literal("validate").executes(StoryCommand::executeValidateStory))
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

        //        Minecraft minecraft = Minecraft.getInstance();
        //        LocalPlayer localPlayer = minecraft.player;
        //
        //        List<ErrorLine> errorLineList = StoryHandler.validateStory();
        //        for(ErrorLine errorLine : errorLineList) {
        //            localPlayer.displayClientMessage(errorLine.toMessage(), false);
        //        }
        //
        //        int errorSize = errorLineList.stream().filter(errorLine -> !errorLine.isWarn()).toList().size();
        //        int warnSize = errorLineList.stream().filter(ErrorLine::isWarn).toList().size();
        //        if(errorSize > 0) {
        //            localPlayer.displayClientMessage((Translation.message("validation.found_errors",
        // Component.literal(String.valueOf(errorSize)).withColor(ChatFormatting.GOLD.getColor())).withColor(ChatFormatting.RED.getColor())), false);
        //        }
        //        if(warnSize > 0) {
        //            localPlayer.displayClientMessage((Translation.message("validation.found_warns",
        // Component.literal(String.valueOf(warnSize)).withColor(ChatFormatting.GOLD.getColor())).withColor(ChatFormatting.YELLOW.getColor())), false);
        //        }
        //
        //        return errorSize > 0 ? 0 : Command.SINGLE_SUCCESS;
        return 1;
    }

    private static int executeValidateStory(CommandContext<CommandSourceStack> context) {

        if (validateStory(context) == 0) return 0;
        context.getSource()
                .sendSystemMessage(
                        Translation.message("validation.no_errors").withColor(ChatFormatting.GREEN.getColor()));

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
            context.getSource().sendFailure(Translation.message("story.load.no_story_playing"));
            return 0;
        }

        storyHandler.stop();
        context.getSource().sendSuccess(() -> Component.literal("Story stopped."), false);

        return Command.SINGLE_SUCCESS;
    }
}
