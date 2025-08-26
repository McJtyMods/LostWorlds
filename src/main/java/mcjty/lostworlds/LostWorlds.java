package mcjty.lostworlds;

import mcjty.lostworlds.setup.ClientSetup;
import mcjty.lostworlds.setup.Config;
import mcjty.lostworlds.setup.ModSetup;
import mcjty.lostworlds.setup.Registration;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;


@Mod(LostWorlds.MODID)
public class LostWorlds {

    public static final String MODID = "lostworlds";

    public static final ModSetup setup = new ModSetup();
    public static LostWorlds instance;
	public static ModContainer container;

    public LostWorlds(IEventBus modEventBus, ModContainer modContainer) {
        Dist dist = FMLEnvironment.dist;

        instance = this;
		container = modContainer;
        Config.register();
        Registration.init(modEventBus);
		modEventBus.addListener(setup::init);

        if (dist.isClient()) {
            modEventBus.addListener(ClientSetup::onRegisterPresetEditorsEvent);
            modEventBus.addListener(ClientSetup::onRegisterDimensionSpecialEffectsEvent);
        }
    }
}
