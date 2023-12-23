package mcjty.lostworlds.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.lostworlds.worldgen.LostWorldType;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class LostWorlsSpecialEffects extends DimensionSpecialEffects {

    public static LostWorldType type;

    public LostWorlsSpecialEffects() {
        super(192.0F, true, DimensionSpecialEffects.SkyType.NORMAL, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        if (type == LostWorldType.ISLANDS) {
            return fogColor.multiply(brightness * 0.94F + 0.06F, brightness * 0.94F + 0.06F, (double)(brightness * 0.91F + 0.09F));
        } else {
            return new Vec3(0.0f, 0.0f, 0.0f);
        }
    }

    @Override
    public SkyType skyType() {
        if (type == LostWorldType.CAVERNS) {
            return SkyType.NONE;
        }
        return super.skyType();
    }

    @Override
    public boolean hasGround() {
        if (type == LostWorldType.ISLANDS) {
            return false;
        }
        return super.hasGround();
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        if (type == LostWorldType.ISLANDS) {
            level.getLevelData().isFlat = true;
        }
        return false;
    }
}
