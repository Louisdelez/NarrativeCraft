package fr.loudo.narrativecraft;

import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.managers.CharacterManager;
import fr.loudo.narrativecraft.managers.PlayerSessionManager;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NarrativeCraftMod {
    private static final NarrativeCraftMod instance = new NarrativeCraftMod();

    public static final String MOD_ID = "narrativecraft";
    public static final String MOD_NAME = "NarrativeCraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static boolean firstTime = false;
    public static MinecraftServer server;

    private final CharacterManager characterManager = new CharacterManager();
    private final PlayerSessionManager playerSessionManager = new PlayerSessionManager();
    private final ChapterManager chapterManager = new ChapterManager();

    public static NarrativeCraftMod getInstance() {
        return instance;
    }

    public CharacterManager getCharacterManager() {
        return characterManager;
    }

    public PlayerSessionManager getPlayerSessionManager() {
        return playerSessionManager;
    }

    public ChapterManager getChapterManager() {
        return chapterManager;
    }

    public void clearManagers() {
        chapterManager.getChapters().clear();
        playerSessionManager.getPlayerSessions().clear();
        characterManager.getCharacterStories().clear();
    }
}
