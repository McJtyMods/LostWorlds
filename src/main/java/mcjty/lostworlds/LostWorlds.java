package mcjty.lostworlds;

import mcjty.lostworlds.setup.ClientSetup;
import mcjty.lostworlds.setup.Config;
import mcjty.lostworlds.compat.LostCitiesCompat;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import mcjty.lostworlds.setup.Registration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import mcjty.lostworlds.setup.Messages;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(LostWorlds.MODID)
public class LostWorlds {

    public static final String MODID = "lostworlds";

    public LostWorlds(IEventBus bus, ModContainer container) {
        Dist dist = FMLEnvironment.dist;

        Config.register(container);
        Config.registerCustomConfig(dist);
        Registration.init(bus);
        bus.addListener((InterModEnqueueEvent event) -> LostCitiesCompat.register());
        bus.addListener(Messages::register);
        NeoForge.EVENT_BUS.addListener(EventHandlers::onPlayerLoggedInEvent);

        NeoForge.EVENT_BUS.addListener(EventHandlers::onPlayerChangedDimensionEvent);
        NeoForge.EVENT_BUS.addListener(EventHandlers::onPlayerRespawn);

        if (dist.isClient()) {
            bus.addListener(ClientSetup::onRegisterPresetEditorsEvent);
            bus.addListener(ClientSetup::onRegisterDimensionSpecialEffectsEvent);
        }
    }
}
