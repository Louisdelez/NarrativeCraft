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

package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.narrative.story.inkAction.sound.SoundInkInstance;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/*
    Don't override volume of a sound ink action when changing volume in client settings
    Otherwise, sounds from sound ink are at max volume and broken
*/
@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {

    @Shadow
    protected abstract float calculateVolume(SoundInstance sound);

    @Shadow
    public abstract void setVolume(SoundInstance soundInstance, float volume);

    @Redirect(
            method = "updateCategoryVolume",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"))
    private void narrativecraft$updateCategoryVolume(
            Map<SoundInstance, ChannelAccess.ChannelHandle> instance,
            BiConsumer<? super SoundInstance, ? super ChannelAccess.ChannelHandle> v) {
        instance.forEach((soundInstance, channelHandle) -> {
            if (soundInstance instanceof SoundInkInstance) return;
            float f = this.calculateVolume(soundInstance);
            channelHandle.execute((channel) -> channel.setVolume(f));
        });
    }
}
