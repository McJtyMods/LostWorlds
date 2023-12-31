package mcjty.lostworlds;

import mcjty.lostworlds.setup.ClientSetup;
import mcjty.lostworlds.setup.Config;
import mcjty.lostworlds.setup.ModSetup;
import mcjty.lostworlds.setup.Registration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(LostWorlds.MODID)
public class LostWorlds {

    public static final String MODID = "lostworlds";

    public static final ModSetup setup = new ModSetup();
    public static LostWorlds instance;

    public LostWorlds() {
        instance = this;
        Config.register();
        Registration.init();
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(EventHandlers::onRegisterPresetEditorsEvent);
        bus.addListener(setup::init);
        MinecraftForge.EVENT_BUS.addListener(EventHandlers::onPlayerLoggedInEvent);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            bus.addListener(ClientSetup::onRegisterDimensionSpecialEffectsEvent);
        });
    }
}
