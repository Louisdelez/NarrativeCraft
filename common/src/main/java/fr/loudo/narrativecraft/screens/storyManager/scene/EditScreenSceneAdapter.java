package fr.loudo.narrativecraft.screens.storyManager.scene;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.storyManager.EditScreenAdapter;
import fr.loudo.narrativecraft.util.ScreenUtils;
import fr.loudo.narrativecraft.util.Translation;
import fr.loudo.narrativecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class EditScreenSceneAdapter implements EditScreenAdapter<Scene> {

    private final Chapter chapter;

    public EditScreenSceneAdapter(Chapter chapter) {
        this.chapter = chapter;
    }

    @Override
    public void initExtraFields(EditInfoScreen<Scene> screen, Scene entry) {
        if(entry == null) return;
        ScreenUtils.LabelBox rankBox = new ScreenUtils.LabelBox(
                Translation.message("scene.rank"),
                screen.getFont(),
                40,
                screen.EDIT_BOX_NAME_HEIGHT,
                0,
                0,
                ScreenUtils.Align.HORIZONTAL
        );
        rankBox.getEditBox().setFilter(string -> string.matches("^\\d*$"));
        rankBox.getEditBox().setValue(String.valueOf(entry.getRank()));
        screen.extraFields.putIfAbsent("rank", rankBox);
        screen.extraFields.putIfAbsent("rankEditBox", rankBox.getEditBox());
    }

    @Override
    public void renderExtraFields(EditInfoScreen<Scene> screen, Scene entry, int x, int y) {
        if(entry == null) return;
        ScreenUtils.LabelBox labelBox = (ScreenUtils.LabelBox) screen.extraFields.get("rank");
        labelBox.setPosition(x, y);
        screen.addRenderableWidget(labelBox.getStringWidget());
        screen.addRenderableWidget(labelBox.getEditBox());
    }

    @Override
    public void buildFromScreen(Map<String, Object> extraFields, Minecraft minecraft, @Nullable Scene existing, String name, String description) {

        if(existing == null) {
            Scene scene = new Scene(name, description, chapter);
            try {
                NarrativeCraftFile.createSceneFolder(scene);
                chapter.addScene(scene);
                NarrativeCraftFile.updateInkIncludes();
                minecraft.setScreen(new ScenesScreen(chapter));
            } catch (Exception e) {
                chapter.removeScene(scene);
                Util.sendCrashMessage(minecraft.player, e);
                minecraft.setScreen(null);
            }
        } else {
            Scene newScene = new Scene(name, description, chapter);
            Scene oldScene = new Scene(existing.getName(), existing.getDescription(), chapter);
            oldScene.setRank(existing.getRank());
           try {
               ScreenUtils.LabelBox labelBox = (ScreenUtils.LabelBox) extraFields.get("rank");
               EditBox editBox = labelBox.getEditBox();
               int rank = 1;
               if(!editBox.getValue().isEmpty()) {
                    rank = Integer.parseInt(editBox.getValue());
               }
               if(rank > chapter.getScenes().size()) {
                   ScreenUtils.sendToast(Translation.message("global.error"), Translation.message("scene.rank_above_scenes_size"));
                   return;
               } else if(rank < 1) {
                   ScreenUtils.sendToast(Translation.message("global.error"), Translation.message("scene.rank_no_under_one"));
                   return;
               }
               newScene.setRank(rank); // Only setRank to correctly update data on json file.
               NarrativeCraftFile.updateSceneData(oldScene, newScene);
               existing.setName(name);
               existing.setDescription(description);
               chapter.setSceneRank(existing, rank);
               NarrativeCraftFile.updateInkIncludes();
               minecraft.setScreen(new ScenesScreen(chapter));
           } catch (Exception e) {
               existing.setName(oldScene.getName());
               existing.setDescription(oldScene.getDescription());
               chapter.setSceneRank(existing, oldScene.getRank());
               Util.sendCrashMessage(minecraft.player, e);
               minecraft.setScreen(null);
           }
        }

    }
}
