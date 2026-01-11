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

package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.inkAction.InkActionUtil;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

/**
 * MC 1.20.x version of WeatherInkAction.
 * Key differences from 1.21.x:
 * - player.level() returns Level, need explicit cast to ServerLevel
 */
public class WeatherInkAction extends InkAction {

    private String weather;
    private boolean instantly;

    public WeatherInkAction(String id, Side side, String syntax, CommandMatcher matcher) {
        super(id, side, syntax, matcher);
    }

    @Override
    protected InkActionResult doValidate(List<String> arguments, Scene scene) {
        if (arguments.size() == 1) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "clear, rain or thunder"));
        }
        weather = arguments.get(1);
        if (!weather.equals("clear") && !weather.equals("rain") && !weather.equals("thunder")) {
            return InkActionResult.error(Translation.message(WRONG_ARGUMENT_TEXT, "Only clear, rain or thunder"));
        }
        instantly = InkActionUtil.getOptionalArgument(command, "instant");
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(PlayerSession playerSession) {
        // MC 1.20.x: Cast Level to ServerLevel explicitly
        ServerLevel level = (ServerLevel) playerSession.getPlayer().level();
        ServerGamePacketListenerImpl connection = playerSession.getPlayer().connection;
        boolean isSinglePlayer = level.getServer().isSingleplayer();
        switch (weather) {
            case "clear" -> {
                if (instantly || !isSinglePlayer) {
                    connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.STOP_RAINING, 0.0F));
                    connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, 0.0F));
                    connection.send(
                            new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, 0.0F));
                } else {
                    level.setWeatherParameters(999999, 0, false, false);
                }
            }
            case "rain" -> {
                if (instantly || !isSinglePlayer) {
                    connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0F));
                    connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, 1.0F));
                    connection.send(
                            new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, 0.0F));
                } else {
                    level.setWeatherParameters(0, 999999, true, false);
                }
            }
            case "thunder" -> {
                if (instantly || !isSinglePlayer) {
                    connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0F));
                    connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, 1.0F));
                    connection.send(
                            new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, 1.0F));
                } else {
                    level.setWeatherParameters(0, 999999, true, true);
                }
            }
            default -> {
                return InkActionResult.error("Weather value is not correct.");
            }
        }
        playerSession.getInkActions().removeIf(inkAction -> inkAction instanceof WeatherInkAction);
        return InkActionResult.ok();
    }

    @Override
    public boolean needScene() {
        return false;
    }
}
