package mcjty.lostworlds.worldgen;

import io.netty.buffer.ByteBuf;
import mcjty.lostworlds.setup.Config;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Predicate;

public enum LostWorldType implements StringRepresentable {
    NORMAL(0, "normal", key -> false, false),
    ATLANTIS(1, "atlantis", key -> false, false),
    ISLANDS(2, "islands", key -> {
        Set<ResourceKey<StructureSet>> excluded = Config.getExludedStructuresIslands();
        return excluded.contains(key);
    }, true),
    CAVES(3, "caves", key -> false, false),
    SPHERES(4, "spheres", key -> {
        Set<ResourceKey<StructureSet>> excluded = Config.getExludedStructuresSpheres();
        return excluded.contains(key);
    }, true);

	private final int id;
    private final String name;
    private final Predicate<ResourceKey<StructureSet>> blocksStructure;
    private final boolean supportsCustomSea;

    LostWorldType(int id, String name, Predicate<ResourceKey<StructureSet>> blocksStructure, boolean supportsCustomSea) {
		this.id = id;
        this.name = name;
        this.blocksStructure = blocksStructure;
        this.supportsCustomSea = supportsCustomSea;
    }

	public static final IntFunction<LostWorldType> BY_ID =
		ByIdMap.continuous(
			LostWorldType::getId,
			LostWorldType.values(),
			ByIdMap.OutOfBoundsStrategy.ZERO
		);

	public static LostWorldType byId(int id) {
		return BY_ID.apply(id);
	}

	@SuppressWarnings("deprecation")
	public static final EnumCodec<LostWorldType> CODEC = StringRepresentable.fromEnum(LostWorldType::values);

	public static final StreamCodec<ByteBuf, LostWorldType> STREAM_CODEC =
		ByteBufCodecs.idMapper(LostWorldType.BY_ID, LostWorldType::getId);



	public int getId() {
		return this.id;
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

	public LostWorldType next() {
		return byId(id + 1 % values().length);
	}
}
