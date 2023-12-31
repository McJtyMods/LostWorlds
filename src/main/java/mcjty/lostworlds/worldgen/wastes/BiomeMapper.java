package mcjty.lostworlds.worldgen.wastes;

import mcjty.lostworlds.setup.ModSetup;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BiomeMapper {
    final HolderLookup.RegistryLookup<Biome> biomes;
    private final List<String> biomeMappingList;
    private final String defaultBiomeId;
    private final Holder<Biome> defaultBiome;
    private Map<Biome, Holder<Biome>> biomeMapping = null;

    public BiomeMapper(HolderLookup.RegistryLookup<Biome> biomes, Optional<String> defaultBiomeId, List<String> biomeMappingList) {
        this.biomes = biomes;
        this.biomeMappingList = biomeMappingList;
        this.defaultBiomeId = defaultBiomeId.orElse(null);
        if (this.defaultBiomeId == null) {
            this.defaultBiome = null;
        } else {
            ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, new ResourceLocation(this.defaultBiomeId));
            this.defaultBiome = biomes.getOrThrow(key);
        }
    }

    public List<String> getBiomeMappingList() {
        return biomeMappingList;
    }

    public String getDefaultBiomeId() {
        return defaultBiomeId;
    }

    private void initBiomeMapping() {
        if (biomeMapping == null) {
            biomeMapping = new HashMap<>();

            for (String replacement : biomeMappingList) {
                String[] split = StringUtils.split(replacement, '=');
                Optional<Holder.Reference<Biome>> source = biomes.get(ResourceKey.create(Registries.BIOME, new ResourceLocation(split[0])));
                if (!source.isPresent()) {
                    ModSetup.logger.warn("Biome '" + split[0] + "' is missing!");
                } else {
                    Optional<Holder.Reference<Biome>> dest = biomes.get(ResourceKey.create(Registries.BIOME, new ResourceLocation(split[1])));
                    if (!dest.isPresent()) {
                        ModSetup.logger.warn("Biome '" + split[1] + "' is missing!");
                    } else {
                        dest.ifPresent(h -> {
                            biomeMapping.put(source.get().get(), h);
                        });
                    }
                }
            }
        }
    }


    public Holder<Biome> getMappedBiome(Holder<Biome> biome) {
        initBiomeMapping();
        return biomeMapping.getOrDefault(biome.get(), defaultBiome == null ? biome : defaultBiome);
    }
}
