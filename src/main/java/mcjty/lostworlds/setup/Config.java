package mcjty.lostworlds.setup;

import com.google.common.collect.Lists;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import mcjty.lostworlds.LostWorlds;
import net.fabricmc.loader.api.FabricLoader;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.config.ModConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Config {

    public static final String CATEGORY_GENERAL = "general";

    private final static Set<Identifier> dimensionsWithSpecialFog = new HashSet<>();

    private static String[] DEF_EXCLUDED_STRUCTURES_ISLANDS = new String[]{
            "minecraft:ocean_monuments",
            "minecraft:ancient_cities",
            "minecraft:mineshafts"
    };
    private static ModConfigSpec.ConfigValue<List<? extends String>> EXCLUDED_STRUCTURES_ISLANDS;
    private static Set<ResourceKey<StructureSet>> exludedStructuresIslands = null;

    private static String[] DEF_EXCLUDED_STRUCTURES_SPHERES = new String[]{
            "minecraft:ocean_monuments",
            "minecraft:mineshafts",
            "minecraft:ancient_cities",
            "minecraft:villages",
            "minecraft:village_plains",
            "minecraft:village_desert",
            "minecraft:village_savanna",
            "minecraft:village_snowy",
            "minecraft:village_taiga"
    };
    private static ModConfigSpec.ConfigValue<List<? extends String>> EXCLUDED_STRUCTURES_SPHERES;
    private static Set<ResourceKey<StructureSet>> exludedStructuresSpheres = null;

    public static void register() {
        ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
        COMMON_BUILDER.comment("General settings").push(CATEGORY_GENERAL);

        EXCLUDED_STRUCTURES_ISLANDS = COMMON_BUILDER
                .comment("A list of structures that should not generate on lost_islands worlds")
                .defineList("excludedStructuresIslands", Lists.newArrayList(Config.DEF_EXCLUDED_STRUCTURES_ISLANDS), s -> s instanceof String);
        EXCLUDED_STRUCTURES_SPHERES = COMMON_BUILDER
                .comment("A list of structures that should not generate on lost_spheres worlds")
                .defineList("excludedStructuresSpheres", Lists.newArrayList(Config.DEF_EXCLUDED_STRUCTURES_SPHERES), s -> s instanceof String);

        COMMON_BUILDER.pop();
        ModConfigSpec COMMON_CONFIG = COMMON_BUILDER.build();
        ConfigRegistry.INSTANCE.register(LostWorlds.MODID, ModConfig.Type.COMMON, COMMON_CONFIG);
    }

    public static void registerCustomConfig() {
        Path config = FabricLoader.getInstance().getConfigDir().resolve("lostworlds-client.cfg");
        // If the file doesn't exist, create it
        if (!config.toFile().exists()) {
            try {
                config.toFile().createNewFile();
                // Write the default configuration to the file. The default configuration is a single line with the default dimension minecraft:overworld
                Files.write(config, Lists.newArrayList("minecraft:overworld", "lostworlds:abyss"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        // Load the configuration from the file. This is a list of dimension resource locations in the format minecraft:overworld
        dimensionsWithSpecialFog.clear();
        try (BufferedReader reader = Files.newBufferedReader(config)) {
            // Read all lines
            List<String> lines = reader.lines().collect(Collectors.toList());
            lines.stream().map(Identifier::parse).forEach(dimensionsWithSpecialFog::add);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Set<ResourceKey<StructureSet>> getExludedStructuresIslands() {
        if (exludedStructuresIslands == null) {
            exludedStructuresIslands = new HashSet<>();
            List<? extends String> strings = EXCLUDED_STRUCTURES_ISLANDS.get();
            for (String s : strings) {
                ResourceKey<StructureSet> key = ResourceKey.create(Registries.STRUCTURE_SET, Identifier.parse(s));
                if (key == null) {
                    throw new RuntimeException("Unknown structure set: " + s);
                }
                exludedStructuresIslands.add(key);
            }
        }
        return exludedStructuresIslands;
    }

    public static Set<ResourceKey<StructureSet>> getExludedStructuresSpheres() {
        if (exludedStructuresSpheres == null) {
            exludedStructuresSpheres = new HashSet<>();
            List<? extends String> strings = EXCLUDED_STRUCTURES_SPHERES.get();
            for (String s : strings) {
                ResourceKey<StructureSet> key = ResourceKey.create(Registries.STRUCTURE_SET, Identifier.parse(s));
                if (key == null) {
                    throw new RuntimeException("Unknown structure set: " + s);
                }
                exludedStructuresSpheres.add(key);
            }
        }
        return exludedStructuresSpheres;
    }

    public static Set<Identifier> getDimensionsWithSpecialFog() {
        return dimensionsWithSpecialFog;
    }

}
