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

package fr.loudo.narrativecraft.narrative.dialog;

public class DialogLetterEffect {
    private DialogAnimationType animation;
    private int currentTick, totalTick;
    private float force;
    private int startIndex, endIndex;

    public DialogLetterEffect(DialogAnimationType animation, double time, float force, int startIndex, int endIndex) {
        this.animation = animation;
        this.force = force;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        totalTick = (int) (time * 20.0);
    }

    public boolean canApplyEffect() {
        return currentTick >= totalTick;
    }

    public void tick() {
        if (currentTick < totalTick) {
            currentTick++;
        }
    }

    public DialogLetterEffect(DialogAnimationType animation) {
        this.animation = animation;
    }

    public DialogAnimationType getAnimation() {
        return animation;
    }

    public float getForce() {
        return force;
    }

    public void setAnimation(DialogAnimationType animation) {
        this.animation = animation;
    }

    public void setForce(float force) {
        this.force = force;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public int getTotalTick() {
        return totalTick;
    }
}
