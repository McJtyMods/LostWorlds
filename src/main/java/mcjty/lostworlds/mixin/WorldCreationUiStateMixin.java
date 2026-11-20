package mcjty.lostworlds.mixin;

import mcjty.lostworlds.setup.ClientSetup;
import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldCreationUiState.class)
public abstract class WorldCreationUiStateMixin {

    @Shadow
    public abstract WorldCreationUiState.WorldTypeEntry getWorldType();

    @Inject(method = "getPresetEditor", at = @At("HEAD"), cancellable = true)
    private void lostworlds$openEditor(CallbackInfoReturnable<PresetEditor> callback) {
        Holder<WorldPreset> preset = getWorldType().preset();
        if (preset != null) {
            preset.unwrapKey().map(ClientSetup::presetEditor).ifPresent(callback::setReturnValue);
        }
    }
}
