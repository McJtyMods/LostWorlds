package mcjty.lostworlds.setup;

import com.google.common.collect.Lists;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Config {

    public static final String CATEGORY_GENERAL = "general";

    private static String[] DEF_EXCLUDED_STRUCTURES_ISLANDS = new String[]{
            "minecraft:ocean_monuments",
            "minecraft:mineshafts"
    };
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> EXCLUDED_STRUCTURES_ISLANDS;
    private static Set<ResourceKey<StructureSet>> exludedStructuresIslands = null;

    private static String[] DEF_EXCLUDED_STRUCTURES_VOID = new String[]{
            "minecraft:ocean_monuments",
            "minecraft:mineshafts"
    };
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> EXCLUDED_STRUCTURES_VOID;
    private static Set<ResourceKey<StructureSet>> exludedStructuresVoid = null;

    public static void register() {
        ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
        COMMON_BUILDER.comment("General settings").push(CATEGORY_GENERAL);

        EXCLUDED_STRUCTURES_ISLANDS = COMMON_BUILDER
                .comment("A list of structures that should not generate on lost_islands worlds")
                .defineList("excludedStructuresIslands", Lists.newArrayList(Config.DEF_EXCLUDED_STRUCTURES_ISLANDS), s -> s instanceof String);
        EXCLUDED_STRUCTURES_VOID = COMMON_BUILDER
                .comment("A list of structures that should not generate on lost_void worlds")
                .defineList("excludedStructuresVoid", Lists.newArrayList(Config.DEF_EXCLUDED_STRUCTURES_VOID), s -> s instanceof String);

        COMMON_BUILDER.pop();
        ForgeConfigSpec COMMON_CONFIG = COMMON_BUILDER.build();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG);
    }

    public static Set<ResourceKey<StructureSet>> getExludedStructuresIslands() {
        if (exludedStructuresIslands == null) {
            exludedStructuresIslands = new HashSet<>();
            List<? extends String> strings = EXCLUDED_STRUCTURES_ISLANDS.get();
            for (String s : strings) {
                ResourceKey<StructureSet> key = ResourceKey.create(Registries.STRUCTURE_SET, new ResourceLocation(s));
                if (key == null) {
                    throw new RuntimeException("Unknown structure set: " + s);
                }
                exludedStructuresIslands.add(key);
            }
        }
        return exludedStructuresIslands;
    }

    public static Set<ResourceKey<StructureSet>> getExludedStructuresVoid() {
        if (exludedStructuresVoid == null) {
            exludedStructuresVoid = new HashSet<>();
            List<? extends String> strings = EXCLUDED_STRUCTURES_VOID.get();
            for (String s : strings) {
                ResourceKey<StructureSet> key = ResourceKey.create(Registries.STRUCTURE_SET, new ResourceLocation(s));
                if (key == null) {
                    throw new RuntimeException("Unknown structure set: " + s);
                }
                exludedStructuresVoid.add(key);
            }
        }
        return exludedStructuresVoid;
    }

}
