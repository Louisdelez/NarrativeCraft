package fr.loudo.narrativecraft.options;

import net.minecraft.client.Minecraft;

public class NarrativeWorldOption {
    public boolean finishedStory = false;
    public boolean showMainScreen = Minecraft.getInstance().isSingleplayer();
    public boolean showCreditsScreen = true;
}
