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

package fr.loudo.narrativecraft.narrative.story;

import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.mixin.accessor.EntityAccessor;
import fr.loudo.narrativecraft.mixin.accessor.LivingEntityAccessor;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.character.CharacterRuntime;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.dialog.DialogData;
import fr.loudo.narrativecraft.narrative.playback.Playback;
import fr.loudo.narrativecraft.narrative.recording.Location;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;

public class StorySave {
    private transient Chapter chapter;
    private transient Scene scene;
    private final String saveData;
    private final DialogData dialogData;
    private final List<String> tagsRunning;
    private final List<CharacterStoryData> characterStoryDataList = new ArrayList<>();

    public StorySave(PlayerSession playerSession) throws Exception {
        StoryHandler storyHandler = playerSession.getStoryHandler();
        chapter = playerSession.getChapter();
        scene = playerSession.getScene();
        saveData = storyHandler.getStory().getState().toJson();
        dialogData = storyHandler.getDialogData();
        List<InkAction> inkActions = playerSession.getInkActions().stream()
                .filter(InkAction::isRunning)
                .toList();
        tagsRunning =
                new ArrayList<>(inkActions.stream().map(InkAction::getCommand).toList());
        removeForbiddenTagLoadSave(tagsRunning);
        for (CharacterRuntime characterRuntime : playerSession.getCharacterRuntimes()) {
            for (Playback playback : playerSession.getPlaybackManager().getPlaybacks()) {
                if (playback.getCharacter()
                        .getName()
                        .equalsIgnoreCase(characterRuntime.getCharacterStory().getName())) return;
                if (characterRuntime.getEntity() == null) return;
            }
            Entity entity = characterRuntime.getEntity();
            CharacterStoryData characterStoryData = new CharacterStoryData(
                    characterRuntime.getCharacterStory(),
                    new Location(
                            entity.getX(),
                            entity.getY(),
                            entity.getZ(),
                            entity.getXRot(),
                            entity.getYRot(),
                            entity.onGround()),
                    false,
                    scene);

            characterStoryData.setPose(entity.getPose());
            characterStoryData.setItems(characterRuntime.getEntity());
            characterStoryData.setEntityByte(
                    characterRuntime.getEntity().getEntityData().get(EntityAccessor.getDATA_SHARED_FLAGS_ID()));
            characterStoryData.setLivingEntityByte(characterRuntime
                    .getEntity()
                    .getEntityData()
                    .get(LivingEntityAccessor.getDATA_LIVING_ENTITY_FLAGS()));
            characterStoryDataList.add(characterStoryData);
        }
    }

    private void removeForbiddenTagLoadSave(List<String> commands) {
        commands.remove("save");
        commands.remove("on enter");
    }

    public Chapter getChapter() {
        return chapter;
    }

    public Scene getScene() {
        return scene;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public String getSaveData() {
        return saveData;
    }

    public DialogData getDialogData() {
        return dialogData;
    }

    public List<String> getTagsRunning() {
        return tagsRunning;
    }

    public List<CharacterStoryData> getCharacterStoryDataList() {
        return characterStoryDataList;
    }
}
