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

package fr.loudo.narrativecraft.controllers.interaction;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.controllers.AbstractController;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.AreaTrigger;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.interaction.CharacterInteraction;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.interaction.EntityInteraction;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.interaction.Interaction;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.recording.Location;
import fr.loudo.narrativecraft.screens.controller.areaTrigger.AreaTriggerControllerScreen;
import fr.loudo.narrativecraft.screens.controller.interaction.InteractionControllerScreen;
import fr.loudo.narrativecraft.util.Translation;
import fr.loudo.narrativecraft.util.Util;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * MC 1.19.x version of InteractionController.
 * Key difference: Uses getLevel() instead of level().
 */
public class InteractionController extends AbstractController {

    private final Interaction interaction;

    private final List<CharacterInteraction> characterInteractions;
    private final List<EntityInteraction> entityInteractions;
    private final List<AreaTrigger> areaTriggers;

    private AreaTrigger areaTriggerEditing;

    public InteractionController(Environment environment, Player player, Interaction interaction) {
        super(environment, player);
        this.interaction = interaction;
        characterInteractions = new ArrayList<>(interaction.getCharacterInteractions());
        entityInteractions = new ArrayList<>(interaction.getEntityInteractions());
        areaTriggers = new ArrayList<>(interaction.getAreaTriggers());
    }

    public void showAreaTriggers(PoseStack poseStack) {
        if (environment != Environment.DEVELOPMENT) return;
        for (AreaTrigger areaTrigger : areaTriggers) {
            areaTrigger.drawSquareLine(poseStack);
        }
    }

    @Override
    public void startSession() {
        stopCurrentSession();
        hudMessage = Translation.message(
                        "controller.interaction.hud", interaction.getScene().getName())
                .getString();
        playerSession.setController(this);
        for (CharacterInteraction characterInteraction : characterInteractions) {
            characterInteraction
                    .getCharacterStoryData()
                    .spawn(playerSession.getPlayer().getLevel(), environment);
            playerSession
                    .getCharacterRuntimes()
                    .add(characterInteraction.getCharacterStoryData().getCharacterRuntime());
        }
        for (EntityInteraction entityInteraction : entityInteractions) {
            entityInteraction.spawn(playerSession.getPlayer(), environment);
        }
        if (environment != Environment.DEVELOPMENT) return;
        Location locToTp = null;
        if (!interaction.getCharacterInteractions().isEmpty()) {
            locToTp = interaction
                    .getCharacterInteractions()
                    .get(0)
                    .getCharacterStoryData()
                    .getLocation();
        } else if (!interaction.getEntityInteractions().isEmpty()) {
            Vec3 vec3 = interaction.getEntityInteractions().get(0).getPosition();
            locToTp = new Location(vec3.x(), vec3.y(), vec3.z(), 0, 0, false);
        } else if (!areaTriggers.isEmpty()) {
            Vec3 vec3 = areaTriggers.get(0).getPosition1();
            locToTp = new Location(vec3.x(), vec3.y(), vec3.z(), 0, 0, false);
        }
        if (locToTp != null) {
            playerSession.getPlayer().teleportTo(locToTp.x(), locToTp.y(), locToTp.z());
        }
    }

    @Override
    public void stopSession(boolean save) {
        for (CharacterInteraction characterInteraction : characterInteractions) {
            characterInteraction.getCharacterStoryData().kill();
        }
        for (EntityInteraction entityInteraction : entityInteractions) {
            entityInteraction.kill(playerSession.getPlayer());
        }
        playerSession.setController(null);
        if (environment != Environment.DEVELOPMENT) return;
        playerSession.getCharacterRuntimes().clear();
        if (save) {
            List<CharacterInteraction> oldCharacterInteractions = interaction.getCharacterInteractions();
            List<EntityInteraction> oldEntityInteractions = interaction.getEntityInteractions();
            List<AreaTrigger> oldAreaTriggers = interaction.getAreaTriggers();
            try {
                interaction.setCharacterInteractions(characterInteractions);
                interaction.setEntityInteractions(entityInteractions);
                interaction.setAreaTriggers(areaTriggers);
                NarrativeCraftFile.updateInteractionsFile(interaction.getScene());
                playerSession.getPlayer().sendSystemMessage(Translation.message("controller.saved"));
            } catch (Exception e) {
                interaction.setCharacterInteractions(oldCharacterInteractions);
                interaction.setEntityInteractions(oldEntityInteractions);
                interaction.setAreaTriggers(oldAreaTriggers);
                Util.sendCrashMessage(playerSession.getPlayer(), e);
            }
        }
    }

    @Override
    public Screen getControllerScreen() {
        return areaTriggerEditing != null
                ? new AreaTriggerControllerScreen(this, areaTriggerEditing)
                : new InteractionControllerScreen(this);
    }

    public Interaction getInteraction() {
        return interaction;
    }

    public List<EntityInteraction> getEntityInteractions() {
        return entityInteractions;
    }

    public List<CharacterInteraction> getCharacterInteractions() {
        return characterInteractions;
    }

    @Override
    public CharacterStoryData getCharacterStoryDataFromEntity(Entity entity) {
        for (CharacterInteraction characterInteraction : characterInteractions) {
            if (Util.isSameEntity(
                    entity,
                    characterInteraction
                            .getCharacterStoryData()
                            .getCharacterRuntime()
                            .getEntity())) {
                return characterInteraction.getCharacterStoryData();
            }
        }
        return null;
    }

    @Override
    public void removeCharacterStoryData(CharacterStoryData characterStoryData) {
        characterInteractions.removeIf(characterInteraction ->
                characterInteraction.getCharacterStoryData().equals(characterStoryData));
    }

    public EntityInteraction getEntityInteraction(Entity entity) {
        for (EntityInteraction entityInteraction : entityInteractions) {
            if (Util.isSameEntity(entity, entityInteraction.getArmorStand())) {
                return entityInteraction;
            }
        }
        return null;
    }

    public CharacterInteraction getCharacterInteractionFromCharacter(CharacterStoryData characterStoryData) {
        for (CharacterInteraction characterInteraction : characterInteractions) {
            if (characterInteraction.getCharacterStoryData().equals(characterStoryData)) {
                return characterInteraction;
            }
        }
        return null;
    }

    public List<AreaTrigger> getAreaTriggers() {
        return areaTriggers;
    }

    public AreaTrigger getAreaTriggerEditing() {
        return areaTriggerEditing;
    }

    public void setAreaTriggerEditing(AreaTrigger areaTriggerEditing) {
        this.areaTriggerEditing = areaTriggerEditing;
    }

    public boolean areaTriggerExists(String name) {
        for (AreaTrigger areaTrigger : areaTriggers) {
            if (areaTrigger.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
