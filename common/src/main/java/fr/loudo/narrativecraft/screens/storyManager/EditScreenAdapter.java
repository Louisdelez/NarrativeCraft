package fr.loudo.narrativecraft.screens.storyManager;

import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Dynamically create a new instance for a class that extends NarrativeEntry
 * After finishing putting info on EditScreen class
 * @param <T>
 */
public interface EditScreenAdapter<T extends NarrativeEntry> {

    /**
     * Init extra fields to EditInfoScreen
     */
    void initExtraFields(EditInfoScreen<T> screen, T entry);

    /**
     * Render extra fields to EditInfoScreen
     */
    void renderExtraFields(EditInfoScreen<T> screen, T entry, int x, int y);

    /**
     * Return the initial data after clicking done, and can retrieve extra features set from screen.
     * If it returns an existing one, then its to edit it.
     */
    void buildFromScreen(Map<String, Object> extraFields, Minecraft minecraft, @Nullable T existing, String name, String description);
}
