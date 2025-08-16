package fr.loudo.narrativecraft.narrative.chapter.scenes.interaction;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;

public class InteractionController extends KeyframeControllerBase {

    private final Interaction interaction;

    public InteractionController(Interaction interaction, ServerPlayer player, Playback.PlaybackType playbackType) {
        super(player, playbackType);
        this.interaction = interaction;
    }

    @Override
    protected void initOldData() {}

    @Override
    public void startSession() {
        PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
        KeyframeControllerBase keyframeControllerBase = playerSession.getKeyframeControllerBase();
        if(keyframeControllerBase != null) {
            keyframeControllerBase.stopSession(false);
        }
        playerSession.setKeyframeControllerBase(this);

        if(interaction instanceof ObjectInteraction objectInteraction) {
            Vec3 position = objectInteraction.getPosition();
            player.teleportTo(position.x, position.y, position.z);
        } else if(interaction instanceof CharacterInteraction characterInteraction) {
            characterInteraction.getCharacterData().spawn(Utils.getServerLevel());
            if(characterInteraction.getCharacterData() != null) {
                Vec3 position = characterInteraction.getCharacterData().getVec3();
                player.teleportTo(position.x, position.y, position.z);
            }
        } else if (interaction instanceof AnimationInteraction animationInteraction) {
            if(animationInteraction.getAnimation() != null) {
                Vec3 position = animationInteraction.getAnimation().getActionsData().getFirst().getMovementData().getFirst().getVec3();
                player.teleportTo(position.x, position.y, position.z);
            }
        }
    }

    @Override
    public void stopSession(boolean save) {
        PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
        playerSession.setKeyframeControllerBase(null);
        NarrativeCraftFile.updateInteractionsFile(interaction.getScene());
        if(interaction instanceof CharacterInteraction characterInteraction) {
            characterInteraction.getCharacterData().getCharacterStory().kill();
        }
    }

    @Override
    public void addKeyframe() {}

    @Override
    public void removeKeyframe(Keyframe keyframe) {}

    @Override
    public void renderHUDInfo(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        String infoText = Translation.message("interaction.hud", interaction.getType().name()).getString();
        int width = minecraft.getWindow().getGuiScaledWidth();
        guiGraphics.drawString(
                font,
                infoText,
                width / 2 - font.width(infoText) / 2,
                10,
                ARGB.colorFromFloat(1, 1, 1, 1)
        );
    }

    @Override
    protected void hideKeyframes() {}

    @Override
    protected void revealKeyframes() {}

    public Interaction getInteraction() {
        return interaction;
    }
}
