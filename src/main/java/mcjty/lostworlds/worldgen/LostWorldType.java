package mcjty.lostworlds.worldgen;

import mcjty.lostworlds.setup.Config;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.Set;
import java.util.function.Predicate;

public enum LostWorldType implements StringRepresentable {
    NORMAL("normal", key -> false, false),
    ISLANDS("islands", key -> {
        Set<ResourceKey<StructureSet>> excluded = Config.getExludedStructuresIslands();
        return excluded.contains(key);
    }, true),
    CAVES("caves", key -> false, false),
    SPHERES("spheres", key -> {
        Set<ResourceKey<StructureSet>> excluded = Config.getExludedStructuresSpheres();
        return excluded.contains(key);
    }, true);

    private final String name;
    private final Predicate<ResourceKey<StructureSet>> blocksStructure;
    private final boolean supportsCustomSea;

    LostWorldType(String name, Predicate<ResourceKey<StructureSet>> blocksStructure, boolean supportsCustomSea) {
        this.name = name;
        this.blocksStructure = blocksStructure;
        this.supportsCustomSea = supportsCustomSea;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public boolean supportsCustomSea() {
        return this.supportsCustomSea;
    }

    public boolean blocksStructure(ResourceKey<StructureSet> key) {
        return this.blocksStructure.test(key);
    }
}
