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

package fr.loudo.narrativecraft.narrative.keyframes.cutscene;

import fr.loudo.narrativecraft.narrative.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.keyframes.KeyframeLocation;
import fr.loudo.narrativecraft.util.Easing;

public class CutsceneKeyframe extends Keyframe {

    private int tick;
    private double speed;
    private long startDelay, pathTime, transitionDelay;
    private boolean isParentGroup;
    private Easing easing;

    public CutsceneKeyframe(int id, KeyframeLocation keyframeCoordinate, int tick, long startDelay, long pathTime) {
        super(id, keyframeCoordinate);
        this.tick = tick;
        this.startDelay = startDelay;
        this.pathTime = pathTime;
        this.transitionDelay = 0;
        this.isParentGroup = false;
        this.speed = 1;
        this.easing = Easing.SMOOTH;
    }

    public CutsceneKeyframe(int id, KeyframeLocation keyframeCoordinate) {
        super(id, keyframeCoordinate);
        this.isParentGroup = false;
    }

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public long getStartDelay() {
        return startDelay;
    }

    public void setStartDelay(long startDelay) {
        this.startDelay = startDelay;
    }

    public long getPathTime() {
        return pathTime;
    }

    public void setPathTime(long pathTime) {
        this.pathTime = pathTime;
    }

    public long getTransitionDelay() {
        return transitionDelay;
    }

    public void setTransitionDelay(long transitionDelay) {
        this.transitionDelay = transitionDelay;
    }

    public boolean isParentGroup() {
        return isParentGroup;
    }

    public void setParentGroup(boolean parentGroup) {
        isParentGroup = parentGroup;
    }

    public Easing getEasing() {
        return easing;
    }

    public void setEasing(Easing easing) {
        this.easing = easing;
    }
}
