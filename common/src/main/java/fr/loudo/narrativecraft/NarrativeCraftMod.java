package fr.loudo.narrativecraft;

import fr.loudo.narrativecraft.managers.CharacterManager;
import fr.loudo.narrativecraft.managers.PlayerSessionManager;
import net.minecraft.server.MinecraftServer;
import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

public class NarrativeCraftMod {
    private static final NarrativeCraftMod instance = new NarrativeCraftMod();

    public static final String MOD_ID = "narrativecraft";
    public static final String MOD_NAME = "NarrativeCraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static boolean firstTime = false;
    public static MinecraftServer server;

    private final CharacterManager characterManager = new CharacterManager();
    private final PlayerSessionManager playerSessionManager = new PlayerSessionManager();

    public static NarrativeCraftMod getInstance() {
        return instance;
    }

    public CharacterManager getCharacterManager() {
        return characterManager;
    }

    public PlayerSessionManager getPlayerSessionManager() {
        return playerSessionManager;
    }
}
