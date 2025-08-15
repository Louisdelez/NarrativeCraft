package fr.loudo.narrativecraft.narrative.chapter.scenes.interaction;

import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import net.minecraft.world.phys.Vec3;

public class ObjectInteraction extends Interaction {

    private double x, y, z;

    public ObjectInteraction(String name, Scene scene) {
        super(name, "", scene);
    }

    public Vec3 getPosition() {
        return new Vec3(x, y, z);
    }

    public void setPosition(Vec3 position) {
        x = position.x;
        y = position.y;
        z = position.z;
    }

    @Override
    public InteractionType getType() {
        return InteractionType.OBJECT;
    }
}
