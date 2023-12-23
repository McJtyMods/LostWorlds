package mcjty.lostworlds.worldgen;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;

public enum FogColor implements StringRepresentable {
    NONE("None", null),
    BLACK("Black", new Vec3(0, 0, 0)),
    GRAY("Gray", new Vec3(0.5, 0.5, 0.5)),
    RED("Red", new Vec3(0.8, 0.4, 0.4)),
    BLUE("Blue", new Vec3(0.4, 0.4, 0.8));

    private final String name;
    private final Vec3 fogColor;

    FogColor(String name, Vec3 fogColor) {
        this.name = name;
        this.fogColor = fogColor;
    }

    public Vec3 getFogColor() {
        return fogColor;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
