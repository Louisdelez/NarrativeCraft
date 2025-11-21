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

package fr.loudo.narrativecraft.narrative.story.inkAction.sound;

import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.inkAction.InkActionUtil;
import fr.loudo.narrativecraft.audio.VolumeAudio;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

public class SoundInkAction extends InkAction {

    private SoundManager soundManager;
    private SoundInstance simpleSoundInstance;
    private String identifier = "minecraft";
    private String name, action;
    private Type type;
    private float volume = 1.0F, pitch = 1.0F;
    private boolean isLooping;
    private double fadeTime;
    private int currentTick, totalTick;

    public SoundInkAction(String id, Side side, String syntax, CommandMatcher matcher) {
        super(id, side, syntax, matcher);
    }

    @Override
    public void tick() {
        if (!soundManager.isActive(simpleSoundInstance)) {
            isRunning = false;
        }
        if (!isRunning || totalTick == 0) return;
        tick++;
        isRunning = tick <= totalTick || action.equals("start");
        if (!isRunning) {
            soundManager.stop(simpleSoundInstance);
        }
    }

    @Override
    public void partialTick(float partialTick) {
        if (!isRunning || totalTick == 0) return;
        double t = Mth.clamp((tick + partialTick) / totalTick, 0.0, 1.0);
        float newVolume = 0;
        if (action.equals("start")) {
            newVolume = (float) Mth.lerp(t, 0.0, volume);
        } else if (action.equals("stop")) {
            newVolume = (float) Mth.lerp(t, volume, 0.0);
        }
        ((VolumeAudio) soundManager).narrativecraft$setVolume(simpleSoundInstance, newVolume);
        if (newVolume == 0.0 && action.equals("stop")) {
            soundManager.stop(simpleSoundInstance);
        }
    }

    @Override
    public void stop() {
        if (soundManager == null || simpleSoundInstance == null) return;
        soundManager.stop(simpleSoundInstance);
    }

    @Override
    protected InkActionResult doValidate(List<String> arguments, Scene scene) {
        Minecraft minecraft = Minecraft.getInstance();
        soundManager = minecraft.getSoundManager();
        if (arguments.size() == 1) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Start or stop"));
        }
        String typeName = arguments.get(0);
        type = Type.valueOf(typeName.toUpperCase());
        action = arguments.get(1);
        if (!action.equals("start") && !action.equals("stop")) {
            return InkActionResult.error(Translation.message(WRONG_ARGUMENT_TEXT, "Only start or stop action"));
        }
        if (arguments.size() == 2) {
            return InkActionResult.error(
                    Translation.message(WRONG_ARGUMENT_TEXT, type.name().toLowerCase() + " name"));
        }
        name = arguments.get(2);
        if (action.equals("stop") && name.equals("all")) {
            return InkActionResult.ok();
        }
        if (name.contains(":")) {
            String[] splitName = name.split(":");
            identifier = splitName[0];
            name = splitName[1];
        }
        ResourceLocation location = new ResourceLocation(identifier, name);
        if (soundManager.getSoundEvent(location) == null) {
            return InkActionResult.warn(Translation.message(
                    "ink_action.validation.sound", type.name().toLowerCase(), name));
        }
        if (action.equals("start") && arguments.size() > 3) {
            isLooping = InkActionUtil.getOptionalArgument(command, "loop");
            try {
                volume = Float.parseFloat(arguments.get(3));
            } catch (NumberFormatException e) {
                return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(3)));
            }
            if (arguments.size() == 4) {
                return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "pitch"));
            }
            try {
                pitch = Float.parseFloat(arguments.get(4));
            } catch (NumberFormatException e) {
                return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(4)));
            }
            if (arguments.size() == 5) return InkActionResult.ok();
            String fadeValue = arguments.get(5);
            if (!fadeValue.equals("fadein")) return InkActionResult.ok();
            if (arguments.size() == 6) {
                return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "fade in value"));
            }
            try {
                fadeTime = Double.parseDouble(arguments.get(6));
            } catch (NumberFormatException e) {
                return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(6)));
            }
            simpleSoundInstance = getSimpleSoundInstance();
            soundManager.play(simpleSoundInstance);
            if (fadeTime > 0) {
                ((VolumeAudio) soundManager).narrativecraft$setVolume(simpleSoundInstance, 0f);
            }
        } else if (action.equals("stop") && arguments.size() > 3) {
            String fadeValue = arguments.get(3);
            if (!fadeValue.equals("fadeout")) return InkActionResult.ok();
            if (arguments.size() == 4) {
                return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "fade out value"));
            }
            try {
                fadeTime = Double.parseDouble(arguments.get(4));
            } catch (NumberFormatException e) {
                return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(4)));
            }
        }
        totalTick = (int) (fadeTime * 20.0);
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(PlayerSession playerSession) {
        if (action.equals("start")) {
            simpleSoundInstance = getSimpleSoundInstance();
            soundManager.play(simpleSoundInstance);
            if (fadeTime > 0) {
                ((VolumeAudio) soundManager).narrativecraft$setVolume(simpleSoundInstance, 0);
            }
        } else if (action.equals("stop")) {
            for (InkAction inkAction : playerSession.getInkActions()) {
                if (inkAction instanceof SoundInkAction soundInkAction) {
                    boolean matchAll =
                            name.equals("all") && (soundInkAction.type == this.type || this.type == Type.SOUND);
                    boolean matchOne =
                            !name.equals("all") && (soundInkAction.name.equals(name) && type == soundInkAction.type);

                    if (matchAll || matchOne) {
                        soundInkAction.isRunning = false;
                    }
                    if (matchAll) {
                        isRunning = false;
                        soundInkAction.stop();
                    } else if (matchOne) {
                        if (fadeTime > 0) {
                            this.simpleSoundInstance = soundInkAction.simpleSoundInstance;
                            this.volume = soundInkAction.volume;
                        } else {
                            isRunning = false;
                            soundInkAction.stop();
                        }
                    }
                }
            }
        }
        return InkActionResult.ok();
    }

    private SoundInstance getSimpleSoundInstance() {
        if (simpleSoundInstance == null) {
            simpleSoundInstance = new SoundInkInstance(
                    new ResourceLocation(identifier, name),
                    SoundSource.MASTER,
                    volume,
                    pitch,
                    SoundInstance.createUnseededRandom(),
                    isLooping,
                    0,
                    SoundInstance.Attenuation.NONE,
                    0,
                    0,
                    0,
                    true);
        }
        return simpleSoundInstance;
    }

    public enum Type {
        SFX,
        SONG,
        SOUND
    }

    @Override
    public boolean needScene() {
        return false;
    }
}
