package fr.loudo.narrativecraft.util;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.entity.player.Player;

public class Util {

    public static String snakeCase(String value) {
        return String.join("_", value.toLowerCase().split(" "));
    }

    public static void sendCrashMessage(Player player, Exception exception) {
        String message = exception.getMessage();
        if(message == null) {
            message = "";
        }
        String finalMessage = message;
        player.displayClientMessage(
                Translation.message("crash.global-message")
                        .withStyle(ChatFormatting.RED)
                        .withStyle((style) -> style
                                .withHoverEvent(new HoverEvent.ShowText(Component.literal(finalMessage)))),
                false
        );
        NarrativeCraftMod.LOGGER.error("Unexpected error occurred on NarrativeCraft: ", exception);
    }
}
