package mcjty.lostworlds.setup;

import mcjty.lostworlds.LostWorlds;
import mcjty.lostworlds.compat.LostCitiesCompat;
import mcjty.lostworlds.worldgen.IslandsDensityFunction;
import mcjty.lostworlds.worldgen.LWChunkGenerator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static mcjty.lostworlds.worldgen.LWChunkGenerator.LOSTWORLDS_CHUNKGEN;

@EventBusSubscriber(modid = LostWorlds.MODID)
public class ModSetup {

    public static Logger logger = null;
    public static Logger getLogger() {
        return logger;
    }

	@SubscribeEvent
	public static void register(RegisterEvent event) {
		event.register(BuiltInRegistries.CHUNK_GENERATOR.key(),
			registry -> {
				registry.register(LOSTWORLDS_CHUNKGEN, LWChunkGenerator.CODEC);
			}
		);

		event.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE.key(),
			registry -> {
				registry.register(IslandsDensityFunction.LOST_ISLANDS, IslandsDensityFunction.CODEC.codec());
				registry.register(IslandsDensityFunction.LOST_ISLANDS_CHEESE, IslandsDensityFunction.CODEC_CHEESE.codec());
			}
		);
	}

	public void init(FMLCommonSetupEvent e) {
        logger = LogManager.getLogger();
        LostCitiesCompat.register();
    }

}
