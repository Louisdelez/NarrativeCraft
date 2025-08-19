package fr.loudo.narrativecraft.keys;

import fr.loudo.narrativecraft.screens.storyManager.chapter.ChaptersScreen;
import net.minecraft.client.Minecraft;

public class PressKeyListener {

    public static void onPressKey(Minecraft minecraft) {
        ModKeys.handleKeyPress(ModKeys.OPEN_STORY_MANAGER, () -> {
            if(!minecraft.player.hasPermissions(2)) return;
            minecraft.setScreen(new ChaptersScreen());
        });
    }

}
