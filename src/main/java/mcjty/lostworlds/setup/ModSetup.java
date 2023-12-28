package mcjty.lostworlds.setup;

import mcjty.lostworlds.compat.LostCitiesCompat;
import mcjty.lostworlds.worldgen.IslandsDensityFunction;
import mcjty.lostworlds.worldgen.LWChunkGenerator;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static mcjty.lostworlds.worldgen.LWChunkGenerator.LOSTWORLDS_CHUNKGEN;

public class ModSetup {

    public static Logger logger = null;

    public static Logger getLogger() {
        return logger;
    }


    public void init(FMLCommonSetupEvent e) {
        logger = LogManager.getLogger();
        e.enqueueWork(() -> {
//            Registry.register(BuiltInRegistries.BIOME_SOURCE, DimensionRegistry.RFTOOLS_BIOMES_ID, RFTBiomeProvider.CODEC);
            Registry.register(BuiltInRegistries.CHUNK_GENERATOR, LOSTWORLDS_CHUNKGEN, LWChunkGenerator.CODEC);
            Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, IslandsDensityFunction.LOST_ISLANDS, IslandsDensityFunction.CODEC.codec());
            Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, IslandsDensityFunction.LOST_ISLANDS_CHEESE, IslandsDensityFunction.CODEC_CHEESE.codec());
        });
        Messages.registerMessages("lostworlds");
        LostCitiesCompat.register();
    }

}
