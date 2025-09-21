package fr.loudo.narrativecraft.platform;

import fr.loudo.narrativecraft.platform.services.IPacketSender;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class FabricPacketSender implements IPacketSender {

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {

    }
}
