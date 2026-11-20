package mcjty.lostworlds.setup;

import mcjty.lostworlds.network.PacketWorldInfoToClient;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class Messages {

    private Messages() {
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(PacketWorldInfoToClient.TYPE, PacketWorldInfoToClient.STREAM_CODEC);
    }

    public static void sendToPlayer(PacketWorldInfoToClient packet, ServerPlayer player) {
        ServerPlayNetworking.send(player, packet);
    }
}
