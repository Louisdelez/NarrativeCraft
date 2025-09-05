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

package fr.loudo.narrativecraft.api.inkAction;

import net.minecraft.network.chat.Component;

public record InkActionResult(
        fr.loudo.narrativecraft.api.inkAction.InkActionResult.Status status, String errorMessage) {
    public enum Status {
        OK,
        IGNORED,
        BLOCK,
        ERROR,
        WARN
    }

    public static InkActionResult ok() {
        return new InkActionResult(Status.OK, null);
    }

    public static InkActionResult ignored() {
        return new InkActionResult(Status.IGNORED, null);
    }

    public static InkActionResult block() {
        return new InkActionResult(Status.BLOCK, null);
    }

    public static InkActionResult error(Class<?> clazz, String message) {
        return new InkActionResult(Status.ERROR, clazz.getSimpleName() + "\n" + message);
    }

    public static InkActionResult error(Class<?> clazz, Component message) {
        return new InkActionResult(Status.ERROR, clazz.getSimpleName() + "\n" + message.getString());
    }

    public static InkActionResult error(Component message) {
        return new InkActionResult(Status.ERROR, message.getString());
    }

    public static InkActionResult error(String message) {
        return new InkActionResult(Status.ERROR, message);
    }

    public static InkActionResult warn(Component message) {
        return new InkActionResult(Status.WARN, message.getString());
    }

    public static InkActionResult warn(String message) {
        return new InkActionResult(Status.WARN, message);
    }

    public boolean isError() {
        return status == Status.ERROR;
    }
}
