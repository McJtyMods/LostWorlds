package mcjty.lostworlds.setup;


import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import mcjty.lostworlds.worldgen.wastes.WastesBiomeSource;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static mcjty.lostworlds.LostWorlds.MODID;

public class Registration {

    public static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOMESOURCE_REGISTRY = DeferredRegister.create(Registries.BIOME_SOURCE, MODID);

    public static void init(IEventBus bus) {
        BIOMESOURCE_REGISTRY.register(bus);
    }

    public static final Supplier<MapCodec<WastesBiomeSource>> WASTED_BIOME_SOURCE = BIOMESOURCE_REGISTRY.register("wasted_biomes", () -> WastesBiomeSource.CODEC);
}
