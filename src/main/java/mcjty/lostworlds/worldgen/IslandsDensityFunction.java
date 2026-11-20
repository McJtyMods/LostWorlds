package mcjty.lostworlds.worldgen;

import com.mojang.serialization.MapCodec;
import mcjty.lostworlds.LostWorlds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Interval;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.densityfunction.*;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class IslandsDensityFunction implements DensityFunction {
    public static final MapCodec<IslandsDensityFunction> CODEC = MapCodec.unit(new IslandsDensityFunction(0L));
    public static final MapCodec<DensityFunction> CODEC_CHEESE = MapCodec.unit(new IslandsDensityFunction(0L).add(new BlendedNoise(0.25D, 0.25D, 80.0D, 160.0D, 4.0D)));
    private static final float ISLAND_THRESHOLD = -0.9F;
    private final SimplexNoise islandNoise;

    public static final Identifier LOST_ISLANDS = Identifier.fromNamespaceAndPath(LostWorlds.MODID, "lost_islands");
    public static final Identifier LOST_ISLANDS_CHEESE = Identifier.fromNamespaceAndPath(LostWorlds.MODID, "lost_islands_cheese");

    public IslandsDensityFunction(long p_208630_) {
        RandomSource randomsource = new LegacyRandomSource(p_208630_);
        randomsource.consumeCount(17292);
        this.islandNoise = new SimplexNoise(randomsource, true);
    }

    private static float getHeightValue(SimplexNoise noise, int x, int z) {
        int i = x / 2;
        int j = z / 2;
        int k = x % 2;
        int l = z % 2;
        float f = 100.0F - Mth.sqrt((float)(x * x + z * z)) * 8.0F;
        // 80 is no islands
        // -100 is a lot of open space
        f = -10.0f;//Mth.clamp(f, -100.0F, 80.0F);

        for(int i1 = -12; i1 <= 12; ++i1) {
            for(int j1 = -12; j1 <= 12; ++j1) {
                long k1 = i + i1;
                long l1 = j + j1;
                if (k1 * k1 + l1 * l1 > 4096L && noise.get(k1, l1) < (double)-0.9F) {
                    float f1 = (Mth.abs((float)k1) * 3439.0F + Mth.abs((float)l1) * 147.0F) % 13.0F + 9.0F;
                    float f2 = (float)(k - i1 * 2);
                    float f3 = (float)(l - j1 * 2);
                    float f4 = 100.0F - Mth.sqrt(f2 * f2 + f3 * f3) * f1;
                    f4 = Mth.clamp(f4, -100.0F, 80.0F);
                    f = Math.max(f, f4);
                }
            }
        }

        return f;
    }

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        return new IslandSampler(islandNoise);
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return this;
    }

    @Override
    public Interval range() {
        return Interval.of(-0.84375F, 0.5625F);
    }

    @Override
    public int domainAxes() {
        return AXIS_X | AXIS_Z;
    }

    @Override
    public MapCodec<IslandsDensityFunction> codec() {
        return CODEC;
    }

    private record IslandSampler(SimplexNoise noise) implements DensitySampler {

        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer output, DensityVolume volume) {
            for (int z = 0; z < volume.sizeZ(); z++) {
                for (int x = 0; x < volume.sizeX(); x++) {
                    float value = sampleValue(context, volume.blockX(x), 0, volume.blockZ(z));
                    output.setRange(volume.indexUnchecked(x, 0, z), volume.sizeY(), value);
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int x, int y, int z) {
            return (getHeightValue(noise, x / 8, z / 8) - 8.0F) / 128.0F;
        }
    }
}
