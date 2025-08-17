package fr.loudo.narrativecraft.narrative.session;

import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import net.minecraft.server.level.ServerPlayer;

public class PlayerSession {

    private final ServerPlayer player;
    private Chapter chapter;
    private Scene scene;

    public PlayerSession(ServerPlayer player) {
        this.player = player;
    }

    public PlayerSession(ServerPlayer player, Chapter chapter, Scene scene) {
        this.player = player;
        this.chapter = chapter;
        this.scene = scene;
    }

    public boolean isSamePlayer(ServerPlayer player) {
        return this.player.getUUID().equals(player.getUUID());
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }
}
