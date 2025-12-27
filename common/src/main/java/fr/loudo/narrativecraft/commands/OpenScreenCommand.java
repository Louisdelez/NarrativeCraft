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
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.network.screen.S2CScreenPacket;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.util.Translation;
// import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.Permissions;

public class OpenScreenCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .requires(commandSourceStack ->
                        commandSourceStack.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                .then(Commands.literal("open")
                        .then(Commands.literal("story_manager").executes(OpenScreenCommand::openStoryManager))
                        .then(Commands.literal("character_manager").executes(OpenScreenCommand::openCharacterManager))
                        .then(Commands.literal("story_options").executes(OpenScreenCommand::openStoryOptions))
                        .then(Commands.literal("main_screen").executes(OpenScreenCommand::openMainScreen))));
    }

    private static int openStoryOptions(CommandContext<CommandSourceStack> context) {
        PlayerSession playerSession = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getSessionByPlayer(context.getSource().getPlayer());
        if (playerSession.getStoryHandler() != null) return 0;
        if (playerSession.getController() != null) {
            playerSession.getPlayer().sendSystemMessage(Translation.message("session.controller_set"));
            return 0;
        }
        if (NarrativeCraftMod.getInstance().getRecordingManager().isRecording(playerSession.getPlayer())) {
            playerSession.getPlayer().sendSystemMessage(Translation.message("record.cant_access"));
            return 0;
        }
        //        StoryOptionsScreen screen = new StoryOptionsScreen(playerSession);
        //        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(screen));
        return Command.SINGLE_SUCCESS;
    }

    private static int openStoryManager(CommandContext<CommandSourceStack> context) {
        PlayerSession playerSession = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getSessionByPlayer(context.getSource().getPlayer());
        if (playerSession.getController() != null) {
            playerSession.getPlayer().sendSystemMessage(Translation.message("session.controller_set"));
            return 0;
        }
        if (NarrativeCraftMod.getInstance().getRecordingManager().isRecording(playerSession.getPlayer())) {
            playerSession.getPlayer().sendSystemMessage(Translation.message("record.cant_access"));
            return 0;
        }
        if (playerSession.getStoryHandler() != null) return 0;
        Services.PACKET_SENDER.sendToPlayer(context.getSource().getPlayer(), S2CScreenPacket.storyManager());

        return Command.SINGLE_SUCCESS;
    }

    private static int openCharacterManager(CommandContext<CommandSourceStack> context) {
        PlayerSession playerSession = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getSessionByPlayer(context.getSource().getPlayer());
        if (playerSession.getController() != null) {
            playerSession.getPlayer().sendSystemMessage(Translation.message("session.controller_set"));
            return 0;
        }
        if (NarrativeCraftMod.getInstance().getRecordingManager().isRecording(playerSession.getPlayer())) {
            playerSession.getPlayer().sendSystemMessage(Translation.message("record.cant_access"));
            return 0;
        }
        Services.PACKET_SENDER.sendToPlayer(context.getSource().getPlayer(), S2CScreenPacket.characterManager());
        return Command.SINGLE_SUCCESS;
    }

    private static int openMainScreen(CommandContext<CommandSourceStack> context) {
        PlayerSession playerSession = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getSessionByPlayer(context.getSource().getPlayer());
        if (playerSession.getStoryHandler() != null) return 0;
        if (playerSession.getController() != null) {
            playerSession.getPlayer().sendSystemMessage(Translation.message("session.controller_set"));
            return 0;
        }
        if (NarrativeCraftMod.getInstance().getRecordingManager().isRecording(playerSession.getPlayer())) {
            playerSession.getPlayer().sendSystemMessage(Translation.message("record.cant_access"));
            return 0;
        }
        //        MainScreen screen = new MainScreen(playerSession, false, false);
        //        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(screen));
        return Command.SINGLE_SUCCESS;
    }
}
