package mcjty.lostworlds.setup;

import mcjty.lostworlds.client.LostWorldsSpecialEffects;
import mcjty.lostworlds.network.PacketWorldInfoToClient;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class Messages {

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1.0").playToClient(PacketWorldInfoToClient.TYPE, PacketWorldInfoToClient.STREAM_CODEC,
                (packet, context) -> {
                    LostWorldsSpecialEffects.type = packet.worldType();
                    LostWorldsSpecialEffects.fogColor = packet.fogColor();
                    LostWorldsSpecialEffects.disabled = packet.disabled();
                });
    }

    public static void sendToPlayer(PacketWorldInfoToClient packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }
}
