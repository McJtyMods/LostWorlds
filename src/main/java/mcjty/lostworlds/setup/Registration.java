package mcjty.lostworlds.setup;


import com.mojang.serialization.Codec;
import mcjty.lostworlds.worldgen.wastes.WastesBiomeSource;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static mcjty.lostworlds.LostWorlds.MODID;

public class Registration {

    public static final DeferredRegister<Codec<? extends BiomeSource>> BIOMESOURCE_REGISTRY = DeferredRegister.create(Registries.BIOME_SOURCE, MODID);

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BIOMESOURCE_REGISTRY.register(bus);
    }

    public static final RegistryObject<Codec<WastesBiomeSource>> WASTED_BIOME_SOURCE = BIOMESOURCE_REGISTRY.register("wasted_biomes", () -> WastesBiomeSource.CODEC);
}
