package mcjty.lostworlds.client;

import mcjty.lostworlds.worldgen.LWChunkGenerator;
import mcjty.lostworlds.worldgen.LWSettings;
import mcjty.lostworlds.worldgen.wastes.WastesBiomeSource;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class LWPresetEditor implements PresetEditor {

    private final boolean wastes;

    public LWPresetEditor(boolean wastes) {
        this.wastes = wastes;
    }

    @Override
    public Screen createEditScreen(CreateWorldScreen worldScreen, WorldCreationContext context) {
        ChunkGenerator chunkgenerator = context.selectedDimensions().overworld();
        RegistryAccess registryaccess = context.worldgenLoadContext();
        HolderGetter<NoiseGeneratorSettings> noisegetter = registryaccess.lookupOrThrow(Registries.NOISE_SETTINGS);
        return new LWScreen(worldScreen, noisegetter, (settings, lwSettings) -> {
            worldScreen.getUiState().updateDimensions(lostWorldsConfigurator(settings, lwSettings));
        }, chunkgenerator instanceof LWChunkGenerator ? ((LWChunkGenerator)chunkgenerator).generatorSettings().get()
                : NoiseGeneratorSettings.dummy());
    }


    private WorldCreationContext.DimensionsUpdater lostWorldsConfigurator(NoiseGeneratorSettings settings, LWSettings lwSettings) {
        return (frozen, dimensions) -> {
            BiomeSource biomesource;
            Holder.Reference<MultiNoiseBiomeSourceParameterList> params = frozen.registryOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).getHolderOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);
            biomesource = MultiNoiseBiomeSource.createFromPreset(params);
            if (wastes) {
                HolderLookup.RegistryLookup<Biome> registry = frozen.registryOrThrow(Registries.BIOME).asLookup();
                biomesource = new WastesBiomeSource(registry, "minecraft:overworld",
                        biomesource, Optional.of("minecraft:desert"), Optional.of("minecraft:jungle"), getBiomeMappingList(), Optional.empty());
            }
            ChunkGenerator chunkgenerator = new LWChunkGenerator(lwSettings, biomesource, Holder.direct(settings));
            return dimensions.replaceOverworldGenerator(frozen, chunkgenerator);
        };
    }

    private static List<String> getBiomeMappingList() {
        return Arrays.asList(
                "minecraft:badlands=minecraft:badlands",
                "minecraft:bamboo_jungle=minecraft:wooded_badlands",
                "minecraft:beach=minecraft:beach",
                "minecraft:birch_forest=minecraft:wooded_badlands",
                "minecraft:cold_ocean=minecraft:cold_ocean",
                "minecraft:crimson_forest=minecraft:wooded_badlands",
                "minecraft:dark_forest=minecraft:wooded_badlands",
                "minecraft:deep_dark=minecraft:deep_dark",
                "minecraft:deep_frozen_ocean=minecraft:deep_frozen_ocean",
                "minecraft:deep_lukewarm_ocean=minecraft:deep_lukewarm_ocean",
                "minecraft:deep_ocean=minecraft:deep_ocean",
                "minecraft:dripstone_caves=minecraft:dripstone_caves",
                "minecraft:end_barrens=minecraft:end_barrens",
                "minecraft:end_highlands=minecraft:end_highlands",
                "minecraft:end_midlands=minecraft:end_midlands",
                "minecraft:eroded_badlands=minecraft:eroded_badlands",
                "minecraft:flower_forest=minecraft:wooded_badlands",
                "minecraft:forest=minecraft:wooded_badlands",
                "minecraft:frozen_ocean=minecraft:frozen_ocean",
                "minecraft:frozen_peaks=minecraft:frozen_peaks",
                "minecraft:frozen_river=minecraft:frozen_river",
                "minecraft:ice_spikes=minecraft:ice_spikes",
                "minecraft:jagged_peaks=minecraft:jagged_peaks",
                "minecraft:jungle=minecraft:wooded_badlands",
                "minecraft:lukewarm_ocean=minecraft:lukewarm_ocean",
                "minecraft:lush_caves=minecraft:dripstone_caves",
                "minecraft:nether_wastes=minecraft:nether_wastes",
                "minecraft:ocean=minecraft:ocean",
                "minecraft:old_growth_birch_forest=minecraft:wooded_badlands",
                "minecraft:old_growth_pine_taiga=minecraft:wooded_badlands",
                "minecraft:old_growth_spruce_taiga=minecraft:wooded_badlands",
                "minecraft:savanna=minecraft:savanna",
                "minecraft:savanna_plateau=minecraft:savanna_plateau",
                "minecraft:small_end_islands=minecraft:small_end_islands",
                "minecraft:soul_sand_valley=minecraft:soul_sand_valley",
                "minecraft:stony_peaks=minecraft:windswept_gravelly_hills",
                "minecraft:the_end=minecraft:the_end",
                "minecraft:the_void=minecraft:the_void",
                "minecraft:warm_ocean=minecraft:warm_ocean",
                "minecraft:warped_forest=minecraft:wooded_badlands",
                "minecraft:windswept_forest=minecraft:wooded_badlands",
                "minecraft:windswept_gravelly_hills=minecraft:windswept_gravelly_hills",
                "minecraft:windswept_hills=minecraft:windswept_hills",
                "minecraft:windswept_savanna=minecraft:windswept_savanna",
                "minecraft:wooded_badlands=minecraft:wooded_badlands"
        );
    }

}
