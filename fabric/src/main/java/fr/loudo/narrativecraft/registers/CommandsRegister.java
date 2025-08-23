package fr.loudo.narrativecraft.registers;

import fr.loudo.narrativecraft.commands.OpenScreenCommand;
import fr.loudo.narrativecraft.commands.PlayerSessionCommand;
import fr.loudo.narrativecraft.commands.RecordCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CommandsRegister {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((commandDispatcher, commandBuildContext, commandSelection) -> {
            PlayerSessionCommand.register(commandDispatcher);
            RecordCommand.register(commandDispatcher);
//            StoryCommand.register(commandDispatcher);
            OpenScreenCommand.register(commandDispatcher);
//            LinkCommand.register(commandDispatcher);
        });
    }

}