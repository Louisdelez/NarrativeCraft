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
import fr.loudo.narrativecraft.narrative.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.narrative.keyframes.keyframeTrigger.KeyframeTrigger;
import fr.loudo.narrativecraft.util.Util;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Base controller for managing multiple groups of keyframes.
 * Provides navigation between keyframes across different groups.
 *
 * @param <T> the type of keyframe, extending {@link Keyframe}
 * @param <E> the type of keyframe group, extending {@link KeyframeGroup}
 *            and containing a list of keyframes of type {@code T}
 */
public abstract class AbstractKeyframeGroupsBase<T extends Keyframe, E extends KeyframeGroup<T>>
        extends AbstractKeyframeController<T> {

    protected final List<E> keyframeGroups = new ArrayList<>();
    protected final AtomicInteger keyframeGroupsCounter = new AtomicInteger();

    public AbstractKeyframeGroupsBase(Environment environment, Player player) {
        super(environment, player);
    }

    @Override
    public T getNextKeyframe(T toKeyframe) {
        for (int i = 0; i < keyframeGroups.size(); i++) {
            KeyframeGroup<T> group = keyframeGroups.get(i);
            List<T> keyframes = group.getKeyframes();
            for (int j = 0; j < keyframes.size(); j++) {
                if (keyframes.get(j).getId() == toKeyframe.getId()) {
                    if (j + 1 < keyframes.size()) {
                        return keyframes.get(j + 1);
                    } else if (i + 1 < keyframeGroups.size()) {
                        return keyframeGroups.get(i + 1).getKeyframes().get(0);
                    }
                }
            }
        }
        return toKeyframe;
    }

    @Override
    public T getPreviousKeyframe(T fromKeyframe) {
        for (int i = 0; i < keyframeGroups.size(); i++) {
            KeyframeGroup<T> group = keyframeGroups.get(i);
            List<T> keyframes = group.getKeyframes();
            for (int j = 0; j < keyframes.size(); j++) {
                if (keyframes.get(j).getId() == fromKeyframe.getId()) {
                    if (j > 0) {
                        return keyframes.get(j - 1);
                    } else if (i > 0) {
                        List<T> prevGroupKeyframes = keyframeGroups.get(i - 1).getKeyframes();
                        return prevGroupKeyframes.get(keyframeGroups.size() - 1);
                    }
                }
            }
        }
        return fromKeyframe;
    }

    @Override
    public T getKeyframeByEntity(Entity entity) {
        for (E keyframeGroup : keyframeGroups) {
            for (T keyframe : keyframeGroup.getKeyframes()) {
                if (Util.isSameEntity(entity, keyframe.getCamera())) {
                    return keyframe;
                }
            }
        }
        return null;
    }

    @Override
    public void hideKeyframes(ServerPlayer player) {
        for (E keyframeGroup : keyframeGroups) {
            keyframeGroup.hideKeyframes(player);
        }
        for (KeyframeTrigger keyframeTrigger : keyframeTriggers) {
            keyframeTrigger.hideKeyframe(player);
        }
    }

    @Override
    public void showKeyframes(ServerPlayer player) {
        for (E keyframeGroup : keyframeGroups) {
            keyframeGroup.showKeyframes(player);
        }
        for (KeyframeTrigger keyframeTrigger : keyframeTriggers) {
            keyframeTrigger.showKeyframe(player);
        }
    }

    @Override
    public void removeKeyframe(T keyframe) {
        E keyframeGroup = getKeyframeGroupOfKeyframe(keyframe);
        keyframe.hideKeyframe(playerSession.getPlayer());
        keyframeGroup.removeKeyframe(keyframe);
    }

    public E getKeyframeGroupOfKeyframe(T keyframe) {
        for (E keyframeGroup : keyframeGroups) {
            for (T keyframeFromGroup : keyframeGroup.getKeyframes()) {
                if (keyframe.getId() == keyframeFromGroup.getId()) {
                    return keyframeGroup;
                }
            }
        }
        return null;
    }

    public List<E> getKeyframeGroups() {
        return keyframeGroups;
    }
}
