package mcjty.lostworlds.worldgen;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;

import java.util.function.IntFunction;

public enum FogColor implements StringRepresentable {
    NONE(0, "None", null),
    BLACK(1, "Black", new Vec3(0, 0, 0)),
    GRAY(2, "Gray", new Vec3(0.5, 0.5, 0.5)),
    RED(3, "Red", new Vec3(0.8, 0.2, 0.2)),
    BLUE(4, "Blue", new Vec3(0.4, 0.4, 0.8)),
    YELLOW(5, "Yellow", new Vec3(0.8, 0.8, 0.4));

	private final int id;
    private final String name;
    private final Vec3 fogColor;

    FogColor(int id, String name, Vec3 fogColor) {
		this.id = id;
        this.name = name;
        this.fogColor = fogColor;
    }

	public static final IntFunction<FogColor> BY_ID =
		ByIdMap.continuous(
			FogColor::getId,
			FogColor.values(),
			ByIdMap.OutOfBoundsStrategy.ZERO
		);

	public static FogColor byId(int id) {
		return BY_ID.apply(id);
	}

	@SuppressWarnings("deprecation")
	public static final EnumCodec<FogColor> CODEC = StringRepresentable.fromEnum(FogColor::values);

	public static final StreamCodec<ByteBuf, FogColor> STREAM_CODEC =
		ByteBufCodecs.idMapper(FogColor.BY_ID, FogColor::getId);



	public int getId() {
		return this.id;
	}

    public Vec3 getFogColor() {
        return fogColor;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

	public FogColor next() {
		return byId(id + 1 % values().length);
	}
}
