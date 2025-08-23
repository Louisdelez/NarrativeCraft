package fr.loudo.narrativecraft.narrative.chapter.scene.data;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.managers.PlaybackManager;
import fr.loudo.narrativecraft.narrative.Environnement;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.SceneData;
import fr.loudo.narrativecraft.narrative.playback.Playback;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class Subscene extends SceneData {

    private transient List<Animation> animations = new ArrayList<>();
    private transient List<Playback> playbacks = new ArrayList<>();

    public Subscene(String name, String description, Scene scene) {
        super(name, description, scene);
    }

    public void start(Level level, Environnement environnement, boolean looping) {
        playbacks = getPlaybacks();
        playbacks.clear();
        for(Animation animation : animations) {
            Playback playback = new Playback(PlaybackManager.ids.incrementAndGet(), animation, level, environnement, looping);
            playback.start();
            playbacks.add(playback);
            NarrativeCraftMod.getInstance().getPlaybackManager().addPlayback(playback);
        }
    }

    public void stop(boolean killEntity) {
        for(Playback playback : playbacks) {
            playback.stop(killEntity);
        }
        NarrativeCraftMod.getInstance().getPlaybackManager().getPlaybacks().removeAll(playbacks);
        playbacks.clear();
    }

    public List<Playback> getPlaybacks() {
        if(playbacks == null) {
            playbacks = new ArrayList<>();
        }
        return playbacks;
    }

    public List<Animation> getAnimations() {
        if (animations == null) {
            animations = new ArrayList<>();
        }
        return animations;
    }

    public List<String> getAnimationsName() {
        return getAnimations().stream().map(Animation::getName).toList();
    }
}
