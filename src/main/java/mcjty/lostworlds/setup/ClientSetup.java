package mcjty.lostworlds.setup;

import mcjty.lostworlds.client.LostWorlsSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;

public class ClientSetup {

    public static void onRegisterDimensionSpecialEffectsEvent(RegisterDimensionSpecialEffectsEvent event) {
        LostWorlsSpecialEffects effects = new LostWorlsSpecialEffects();
        event.register(new ResourceLocation("minecraft:overworld"), effects);
    }
}
