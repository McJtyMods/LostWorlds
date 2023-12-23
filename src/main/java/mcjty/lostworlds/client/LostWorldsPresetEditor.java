package mcjty.lostworlds.client;

import mcjty.lostworlds.worldgen.LostWorldType;
import mcjty.lostworlds.worldgen.LostWorldsChunkGenerator;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public class LostWorldsPresetEditor implements PresetEditor {

    @Override
    public Screen createEditScreen(CreateWorldScreen worldScreen, WorldCreationContext context) {
        ChunkGenerator chunkgenerator = context.selectedDimensions().overworld();
        RegistryAccess registryaccess = context.worldgenLoadContext();
        HolderGetter<NoiseGeneratorSettings> noisegetter = registryaccess.lookupOrThrow(Registries.NOISE_SETTINGS);
        return new LostWorldsScreen(worldScreen, noisegetter, (settings, type) -> {
            worldScreen.getUiState().updateDimensions(lostWorldsConfigurator(settings, type));
        }, chunkgenerator instanceof LostWorldsChunkGenerator ? ((LostWorldsChunkGenerator)chunkgenerator).generatorSettings().get()
                : NoiseGeneratorSettings.dummy());
    }


    private static WorldCreationContext.DimensionsUpdater lostWorldsConfigurator(NoiseGeneratorSettings settings, LostWorldType type) {
        return (frozen, dimensions) -> {
            Holder.Reference<MultiNoiseBiomeSourceParameterList> params = frozen.registryOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).getHolderOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);

            BiomeSource biomesource = MultiNoiseBiomeSource.createFromPreset(params);
            ChunkGenerator chunkgenerator = new LostWorldsChunkGenerator(type, biomesource, Holder.direct(settings));
            return dimensions.replaceOverworldGenerator(frozen, chunkgenerator);
        };
    }

}
