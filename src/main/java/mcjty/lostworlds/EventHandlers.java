package mcjty.lostworlds;

import mcjty.lostworlds.network.PacketWorldInfoToClient;
import mcjty.lostworlds.setup.Messages;
import mcjty.lostworlds.worldgen.FogColor;
import mcjty.lostworlds.worldgen.LWChunkGenerator;
import mcjty.lostworlds.worldgen.LostWorldType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class EventHandlers {

    public static void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        sendWorldInfo(event.getEntity());
    }

    public static void onPlayerChangedDimensionEvent(PlayerEvent.PlayerChangedDimensionEvent event) {
        sendWorldInfo(event.getEntity());
    }

    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        sendWorldInfo(event.getEntity());
    }

    public static void sendWorldInfo(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.level().getChunkSource().getGenerator() instanceof LWChunkGenerator generator) {
                Messages.sendToPlayer(new PacketWorldInfoToClient(generator.getLwSettings().type(), generator.getLwSettings().fogColor(), false), serverPlayer);
            } else {
                Messages.sendToPlayer(new PacketWorldInfoToClient(LostWorldType.NORMAL, FogColor.NONE, true), serverPlayer);
            }
        }
    }
}
