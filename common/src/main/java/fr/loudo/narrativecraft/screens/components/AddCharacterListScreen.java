package fr.loudo.narrativecraft.screens.components;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;

public class AddCharacterListScreen extends OptionsSubScreen {

    private final Scene scene;
    private CharacterList characterList;
    private final List<CharacterStory> characterStoryList;
    private CharacterStory.CharacterType characterType;
    private Consumer<CharacterStoryData> characterStoryDataConsumer;

    public AddCharacterListScreen(Scene scene, List<CharacterStory> characterStoryList, CharacterStory.CharacterType characterType, Consumer<CharacterStoryData> characterStoryDataConsumer) {
        super(null, Minecraft.getInstance().options, Component.literal("Spawn character"));
        this.characterStoryList = characterStoryList;
        this.characterType = characterType;
        this.characterStoryDataConsumer = characterStoryDataConsumer;
        this.scene = scene;
    }

    public AddCharacterListScreen(Scene scene, Consumer<CharacterStoryData> characterStoryDataConsumer) {
        super(null, Minecraft.getInstance().options, Component.literal("Spawn character"));
        this.characterStoryList = NarrativeCraftMod.getInstance().getCharacterManager().getCharacterStories();
        this.characterType = CharacterStory.CharacterType.MAIN;
        this.characterStoryDataConsumer = characterStoryDataConsumer;
        this.scene = scene;
    }

    @Override
    protected void addTitle() {
        LinearLayout linearlayout = this.layout.addToHeader(LinearLayout.horizontal()).spacing(8);
        linearlayout.defaultCellSetting().alignVerticallyMiddle();
        linearlayout.addChild(new StringWidget(this.title, this.font));
        if(scene != null) {
            linearlayout.addChild(Button.builder(characterType == CharacterStory.CharacterType.NPC ? Component.literal("MAIN") : Component.literal("NPC"), button -> {
                Screen screen;
                if(characterType == CharacterStory.CharacterType.MAIN) {
                    screen = new AddCharacterListScreen(scene, scene.getNpcs(), CharacterStory.CharacterType.NPC, characterStoryDataConsumer);
                } else {
                    screen = new AddCharacterListScreen(scene, NarrativeCraftMod.getInstance().getCharacterManager().getCharacterStories(), CharacterStory.CharacterType.MAIN, characterStoryDataConsumer);
                }
                minecraft.setScreen(screen);
            }).width(40).build());
        }
    }

    protected void addContents() {
        this.characterList = this.layout.addToContents(new CharacterList(this.minecraft));
    }

    protected void addOptions() {
    }

    protected void repositionElements() {
        super.repositionElements();
        this.characterList.updateSize(this.width, this.layout);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(null);
        CharacterList.Entry entry = this.characterList.getSelected();
        if(entry == null) return;
        CharacterStory selectedCharacter = entry.characterStory;
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        Vec3 position = localPlayer.position();
        CharacterStoryData characterStoryData = new CharacterStoryData(
                selectedCharacter,
                selectedCharacter.getCharacterSkinController().getMainSkinFile().getName(),
                position.x,
                position.y,
                position.z,
                localPlayer.getXRot(),
                localPlayer.getYRot(),
                false
        );
        characterStoryDataConsumer.accept(characterStoryData);
    }

    class CharacterList extends ObjectSelectionList<CharacterList.Entry> {
        public CharacterList(Minecraft minecraft) {
            super(minecraft, AddCharacterListScreen.this.width, AddCharacterListScreen.this.height - 33 - 53, 33, 18);
            String selectedCharacter = "";
            characterStoryList.forEach(characterStory1 -> {
                Entry entry = new Entry(characterStory1);
                this.addEntry(entry);
                if(selectedCharacter.equalsIgnoreCase(characterStory1.getName())) {
                    this.setSelected(entry);
                }
            });
            if (this.getSelected() != null) {
                this.centerScrollOn(this.getSelected());
            }

        }

        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        public class Entry extends ObjectSelectionList.Entry<Entry> {
            private final CharacterStory characterStory;

            public Entry(CharacterStory characterStory) {
                this.characterStory = characterStory;
            }

            public void render(GuiGraphics p_345300_, int p_345469_, int p_345328_, int p_345700_, int p_345311_, int p_345185_, int p_344805_, int p_345963_, boolean p_345912_, float p_346091_) {
                p_345300_.drawCenteredString(AddCharacterListScreen.this.font, this.characterStory.getName(), CharacterList.this.width / 2, p_345328_ + p_345185_ / 2 - 4, -1);
            }

            public boolean keyPressed(int p_346403_, int p_345881_, int p_345858_) {
                if (CommonInputs.selected(p_346403_)) {
                    this.select();
                    AddCharacterListScreen.this.onClose();
                    return true;
                } else {
                    return super.keyPressed(p_346403_, p_345881_, p_345858_);
                }
            }

            public boolean mouseClicked(double p_344965_, double p_345385_, int p_345080_) {
                this.select();
                return super.mouseClicked(p_344965_, p_345385_, p_345080_);
            }

            private void select() {
                CharacterList.this.setSelected(this);
            }

            @Override
            public Component getNarration() {
                return Component.literal(characterStory.getName());
            }

        }
    }
}
