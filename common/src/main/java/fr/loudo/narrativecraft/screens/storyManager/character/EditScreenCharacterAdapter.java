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

package fr.loudo.narrativecraft.screens.storyManager.character;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.managers.CharacterManager;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.character.CharacterModel;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.MainCharacterAttribute;
import fr.loudo.narrativecraft.network.data.BiCharacterDataPacket;
import fr.loudo.narrativecraft.network.data.BiNpcDataPacket;
import fr.loudo.narrativecraft.network.data.TypeStoryData;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.screens.characters.CharacterAdvancedScreen;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.storyManager.EditScreenAdapter;
import fr.loudo.narrativecraft.util.ScreenUtils;
import fr.loudo.narrativecraft.util.Translation;
import fr.loudo.narrativecraft.util.Util;
import java.time.LocalDate;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class EditScreenCharacterAdapter implements EditScreenAdapter<CharacterStory> {

    private final Scene scene;
    private MainCharacterAttribute attribute;

    public EditScreenCharacterAdapter(Scene scene) {
        this.scene = scene;
    }

    @Override
    public void initExtraFields(EditInfoScreen<CharacterStory> screen, CharacterStory entry) {
        if (scene == null) {
            LocalDate localDate = LocalDate.now();
            ScreenUtils.LabelBox dayLabelBox = new ScreenUtils.LabelBox(
                    Component.literal("Day"), screen.getFont(), 20, 20, 0, 0, ScreenUtils.Align.HORIZONTAL);
            screen.extraFields.put("day", dayLabelBox);
            screen.extraFields.put("dayBox", dayLabelBox.getEditBox());
            dayLabelBox.getEditBox().setFilter(string -> string.matches(Util.REGEX_INT));
            dayLabelBox.getEditBox().setValue(String.valueOf(localDate.getDayOfMonth()));

            ScreenUtils.LabelBox monthLabelBox = new ScreenUtils.LabelBox(
                    Component.literal("Month"), screen.getFont(), 20, 20, 0, 0, ScreenUtils.Align.HORIZONTAL);
            monthLabelBox.getEditBox().setFilter(string -> string.matches(Util.REGEX_INT));
            monthLabelBox.getEditBox().setValue(String.valueOf(localDate.getMonthValue()));
            screen.extraFields.put("month", monthLabelBox);

            ScreenUtils.LabelBox yearLabelBox = new ScreenUtils.LabelBox(
                    Component.literal("Year"), screen.getFont(), 32, 20, 0, 0, ScreenUtils.Align.HORIZONTAL);
            yearLabelBox.getEditBox().setFilter(string -> string.matches(Util.REGEX_INT));
            yearLabelBox.getEditBox().setValue("2000");
            screen.extraFields.put("year", yearLabelBox);
        }

        Button modelButton = Button.builder(Component.literal(CharacterModel.WIDE.name()), button -> {
                    String currentModel = button.getMessage().getString();

                    if (currentModel.equalsIgnoreCase(CharacterModel.WIDE.name())) {
                        button.setMessage(Component.literal(CharacterModel.SLIM.name()));
                    } else {
                        button.setMessage(Component.literal(CharacterModel.WIDE.name()));
                    }
                })
                .width(70)
                .build();
        screen.extraFields.put("modelBtn", modelButton);

        if (attribute == null && scene == null) {
            if (entry == null) {
                attribute = new MainCharacterAttribute();
            } else {
                attribute = new MainCharacterAttribute(entry.getMainCharacterAttribute());
            }
        }

        if (entry != null) {
            if (scene == null) {
                String[] birthDateSplit = entry.getBirthDate().split("/");
                try {
                    ScreenUtils.LabelBox dayLabelBox = (ScreenUtils.LabelBox) screen.extraFields.get("day");
                    ScreenUtils.LabelBox monthLabelBox = (ScreenUtils.LabelBox) screen.extraFields.get("month");
                    ScreenUtils.LabelBox yearLabelBox = (ScreenUtils.LabelBox) screen.extraFields.get("year");

                    dayLabelBox.getEditBox().setValue(birthDateSplit[0]);
                    monthLabelBox.getEditBox().setValue(birthDateSplit[1]);
                    yearLabelBox.getEditBox().setValue(birthDateSplit[2]);
                } catch (Exception e) {
                    Util.sendCrashMessage(Minecraft.getInstance().player, e);
                    Minecraft.getInstance().setScreen(null);
                }
            }
            modelButton.setMessage(Component.literal(entry.getModel().name()));
        }
    }

    @Override
    public void renderExtraFields(EditInfoScreen<CharacterStory> screen, CharacterStory entry, int x, int y) {
        if (scene == null) {
            ScreenUtils.LabelBox dayLabelBox = (ScreenUtils.LabelBox) screen.extraFields.get("day");
            dayLabelBox.setPosition(x, y);
            screen.addRenderableWidget(dayLabelBox.getEditBox());
            screen.addRenderableWidget(dayLabelBox.getStringWidget());

            ScreenUtils.LabelBox monthLabelBox = (ScreenUtils.LabelBox) screen.extraFields.get("month");
            monthLabelBox.setPosition(
                    dayLabelBox.getEditBox().getX() + dayLabelBox.getEditBox().getWidth() + 10, y);
            screen.addRenderableWidget(monthLabelBox.getEditBox());
            screen.addRenderableWidget(monthLabelBox.getStringWidget());

            ScreenUtils.LabelBox yearLabelBox = (ScreenUtils.LabelBox) screen.extraFields.get("year");
            yearLabelBox.setPosition(
                    monthLabelBox.getEditBox().getX()
                            + monthLabelBox.getEditBox().getWidth()
                            + 10,
                    y);
            screen.addRenderableWidget(yearLabelBox.getEditBox());
            screen.addRenderableWidget(yearLabelBox.getStringWidget());

            y += yearLabelBox.getEditBox().getHeight() + screen.GAP;
        }

        Button modelButton = (Button) screen.extraFields.get("modelBtn");
        Component label = Component.literal("Model");
        StringWidget modelText = ScreenUtils.text(
                label, screen.getFont(), x, y + modelButton.getHeight() / 2 - screen.getFont().lineHeight / 2);
        screen.addRenderableWidget(modelText);
        screen.addRenderableWidget(modelButton);
        modelButton.setPosition(x + modelText.getWidth() + 5, y);
        if (entry != null) {
            Button advancedBtn = Button.builder(Translation.message("global.advanced"), button -> {
                        screen.setName(screen.getNameBox().getEditBox().getValue());
                        screen.setDescription(
                                screen.getDescriptionBox().getMultiLineEditBox().getValue());
                        CharacterAdvancedScreen screen1 = new CharacterAdvancedScreen(screen, entry);
                        minecraft.setScreen(screen1);
                    })
                    .width(83)
                    .build();
            advancedBtn.setPosition(modelButton.getX() + modelButton.getWidth() + 5, y);
            screen.addRenderableWidget(advancedBtn);
        }
    }

    @Override
    public void buildFromScreen(
            Screen screen,
            Map<String, Object> extraFields,
            Minecraft minecraft,
            @Nullable CharacterStory existing,
            String name,
            String description) {
        CharacterModel model = CharacterModel.valueOf(
                ((Button) extraFields.get("modelBtn")).getMessage().getString());
        if (scene == null) {
            String day =
                    ((ScreenUtils.LabelBox) extraFields.get("day")).getEditBox().getValue();
            String month = ((ScreenUtils.LabelBox) extraFields.get("month"))
                    .getEditBox()
                    .getValue();
            String year = ((ScreenUtils.LabelBox) extraFields.get("year"))
                    .getEditBox()
                    .getValue();
            CharacterManager characterManager = NarrativeCraftMod.getInstance().getCharacterManager();
            if (characterManager.characterExists(name)
                    && (existing == null || !existing.getName().equals(name))) {
                ScreenUtils.sendToast(
                        Translation.message("global.error"), Translation.message("character.already_exists", name));
                return;
            }
            if (existing == null) {
                Services.PACKET_SENDER.sendToServer(new BiCharacterDataPacket(
                        name,
                        description,
                        model,
                        day,
                        month,
                        year,
                        true,
                        characterManager.getMainCharacter() == null,
                        false,
                        false,
                        "",
                        TypeStoryData.ADD));
            } else {
                Services.PACKET_SENDER.sendToServer(new BiCharacterDataPacket(
                        name,
                        description,
                        model,
                        day,
                        month,
                        year,
                        existing.showNametag(),
                        existing.getMainCharacterAttribute().isMainCharacter(),
                        existing.getMainCharacterAttribute().isSameSkinAsPlayer(),
                        existing.getMainCharacterAttribute().isSameSkinAsTheir(),
                        existing.getName(),
                        TypeStoryData.EDIT));
            }
        } else {
            if (scene.npcExists(name)
                    && (existing == null || !existing.getName().equals(name))) {
                ScreenUtils.sendToast(
                        Translation.message("global.error"),
                        Translation.message("npc.already_exists", name, scene.getName()));
                return;
            }
            if (existing == null) {
                Services.PACKET_SENDER.sendToServer(new BiNpcDataPacket(
                        name,
                        description,
                        model,
                        false,
                        scene.getChapter().getIndex(),
                        scene.getName(),
                        "",
                        TypeStoryData.ADD));
            } else {
                Services.PACKET_SENDER.sendToServer(new BiNpcDataPacket(
                        name,
                        description,
                        model,
                        existing.showNametag(),
                        scene.getChapter().getIndex(),
                        scene.getName(),
                        existing.getName(),
                        TypeStoryData.EDIT));
            }
        }
    }
}
