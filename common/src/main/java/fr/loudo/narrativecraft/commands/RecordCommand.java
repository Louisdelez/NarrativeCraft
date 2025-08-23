package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.managers.PlaybackManager;
import fr.loudo.narrativecraft.managers.PlayerSessionManager;
import fr.loudo.narrativecraft.managers.RecordingManager;
import fr.loudo.narrativecraft.narrative.Environnement;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import fr.loudo.narrativecraft.narrative.playback.Playback;
import fr.loudo.narrativecraft.narrative.recording.Recording;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.screens.animation.AnimationCharacterLinkScreen;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecordCommand {
    
    private static final RecordingManager recordingManager = NarrativeCraftMod.getInstance().getRecordingManager();
    private static final PlayerSessionManager playerSessionManager = NarrativeCraftMod.getInstance().getPlayerSessionManager();
    public static final List<ServerPlayer> playerTryingOverride = new ArrayList<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc").requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                .then(Commands.literal("record")
                        .then(Commands.literal("start")
                                .then(Commands.literal("with")
                                        .then(Commands.argument("subscenes", StringArgumentType.greedyString())
//                                                .suggests(NarrativeCraftMod.getInstance().getChapterManager().getSubscenesOfScenesSuggestions())
                                                .executes(commandContext -> {
                                                    String subscenes = StringArgumentType.getString(commandContext, "subscenes");
                                                    return startRecordingWithSubscenes(commandContext, subscenes);
                                                })
                                        )
                                )
                                .executes(RecordCommand::startRecording)
                        )
                        .then(Commands.literal("stop")
                                .executes(RecordCommand::stopRecording)
                        )
                        .then(Commands.literal("save")
                                .then(Commands.argument("animation_name", StringArgumentType.string())
                                        .executes(context -> saveRecording(context, StringArgumentType.getString(context, "animation_name")))
                                )
                        )
                        .then(Commands.literal("test")
                                .executes(context -> {
                                    Animation animation = NarrativeCraftMod.getInstance().getChapterManager().getChapters().getFirst().getScenes().getFirst().getAnimations().getFirst();
                                    Playback playback = new Playback(PlaybackManager.ids.incrementAndGet(), animation, context.getSource().getLevel(), Environnement.RECORDING, false);
                                    playback.start();
                                    NarrativeCraftMod.getInstance().getPlaybackManager().addPlayback(playback);
                                    return 1;
                                })
                        )
                )
        );
    }

    private static int startRecording(CommandContext<CommandSourceStack> context) {

        PlayerSession playerSession = getSession(context);
        if(playerSession == null) return 0;

        ServerPlayer player = context.getSource().getPlayer();

        if(recordingManager.isRecording(player)) {
            context.getSource().sendFailure(Translation.message("record.start.already_recording"));
            return 0;
        }

        Recording recording = recordingManager.getRecording(player);
        if(recording == null) {
            recording = new Recording(context.getSource().getPlayer());
        }
        recording.start();
        recordingManager.addRecording(recording);

        context.getSource().sendSuccess(() -> Translation.message("record.start.success"), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int startRecordingWithSubscenes(CommandContext<CommandSourceStack> context, String subscenes) {

        PlayerSession playerSession = getSession(context);
        if(playerSession == null) return 0;

        ServerPlayer player = context.getSource().getPlayer();

        if(recordingManager.isRecording(player)) {
            context.getSource().sendFailure(Translation.message("record.start.already_recording"));
            return 0;
        }

        List<Subscene> subsceneToPlay = new ArrayList<>();
        subscenes = subscenes.replaceAll("\"", "");
        String[] subsceneNameList = subscenes.split(",");
        for(String subsceneName : subsceneNameList) {
            Subscene subscene = playerSession.getScene().getSubsceneByName(subsceneName);
            if(subscene != null) {
                subsceneToPlay.add(subscene);
            } else {
                context.getSource().sendFailure(Translation.message("subscene.no_exists", subsceneName));
            }
        }

        if(subsceneNameList.length == subsceneToPlay.size()) {
            Recording recording = recordingManager.getRecording(player);
            if(recording == null) {
                recording = new Recording(context.getSource().getPlayer(), subsceneToPlay);
            }
            recording.start();
            recordingManager.addRecording(recording);
            context.getSource().sendSuccess(() -> Translation.message("record.start.with_subscenes", Arrays.toString(subsceneNameList)), true);
        }


        return Command.SINGLE_SUCCESS;
    }


    private static int stopRecording(CommandContext<CommandSourceStack> context) {

        PlayerSession playerSession = getSession(context);
        if(playerSession == null) return 0;

        ServerPlayer player = context.getSource().getPlayer();

        if(!recordingManager.isRecording(player)) {
            context.getSource().sendFailure(Translation.message("record.stop.no_recording"));
            return 0;
        }

        Recording recording = recordingManager.getRecording(player);
        recording.stop();

        context.getSource().sendSuccess(() -> Translation.message("record.stop.success"), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int saveRecording(CommandContext<CommandSourceStack> context, String newAnimationName) {
        PlayerSession playerSession = getSession(context);
        if(playerSession == null) return 0;

        ServerPlayer player = context.getSource().getPlayer();

        Recording recording = recordingManager.getRecording(context.getSource().getPlayer());
        if(recording == null) {
            context.getSource().sendFailure(Translation.message("record.save.recorded_nothing"));
            return 0;
        }

        if(recording.isRecording()) {
            context.getSource().sendFailure(Translation.message("record.save.stop_record_before_save"));
            return 0;
        }

        recording.stop();

        Animation animation = playerSession.getScene().getAnimationByName(newAnimationName);
        // If a player tries to override an animation that already exists
        if (animation != null) {
            if(!playerTryingOverride.contains(player)) {
                playerTryingOverride.add(context.getSource().getPlayer());
                context.getSource().sendFailure(Translation.message("record.save.overwrite", newAnimationName, playerSession.getScene().getName(), playerSession.getChapter().getIndex()));
                return 0;
            } else {
                playerTryingOverride.remove(player);
            }
        } else {
            animation = new Animation(newAnimationName, playerSession.getScene());
            playerTryingOverride.remove(player);
        }
        Animation finalAnimation = animation;
        AnimationCharacterLinkScreen screen = new AnimationCharacterLinkScreen(null, animation, characterStory -> {
            try {
                finalAnimation.setCharacter(characterStory);
                recording.save(finalAnimation);
                context.getSource().sendSuccess(() -> Translation.message("record.save.success", finalAnimation.getName(), playerSession.getScene().getName(), playerSession.getChapter().getIndex()), true);
                recordingManager.removeRecording(recording);
            } catch (IOException e) {
                context.getSource().sendFailure(Translation.message("record.save.fail", finalAnimation.getName(), playerSession.getChapter().getIndex(), playerSession.getScene().getName()));
            }
        });
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(screen));

        return Command.SINGLE_SUCCESS;

    }

    private static PlayerSession getSession(CommandContext<CommandSourceStack> context) {

        PlayerSession playerSession = playerSessionManager.getSessionByPlayer(context.getSource().getPlayer());
        if(!playerSession.isSessionSet()) {
            context.getSource().sendFailure(Translation.message("session.not_set"));
            return null;
        }
        return playerSession;
    }
}