package mcjty.lostworlds;

import mcjty.lostworlds.client.LWPresetEditor;
import mcjty.lostworlds.network.PacketWorldInfoToClient;
import mcjty.lostworlds.setup.Messages;
import mcjty.lostworlds.worldgen.FogColor;
import mcjty.lostworlds.worldgen.LostWorldType;
import mcjty.lostworlds.worldgen.LWChunkGenerator;
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
    private final static ResourceKey<WorldPreset> LOST_WORLD_WASTES_PRESET = ResourceKey.create(Registries.WORLD_PRESET, new ResourceLocation(LostWorlds.MODID, "lost_worlds_wastes"));

    public static void onRegisterPresetEditorsEvent(RegisterPresetEditorsEvent event) {
        event.register(LOST_WORLD_PRESET, new LWPresetEditor(false));
        event.register(LOST_WORLD_WASTES_PRESET, new LWPresetEditor(true));
    }

    public static void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            if (((ServerLevel)player.level()).getChunkSource().getGenerator() instanceof LWChunkGenerator generator) {
                LostWorldType type = generator.getLwSettings().type();
                FogColor fogColor = generator.getLwSettings().fogColor();
                Messages.INSTANCE.sendTo(new PacketWorldInfoToClient(type, fogColor), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            } else {
                Messages.INSTANCE.sendTo(new PacketWorldInfoToClient(LostWorldType.ISLANDS, FogColor.NONE), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            }
        }
    }
}
