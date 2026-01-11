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

package fr.loudo.narrativecraft.narrative.inkTag;

import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.narrative.error.NarrativeException;
import org.jetbrains.annotations.Nullable;

/**
 * Exception thrown when an Ink tag fails to execute.
 *
 * T072: Enhanced with NarrativeException integration for better error messages.
 */
public class InkTagHandlerException extends Exception {

    private final String tagName;
    @Nullable
    private final String sceneName;
    @Nullable
    private final String suggestion;

    public InkTagHandlerException(Class<? extends InkAction> inkActionClass, String message) {
        super("Tag " + inkActionClass.getSimpleName() + " cannot be executed! " + message);
        this.tagName = inkActionClass.getSimpleName();
        this.sceneName = null;
        this.suggestion = null;
    }

    public InkTagHandlerException(Class<? extends InkAction> inkActionClass, String message,
                                   String sceneName, String suggestion) {
        super("Tag " + inkActionClass.getSimpleName() + " cannot be executed! " + message);
        this.tagName = inkActionClass.getSimpleName();
        this.sceneName = sceneName;
        this.suggestion = suggestion;
    }

    /**
     * Creates an InkTagHandlerException from a NarrativeException.
     */
    public InkTagHandlerException(NarrativeException narrativeException) {
        super(narrativeException.getUserFriendlyMessage(), narrativeException);
        this.tagName = "unknown";
        this.sceneName = narrativeException.getSceneName();
        this.suggestion = narrativeException.getSuggestion();
    }

    /**
     * Gets the tag name that caused the error.
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * Gets the scene name where the error occurred.
     */
    @Nullable
    public String getSceneName() {
        return sceneName;
    }

    /**
     * Gets the suggestion for fixing the error.
     */
    @Nullable
    public String getSuggestion() {
        return suggestion;
    }

    /**
     * Returns a user-friendly error message.
     */
    public String getUserFriendlyMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("[Tag Error] ").append(tagName);
        if (sceneName != null) {
            sb.append(" in ").append(sceneName);
        }
        sb.append(": ").append(getMessage());
        if (suggestion != null) {
            sb.append("\nSuggestion: ").append(suggestion);
        }
        return sb.toString();
    }
}
