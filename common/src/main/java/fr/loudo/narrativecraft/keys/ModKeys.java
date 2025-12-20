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

package fr.loudo.narrativecraft.keys;

import com.mojang.blaze3d.platform.InputConstants;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class ModKeys {

    private static final Map<KeyMapping, Boolean> previousStatesKeyMapping = new HashMap<>();
    private static final Map<Integer, Boolean> previousStatesKeyCode = new HashMap<>();
    private static final List<KeyMapping> ALL_KEYS = new ArrayList<>();
    private static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "main"));

    public static final KeyMapping OPEN_STORY_MANAGER = registerKey("key.screen.story.open", InputConstants.KEY_N);
    public static final KeyMapping START_ANIMATION_RECORDING =
            registerKey("key.animation.record.start", InputConstants.KEY_V);
    public static final KeyMapping STOP_ANIMATION_RECORDING =
            registerKey("key.animation.record.stop", InputConstants.KEY_B);
    public static final KeyMapping OPEN_CONTROLLER_SCREEN =
            registerKey("key.screen.keyframe_controller", InputConstants.KEY_G);
    public static final KeyMapping NEXT_DIALOG = registerKey("key.dialog.next", InputConstants.KEY_RETURN);
    public static final KeyMapping SELECT_CHOICE_1 = registerKey("key.choice.1", InputConstants.KEY_1);
    public static final KeyMapping SELECT_CHOICE_2 = registerKey("key.choice.2", InputConstants.KEY_2);
    public static final KeyMapping SELECT_CHOICE_3 = registerKey("key.choice.3", InputConstants.KEY_3);
    public static final KeyMapping SELECT_CHOICE_4 = registerKey("key.choice.4", InputConstants.KEY_4);
    public static final KeyMapping STORY_DEBUG = registerKey("key.story_debug", InputConstants.KEY_F9);

    private static KeyMapping registerKey(String translationKey, int code) {
        KeyMapping key = new KeyMapping(translationKey, InputConstants.Type.KEYSYM, code, CATEGORY);
        ALL_KEYS.add(key);
        return key;
    }

    public static List<KeyMapping> getAllKeys() {
        return ALL_KEYS;
    }

    public static void handleKeyPress(KeyMapping key, Runnable action) {
        boolean isDown = key.isDown();
        boolean wasDown = previousStatesKeyMapping.getOrDefault(key, false);

        if (isDown && !wasDown) {
            action.run();
        }

        previousStatesKeyMapping.put(key, isDown);
    }

    public static void handleKeyPress(int code, boolean isDown, Runnable action) {
        boolean wasDown = previousStatesKeyCode.getOrDefault(code, false);

        if (isDown && !wasDown) {
            action.run();
        }

        previousStatesKeyCode.put(code, isDown);
    }
}
