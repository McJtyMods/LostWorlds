package mcjty.lostworlds.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.varia.NoiseGeneratorPerlin;
import mcjty.lostworlds.LostWorlds;
import mcjty.lostcities.api.ILostWorldsChunkGenerator;
import mcjty.lostworlds.compat.LostCitiesCompat;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayList;
import java.util.List;

public class LWChunkGenerator extends NoiseBasedChunkGenerator implements ILostWorldsChunkGenerator {

    public static final ResourceLocation LOSTWORLDS_CHUNKGEN = new ResourceLocation(LostWorlds.MODID, "lostworlds");
    public static final ResourceKey<NoiseGeneratorSettings> LOST_ISLANDS = ResourceKey.create(Registries.NOISE_SETTINGS, new ResourceLocation(LostWorlds.MODID, "lost_islands"));
    public static final ResourceKey<NoiseGeneratorSettings> LOST_CAVES = ResourceKey.create(Registries.NOISE_SETTINGS, new ResourceLocation(LostWorlds.MODID, "lost_caves"));
    public static final ResourceKey<NoiseGeneratorSettings> LOST_SPHERES = ResourceKey.create(Registries.NOISE_SETTINGS, new ResourceLocation(LostWorlds.MODID, "lost_spheres"));
    public static final ResourceKey<NoiseGeneratorSettings> LOST_NORMAL = ResourceKey.create(Registries.NOISE_SETTINGS, new ResourceLocation(LostWorlds.MODID, "lost_normal"));
    public static final ResourceKey<NoiseGeneratorSettings> LOST_ATLANTIS = ResourceKey.create(Registries.NOISE_SETTINGS, new ResourceLocation(LostWorlds.MODID, "lost_atlantis"));
    public static final float GROUND_SCALE = 3.0f;

    private final LWSettings lwSettings;
    private final NoiseGeneratorPerlin groundNoise; // For the deep ground in the ocean
    private double[] groundBuffer = new double[256];
    private Level cachedLevel;

    public static final Codec<LWChunkGenerator> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                LWSettings.CODEC.fieldOf("lwsettings").forGetter(LWChunkGenerator::getLwSettings),
                BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
                NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(NoiseBasedChunkGenerator::generatorSettings))
            .apply(instance, instance.stable(LWChunkGenerator::new)));

    public LWChunkGenerator(LWSettings lwSettings, BiomeSource source, Holder<NoiseGeneratorSettings> settings) {
        super(source, settings);
        this.lwSettings = lwSettings;
        RandomSource rand = new LegacyRandomSource(0L);
        this.groundNoise = new NoiseGeneratorPerlin(rand, 4);
    }

    public LWSettings getLwSettings() {
        return lwSettings;
    }

    @Override
    public ChunkAccess doFill(Blender blender, StructureManager structureManager, RandomState random, ChunkAccess chunkAccess, int minCellY, int cellCountY) {
        ChunkPos cp = chunkAccess.getPos();
        boolean customSea = lwSettings.hasCustomSea();
        if (customSea) {
            this.groundBuffer = this.groundNoise.getRegion(this.groundBuffer, (cp.x * 16), (cp.z * 16), 16, 16, 1.0 / 16.0, 1.0 / 16.0, 1.0D);
        }

        if (lwSettings.type() == LostWorldType.SPHERES) {
            WorldGenRegion region = (WorldGenRegion) structureManager.level;
            cachedLevel = region.getLevel();
            LostCitiesCompat.LostCitiesContext context = LostCitiesCompat.getLostCitiesContext(region.getLevel());
            // Only generate the chunk if we have Lost Cities and we are in a sphere
            if (context != null && context.isInSphereFullOrPartially(cp.getMinBlockX(), cp.getMinBlockZ())) {
                chunkAccess = super.doFill(blender, structureManager, random, chunkAccess, minCellY, cellCountY);
                int minBuildHeight = chunkAccess.getMinBuildHeight();
                int maxBuildHeight = chunkAccess.getMaxBuildHeight();
                int minSphereY = context.getMinSphereY(cp.getMinBlockX(), cp.getMinBlockZ());
                int maxSphereY = context.getMaxSphereY(cp.getMinBlockX(), cp.getMinBlockZ());
                BlockState defaultBlock = generatorSettings().get().defaultBlock();
                BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
                BlockState air = Blocks.AIR.defaultBlockState();
                BlockState water;
                if (customSea) {
                    water = Blocks.WATER.defaultBlockState();
                } else {
                    water = air;
                }
                int level = lwSettings.seaLevel() == null ? -63 : lwSettings.seaLevel();
                for (int y = minBuildHeight; y < maxBuildHeight; ++y) {
                    BlockState block = y <= level ? water : air;
                    LevelChunkSection levelchunksection = chunkAccess.getSection(chunkAccess.getSectionIndex(y));
                    if (y >= minSphereY && y <= maxSphereY) {
                        for (int x = 0; x < 16; ++x) {
                            for (int z = 0; z < 16; ++z) {
                                if (!context.isInSphere(cp.getMinBlockX() + x, y, cp.getMinBlockZ() + z)) {
                                    if (customSea && y < getGroundLevel(x, z)) {
                                        levelchunksection.setBlockState(x, y & 15, z, y < minBuildHeight + 2 ? bedrock : defaultBlock, false);
                                    } else {
                                        levelchunksection.setBlockState(x, y & 15, z, block, false);
                                    }
                                }
                            }
                        }
                    } else {
                        for (int x = 0; x < 16; ++x) {
                            for (int z = 0; z < 16; ++z) {
                                if (customSea && y < getGroundLevel(x, z)) {
                                    levelchunksection.setBlockState(x, y & 15, z, y < minBuildHeight + 2 ? bedrock : defaultBlock, false);
                                } else {
                                    levelchunksection.setBlockState(x, y & 15, z, block, false);
                                }
                            }
                        }
                    }
                }
            } else {
                if (customSea) {
                    fillCustomSea(chunkAccess, cp);
                }
            }
        } else if (lwSettings.type() == LostWorldType.CAVESPHERES) {
            chunkAccess = super.doFill(blender, structureManager, random, chunkAccess, minCellY, cellCountY);
            WorldGenRegion region = (WorldGenRegion) structureManager.level;
            cachedLevel = region.getLevel();
            LostCitiesCompat.LostCitiesContext context = LostCitiesCompat.getLostCitiesContext(region.getLevel());
            int minBuildHeight = chunkAccess.getMinBuildHeight();
            int minSphereY = context.getMinSphereY(cp.getMinBlockX(), cp.getMinBlockZ());
            int maxSphereY = context.getMaxSphereY(cp.getMinBlockX(), cp.getMinBlockZ());
            BlockState defaultBlock = generatorSettings().get().defaultBlock();
            BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
            BlockState air = Blocks.AIR.defaultBlockState();
//            int middleY = (minSphereY + maxSphereY) / 2;
            for (int y = minSphereY; y <= maxSphereY; ++y) {
//                BlockState block = y <= middleY ? defaultBlock : air;
                LevelChunkSection levelchunksection = chunkAccess.getSection(chunkAccess.getSectionIndex(y));
                for (int x = 0; x < 16; ++x) {
                    for (int z = 0; z < 16; ++z) {
                        if (!context.isInSphere(cp.getMinBlockX() + x, y, cp.getMinBlockZ() + z)) {
                            if (y < getGroundLevel(x, z)) {
                                levelchunksection.setBlockState(x, y & 15, z, y < minBuildHeight + 2 ? bedrock : defaultBlock, false);
                            } else {
                                levelchunksection.setBlockState(x, y & 15, z, air, false);
                            }
                        }
                    }
                }
            }
        } else {
            chunkAccess = super.doFill(blender, structureManager, random, chunkAccess, minCellY, cellCountY);
            if (customSea) {
                fillCustomSea(chunkAccess, cp);
            }
        }
        return chunkAccess;
    }

    private int getGroundLevel(int x, int z) {
        double vr = -15 + groundBuffer[x + z * 16] / GROUND_SCALE;
        return (int) vr;
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {
        if (lwSettings.type() == LostWorldType.CAVES || lwSettings.type() == LostWorldType.NORMAL || lwSettings.type() == LostWorldType.ATLANTIS || lwSettings.type() == LostWorldType.CAVESPHERES) {
            super.applyCarvers(level, seed, random, biomeManager, structureManager, chunk, step);
        }  else if (lwSettings.type() == LostWorldType.SPHERES) {
            ChunkPos cp = chunk.getPos();
            WorldGenRegion region = (WorldGenRegion) structureManager.level;
            cachedLevel = region.getLevel();
            LostCitiesCompat.LostCitiesContext context = LostCitiesCompat.getLostCitiesContext(region.getLevel());
            // Only apply carvers if we have Lost Cities and we are in a sphere
            if (lwSettings.hasCustomSea()) {
                // We only do carvers if the chunk is fully in a sphere because otherwise we get weird caves in the water
                if (context != null && context.isInSphereFull(cp.getMinBlockX(), cp.getMinBlockZ())) {
                    super.applyCarvers(level, seed, random, biomeManager, structureManager, chunk, step);
                }
            } else {
                if (context != null && context.isInSphereFullOrPartially(cp.getMinBlockX(), cp.getMinBlockZ())) {
                    super.applyCarvers(level, seed, random, biomeManager, structureManager, chunk, step);
                }
            }
        }
    }

    private void fillCustomSea(ChunkAccess chunkAccess, ChunkPos cp) {
        Heightmap heightmap = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap heightmap1 = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

        BlockState defaultBlock = generatorSettings().get().defaultBlock();
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        int minBuildHeight = chunkAccess.getMinBuildHeight();

        if (lwSettings.type() == LostWorldType.ISLANDS) {
            // For islands we don't override blocks that are already there
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    int vr = getGroundLevel(x, z);
                    for (int y = minBuildHeight; y <= lwSettings.seaLevel(); ++y) {
                        BlockState b;
                        if (y < vr) {
                            b = y < (minBuildHeight + 2) ? bedrock : defaultBlock;
                        } else {
                            b = water;
                        }
                        LevelChunkSection levelchunksection = chunkAccess.getSection(chunkAccess.getSectionIndex(y));
                        if (levelchunksection.getBlockState(x, y & 15, z) == air) {
                            levelchunksection.setBlockState(x, y & 15, z, b, false);
                            heightmap.update(x, y, z, b);
                            heightmap1.update(x, y, z, b);
                        }
                    }
                }
            }
        } else {
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    int vr = getGroundLevel(x, z);
                    for (int y = minBuildHeight; y <= lwSettings.seaLevel(); ++y) {
                        BlockState b;
                        if (y < vr) {
                            b = y < (minBuildHeight + 2) ? bedrock : defaultBlock;
                        } else {
                            b = water;
                        }
                        LevelChunkSection levelchunksection = chunkAccess.getSection(chunkAccess.getSectionIndex(y));
                        levelchunksection.setBlockState(x, y & 15, z, b, false);
                        heightmap.update(x, y, z, b);
                        heightmap1.update(x, y, z, b);
                    }
                }
            }
        }
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        if (lwSettings.type() == LostWorldType.SPHERES && cachedLevel != null) {
            LostCitiesCompat.LostCitiesContext context = LostCitiesCompat.getLostCitiesContext(cachedLevel);
            if (context != null) {
                if (!context.isInSphereFullOrPartially(x << 4, z << 4)) {
                    return level.getMinBuildHeight();
                }
            }
        }
        return super.getBaseHeight(x, z, type, level, random);
    }

    // We override buildSurface because there is some hardcoded badlands stuff that is done here and we don't want that
    @Override
    public void buildSurface(ChunkAccess chunkAccess, WorldGenerationContext wgContext, RandomState random, StructureManager structureManager, BiomeManager biomeManager, Registry<Biome> biomes, Blender blender) {
        if (lwSettings.type() == LostWorldType.SPHERES) {
            ChunkPos cp = chunkAccess.getPos();
            boolean customSea = lwSettings.hasCustomSea();
            if (customSea) {
                this.groundBuffer = this.groundNoise.getRegion(this.groundBuffer, (cp.x * 16), (cp.z * 16), 16, 16, 1.0 / 16.0, 1.0 / 16.0, 1.0D);
            }
            BlockState water;
            BlockState air = Blocks.AIR.defaultBlockState();
            if (customSea) {
                water = Blocks.WATER.defaultBlockState();
            } else {
                water = air;
            }
            BlockState defaultBlock = generatorSettings().get().defaultBlock();
            BlockState bedrock = Blocks.BEDROCK.defaultBlockState();

            WorldGenRegion region = (WorldGenRegion) structureManager.level;
            cachedLevel = region.getLevel();
            LostCitiesCompat.LostCitiesContext context = LostCitiesCompat.getLostCitiesContext(region.getLevel());
            // Only generate the chunk if we have Lost Cities and we are in a sphere
            if (context != null) {
                if (context.isInSphereFull(cp.getMinBlockX(), cp.getMinBlockZ())) {
                    // We are fully in the sphere. Just generate the chunk as normal
                    super.buildSurface(chunkAccess, wgContext, random, structureManager, biomeManager, biomes, blender);
                } else if (context.isInSphereFullOrPartially(cp.getMinBlockX(), cp.getMinBlockZ())) {
                    // We are partially in the sphere. Generate the chunk but remove blocks that are outside the sphere
                    super.buildSurface(chunkAccess, wgContext, random, structureManager, biomeManager, biomes, blender);
                    int minBuildHeight = chunkAccess.getMinBuildHeight();
                    int maxBuildHeight = chunkAccess.getMaxBuildHeight();
                    int minSphereY = context.getMinSphereY(cp.getMinBlockX(), cp.getMinBlockZ());
                    int maxSphereY = context.getMaxSphereY(cp.getMinBlockX(), cp.getMinBlockZ());
                    int level = lwSettings.seaLevel() == null ? -63 : lwSettings.seaLevel();
                    for (int y = minBuildHeight; y < maxBuildHeight; ++y) {
                        BlockState block = y <= level ? water : air;
                        LevelChunkSection levelchunksection = chunkAccess.getSection(chunkAccess.getSectionIndex(y));
                        if (y >= minSphereY && y <= maxSphereY) {
                            for (int x = 0; x < 16; ++x) {
                                for (int z = 0; z < 16; ++z) {
                                    if (!context.isInSphere(cp.getMinBlockX() + x, y, cp.getMinBlockZ() + z)) {
                                        if (customSea && y < getGroundLevel(x, z)) {
                                            levelchunksection.setBlockState(x, y & 15, z, y < minBuildHeight + 2 ? bedrock : defaultBlock, false);
                                        } else {
                                            levelchunksection.setBlockState(x, y & 15, z, block, false);
                                        }
                                    }
                                }
                            }
                        } else {
                            for (int x = 0; x < 16; ++x) {
                                for (int z = 0; z < 16; ++z) {
                                    if (customSea && y < getGroundLevel(x, z)) {
                                        levelchunksection.setBlockState(x, y & 15, z, y < minBuildHeight + 2 ? bedrock : defaultBlock, false);
                                    } else {
                                        levelchunksection.setBlockState(x, y & 15, z, block, false);
                                    }
                                }
                            }
                        }
                    }
                }
                return;
            }
        }
        super.buildSurface(chunkAccess, wgContext, random, structureManager, biomeManager, biomes, blender);
    }

    @Override
    public void createStructures(RegistryAccess access, ChunkGeneratorStructureState structureState, StructureManager structureManager, ChunkAccess chunk, StructureTemplateManager templateManager) {
        if (structureManager.level instanceof ServerLevel serverLevel) {
            cachedLevel = serverLevel;
        } else if (structureManager.level instanceof WorldGenRegion region) {
            cachedLevel = region.getLevel();
        }
        ChunkPos cp = chunk.getPos();
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

            if (structureplacement.isStructureChunk(structureState, cp.x, cp.z)) {
                if (list.size() == 1) {
                    this.tryGenerateStructure(list.get(0), structureManager, access, randomstate, templateManager, structureState.getLevelSeed(), chunk, cp, sectionpos);
                } else {
                    List<StructureSet.StructureSelectionEntry> listCopy = new ArrayList<>(list);
                    WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
                    worldgenrandom.setLargeFeatureSeed(structureState.getLevelSeed(), cp.x, cp.z);
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
                        if (this.tryGenerateStructure(entry, structureManager, access, randomstate, templateManager, structureState.getLevelSeed(), chunk, cp, sectionpos)) {
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
    public int getSeaLevel() {
        if (lwSettings.hasCustomSea()) {
            return lwSettings.seaLevel();
        }
        return super.getSeaLevel();
    }

    @Override
    public Integer getOuterSeaLevel() {
        return lwSettings.seaLevel();
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }
}
