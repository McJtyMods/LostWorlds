package mcjty.lostworlds;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class LostWorldsChunkGenerator extends NoiseBasedChunkGenerator {

    public static final ResourceLocation LOSTWORLDS_CHUNKGEN = new ResourceLocation(LostWorlds.MODID, "lostworlds");
    public static final ResourceKey<NoiseGeneratorSettings> LOST_ISLANDS = ResourceKey.create(Registries.NOISE_SETTINGS, new ResourceLocation(LostWorlds.MODID, "lost_islands"));
    public static final ResourceKey<NoiseGeneratorSettings> LOST_CAVES = ResourceKey.create(Registries.NOISE_SETTINGS, new ResourceLocation(LostWorlds.MODID, "lost_caves"));

    public static final Codec<NoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(NoiseBasedChunkGenerator::generatorSettings))
            .apply(instance, instance.stable(LostWorldsChunkGenerator::new)));

    public LostWorldsChunkGenerator(BiomeSource source, Holder<NoiseGeneratorSettings> settings) {
        super(source, settings);
    }

    @Override
    public void createStructures(RegistryAccess registryAccess, ChunkGeneratorStructureState structureState, StructureManager structureManager, ChunkAccess chunk, StructureTemplateManager templateManager) {
        super.createStructures(registryAccess, structureState, structureManager, chunk, templateManager);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }
}
