package mcjty.lostworlds.worldgen.wastes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostworlds.compat.LostCitiesCompat;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class WastesBiomeSource extends BiomeSource {
    public static final MapCodec<WastesBiomeSource> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance
                    .group(
                            RegistryOps.<Biome, WastesBiomeSource>retrieveGetter(Registries.BIOME),
                            Codec.STRING.fieldOf("dimension").forGetter(provider -> provider.dimensionId),
                            BiomeSource.CODEC.fieldOf("adapt_biome_source").forGetter(provider -> provider.biomeSource),
                            Codec.STRING.optionalFieldOf("default").forGetter(provider -> Optional.ofNullable(provider.mapper.getDefaultBiomeId())),
                            Codec.STRING.optionalFieldOf("sphere_default").forGetter(provider -> Optional.ofNullable(provider.sphereMapper.getDefaultBiomeId())),
                            Codec.list(Codec.STRING).fieldOf("mapping").forGetter(provider -> provider.mapper.getBiomeMappingList()),
                            Codec.list(Codec.STRING).optionalFieldOf("sphere_mapping").forGetter(provider -> Optional.ofNullable(provider.sphereMapper.getBiomeMappingList()))
                    )
                    .apply(instance, instance.stable(WastesBiomeSource::new)));

    private final String dimensionId;
    private final BiomeSource biomeSource;
    private final BiomeMapper mapper;
    private final BiomeMapper sphereMapper;
    private Level level;    // Only needed for Lost Cities

    public WastesBiomeSource(HolderGetter<Biome> biomes, String dimensionId, BiomeSource biomeSource,
                             Optional<String> defaultBiome,
                             Optional<String> defaultSphereBiome,
                             List<String> biomeMappingList,
                             Optional<List<String>> sphereBiomeMappingList) {
        // @todo 1.19.4
//        super(biomeSource.possibleBiomes().stream());
        super();
        this.dimensionId = dimensionId;
        this.biomeSource = biomeSource;
        this.mapper = new BiomeMapper(biomes, defaultBiome, biomeMappingList);
        this.sphereMapper = new BiomeMapper(biomes, defaultSphereBiome, sphereBiomeMappingList.orElse(Collections.emptyList()));
    }

    @Override
    @Nonnull
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return biomeSource.possibleBiomes().stream();   // @todo 1.19.4 is this correct?
    }

    @Override
    public BiomeResolver createResolver(Climate.Sampler sampler) {
        BiomeResolver delegate = biomeSource.createResolver(sampler);
        return (x, y, z) -> {
            Holder<Biome> biome = delegate.getNoiseBiome(x, y, z);
            Level dimension = level;
            if (dimension == null && LostCitiesCompat.isHasLostCities()) {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                if (server != null) {
                    dimension = server.getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse(dimensionId)));
                    level = dimension;
                }
            }
            BiomeMapper selected = dimension != null && LostCitiesCompat.isInSphere(dimension, (x << 2) + 2, (z << 2) + 2)
                    ? sphereMapper : mapper;
            return selected.getMappedBiome(biome);
        };
    }
}
