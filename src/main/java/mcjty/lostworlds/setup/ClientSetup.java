package mcjty.lostworlds.setup;

import mcjty.lostworlds.LostWorlds;
import mcjty.lostworlds.client.LWPresetEditor;
import mcjty.lostworlds.client.ClientFog;
import mcjty.lostworlds.network.PacketWorldInfoToClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterPresetEditorsEvent;

public class ClientSetup {

    private final static ResourceKey<WorldPreset> LOST_WORLD_PRESET = ResourceKey.create(Registries.WORLD_PRESET, Identifier.fromNamespaceAndPath(LostWorlds.MODID, "lost_worlds"));
    private final static ResourceKey<WorldPreset> LOST_WORLD_WASTES_PRESET = ResourceKey.create(Registries.WORLD_PRESET, Identifier.fromNamespaceAndPath(LostWorlds.MODID, "lost_worlds_wastes"));

    public static void registerPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(PacketWorldInfoToClient.TYPE, (packet, context) -> ClientFog.update(packet));
    }

    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        Vec3 color = ClientFog.color(Minecraft.getInstance().level);
        if (color != null) {
            event.setRed((float) color.x);
            event.setGreen((float) color.y);
            event.setBlue((float) color.z);
        }
    }

    public static void onRegisterPresetEditorsEvent(RegisterPresetEditorsEvent event) {
        event.register(LOST_WORLD_PRESET, new LWPresetEditor(false));
        event.register(LOST_WORLD_WASTES_PRESET, new LWPresetEditor(true));
    }
}
