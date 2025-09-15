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

import fr.loudo.narrativecraft.narrative.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.keyframes.keyframeTrigger.KeyframeTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public interface KeyframeControllerInterface<T extends Keyframe> {
    void setCamera(Keyframe keyframe);

    void showKeyframes(ServerPlayer player);

    void hideKeyframes(ServerPlayer player);

    void removeKeyframe(T keyframe);

    T getNextKeyframe(T fromKeyframe);

    T getPreviousKeyframe(T fromKeyframe);

    T getKeyframeByEntity(Entity entity);

    KeyframeTrigger getKeyframeTriggerByEntity(Entity entity);

    void addKeyframeTrigger(int tick, String commands);

    void removeKeyframeTrigger(KeyframeTrigger keyframeTrigger);
}
