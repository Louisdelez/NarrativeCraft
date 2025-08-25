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
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.managers.PlaybackManager;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import fr.loudo.narrativecraft.narrative.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.util.CommandUtil;
import fr.loudo.narrativecraft.util.Translation;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

// TODO: Play a playback for a specific player
public class PlaybackCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                .then(Commands.literal("playback")
                        .then(Commands.literal("play")
                                .then(Commands.literal("animation")
                                        .then(Commands.argument("animation_name", StringArgumentType.string())
                                                .suggests(PlaybackCommand::getAnimationSuggestion)
                                                .executes(context -> playAnimation(
                                                        context,
                                                        StringArgumentType.getString(context, "animation_name")))))
                                .then(Commands.literal("subscene")
                                        .then(Commands.argument("subscene_name", StringArgumentType.string())
                                                .suggests(PlaybackCommand::getSubscenesSuggestion)
                                                .executes(context -> playSubscene(
                                                        context,
                                                        StringArgumentType.getString(context, "subscene_name"))))))
                        .then(Commands.literal("stop")
                                .then(Commands.literal("animation")
                                        .then(Commands.argument("animation_name", StringArgumentType.string())
                                                .suggests(PlaybackCommand::getAnimationsPlaying)
                                                .executes(context -> stopAnimation(
                                                        context,
                                                        StringArgumentType.getString(context, "animation_name")))))
                                .then(Commands.literal("subscene")
                                        .then(Commands.argument("subscene_name", StringArgumentType.string())
                                                .suggests(PlaybackCommand::getSubscenesPlaying)
                                                .executes(context -> stopSubscene(
                                                        context,
                                                        StringArgumentType.getString(context, "subscene_name"))))))
                        .then(Commands.literal("stop_all").executes(PlaybackCommand::stopAllPlayback))));
    }

    private static int playAnimation(CommandContext<CommandSourceStack> context, String animationName) {
        PlayerSession playerSession =
                CommandUtil.getSession(context, context.getSource().getPlayer());
        if (playerSession == null) return 0;
        Animation animation = playerSession.getScene().getAnimationByName(animationName);
        if (animation == null) {
            context.getSource()
                    .sendFailure(Translation.message(
                            "animation.no_exists",
                            animationName,
                            playerSession.getScene().getName()));
            return 0;
        }
        Playback playback = new Playback(
                PlaybackManager.ids.incrementAndGet(),
                animation,
                context.getSource().getLevel(),
                Environment.RECORDING,
                false);
        playback.start();
        context.getSource()
                .sendSuccess(() -> Translation.message("playback.animation.play", animation.getName()), false);
        NarrativeCraftMod.getInstance().getPlaybackManager().addPlayback(playback);
        return Command.SINGLE_SUCCESS;
    }

    private static int playSubscene(CommandContext<CommandSourceStack> context, String subsceneName) {
        PlayerSession playerSession =
                CommandUtil.getSession(context, context.getSource().getPlayer());
        if (playerSession == null) return 0;
        Subscene subscene = playerSession.getScene().getSubsceneByName(subsceneName);
        if (subscene == null) {
            context.getSource()
                    .sendFailure(Translation.message(
                            "subscene.no_exists",
                            subsceneName,
                            playerSession.getScene().getName()));
            return 0;
        }
        subscene.start(context.getSource().getLevel(), Environment.RECORDING, false);
        NarrativeCraftMod.getInstance().getPlaybackManager().getPlaybacks().addAll(subscene.getPlaybacks());
        context.getSource().sendSuccess(() -> Translation.message("playback.subscene.play", subscene.getName()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int stopAnimation(CommandContext<CommandSourceStack> context, String animationName) {
        PlayerSession playerSession =
                CommandUtil.getSession(context, context.getSource().getPlayer());
        if (playerSession == null) return 0;
        Animation animation = playerSession.getScene().getAnimationByName(animationName);
        if (animation == null) {
            context.getSource()
                    .sendFailure(Translation.message(
                            "animation.no_exists",
                            animationName,
                            playerSession.getScene().getName()));
            return 0;
        }
        List<Playback> playbacks =
                NarrativeCraftMod.getInstance().getPlaybackManager().getAnimationsByNamePlaying(animationName);
        if (playbacks.isEmpty()) {
            context.getSource()
                    .sendFailure(Translation.message(
                            "playback.animation.not_playing",
                            animationName,
                            playerSession.getScene().getName()));
            return 0;
        }
        for (Playback playback : playbacks) {
            playback.stop(true);
        }
        context.getSource()
                .sendSuccess(() -> Translation.message("playback.animation.stop", animation.getName()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int stopSubscene(CommandContext<CommandSourceStack> context, String subsceneName) {
        PlayerSession playerSession =
                CommandUtil.getSession(context, context.getSource().getPlayer());
        if (playerSession == null) return 0;
        Subscene subscene = playerSession.getScene().getSubsceneByName(subsceneName);
        if (subscene == null) {
            context.getSource()
                    .sendFailure(Translation.message(
                            "subscene.no_exists",
                            subsceneName,
                            playerSession.getScene().getName()));
            return 0;
        }
        if (!subscene.isPlaying()) {
            context.getSource()
                    .sendFailure(Translation.message(
                            "playback.subscene.not_playing",
                            subsceneName,
                            playerSession.getScene().getName()));
            return 0;
        }
        subscene.stop(true);
        context.getSource().sendSuccess(() -> Translation.message("playback.subscene.stop", subscene.getName()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int stopAllPlayback(CommandContext<CommandSourceStack> context) {
        PlaybackManager playbackManager = NarrativeCraftMod.getInstance().getPlaybackManager();
        if (playbackManager.getPlaybacksPlaying().isEmpty()) {
            context.getSource().sendFailure(Translation.message("playbacks.not_playing"));
            return 0;
        }
        playbackManager.stopAll();
        context.getSource().sendSuccess(() -> Translation.message("playback.stop_all"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static CompletableFuture<Suggestions> getAnimationSuggestion(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        PlayerSession playerSession = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getSessionByPlayer(context.getSource().getPlayer());
        if (playerSession == null) return builder.buildFuture();
        return CommandUtil.getNamesNarrativeEntrySuggestion(
                builder, playerSession.getScene().getAnimations());
    }

    private static CompletableFuture<Suggestions> getSubscenesSuggestion(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        PlayerSession playerSession = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getSessionByPlayer(context.getSource().getPlayer());
        if (playerSession == null) return builder.buildFuture();
        return CommandUtil.getNamesNarrativeEntrySuggestion(
                builder, playerSession.getScene().getSubscenes());
    }

    private static CompletableFuture<Suggestions> getAnimationsPlaying(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        PlayerSession playerSession = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getSessionByPlayer(context.getSource().getPlayer());
        if (playerSession == null) return builder.buildFuture();
        List<Animation> animationPlaying = new ArrayList<>();
        for (Playback playback :
                NarrativeCraftMod.getInstance().getPlaybackManager().getAnimationsByNamePlaying()) {
            animationPlaying.add(playback.getAnimation());
        }
        return CommandUtil.getNamesNarrativeEntrySuggestion(builder, animationPlaying);
    }

    private static CompletableFuture<Suggestions> getSubscenesPlaying(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        PlayerSession playerSession = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getSessionByPlayer(context.getSource().getPlayer());
        if (playerSession == null) return builder.buildFuture();
        List<Subscene> subscenePlaying = new ArrayList<>();
        for (Subscene subscene : playerSession.getScene().getSubscenes()) {
            if (subscene.isPlaying()) {
                subscenePlaying.add(subscene);
            }
        }
        return CommandUtil.getNamesNarrativeEntrySuggestion(builder, subscenePlaying);
    }
}
