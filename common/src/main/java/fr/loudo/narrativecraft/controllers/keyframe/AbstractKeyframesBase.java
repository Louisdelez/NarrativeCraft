/*
 * NarrativeCraft - Create your own stories, easily, and freely in Minecraft.
 * Copyright (c) 2025 LOUDO and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package fr.loudo.narrativecraft.controllers.keyframe;

import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.keyframes.Keyframe;
import fr.loudo.narrativecraft.util.Util;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public abstract class AbstractKeyframesBase<T extends Keyframe> extends AbstractKeyframeController<T> {

    protected final List<T> keyframes = new ArrayList<>();

    public AbstractKeyframesBase(Environment environment, Player player) {
        super(environment, player);
    }

    @Override
    public T getNextKeyframe(T toKeyframe) {
        return null;
    }

    @Override
    public T getPreviousKeyframe(T fromKeyframe) {
        return null;
    }

    @Override
    public T getKeyframeByEntity(Entity entity) {
        for (T keyframe : keyframes) {
            if (Util.isSameEntity(entity, keyframe.getCamera())) {
                return keyframe;
            }
        }
        return null;
    }

    @Override
    public void hideKeyframes(ServerPlayer player) {
        for (T keyframe : keyframes) {
            keyframe.hideKeyframe(player);
        }
    }

    @Override
    public void showKeyframes(ServerPlayer player) {
        for (T keyframe : keyframes) {
            keyframe.showKeyframe(player);
        }
    }

    @Override
    public void removeKeyframe(T keyframe) {}

    public List<T> getKeyframes() {
        return keyframes;
    }
}
