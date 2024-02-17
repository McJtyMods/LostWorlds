package mcjty.lostworlds;

import mcjty.lostworlds.setup.ClientSetup;
import mcjty.lostworlds.setup.Config;
import mcjty.lostworlds.setup.ModSetup;
import mcjty.lostworlds.setup.Registration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(LostWorlds.MODID)
public class LostWorlds {

    public static final String MODID = "lostworlds";

    public static final ModSetup setup = new ModSetup();
    public static LostWorlds instance;

    public LostWorlds() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        Dist dist = FMLEnvironment.dist;

        instance = this;
        Config.register();
        Registration.init(bus);
        bus.addListener(setup::init);
        MinecraftForge.EVENT_BUS.addListener(EventHandlers::onPlayerLoggedInEvent);

        if (dist.isClient()) {
            bus.addListener(ClientSetup::onRegisterPresetEditorsEvent);
            bus.addListener(ClientSetup::onRegisterDimensionSpecialEffectsEvent);
        }
    }
}
