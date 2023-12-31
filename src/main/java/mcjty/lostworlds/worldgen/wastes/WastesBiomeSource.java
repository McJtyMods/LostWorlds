package mcjty.lostworlds.worldgen.wastes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostworlds.compat.LostCitiesCompat;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class WastesBiomeSource extends BiomeSource {
    public static final Codec<WastesBiomeSource> CODEC = RecordCodecBuilder.create(
            instance -> instance
                    .group(
                            RegistryOps.retrieveRegistryLookup(Registries.BIOME).forGetter(provider -> provider.biomes),
                            Codec.STRING.fieldOf("dimension").forGetter(provider -> provider.dimensionId),
                            BiomeSource.CODEC.fieldOf("adapt_biome_source").forGetter(provider -> provider.biomeSource),
                            Codec.STRING.optionalFieldOf("default").forGetter(provider -> Optional.ofNullable(provider.mapper.getDefaultBiomeId())),
                            Codec.STRING.optionalFieldOf("sphere_default").forGetter(provider -> Optional.ofNullable(provider.sphereMapper.getDefaultBiomeId())),
                            Codec.list(Codec.STRING).fieldOf("mapping").forGetter(provider -> provider.mapper.getBiomeMappingList()),
                            Codec.list(Codec.STRING).optionalFieldOf("sphere_mapping").forGetter(provider -> Optional.ofNullable(provider.sphereMapper.getBiomeMappingList()))
                    )
                    .apply(instance, instance.stable(WastesBiomeSource::new)));

    private final String dimensionId;
    private final HolderLookup.RegistryLookup<Biome> biomes;
    private final BiomeSource biomeSource;
    private final BiomeMapper mapper;
    private final BiomeMapper sphereMapper;
    private Level level;    // Only needed for Lost Cities

    public WastesBiomeSource(HolderLookup.RegistryLookup<Biome> biomes, String dimensionId, BiomeSource biomeSource,
                             Optional<String> defaultBiome,
                             Optional<String> defaultSphereBiome,
                             List<String> biomeMappingList,
                             Optional<List<String>> sphereBiomeMappingList) {
        // @todo 1.19.4
//        super(biomeSource.possibleBiomes().stream());
        super();
        this.dimensionId = dimensionId;
        this.biomes = biomes;
        this.biomeSource = biomeSource;
        this.mapper = new BiomeMapper(biomes, defaultBiome, biomeMappingList);
        this.sphereMapper = new BiomeMapper(biomes, defaultSphereBiome, sphereBiomeMappingList.orElse(Collections.emptyList()));
    }

    @Override
    @Nonnull
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return biomeSource.possibleBiomes().stream();   // @todo 1.19.4 is this correct?
    }

    @Override
    @Nonnull
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        Holder<Biome> biome = biomeSource.getNoiseBiome(x, y, z, sampler);
        BiomeMapper m = mapper;
        if (LostCitiesCompat.isHasLostCities()) {
            if (level == null) {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                level = server.getLevel(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimensionId)));
            }
            if (level != null && LostCitiesCompat.isInSphere(level, (x << 2) + 2, (z << 2) + 2)) {
                m = sphereMapper;
            }
        }
        return m.getMappedBiome(biome);
    }
}
