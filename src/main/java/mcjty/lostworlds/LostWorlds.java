package mcjty.lostworlds;

import mcjty.lostworlds.setup.ClientSetup;
import mcjty.lostworlds.setup.Config;
import mcjty.lostworlds.setup.ModSetup;
import mcjty.lostworlds.setup.Registration;
import net.neoforged.neoforge.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fml.common.Mod;
import net.neoforged.neoforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.fml.loading.FMLEnvironment;

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
