package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.NarrativeEntryInit;
import net.minecraft.server.MinecraftServer;

public class OnLifecycle {

    public static void serverStart(MinecraftServer server) {
        if(server == null) return;
        NarrativeCraftMod.server = server;
        NarrativeCraftFile.init(server);
        NarrativeEntryInit.init();
    }

}