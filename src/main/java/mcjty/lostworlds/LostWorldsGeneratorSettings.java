package mcjty.lostworlds;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class LostWorldsGeneratorSettings {
   public static final Codec<LostWorldsGeneratorSettings> CODEC = RecordCodecBuilder.<LostWorldsGeneratorSettings>create((instance) ->
           instance.group(
                   RegistryCodecs.homogeneousList(Registries.STRUCTURE_SET).optionalFieldOf("structure_overrides").forGetter((settings) -> settings.structureOverrides),
                   FlatLayerInfo.CODEC.listOf().fieldOf("layers").forGetter(LostWorldsGeneratorSettings::getLayersInfo),
                   Codec.BOOL.fieldOf("lakes").orElse(false).forGetter((settings) -> settings.addLakes),
                   Codec.BOOL.fieldOf("features").orElse(false).forGetter((settings) -> settings.decoration),
                   Biome.CODEC.optionalFieldOf("biome").orElseGet(Optional::empty).forGetter((settings) -> Optional.of(settings.biome)),
                   RegistryOps.retrieveElement(Biomes.PLAINS),
                   RegistryOps.retrieveElement(MiscOverworldPlacements.LAKE_LAVA_UNDERGROUND),
                   RegistryOps.retrieveElement(MiscOverworldPlacements.LAKE_LAVA_SURFACE)).apply(instance, LostWorldsGeneratorSettings::new))
           .comapFlatMap(LostWorldsGeneratorSettings::validateHeight, Function.identity()).stable();

   private final Optional<HolderSet<StructureSet>> structureOverrides;
   private final List<FlatLayerInfo> layersInfo = Lists.newArrayList();
   private final Holder<Biome> biome;
   private final List<BlockState> layers;
   private boolean voidGen;
   private boolean decoration;
   private boolean addLakes;
   private final List<Holder<PlacedFeature>> lakes;

   private static DataResult<LostWorldsGeneratorSettings> validateHeight(LostWorldsGeneratorSettings p_161906_) {
      int i = p_161906_.layersInfo.stream().mapToInt(FlatLayerInfo::getHeight).sum();
      return i > DimensionType.Y_SIZE ? DataResult.error(() -> "Sum of layer heights is > " + DimensionType.Y_SIZE, p_161906_) : DataResult.success(p_161906_);
   }

   private LostWorldsGeneratorSettings(Optional<HolderSet<StructureSet>> p_256456_, List<FlatLayerInfo> p_255826_, boolean p_255740_, boolean p_255726_, Optional<Holder<Biome>> p_256292_, Holder.Reference<Biome> p_255964_, Holder<PlacedFeature> p_256419_, Holder<PlacedFeature> p_255710_) {
      this(p_256456_, getBiome(p_256292_, p_255964_), List.of(p_256419_, p_255710_));
      if (p_255740_) {
         this.setAddLakes();
      }

      if (p_255726_) {
         this.setDecoration();
      }

      this.layersInfo.addAll(p_255826_);
      this.updateLayers();
   }

   private static Holder<Biome> getBiome(Optional<? extends Holder<Biome>> pBiome, Holder<Biome> pDefaultBiome) {
      if (pBiome.isEmpty()) {
         LOGGER.error("Unknown biome, defaulting to plains");
         return pDefaultBiome;
      } else {
         return pBiome.get();
      }
   }

   public LostWorldsGeneratorSettings(Optional<HolderSet<StructureSet>> pStructureOverrides, Holder<Biome> pBiome, List<Holder<PlacedFeature>> pLakes) {
      this.structureOverrides = pStructureOverrides;
      this.biome = pBiome;
      this.layers = Lists.newArrayList();
      this.lakes = pLakes;
   }

   public LostWorldsGeneratorSettings withBiomeAndLayers(List<FlatLayerInfo> pLayerInfos, Optional<HolderSet<StructureSet>> pStructureSets, Holder<Biome> pBiome) {
      LostWorldsGeneratorSettings flatlevelgeneratorsettings = new LostWorldsGeneratorSettings(pStructureSets, pBiome, this.lakes);

      for(FlatLayerInfo flatlayerinfo : pLayerInfos) {
         flatlevelgeneratorsettings.layersInfo.add(new FlatLayerInfo(flatlayerinfo.getHeight(), flatlayerinfo.getBlockState().getBlock()));
         flatlevelgeneratorsettings.updateLayers();
      }

      if (this.decoration) {
         flatlevelgeneratorsettings.setDecoration();
      }

      if (this.addLakes) {
         flatlevelgeneratorsettings.setAddLakes();
      }

      return flatlevelgeneratorsettings;
   }

   public void setDecoration() {
      this.decoration = true;
   }

   public void setAddLakes() {
      this.addLakes = true;
   }

   public BiomeGenerationSettings adjustGenerationSettings(Holder<Biome> pBiome) {
      if (!pBiome.equals(this.biome)) {
         return pBiome.value().getGenerationSettings();
      } else {
         BiomeGenerationSettings biomegenerationsettings = this.getBiome().value().getGenerationSettings();
         BiomeGenerationSettings.PlainBuilder biomegenerationsettings$plainbuilder = new BiomeGenerationSettings.PlainBuilder();
         if (this.addLakes) {
            for(Holder<PlacedFeature> holder : this.lakes) {
               biomegenerationsettings$plainbuilder.addFeature(GenerationStep.Decoration.LAKES, holder);
            }
         }

         boolean flag = (!this.voidGen || pBiome.is(Biomes.THE_VOID)) && this.decoration;
         if (flag) {
            List<HolderSet<PlacedFeature>> list = biomegenerationsettings.features();

            for(int i = 0; i < list.size(); ++i) {
               if (i != GenerationStep.Decoration.UNDERGROUND_STRUCTURES.ordinal() && i != GenerationStep.Decoration.SURFACE_STRUCTURES.ordinal() && (!this.addLakes || i != GenerationStep.Decoration.LAKES.ordinal())) {
                  for(Holder<PlacedFeature> holder1 : list.get(i)) {
                     biomegenerationsettings$plainbuilder.addFeature(i, holder1);
                  }
               }
            }
         }

         List<BlockState> list1 = this.getLayers();

         for(int j = 0; j < list1.size(); ++j) {
            BlockState blockstate = list1.get(j);
            if (!Heightmap.Types.MOTION_BLOCKING.isOpaque().test(blockstate)) {
               list1.set(j, (BlockState)null);
               biomegenerationsettings$plainbuilder.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, PlacementUtils.inlinePlaced(Feature.FILL_LAYER, new LayerConfiguration(j, blockstate)));
            }
         }

         return biomegenerationsettings$plainbuilder.build();
      }
   }

   public Optional<HolderSet<StructureSet>> structureOverrides() {
      return this.structureOverrides;
   }

   /**
    * Return the biome used on this preset.
    */
   public Holder<Biome> getBiome() {
      return this.biome;
   }

   /**
    * Return the list of layers on this preset.
    */
   public List<FlatLayerInfo> getLayersInfo() {
      return this.layersInfo;
   }

   public List<BlockState> getLayers() {
      return this.layers;
   }

   public void updateLayers() {
      this.layers.clear();

      for(FlatLayerInfo flatlayerinfo : this.layersInfo) {
         for(int i = 0; i < flatlayerinfo.getHeight(); ++i) {
            this.layers.add(flatlayerinfo.getBlockState());
         }
      }

      this.voidGen = this.layers.stream().allMatch((p_209802_) -> p_209802_.is(Blocks.AIR));
   }

   public static LostWorldsGeneratorSettings getDefault(HolderGetter<Biome> pBiomes, HolderGetter<StructureSet> pStructureSetGetter, HolderGetter<PlacedFeature> pPlacedFeatureGetter) {
      HolderSet<StructureSet> holderset = HolderSet.direct(pStructureSetGetter.getOrThrow(BuiltinStructureSets.STRONGHOLDS), pStructureSetGetter.getOrThrow(BuiltinStructureSets.VILLAGES));
      LostWorldsGeneratorSettings flatlevelgeneratorsettings = new LostWorldsGeneratorSettings(Optional.of(holderset), getDefaultBiome(pBiomes), createLakesList(pPlacedFeatureGetter));
      flatlevelgeneratorsettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BEDROCK));
      flatlevelgeneratorsettings.getLayersInfo().add(new FlatLayerInfo(2, Blocks.DIRT));
      flatlevelgeneratorsettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.GRASS_BLOCK));
      flatlevelgeneratorsettings.updateLayers();
      return flatlevelgeneratorsettings;
   }

   public static Holder<Biome> getDefaultBiome(HolderGetter<Biome> pBiomes) {
      return pBiomes.getOrThrow(Biomes.PLAINS);
   }

   public static List<Holder<PlacedFeature>> createLakesList(HolderGetter<PlacedFeature> pPlacedFEatureGetter) {
      return List.of(pPlacedFEatureGetter.getOrThrow(MiscOverworldPlacements.LAKE_LAVA_UNDERGROUND), pPlacedFEatureGetter.getOrThrow(MiscOverworldPlacements.LAKE_LAVA_SURFACE));
   }
}