package fr.loudo.narrativecraft.keys;

import net.minecraft.client.Minecraft;

public class PressKeyListener {

    public static void onPressKey(Minecraft minecraft) {
        ModKeys.handleKeyPress(ModKeys.OPEN_STORY_MANAGER, () -> {
            if(!minecraft.player.hasPermissions(2)) return;
//            if (storyHandler != null && storyHandler.isRunning()) return;
//            Screen screen;
//            PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
//            if (playerSession == null || playerSession.getScene() == null) {
//                screen = new ChaptersScreen();
//            } else {
//                screen = new ScenesMenuScreen(playerSession.getScene());
//            }
//            client.execute(() -> client.setScreen(screen));
        });
    }

}
