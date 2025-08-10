package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.items.CutsceneEditItems;
import fr.loudo.narrativecraft.keys.ModKeys;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.options.NarrativeWorldOption;
import fr.loudo.narrativecraft.screens.mainScreen.MainScreen;
import fr.loudo.narrativecraft.utils.ConstantsLink;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.net.URI;

public class OnPlayerServerConnection {

    public static void playerJoin(ServerPlayer player) {
        if(player instanceof FakePlayer) return;
        NarrativeCraftMod.playingOnIncompatibleWorld = false;
        Minecraft minecraft = Minecraft.getInstance();
        CutsceneEditItems.init(player.registryAccess());
        NarrativeCraftMod.getInstance().setNarrativeWorldOption(NarrativeCraftFile.loadWorldOptions());
        NarrativeCraftMod.getInstance().setNarrativeClientOptions(NarrativeCraftFile.loadUserOptions());
        if(NarrativeCraftMod.firstTime) {
            MutableComponent inkyLink = Component.literal("Inky").withStyle(style ->
                    style.withColor(ChatFormatting.YELLOW)
                            .withUnderlined(true)
                            .withClickEvent(new ClickEvent.OpenUrl(URI.create(ConstantsLink.INKY)))
            );
            MutableComponent docLink = Component.literal(ConstantsLink.DOCS).withStyle(style ->
                    style.withUnderlined(true).
                            withClickEvent(new ClickEvent.OpenUrl(URI.create(ConstantsLink.DOCS)))
            );
            MutableComponent discordLink = Component.literal("discord").withStyle(style ->
                    style.withColor(ChatFormatting.BLUE)
                            .withUnderlined(true)
                            .withClickEvent(new ClickEvent.OpenUrl(URI.create(ConstantsLink.DISCORD)))
            );
            player.sendSystemMessage(Translation.message("user.first_time",
                    ModKeys.OPEN_STORY_MANAGER.getDefaultKey().getDisplayName(),
                    inkyLink,
                    docLink,
                    discordLink
            ));
        } else {
            if(!NarrativeCraftFile.getStoryFile().exists()) return;
            NarrativeWorldOption worldOption = NarrativeCraftMod.getInstance().getNarrativeWorldOption();
            if(!worldOption.stringMcVersion.isEmpty() && !minecraft.getVersionType().equals(worldOption.stringMcVersion)) {
                ConfirmScreen confirmScreen = new ConfirmScreen(b -> {
                    if(b) {
                        showMainScreen(worldOption, minecraft);
                        NarrativeCraftMod.playingOnIncompatibleWorld = true;
                    } else {
                        Utils.disconnectPlayer(minecraft);
                    }
                }, Component.literal(""), Translation.message("screen.incompatible-version", worldOption.stringMcVersion, minecraft.getVersionType()),
                        CommonComponents.GUI_YES, CommonComponents.GUI_CANCEL);
                minecraft.setScreen(confirmScreen);
            } else {
                showMainScreen(worldOption, minecraft);
            }
        }
    }

    private static void showMainScreen(NarrativeWorldOption worldOption, Minecraft minecraft) {
        if(worldOption.showMainScreen) {
            MainScreen mainScreen = new MainScreen(false, false);
            minecraft.execute(() -> Minecraft.getInstance().setScreen(mainScreen));
        }
    }

    public static void playerLeave(ServerPlayer player) {
        if(player instanceof FakePlayer) return;
        PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
        KeyframeControllerBase keyframeControllerBase = playerSession.getKeyframeControllerBase();
        if(keyframeControllerBase != null) {
            keyframeControllerBase.stopSession(true);
        }
        Recording recording = NarrativeCraftMod.getInstance().getRecordingHandler().getRecordingOfPlayer(player);
        if(recording != null) {
            recording.stop();
        }
        StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        if(storyHandler != null) {
            storyHandler.stop(true);
        }
        NarrativeCraftFile.updateWorldOptions(NarrativeCraftMod.getInstance().getNarrativeWorldOption());
    }

}
