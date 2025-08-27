package mcjty.lostworlds;

import mcjty.lostworlds.network.PacketWorldInfoToClient;
import mcjty.lostworlds.setup.Messages;
import mcjty.lostworlds.worldgen.FogColor;
import mcjty.lostworlds.worldgen.LWChunkGenerator;
import mcjty.lostworlds.worldgen.LostWorldType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = LostWorlds.MODID)
public class EventHandlers {

	@SubscribeEvent
    public static void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            if (((ServerLevel)player.level()).getChunkSource().getGenerator() instanceof LWChunkGenerator generator) {
                LostWorldType type = generator.getLwSettings().type();
                FogColor fogColor = generator.getLwSettings().fogColor();
                Messages.sendToPlayer(new PacketWorldInfoToClient(type, fogColor), player);
            } else {
                Messages.sendToPlayer(new PacketWorldInfoToClient(LostWorldType.ISLANDS, FogColor.NONE), player);
            }
        }
    }
}
