package fr.loudo.narrativecraft.screens.storyManager.character;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.managers.CharacterManager;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterType;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.storyManager.EditScreenAdapter;
import fr.loudo.narrativecraft.util.ScreenUtils;
import fr.loudo.narrativecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.Map;

public class EditScreenCharacterAdapter implements EditScreenAdapter<CharacterStory> {
    @Override
    public void initExtraFields(EditInfoScreen<CharacterStory> screen, CharacterStory entry) {
        LocalDate localDate = LocalDate.now();
        ScreenUtils.LabelBox dayLabelBox = new ScreenUtils.LabelBox(
                Component.literal("Day"),
                screen.getFont(),
                20,
                20,
                0,
                0,
                ScreenUtils.Align.HORIZONTAL
        );
        screen.extraFields.put("day", dayLabelBox);
        screen.extraFields.put("dayBox", dayLabelBox.getEditBox());
        dayLabelBox.getEditBox().setFilter(string -> string.matches("^\\d*$"));
        dayLabelBox.getEditBox().setValue(String.valueOf(localDate.getDayOfMonth()));
        ScreenUtils.LabelBox monthLabelBox = new ScreenUtils.LabelBox(
                Component.literal("Month"),
                screen.getFont(),
                20,
                20,
                0,
                0,
                ScreenUtils.Align.HORIZONTAL
        );
        monthLabelBox.getEditBox().setFilter(string -> string.matches("^\\d*$"));
        monthLabelBox.getEditBox().setValue(String.valueOf(localDate.getMonthValue()));
        screen.extraFields.put("month", monthLabelBox);
        ScreenUtils.LabelBox yearLabelBox = new ScreenUtils.LabelBox(
                Component.literal("Year"),
                screen.getFont(),
                32,
                20,
                0,
                0,
                ScreenUtils.Align.HORIZONTAL
        );
        yearLabelBox.getEditBox().setFilter(string -> string.matches("^\\d*$"));
        yearLabelBox.getEditBox().setValue("2000");
        screen.extraFields.put("year", yearLabelBox);

        Button modelButton = Button.builder(Component.literal(PlayerSkin.Model.WIDE.name()), button -> {
            String currentModel = button.getMessage().getString();

            if(currentModel.equalsIgnoreCase(PlayerSkin.Model.WIDE.name())) {
                button.setMessage(Component.literal(PlayerSkin.Model.SLIM.name()));
            } else {
                button.setMessage(Component.literal(PlayerSkin.Model.WIDE.name()));
            }
        }).width(70).build();
        screen.extraFields.put("modelBtn", modelButton);
        if(entry != null) {
            String[] birthDateSplit = entry.getBirthDate().split("/");
            try {
                dayLabelBox.getEditBox().setValue(birthDateSplit[0]);
                monthLabelBox.getEditBox().setValue(birthDateSplit[1]);
                yearLabelBox.getEditBox().setValue(birthDateSplit[2]);
            } catch (Exception e) {
                Util.sendCrashMessage(Minecraft.getInstance().player, e);
                Minecraft.getInstance().setScreen(null);
            }
            modelButton.setMessage(Component.literal(entry.getModel().name()));
        }
    }

    @Override
    public void renderExtraFields(EditInfoScreen<CharacterStory> screen, CharacterStory entry, int x, int y) {
        ScreenUtils.LabelBox dayLabelBox = (ScreenUtils.LabelBox) screen.extraFields.get("day");
        dayLabelBox.setPosition(x, y);
        screen.addRenderableWidget(dayLabelBox.getEditBox());
        screen.addRenderableWidget(dayLabelBox.getStringWidget());

        ScreenUtils.LabelBox monthLabelBox = (ScreenUtils.LabelBox) screen.extraFields.get("month");
        monthLabelBox.setPosition( dayLabelBox.getEditBox().getX() + dayLabelBox.getEditBox().getWidth() + 10, y);
        screen.addRenderableWidget(monthLabelBox.getEditBox());
        screen.addRenderableWidget(monthLabelBox.getStringWidget());

        ScreenUtils.LabelBox yearLabelBox = (ScreenUtils.LabelBox) screen.extraFields.get("year");
        yearLabelBox.setPosition( monthLabelBox.getEditBox().getX() + monthLabelBox.getEditBox().getWidth() + 10, y);
        screen.addRenderableWidget(yearLabelBox.getEditBox());
        screen.addRenderableWidget(yearLabelBox.getStringWidget());

        y += yearLabelBox.getEditBox().getHeight() + screen.GAP;

        Button modelButton = (Button) screen.extraFields.get("modelBtn");
        Component label = Component.literal("Model");
        StringWidget modelText = ScreenUtils.text(label, screen.getFont(), x, y + modelButton.getHeight() / 2 - screen.getFont().lineHeight / 2);
        screen.addRenderableWidget(modelText);
        modelButton.setPosition(x + modelText.getWidth() + 5, y);
        screen.addRenderableWidget(modelButton);
    }

    @Override
    public void buildFromScreen(Map<String, Object> extraFields, Minecraft minecraft, @Nullable CharacterStory existing, String name, String description) {
        CharacterManager characterManager = NarrativeCraftMod.getInstance().getCharacterManager();
        String day = ((ScreenUtils.LabelBox)extraFields.get("day")).getEditBox().getValue();
        String month = ((ScreenUtils.LabelBox)extraFields.get("month")).getEditBox().getValue();
        String year = ((ScreenUtils.LabelBox)extraFields.get("year")).getEditBox().getValue();
        PlayerSkin.Model model = PlayerSkin.Model.valueOf(((Button)extraFields.get("modelBtn")).getMessage().getString());
        CharacterStory newCharacter = new CharacterStory(name, description, day, month, year, model, CharacterType.MAIN);
        if(existing == null) {
            try {
                NarrativeCraftFile.createCharacterFolder(newCharacter);
                characterManager.addCharacter(newCharacter);
                minecraft.setScreen(new CharactersScreen());
            } catch (Exception e) {
                Util.sendCrashMessage(minecraft.player, e);
                minecraft.setScreen(null);
            }
        } else {
            try {
                NarrativeCraftFile.updateCharacterData(existing, newCharacter);
                characterManager.removeCharacter(existing);
                characterManager.addCharacter(newCharacter);
                minecraft.setScreen(new CharactersScreen());
            } catch (Exception e) {
                Util.sendCrashMessage(minecraft.player, e);
                minecraft.setScreen(null);
            }
        }
    }
}
