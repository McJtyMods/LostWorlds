package mcjty.lostworlds.worldgen;

import mcjty.lostworlds.setup.Config;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.Set;
import java.util.function.Predicate;

public enum LostWorldType implements StringRepresentable {
    ISLANDS("islands", key -> {
        Set<ResourceKey<StructureSet>> excluded = Config.getExludedStructuresIslands();
        return excluded.contains(key);
    }),
    CAVES("caves", key -> false),
    VOID("void", key -> {
        Set<ResourceKey<StructureSet>> excluded = Config.getExludedStructuresVoid();
        return excluded.contains(key);
    });

    private final String name;
    private final Predicate<ResourceKey<StructureSet>> blocksStructure;

    LostWorldType(String name, Predicate<ResourceKey<StructureSet>> blocksStructure) {
        this.name = name;
        this.blocksStructure = blocksStructure;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public boolean blocksStructure(ResourceKey<StructureSet> key) {
        return this.blocksStructure.test(key);
    }
}
