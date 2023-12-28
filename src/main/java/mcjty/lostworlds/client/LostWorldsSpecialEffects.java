package mcjty.lostworlds.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.lostworlds.worldgen.FogColor;
import mcjty.lostworlds.worldgen.LostWorldType;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class LostWorldsSpecialEffects extends DimensionSpecialEffects {

    public static LostWorldType type;
    public static FogColor fogColor;

    public LostWorldsSpecialEffects() {
        super(192.0F, true, DimensionSpecialEffects.SkyType.NORMAL, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 color, float brightness) {
        if (fogColor == FogColor.NONE) {
            if (type == LostWorldType.ISLANDS || type == LostWorldType.ISLANDS_WATER || type == LostWorldType.VOID) {
                return color.multiply(brightness * 0.94F + 0.06F, brightness * 0.94F + 0.06F, (double) (brightness * 0.91F + 0.09F));
            } else {
                return new Vec3(0.0f, 0.0f, 0.0f);
            }
        } else {
            return fogColor.getFogColor();
        }
    }

    @Override
    public SkyType skyType() {
        if (type == LostWorldType.CAVES) {
            return SkyType.NONE;
        }
        return super.skyType();
    }

    @Override
    public boolean hasGround() {
        if (type == LostWorldType.ISLANDS || type == LostWorldType.ISLANDS_WATER || type == LostWorldType.VOID) {
            return false;
        }
        return super.hasGround();
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return fogColor != FogColor.NONE;
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        if (type == LostWorldType.ISLANDS || type == LostWorldType.ISLANDS_WATER || type == LostWorldType.VOID) {
            level.getLevelData().isFlat = true;
        }
        return false;
    }
}
