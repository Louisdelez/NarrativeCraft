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
import fr.loudo.narrativecraft.commands.RecordCommand;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.items.CutsceneEditItems;
import fr.loudo.narrativecraft.managers.PlayerSessionManager;
import fr.loudo.narrativecraft.managers.RecordingManager;
import fr.loudo.narrativecraft.narrative.NarrativeEntryInit;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import fr.loudo.narrativecraft.narrative.recording.Recording;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.network.storyDataSyncs.*;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.util.FakePlayer;
import fr.loudo.narrativecraft.util.Translation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

public class OnPlayerServerConnection {

    public static void playerJoin(ServerPlayer player) {
        if (player instanceof FakePlayer) return;
        initSession(player);
        NarrativeCraftMod.getInstance().setNarrativeWorldOption(NarrativeCraftFile.loadWorldOptions());
        //        NarrativeCraftMod.getInstance().setNarrativeClientOptions(NarrativeCraftFile.loadUserOptions());
        loadStoryDataToClient(player);
        if (player.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) {
            if (NarrativeEntryInit.hasError) {
                player.sendSystemMessage(
                        Translation.message("crash.narrative-data").withStyle(ChatFormatting.RED));
            }
        }
        CutsceneEditItems.init(player.registryAccess());
        PlayerSession playerSession =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getSessionByPlayer(player);
        // TODO: Move this on client side OR send packet
        //        if (NarrativeCraftMod.getInstance().getNarrativeWorldOption().showMainScreen) {
        //            MainScreen mainScreen = new MainScreen(playerSession, false, false);
        //            Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(mainScreen));
        //        }
    }

    public static void playerLeave(ServerPlayer player) {
        if (player instanceof FakePlayer) return;
        clearSession(player);
        RecordCommand.playerTryingOverride.remove(player);
        // If player is recording while leaving, then stop it and remove recording from manager.
        RecordingManager recordingManager = NarrativeCraftMod.getInstance().getRecordingManager();
        Recording recording = recordingManager.getRecording(player);
        if (recording != null && recording.isRecording()) {
            recording.stop();
        }
        recordingManager.removeRecording(recording);
    }

    private static void initSession(ServerPlayer player) {
        PlayerSession playerSession = new PlayerSession(player);
        NarrativeCraftMod.getInstance().getPlayerSessionManager().addSession(playerSession);
    }

    private static void clearSession(ServerPlayer player) {
        PlayerSessionManager playerSessionManager =
                NarrativeCraftMod.getInstance().getPlayerSessionManager();
        PlayerSession playerSession = playerSessionManager.getSessionByPlayer(player);
        if (playerSession == null) return;
        playerSessionManager.removeSession(playerSession);
    }

    private static void loadStoryDataToClient(ServerPlayer player) {
        Services.PACKET_SENDER.sendToPlayer(
                player,
                new S2CSyncCharactersPacket(
                        NarrativeCraftMod.getInstance().getCharacterManager().getCharacterStories()));
        List<Chapter> chapters =
                NarrativeCraftMod.getInstance().getChapterManager().getChapters();
        Services.PACKET_SENDER.sendToPlayer(player, new S2CSyncChaptersPacket(chapters));
        for (Chapter chapter : chapters) {
            Services.PACKET_SENDER.sendToPlayer(
                    player, new S2CSyncScenesPacket(chapter.getIndex(), chapter.getScenes()));
            for (Scene scene : chapter.getScenes()) {
                Services.PACKET_SENDER.sendToPlayer(
                        player,
                        new S2CSyncAnimationsPacket(chapter.getIndex(), scene.getName(), scene.getAnimations()));
                Services.PACKET_SENDER.sendToPlayer(
                        player,
                        new S2CSyncCameraAnglesPacket(chapter.getIndex(), scene.getName(), scene.getCameraAngles()));
                Services.PACKET_SENDER.sendToPlayer(
                        player, new S2CSyncSubscenesPacket(chapter.getIndex(), scene.getName(), scene.getSubscenes()));
                Services.PACKET_SENDER.sendToPlayer(
                        player, new S2CSyncCutscenesPacket(chapter.getIndex(), scene.getName(), scene.getCutscenes()));
                Services.PACKET_SENDER.sendToPlayer(
                        player,
                        new S2CSyncInteractionsPacket(chapter.getIndex(), scene.getName(), scene.getInteractions()));
                Services.PACKET_SENDER.sendToPlayer(
                        player, new S2CSyncNpcsPacket(chapter.getIndex(), scene.getName(), scene.getNpcs()));

                // Sync links (animations for subscenes, subscenes/animations for cutscenes)
                Map<String, List<String>> subsceneAnimations = new HashMap<>();
                for (Subscene subscene : scene.getSubscenes()) {
                    subsceneAnimations.put(subscene.getName(), subscene.getAnimationsName());
                }

                Map<String, List<String>> cutsceneSubscenes = new HashMap<>();
                Map<String, List<String>> cutsceneAnimations = new HashMap<>();
                for (Cutscene cutscene : scene.getCutscenes()) {
                    cutsceneSubscenes.put(cutscene.getName(), cutscene.getSubscenesName());
                    cutsceneAnimations.put(cutscene.getName(), cutscene.getAnimationsName());
                }

                Map<String, String> animationCharacters = new HashMap<>();
                for (Animation animation : scene.getAnimations()) {
                    if (animation.getCharacter() != null) {
                        animationCharacters.put(
                                animation.getName(), animation.getCharacter().getName());
                    }
                }

                Services.PACKET_SENDER.sendToPlayer(
                        player,
                        new S2CLinksSyncPacket(
                                chapter.getIndex(),
                                scene.getName(),
                                subsceneAnimations,
                                cutsceneSubscenes,
                                cutsceneAnimations,
                                animationCharacters));
            }
        }
    }
}
