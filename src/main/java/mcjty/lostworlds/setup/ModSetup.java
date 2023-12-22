package mcjty.lostworlds.setup;

import mcjty.lostworlds.LostIslandsDensityFunction;
import mcjty.lostworlds.LostWorldsChunkGenerator;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static mcjty.lostworlds.LostWorldsChunkGenerator.LOSTWORLDS_CHUNKGEN;

public class ModSetup {

    public static Logger logger = null;

    public static Logger getLogger() {
        return logger;
    }


    public void init(FMLCommonSetupEvent e) {
        logger = LogManager.getLogger();
        e.enqueueWork(() -> {
//            Registry.register(BuiltInRegistries.BIOME_SOURCE, DimensionRegistry.RFTOOLS_BIOMES_ID, RFTBiomeProvider.CODEC);
            Registry.register(BuiltInRegistries.CHUNK_GENERATOR, LOSTWORLDS_CHUNKGEN, LostWorldsChunkGenerator.CODEC);
            Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, LostIslandsDensityFunction.LOST_ISLANDS, LostIslandsDensityFunction.CODEC.codec());
            Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, LostIslandsDensityFunction.LOST_ISLANDS_CHEESE, LostIslandsDensityFunction.CODEC_CHEESE.codec());
        });
        Messages.registerMessages("lostworlds");

    }
}
