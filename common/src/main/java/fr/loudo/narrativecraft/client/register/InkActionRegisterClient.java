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

package fr.loudo.narrativecraft.client.register;

import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionRegistry;
import fr.loudo.narrativecraft.narrative.story.inkAction.client.*;
import fr.loudo.narrativecraft.narrative.story.inkAction.client.sound.SoundInkAction;
import fr.loudo.narrativecraft.narrative.story.inkAction.client.text.TextInkAction;
import fr.loudo.narrativecraft.narrative.story.inkAction.server.*;

public class InkActionRegisterClient {
    public static void register() {
        InkActionRegistry.register(() -> new BorderInkAction(
                "border",
                InkAction.Side.CLIENT,
                "border %up% %right% %down% %left% [%color%] [%opacity%]",
                command -> command.startsWith("border")));
        InkActionRegistry.register(() -> new ChangeDayTimeInkAction(
                "change_day_time",
                InkAction.Side.CLIENT,
                "time <set,add> <day,midnight,night,noon,%tick%> [to <day,midnight,night,noon,%tick%> for %time% <second(s), minute(s), hour(s)> [%easing%]]",
                command -> command.startsWith("time set") || command.startsWith("time add")));
        InkActionRegistry.register(() -> new DialogParametersInkAction(
                "dialog",
                InkAction.Side.CLIENT,
                "dialog <offset, scale, padding, width, textColor, backgroundColor, gap, letterSpacing, unSkippable, autoSkip, bobbing> [%value1%] [%value2%]",
                command -> command.startsWith("dialog")));
        InkActionRegistry.register(() -> new FadeInkAction(
                "fade",
                InkAction.Side.CLIENT,
                "fade %fadeInValue% %stayValue% %fadeOutValue% %color%",
                command -> command.startsWith("fade")));
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
        InkActionRegistry.register(() -> new ShakeScreenInkAction(
                "shake_screen",
                InkAction.Side.CLIENT,
                "shake %strength% %decay_rate% %speed%",
                command -> command.startsWith("shake")));
        InkActionRegistry.register(() -> new TextInkAction(
                "text",
                InkAction.Side.CLIENT,
                "text %id% <create|remove|edit|position|color|scale|spacing|width|fade|fadein|fadeout|type|font|sound> %...values%",
                command -> command.startsWith("text")));
    }
}
