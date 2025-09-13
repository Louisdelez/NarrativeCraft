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
import fr.loudo.narrativecraft.api.inkAction.InkActionRegistry;
import fr.loudo.narrativecraft.narrative.character.CharacterRuntime;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.dialog.DialogRenderer;
import fr.loudo.narrativecraft.narrative.dialog.DialogRenderer3D;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.inkAction.SoundInkAction;
import fr.loudo.narrativecraft.network.OpenChaptersScreenPacket;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.util.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class OpenScreenCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                .then(Commands.literal("open")
                        .then(Commands.literal("story_manager").executes(OpenScreenCommand::openStoryManager))
                        .then(Commands.literal("character_manager").executes(OpenScreenCommand::openCharacterManager))
                        .then(Commands.literal("story_options").executes(OpenScreenCommand::openStoryOptions))
                        .then(Commands.literal("main_screen").executes(OpenScreenCommand::openMainScreen))
                        .then(Commands.literal("dialog").executes(OpenScreenCommand::dialog))
                        .then(Commands.literal("dialogUpdate").executes(OpenScreenCommand::updateDialog))
                        .then(Commands.literal("dialogStop").executes(OpenScreenCommand::stopDialog))
                        .then(Commands.literal("test2").executes(OpenScreenCommand::test2))));
    }

    private static int dialog(CommandContext<CommandSourceStack> context) {
        PlayerSession playerSession = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getSessionByPlayer(context.getSource().getPlayer());
        CharacterStory characterStory = NarrativeCraftMod.getInstance()
                .getCharacterManager()
                .getCharacterStories()
                .getFirst();
        LivingEntity entity = Util.createEntityFromCharacter(
                characterStory, context.getSource().getLevel());
        Vec3 pos = context.getSource().getPosition();
        entity.teleportTo(pos.x, pos.y, pos.z);
        CharacterRuntime characterRuntime = new CharacterRuntime(characterStory, entity);
        DialogRenderer3D dialogRenderer3D = new DialogRenderer3D(
                "Oh, hello!", "", characterRuntime, new Vec2(0, 0.8F), 90, 5, 10, 0.6F, 0, 0, 0, -1);
        dialogRenderer3D.start();
        playerSession.setDialogRenderer(dialogRenderer3D);
        return Command.SINGLE_SUCCESS;
    }

    private static int updateDialog(CommandContext<CommandSourceStack> context) {
        PlayerSession playerSession = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getSessionByPlayer(context.getSource().getPlayer());
        DialogRenderer dialogRenderer = playerSession.getDialogRenderer();
        dialogRenderer.setWidth(25);
        dialogRenderer.update();
        return Command.SINGLE_SUCCESS;
    }

    private static int stopDialog(CommandContext<CommandSourceStack> context) {
        PlayerSession playerSession = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getSessionByPlayer(context.getSource().getPlayer());
        DialogRenderer dialogRenderer = playerSession.getDialogRenderer();
        dialogRenderer.stop();
        return Command.SINGLE_SUCCESS;
    }

    private static int test2(CommandContext<CommandSourceStack> context) {
        PlayerSession playerSession = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getSessionByPlayer(context.getSource().getPlayer());
        SoundInkAction soundInkAction = (SoundInkAction) InkActionRegistry.get("sound");
        soundInkAction.validateAndExecute("sound stop all", playerSession);
        return Command.SINGLE_SUCCESS;
    }

    private static int openStoryOptions(CommandContext<CommandSourceStack> context) {

        return Command.SINGLE_SUCCESS;
    }

    private static int openStoryManager(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        Services.PACKET_SENDER.sendToPlayer(player, new OpenChaptersScreenPacket("caca", 13));
        return Command.SINGLE_SUCCESS;
    }

    private static int openCharacterManager(CommandContext<CommandSourceStack> context) {
        //        CharactersScreen screen = new CharactersScreen();
        //        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(screen));
        return Command.SINGLE_SUCCESS;
    }

    private static int openMainScreen(CommandContext<CommandSourceStack> context) {
        //        MainScreen screen = new MainScreen(false, false);
        //        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(screen));
        return Command.SINGLE_SUCCESS;
    }
}
