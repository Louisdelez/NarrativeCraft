package fr.loudo.narrativecraft.screens.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class GenericSelectionScreen<T extends SelectionScreenSelectable> extends Screen {
    protected final List<T> itemList;
    protected final Consumer<T> consumer;
    protected final Function<T, String> nameExtractor;
    protected final T currentSelection;
    protected final String screenTitle;
    protected final Screen lastScreen;
    protected final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    
    private SelectionList<T> selectionList;

    public GenericSelectionScreen(Screen lastScreen, 
                                String screenTitle,
                                List<T> itemList, 
                                T currentSelection,
                                Function<T, String> nameExtractor,
                                Consumer<T> consumer) {
        super(Component.literal(screenTitle));
        this.lastScreen = lastScreen;
        this.screenTitle = screenTitle;
        this.itemList = itemList;
        this.currentSelection = currentSelection;
        this.nameExtractor = nameExtractor;
        this.consumer = consumer;
    }

    public GenericSelectionScreen(Screen lastScreen,
                                String screenTitle,
                                List<T> itemList, 
                                T currentSelection,
                                Consumer<T> consumer) {
        this(lastScreen, screenTitle, itemList, currentSelection, SelectionScreenSelectable::getName, consumer);
    }

    @Override
    protected void init() {
        this.addTitle();
        this.addContents();
        this.addFooter();
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    protected void addTitle() {
        LinearLayout linearlayout = this.layout.addToHeader(LinearLayout.horizontal()).spacing(8);
        linearlayout.defaultCellSetting().alignVerticallyMiddle();
        linearlayout.addChild(new StringWidget(this.title, this.font));
        
        addCustomTitleButtons(linearlayout);
    }
    
    protected void addCustomTitleButtons(LinearLayout layout) {}

    protected void addContents() {
        this.selectionList = this.layout.addToContents(new SelectionList<>(this.minecraft, this));
    }

    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, p_345997_ -> this.onClose()).width(200).build());
    }

    protected void repositionElements() {
        this.layout.arrangeElements();
        this.selectionList.updateSize(this.width, this.layout);
    }

    @Override
    public void onClose() {
        SelectionList<T>.Entry entry = this.selectionList.getSelected();
        if(entry == null) {
            consumer.accept(null);
        } else {
            consumer.accept(entry.getItem());
        }
        minecraft.setScreen(lastScreen);
    }
    
    protected List<T> getItemList() { return itemList; }
    protected Function<T, String> getNameExtractor() { return nameExtractor; }
    protected T getCurrentSelection() { return currentSelection; }

    static class SelectionList<T extends SelectionScreenSelectable> extends ObjectSelectionList<SelectionList<T>.Entry> {
        private final GenericSelectionScreen<T> parentScreen;

        public SelectionList(Minecraft minecraft, GenericSelectionScreen<T> parentScreen) {
            super(minecraft, parentScreen.width, parentScreen.height - 33 - 53, 33, 18);
            this.parentScreen = parentScreen;
            
            String selectedName = "";
            if(parentScreen.getCurrentSelection() != null) {
                selectedName = parentScreen.getNameExtractor().apply(parentScreen.getCurrentSelection());
            }
            
            for(T item : parentScreen.getItemList()) {
                Entry entry = new Entry(item);
                this.addEntry(entry);
                
                String itemName = parentScreen.getNameExtractor().apply(item);
                if(selectedName.equalsIgnoreCase(itemName)) {
                    this.setSelected(entry);
                }
            }
            
            if (this.getSelected() != null) {
                this.centerScrollOn(this.getSelected());
            }
        }

        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        public class Entry extends ObjectSelectionList.Entry<Entry> {
            private final T item;

            public Entry(T item) {
                this.item = item;
            }
            
            public T getItem() {
                return item;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY, int i6, boolean isSelected, float partialTick) {
                String displayName = parentScreen.getNameExtractor().apply(this.item);
                guiGraphics.drawCenteredString(parentScreen.font, displayName, SelectionList.this.width / 2, y + 3, -1);
            }

            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (CommonInputs.selected(keyCode)) {
                    this.select();
                    parentScreen.onClose();
                    return true;
                } else {
                    return super.keyPressed(keyCode, scanCode, modifiers);
                }
            }

            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                this.select();
                return super.mouseClicked(mouseX, mouseY, button);
            }

            private void select() {
                SelectionList.this.setSelected(this);
            }

            @Override
            public Component getNarration() {
                String displayName = parentScreen.getNameExtractor().apply(this.item);
                return Component.literal(displayName);
            }

        }
    }
}