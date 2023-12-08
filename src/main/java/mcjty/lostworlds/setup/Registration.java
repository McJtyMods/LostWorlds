package mcjty.lostworlds.setup;


import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class Registration {

//    public static final DeferredRegister<WorldPreset> PRESETS = DeferredRegister.create(Registries.WORLD_PRESET, LostWorlds.MODID);

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
//        PRESETS.register(bus);
    }

//    public static final RegistryObject<WorldPreset> LOST_ISLANDS = PRESETS.register("lost_islands", () -> new WorldPreset(Collections.emptyMap()));

//    public static final RegistryObject<LostCityFeature> LOSTCITY_FEATURE = FEATURES.register("lostcity", LostCityFeature::new);
//    public static final RegistryObject<LostCitySphereFeature> LOSTCITY_SPHERE_FEATURE = FEATURES.register("spheres", LostCitySphereFeature::new);

//    public static final ResourceLocation LOSTCITY = new ResourceLocation(LostWorlds.MODID, "lostcity");

//    public static final ResourceKey<DimensionType> DIMENSION_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, LOSTCITY);
//    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, LOSTCITY);
}
