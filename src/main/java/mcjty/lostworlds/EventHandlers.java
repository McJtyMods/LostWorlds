package mcjty.lostworlds;

import mcjty.lostworlds.client.LostWorldsPresetEditor;
import mcjty.lostworlds.network.PacketWorldInfoToClient;
import mcjty.lostworlds.setup.Messages;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraftforge.client.event.RegisterPresetEditorsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.network.NetworkDirection;

public class EventHandlers {

    private final static ResourceKey<WorldPreset> LOST_WORLD_PRESET = ResourceKey.create(Registries.WORLD_PRESET, new ResourceLocation(LostWorlds.MODID, "lost_worlds"));

    public static void onRegisterPresetEditorsEvent(RegisterPresetEditorsEvent event) {
        event.register(LOST_WORLD_PRESET, new LostWorldsPresetEditor());
    }

    public static void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            boolean isFlat = ((ServerLevel)player.level()).getChunkSource().getGenerator() instanceof LostWorldsChunkGenerator;
            Messages.INSTANCE.sendTo(new PacketWorldInfoToClient(isFlat), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
    }
}
