package mcjty.lostworlds.setup;

import mcjty.lostworlds.worldgen.IslandsDensityFunction;
import mcjty.lostworlds.worldgen.LWChunkGenerator;
import mcjty.lostworlds.worldgen.wastes.WastesBiomeSource;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import static mcjty.lostworlds.LostWorlds.MODID;

public final class Registration {

    private static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES = DeferredRegister.create(Registries.BIOME_SOURCE, MODID);
    private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> GENERATORS = DeferredRegister.create(Registries.CHUNK_GENERATOR, MODID);
    private static final DeferredRegister<MapCodec<? extends DensityFunction>> DENSITY_FUNCTIONS = DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, MODID);

    private Registration() {
    }

    public static void init(IEventBus bus) {
        BIOME_SOURCES.register("wasted_biomes", () -> WastesBiomeSource.CODEC);
        GENERATORS.register(LWChunkGenerator.LOSTWORLDS_CHUNKGEN.getPath(), () -> LWChunkGenerator.CODEC);
        DENSITY_FUNCTIONS.register(IslandsDensityFunction.LOST_ISLANDS.getPath(), IslandsDensityFunction.CODEC::codec);
        DENSITY_FUNCTIONS.register(IslandsDensityFunction.LOST_ISLANDS_CHEESE.getPath(), IslandsDensityFunction.CODEC_CHEESE::codec);
        BIOME_SOURCES.register(bus);
        GENERATORS.register(bus);
        DENSITY_FUNCTIONS.register(bus);
    }
}
