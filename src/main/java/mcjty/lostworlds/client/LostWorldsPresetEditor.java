package mcjty.lostworlds.client;

import mcjty.lostworlds.LostWorldsGeneratorSettings;
import mcjty.lostworlds.LostWorldsSource;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public class LostWorldsPresetEditor implements PresetEditor {

    @Override
    public Screen createEditScreen(CreateWorldScreen worldScreen, WorldCreationContext context) {
        ChunkGenerator chunkgenerator = context.selectedDimensions().overworld();
        RegistryAccess registryaccess = context.worldgenLoadContext();
        HolderGetter<Biome> holdergetter = registryaccess.lookupOrThrow(Registries.BIOME);
        HolderGetter<StructureSet> holdergetter1 = registryaccess.lookupOrThrow(Registries.STRUCTURE_SET);
        HolderGetter<PlacedFeature> holdergetter2 = registryaccess.lookupOrThrow(Registries.PLACED_FEATURE);
        return new LostWorldsScreen(worldScreen, (settings) -> {
            worldScreen.getUiState().updateDimensions(lostWorldsConfigurator(settings));
        }, chunkgenerator instanceof LostWorldsSource ? ((LostWorldsSource)chunkgenerator).settings()
                : LostWorldsGeneratorSettings.getDefault(holdergetter, holdergetter1, holdergetter2));
    }


    private static WorldCreationContext.DimensionsUpdater lostWorldsConfigurator(LostWorldsGeneratorSettings settings) {
        return (frozen, dimensions) -> {
            ChunkGenerator chunkgenerator = new LostWorldsSource(settings);
            return dimensions.replaceOverworldGenerator(frozen, chunkgenerator);
        };
    }

}
