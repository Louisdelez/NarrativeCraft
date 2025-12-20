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

package fr.loudo.narrativecraft.narrative.story.inkAction.server;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionSet;

public class MinecraftCommandInkAction extends InkAction {

    private String command;

    public MinecraftCommandInkAction(String id, Side side, String syntax, CommandMatcher matcher) {
        super(id, side, syntax, matcher);
    }

    @Override
    protected InkActionResult doValidate(List<String> arguments, Scene scene) {
        if (arguments.size() == 1) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Command value"));
        }
        command = arguments.get(1);
        command = command.replace("\\{", "{");
        command = command.replace("\\}", "}");
        CommandSourceStack commandSourceStack = new CommandSourceStack(
                null, null, null, null, PermissionSet.ALL_PERMISSIONS, null, null, NarrativeCraftMod.server, null);
        ParseResults<CommandSourceStack> parse = NarrativeCraftMod.server
                .getCommands()
                .getDispatcher()
                .parse(new StringReader(command), commandSourceStack);
        if (parse.getReader().canRead()) {
            if (parse.getExceptions().size() == 1) {
                String commandError =
                        parse.getExceptions().values().iterator().next().getMessage();
                return InkActionResult.error(
                        Translation.message(WRONG_ARGUMENT_TEXT, "Command can't be executed: " + commandError));
            }
        }
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(PlayerSession playerSession) {
        CommandSourceStack commandSourceStack = getCommandSourceStack(playerSession);
        command = command.replace("@p", playerSession.getPlayer().getName().getString());
        try {
            playerSession
                    .getPlayer()
                    .level()
                    .getServer()
                    .getCommands()
                    .getDispatcher()
                    .execute(command, commandSourceStack);
        } catch (CommandSyntaxException e) {
            return InkActionResult.error(
                    Translation.message(WRONG_ARGUMENT_TEXT, "Command can't be executed: " + e.getMessage()));
        }
        isRunning = false;
        return InkActionResult.ok();
    }

    private CommandSourceStack getCommandSourceStack(PlayerSession playerSession) {
        ServerPlayer player = playerSession.getPlayer();
        CommandSource commandSource = CommandSource.NULL;
        return new CommandSourceStack(
                commandSource,
                player.position(),
                player.getRotationVector(),
                player.level(),
                PermissionSet.ALL_PERMISSIONS,
                player.getName().getString(),
                player.getDisplayName(),
                player.level().getServer(),
                player);
    }

    @Override
    public boolean needScene() {
        return false;
    }
}
