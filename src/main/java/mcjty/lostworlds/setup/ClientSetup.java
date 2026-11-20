package mcjty.lostworlds.setup;

import mcjty.lostworlds.LostWorlds;
import mcjty.lostworlds.client.ClientFog;
import mcjty.lostworlds.client.LWPresetEditor;
import mcjty.lostworlds.network.PacketWorldInfoToClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import javax.annotation.Nullable;

public final class ClientSetup implements ClientModInitializer {

    private static final PresetEditor NORMAL = new LWPresetEditor(false);
    private static final PresetEditor WASTES = new LWPresetEditor(true);

    @Override
    public void onInitializeClient() {
        Config.registerCustomConfig();
        ClientPlayNetworking.registerGlobalReceiver(PacketWorldInfoToClient.TYPE, (payload, context) -> ClientFog.update(payload));
    }

    @Nullable
    public static PresetEditor presetEditor(ResourceKey<WorldPreset> preset) {
        if (!LostWorlds.MODID.equals(preset.identifier().getNamespace())) {
            return null;
        }
        return switch (preset.identifier().getPath()) {
            case "lost_worlds" -> NORMAL;
            case "lost_worlds_wastes" -> WASTES;
            default -> null;
        };
    }
}
