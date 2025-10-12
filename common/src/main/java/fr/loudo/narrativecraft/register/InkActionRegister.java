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

package fr.loudo.narrativecraft.register;

import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionRegistry;
import fr.loudo.narrativecraft.narrative.story.inkAction.*;

public class InkActionRegister {
    public static void register() {
        InkActionRegistry.register(() -> new AnimationInkAction(
                "animation",
                InkAction.Side.SERVER,
                "animation start %animation_name% [loop=true/false] [unique=true/false] [block=true/false]",
                command -> command.startsWith("animation start") || command.startsWith("animation stop")));
        InkActionRegistry.register(() -> new BorderInkAction(
                "border",
                InkAction.Side.CLIENT,
                "border %up% %right% %down% %left% [%color%] [%opacity%]",
                command -> command.startsWith("border")));
        InkActionRegistry.register(() -> new CameraAngleInkAction(
                "camera", InkAction.Side.CLIENT, "camera %parent% %child%", command -> command.startsWith("camera")));
        InkActionRegistry.register(() -> new ChangeDayTimeInkAction(
                "change_day_time",
                InkAction.Side.CLIENT,
                "time <set,add> <day,midnight,night,noon,%tick%> [to <day,midnight,night,noon,%tick%> for %time% <second(s), minute(s), hour(s)> [%easing%]]",
                command -> command.startsWith("time set") || command.startsWith("time add")));
        InkActionRegistry.register(() -> new CutsceneInkAction(
                "cutscene",
                InkAction.Side.SERVER,
                "cutscene start %cutscene_name%",
                command -> command.startsWith("cutscene start")));
        InkActionRegistry.register(() -> new CooldownInkAction(
                "cooldown",
                InkAction.Side.SERVER,
                "wait %time% <second(s), minute(s), hour(s)>",
                command -> command.startsWith("wait")));
        InkActionRegistry.register(() -> new DialogParametersInkAction(
                "dialog",
                InkAction.Side.CLIENT,
                "dialog <offset, scale, padding, width, textColor, backgroundColor, gap, letterSpacing, unSkippable, autoSkip, bobbing> [%value1%] [%value2%]",
                command -> command.startsWith("dialog")));
        InkActionRegistry.register(() -> new MinecraftCommandInkAction(
                "minecraft_command",
                InkAction.Side.SERVER,
                "command \"%command_value%\"",
                command -> command.startsWith("command")));
        InkActionRegistry.register(() -> new EmoteInkAction(
                "emotecraft_emote",
                InkAction.Side.SERVER,
                "emote <play,stop> %emote_name% %character_name% %isForced%",
                command -> command.startsWith("emote play") || command.startsWith("emote stop")));
        InkActionRegistry.register(() -> new FadeInkAction(
                "fade",
                InkAction.Side.CLIENT,
                "fade %fadeInValue% %stayValue% %fadeOutValue% %color%",
                command -> command.startsWith("fade")));
        InkActionRegistry.register(() -> new KillCharacterInkAction(
                "kill_character",
                InkAction.Side.SERVER,
                "kill %character_name%",
                command -> command.startsWith("kill")));
        InkActionRegistry.register(() -> new OnEnterInkAction(
                "enter", InkAction.Side.SERVER, "on enter", command -> command.equals("on enter")));
        InkActionRegistry.register(
                () -> new SaveInkAction("save", InkAction.Side.CLIENT, "save", command -> command.equals("save")));
        InkActionRegistry.register(() -> new SoundInkAction(
                "sound",
                InkAction.Side.CLIENT,
                "<song|sfx> <start|stop> %namespace:category.name% [%volume> %pitch% [loop=<true|false>] [<fadein|fadeout> %time%]]",
                command -> command.startsWith("song start")
                        || command.startsWith("sfx start")
                        || command.startsWith("song stop")
                        || command.startsWith("sfx stop")
                        || command.equals("sound stop all")));
        InkActionRegistry.register(() -> new SubsceneInkAction(
                "subscene",
                InkAction.Side.SERVER,
                "subscene <start,stop> %subscene_name% [loop=true/false] [unique=true/false] [block=true/false]",
                command -> command.startsWith("subscene start") || command.startsWith("subscene stop")));
        InkActionRegistry.register(() -> new WeatherInkAction(
                "weather",
                InkAction.Side.SERVER,
                "weather <clear, rain, thunder>",
                command -> command.startsWith("weather")));
        InkActionRegistry.register(() -> new ShakeScreenInkAction(
                "shake_screen",
                InkAction.Side.CLIENT,
                "shake %strength% %decay_rate% %speed%",
                command -> command.startsWith("shake")));
        InkActionRegistry.register(() -> new InteractionInkAction(
                "interaction",
                InkAction.Side.SERVER,
                "interaction <summon, remove> %interaction_name%",
                command -> command.startsWith("interaction")));
        InkActionRegistry.register(() -> new GameplayInkAction(
                "gameplay", InkAction.Side.SERVER, "gameplay", command -> command.startsWith("gameplay")));
    }
}
