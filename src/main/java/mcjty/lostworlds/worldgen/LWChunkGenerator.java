package mcjty.lostworlds.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostworlds.LostWorlds;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayList;
import java.util.List;

public class LWChunkGenerator extends NoiseBasedChunkGenerator {

    public static final ResourceLocation LOSTWORLDS_CHUNKGEN = new ResourceLocation(LostWorlds.MODID, "lostworlds");
    public static final ResourceKey<NoiseGeneratorSettings> LOST_ISLANDS = ResourceKey.create(Registries.NOISE_SETTINGS, new ResourceLocation(LostWorlds.MODID, "lost_islands"));
    public static final ResourceKey<NoiseGeneratorSettings> LOST_CAVES = ResourceKey.create(Registries.NOISE_SETTINGS, new ResourceLocation(LostWorlds.MODID, "lost_caves"));
    public static final ResourceKey<NoiseGeneratorSettings> LOST_VOID = ResourceKey.create(Registries.NOISE_SETTINGS, new ResourceLocation(LostWorlds.MODID, "lost_void"));

    private final LWSettings lwSettings;

    public static final Codec<LWChunkGenerator> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                LWSettings.CODEC.fieldOf("lwsettings").forGetter(LWChunkGenerator::getLwSettings),
                BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
                NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(NoiseBasedChunkGenerator::generatorSettings))
            .apply(instance, instance.stable(LWChunkGenerator::new)));

    public LWChunkGenerator(LWSettings lwSettings, BiomeSource source, Holder<NoiseGeneratorSettings> settings) {
        super(source, settings);
        this.lwSettings = lwSettings;
    }

    public LWSettings getLwSettings() {
        return lwSettings;
    }

    @Override
    public void createStructures(RegistryAccess access, ChunkGeneratorStructureState structureState, StructureManager structureManager, ChunkAccess chunk, StructureTemplateManager templateManager) {
        ChunkPos chunkpos = chunk.getPos();
        SectionPos sectionpos = SectionPos.bottomOf(chunk);
        RandomState randomstate = structureState.randomState();
        structureState.possibleStructureSets().forEach((set) -> {
            StructurePlacement structureplacement = set.value().placement();
            List<StructureSet.StructureSelectionEntry> list = set.value().structures();
            ResourceKey<StructureSet> key = set.unwrapKey().get();
            if (lwSettings.type().blocksStructure(key)) {
                return;
            }

            for (StructureSet.StructureSelectionEntry entry : list) {
                StructureStart structurestart = structureManager.getStartForStructure(sectionpos, entry.structure().value(), chunk);
                if (structurestart != null && structurestart.isValid()) {
                    return;
                }
            }

            if (structureplacement.isStructureChunk(structureState, chunkpos.x, chunkpos.z)) {
                if (list.size() == 1) {
                    this.tryGenerateStructure(list.get(0), structureManager, access, randomstate, templateManager, structureState.getLevelSeed(), chunk, chunkpos, sectionpos);
                } else {
                    List<StructureSet.StructureSelectionEntry> listCopy = new ArrayList<>(list);
                    WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
                    worldgenrandom.setLargeFeatureSeed(structureState.getLevelSeed(), chunkpos.x, chunkpos.z);
                    int totalweight = 0;
                    for (StructureSet.StructureSelectionEntry entry : listCopy) {
                        totalweight += entry.weight();
                    }

                    while (!listCopy.isEmpty()) {
                        int w = worldgenrandom.nextInt(totalweight);
                        int selected = 0;

                        for (StructureSet.StructureSelectionEntry entry : listCopy) {
                            w -= entry.weight();
                            if (w < 0) {
                                break;
                            }
                            ++selected;
                        }

                        StructureSet.StructureSelectionEntry entry = listCopy.get(selected);
                        if (this.tryGenerateStructure(entry, structureManager, access, randomstate, templateManager, structureState.getLevelSeed(), chunk, chunkpos, sectionpos)) {
                            return;
                        }

                        listCopy.remove(selected);
                        totalweight -= entry.weight();
                    }

                }
            }
        });
    }

    private boolean tryGenerateStructure(StructureSet.StructureSelectionEntry entry, StructureManager structureManager, RegistryAccess registryAccess, RandomState rnd, StructureTemplateManager templateManager, long seed, ChunkAccess chunk, ChunkPos cp, SectionPos sp) {
        Structure structure = entry.structure().value();
        StructureStart start = structureManager.getStartForStructure(sp, structure, chunk);
        int references = start != null ? start.getReferences() : 0;
        StructureStart structurestart = structure.generate(registryAccess, this, this.biomeSource, rnd, templateManager, seed, cp, references, chunk, structure.biomes()::contains);
        if (structurestart.isValid()) {
            structureManager.setStartForStructure(sp, structure, structurestart, chunk);
            return true;
        } else {
            return false;
        }
    }


    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }
}
