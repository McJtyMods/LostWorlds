package mcjty.lostworlds.mixin;

import mcjty.lostworlds.client.ClientFog;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @Inject(method = "computeFogColor", at = @At("RETURN"))
    private void lostworlds$applyFog(Camera camera, float partialTick, ClientLevel level,
                                    int distance, float darkening, Vector4f destination, CallbackInfo callback) {
        Vec3 color = ClientFog.color(level);
        if (color != null) {
            destination.set((float) color.x, (float) color.y, (float) color.z, destination.w);
        }
    }
}
