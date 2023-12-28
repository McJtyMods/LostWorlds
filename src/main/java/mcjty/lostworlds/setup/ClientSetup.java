package mcjty.lostworlds.setup;

import mcjty.lostworlds.client.LostWorldsSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;

public class ClientSetup {

    public static void onRegisterDimensionSpecialEffectsEvent(RegisterDimensionSpecialEffectsEvent event) {
        LostWorldsSpecialEffects effects = new LostWorldsSpecialEffects();
        event.register(new ResourceLocation("minecraft:overworld"), effects);
    }
}
