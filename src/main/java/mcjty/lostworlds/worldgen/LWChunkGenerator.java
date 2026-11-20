package mcjty.lostworlds.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.varia.NoiseGeneratorPerlin;
import mcjty.lostworlds.LostWorlds;
import mcjty.lostcities.api.ILostWorldsChunkGenerator;
import mcjty.lostworlds.compat.LostCitiesCompat;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class LWChunkGenerator extends NoiseBasedChunkGenerator implements ILostWorldsChunkGenerator {

    public static final Identifier LOSTWORLDS_CHUNKGEN = Identifier.fromNamespaceAndPath(LostWorlds.MODID, "lostworlds");
    public static final ResourceKey<NoiseGeneratorSettings> LOST_ISLANDS = ResourceKey.create(Registries.NOISE_SETTINGS, Identifier.fromNamespaceAndPath(LostWorlds.MODID, "lost_islands"));
    public static final ResourceKey<NoiseGeneratorSettings> LOST_CAVES = ResourceKey.create(Registries.NOISE_SETTINGS, Identifier.fromNamespaceAndPath(LostWorlds.MODID, "lost_caves"));
    public static final ResourceKey<NoiseGeneratorSettings> LOST_SPHERES = ResourceKey.create(Registries.NOISE_SETTINGS, Identifier.fromNamespaceAndPath(LostWorlds.MODID, "lost_spheres"));
    public static final ResourceKey<NoiseGeneratorSettings> LOST_NORMAL = ResourceKey.create(Registries.NOISE_SETTINGS, Identifier.fromNamespaceAndPath(LostWorlds.MODID, "lost_normal"));
    public static final ResourceKey<NoiseGeneratorSettings> LOST_ATLANTIS = ResourceKey.create(Registries.NOISE_SETTINGS, Identifier.fromNamespaceAndPath(LostWorlds.MODID, "lost_atlantis"));
    public static final ResourceKey<NoiseGeneratorSettings> LOST_CAVESPHERES = ResourceKey.create(Registries.NOISE_SETTINGS, Identifier.fromNamespaceAndPath(LostWorlds.MODID, "lost_cavespheres"));
    public static final float GROUND_SCALE = 3.0f;

    private final LWSettings lwSettings;
    private final NoiseGeneratorPerlin groundNoise; // For the deep ground in the ocean
    private volatile Level cachedLevel;

    public static final MapCodec<LWChunkGenerator> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
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
    public CompletableFuture<ChunkAccess> buildTerrain(ChunkAccess chunk, Blender blender, RandomState random,
                                                        StructureManager structures, BiomeManager biomes,
                                                        WorldGenRegion carverBiomeRegion, Set<Holder<Biome>> possibleBiomes) {
        if (structures.level instanceof WorldGenRegion region) {
            cachedLevel = region.getLevel();
        }
        if (lwSettings.type() == LostWorldType.SPHERES) {
            ChunkPos pos = chunk.getPos();
            LostCitiesCompat.LostCitiesContext context = sphereContext();
            if (context == null || !context.isInSphereFullOrPartially(pos.getMinBlockX(), pos.getMinBlockZ())) {
                if (lwSettings.hasCustomSea()) {
                    fillCustomSea(chunk, sampleGroundNoise(pos));
                }
                return CompletableFuture.completedFuture(chunk);
            }
        }
        return super.buildTerrain(chunk, blender, random, structures, biomes, carverBiomeRegion, possibleBiomes);
    }

    @Override
    public void doFill(NoiseChunk noiseChunk, ChunkAccess chunk) {
        super.doFill(noiseChunk, chunk);
        boolean customSea = lwSettings.hasCustomSea();
        if (lwSettings.type() == LostWorldType.CAVESPHERES) {
            shapeCaveSpheres(chunk);
        } else if (lwSettings.type() == LostWorldType.SPHERES) {
            shapeSpheres(chunk, customSea ? sampleGroundNoise(chunk.getPos()) : null);
        } else if (customSea) {
            fillCustomSea(chunk, sampleGroundNoise(chunk.getPos()));
        }
    }

    private LostCitiesCompat.LostCitiesContext sphereContext() {
        return cachedLevel == null ? null : LostCitiesCompat.getLostCitiesContext(cachedLevel);
    }

    private void shapeCaveSpheres(ChunkAccess chunkAccess) {
        LostCitiesCompat.LostCitiesContext context = sphereContext();
        if (context == null) {
            return;
        }
        ChunkPos cp = chunkAccess.getPos();
        double[] groundBuffer = sampleGroundNoise(cp);
        int minBuildHeight = chunkAccess.getMinY();
        int maxBuildHeight = (chunkAccess.getMaxY() + 1);
        int minSphereY = context.getMinSphereY(cp.getMinBlockX(), cp.getMinBlockZ());
        int maxSphereY = context.getMaxSphereY(cp.getMinBlockX(), cp.getMinBlockZ());
        BlockState defaultBlock = generatorSettings().value().defaultBlock();
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        int middleY = (minSphereY + maxSphereY) / 2;
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int height = getGroundLevel(groundBuffer, x, z) + middleY + 15;
                for (int y = Math.max(minBuildHeight, minSphereY); y <= Math.min(maxBuildHeight-1, maxSphereY); ++y) {
                    LevelChunkSection levelchunksection = chunkAccess.getSection(chunkAccess.getSectionIndex(y));
                    if (context.isInSphere(cp.getMinBlockX() + x, y, cp.getMinBlockZ() + z)) {
                        if (y < height) {
                            levelchunksection.setBlockState(x, y & 15, z, y < minBuildHeight + 2 ? bedrock : defaultBlock, false);
                        } else {
                            levelchunksection.setBlockState(x, y & 15, z, air, false);
                        }
                    }
                }
            }
        }
    }

    private void shapeSpheres(ChunkAccess chunkAccess, double[] groundBuffer) {
        LostCitiesCompat.LostCitiesContext context = sphereContext();
        if (context == null) {
            return;
        }
        boolean customSea = groundBuffer != null;
        ChunkPos cp = chunkAccess.getPos();
        int minBuildHeight = chunkAccess.getMinY();
        int maxBuildHeight = (chunkAccess.getMaxY() + 1);
        int minSphereY = context.getMinSphereY(cp.getMinBlockX(), cp.getMinBlockZ());
        int maxSphereY = context.getMaxSphereY(cp.getMinBlockX(), cp.getMinBlockZ());
        BlockState defaultBlock = generatorSettings().value().defaultBlock();
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
                            if (customSea && y < getGroundLevel(groundBuffer, x, z)) {
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
                        if (customSea && y < getGroundLevel(groundBuffer, x, z)) {
                            levelchunksection.setBlockState(x, y & 15, z, y < minBuildHeight + 2 ? bedrock : defaultBlock, false);
                        } else {
                            levelchunksection.setBlockState(x, y & 15, z, block, false);
                        }
                    }
                }
            }
        }
    }

    private double[] sampleGroundNoise(ChunkPos chunkPos) {
        return groundNoise.getRegion(null, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(), 16, 16, 1.0 / 16.0, 1.0 / 16.0, 1.0D);
    }

    private int getGroundLevel(double[] groundBuffer, int x, int z) {
        double vr = -15 + groundBuffer[x + z * 16] / GROUND_SCALE;
        return (int) vr;
    }

    @Override
    public void generateCarvers(ChunkAccess chunk, Blender blender, NoiseChunk noiseChunk, RandomState random,
                               BiomeManager biomes, WorldGenRegion carverBiomeRegion, MaterialRule materialRule) {
        if (lwSettings.type() == LostWorldType.ISLANDS) {
            return;
        }
        if (lwSettings.type() == LostWorldType.SPHERES && lwSettings.hasCustomSea()) {
            LostCitiesCompat.LostCitiesContext context = sphereContext();
            ChunkPos pos = chunk.getPos();
            if (context == null || !context.isInSphereFull(pos.getMinBlockX(), pos.getMinBlockZ())) {
                return;
            }
        }
        super.generateCarvers(chunk, blender, noiseChunk, random, biomes, carverBiomeRegion, materialRule);
    }

    private void fillCustomSea(ChunkAccess chunkAccess, double[] groundBuffer) {
        Heightmap oceanFloor = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap worldSurface = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

        BlockState defaultBlock = generatorSettings().value().defaultBlock();
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        int minBuildHeight = chunkAccess.getMinY();
        int seaLevel = Math.min(lwSettings.seaLevel(), (chunkAccess.getMaxY() + 1) - 1);
        boolean islands = lwSettings.type() == LostWorldType.ISLANDS;
        int bedrockCeiling = islands ? Math.min(minBuildHeight + 2, seaLevel) : minBuildHeight + 2;
        int groundOffset = Math.min(0, seaLevel);

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int firstWaterY = getGroundLevel(groundBuffer, x, z);
                if (islands) {
                    // Lower the seabed with a negative sea level and leave room for water near the bottom.
                    firstWaterY = Mth.clamp(firstWaterY + groundOffset, bedrockCeiling, seaLevel);
                }
                for (int y = minBuildHeight; y <= seaLevel; ++y) {
                    LevelChunkSection section = chunkAccess.getSection(chunkAccess.getSectionIndex(y));
                    if (islands && section.getBlockState(x, y & 15, z) != air) {
                        continue;
                    }
                    BlockState block = water;
                    if (y < firstWaterY) {
                        block = y < bedrockCeiling ? bedrock : defaultBlock;
                    }
                    section.setBlockState(x, y & 15, z, block, false);
                    oceanFloor.update(x, y, z, block);
                    worldSurface.update(x, y, z, block);
                }
            }
        }
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        if (lwSettings.type() == LostWorldType.SPHERES && cachedLevel != null) {
            LostCitiesCompat.LostCitiesContext context = LostCitiesCompat.getLostCitiesContext(cachedLevel);
            if (context != null && !context.isInSphereFullOrPartially(x, z)) {
                return level.getMinY();
            }
        }
        return super.getBaseHeight(x, z, type, level, random);
    }

    @Override
    public void buildSurface(ChunkAccess chunk, NoiseChunk noiseChunk, RandomState random,
                             BiomeManager biomes, Set<Holder<Biome>> possibleBiomes, MaterialRule materialRule) {
        super.buildSurface(chunk, noiseChunk, random, biomes, possibleBiomes, materialRule);
        if (lwSettings.type() != LostWorldType.SPHERES) {
            return;
        }
        LostCitiesCompat.LostCitiesContext context = sphereContext();
        ChunkPos pos = chunk.getPos();
        if (context != null
                && !context.isInSphereFull(pos.getMinBlockX(), pos.getMinBlockZ())) {
            shapeSpheres(chunk, lwSettings.hasCustomSea() ? sampleGroundNoise(pos) : null);
        }
    }

    @Override
    public void createStructures(RegistryAccess access, ChunkGeneratorStructureState structureState, StructureManager structureManager, ChunkAccess chunk, StructureTemplateManager templateManager, ResourceKey<Level> level) {
        if (structureManager.level instanceof ServerLevel serverLevel) {
            cachedLevel = serverLevel;
        } else if (structureManager.level instanceof WorldGenRegion region) {
            cachedLevel = region.getLevel();
        }
        ChunkPos cp = chunk.getPos();
        RandomState randomstate = structureState.randomState();
        Climate.Sampler climateSampler = randomstate.createClimateSampler(SamplerContext.builder().enableCaches().build());
        structureState.possibleStructureSets().forEach((set) -> {
            StructurePlacement structureplacement = set.value().placement();
            List<StructureSet.StructureSelectionEntry> list = set.value().structures();
            ResourceKey<StructureSet> key = set.unwrapKey().get();
            if (lwSettings.type().blocksStructure(key)) {
                return;
            }

            for (StructureSet.StructureSelectionEntry entry : list) {
                StructureStart structurestart = structureManager.getStartForStructure(entry.structure().value(), chunk);
                if (structurestart != null && structurestart.isValid()) {
                    return;
                }
            }

            if (structureplacement.isStructureChunk(structureState, cp.x(), cp.z())) {
                if (list.size() == 1) {
                    this.tryGenerateStructure(list.get(0), structureManager, access, randomstate, templateManager, structureState.getLevelSeed(), chunk, cp, level, climateSampler);
                } else {
                    List<StructureSet.StructureSelectionEntry> listCopy = new ArrayList<>(list);
                    WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
                    worldgenrandom.setLargeFeatureSeed(structureState.getLevelSeed(), cp.x(), cp.z());
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
                        if (this.tryGenerateStructure(entry, structureManager, access, randomstate, templateManager, structureState.getLevelSeed(), chunk, cp, level, climateSampler)) {
                            return;
                        }

                        listCopy.remove(selected);
                        totalweight -= entry.weight();
                    }

                }
            }
        });
    }

    private boolean tryGenerateStructure(StructureSet.StructureSelectionEntry entry, StructureManager structureManager, RegistryAccess registryAccess, RandomState rnd, StructureTemplateManager templateManager, long seed, ChunkAccess chunk, ChunkPos cp, ResourceKey<Level> level, Climate.Sampler climateSampler) {
        Structure structure = entry.structure().value();
        StructureStart start = structureManager.getStartForStructure(structure, chunk);
        int references = start != null ? start.getReferences() : 0;
        StructureStart structurestart = structure.generate(entry.structure(), level, registryAccess, this, this.biomeSource, climateSampler, rnd, templateManager, seed, cp, references, chunk, structure.biomes()::contains);
        if (structurestart.isValid()) {
            structureManager.setStartForStructure(structure, structurestart, chunk);
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
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }
}
