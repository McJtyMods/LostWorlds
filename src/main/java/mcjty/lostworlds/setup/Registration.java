package mcjty.lostworlds.setup;

import mcjty.lostworlds.worldgen.IslandsDensityFunction;
import mcjty.lostworlds.worldgen.LWChunkGenerator;
import mcjty.lostworlds.worldgen.wastes.WastesBiomeSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import static mcjty.lostworlds.LostWorlds.MODID;

public final class Registration {

    private Registration() {
    }

    public static void init() {
        Registry.register(BuiltInRegistries.BIOME_SOURCE, Identifier.fromNamespaceAndPath(MODID, "wasted_biomes"), WastesBiomeSource.CODEC);
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, LWChunkGenerator.LOSTWORLDS_CHUNKGEN, LWChunkGenerator.CODEC);
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, IslandsDensityFunction.LOST_ISLANDS, IslandsDensityFunction.CODEC);
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, IslandsDensityFunction.LOST_ISLANDS_CHEESE, IslandsDensityFunction.CODEC_CHEESE);
    }
}
