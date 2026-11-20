package mcjty.lostworlds.mixin;

import mcjty.lostworlds.client.ClientFog;
import mcjty.lostworlds.worldgen.LostWorldType;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
abstract class SkyRendererMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void lostworlds$adjustSky(ClientLevel level, float partialTick, Camera camera,
                                      SkyRenderState state, CallbackInfo ci) {
        LostWorldType type = ClientFog.worldType(level);
        if (type == LostWorldType.CAVES) {
            state.skybox = DimensionType.Skybox.NONE;
        } else if (type == LostWorldType.ISLANDS || type == LostWorldType.SPHERES) {
            state.shouldRenderDarkDisc = false;
        }
    }
}
