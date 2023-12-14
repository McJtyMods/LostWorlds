package mcjty.lostworlds;

import mcjty.lostworlds.setup.ModSetup;
import mcjty.lostworlds.setup.Registration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(LostWorlds.MODID)
public class LostWorlds {

    public static final String MODID = "lostworlds";

    public static final ModSetup setup = new ModSetup();
    public static LostWorlds instance;

    public LostWorlds() {
        instance = this;
        Registration.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(EventHandlers::onRegisterPresetEditorsEvent);
    }
}
