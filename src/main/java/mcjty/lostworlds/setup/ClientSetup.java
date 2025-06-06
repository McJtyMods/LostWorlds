package mcjty.lostworlds.setup;

import mcjty.lostworlds.LostWorlds;
import mcjty.lostworlds.client.LWPresetEditor;
import mcjty.lostworlds.client.LostWorldsSpecialEffects;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.RegisterPresetEditorsEvent;

public class ClientSetup {

    private final static ResourceKey<WorldPreset> LOST_WORLD_PRESET = ResourceKey.create(Registries.WORLD_PRESET, new ResourceLocation(LostWorlds.MODID, "lost_worlds"));
    private final static ResourceKey<WorldPreset> LOST_WORLD_WASTES_PRESET = ResourceKey.create(Registries.WORLD_PRESET, new ResourceLocation(LostWorlds.MODID, "lost_worlds_wastes"));

    public static void onRegisterDimensionSpecialEffectsEvent(RegisterDimensionSpecialEffectsEvent event) {
        LostWorldsSpecialEffects effects = new LostWorldsSpecialEffects();
        Config.getDimensionsWithSpecialFog().forEach(key -> event.register(key, effects));
    }

    public static void onRegisterPresetEditorsEvent(RegisterPresetEditorsEvent event) {
        event.register(LOST_WORLD_PRESET, new LWPresetEditor(false));
        event.register(LOST_WORLD_WASTES_PRESET, new LWPresetEditor(true));
    }
}
