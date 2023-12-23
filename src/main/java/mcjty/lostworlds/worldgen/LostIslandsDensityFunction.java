package mcjty.lostworlds.worldgen;

import com.mojang.serialization.MapCodec;
import mcjty.lostworlds.LostWorlds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class LostIslandsDensityFunction implements DensityFunction.SimpleFunction {
    public static final KeyDispatchDataCodec<LostIslandsDensityFunction> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(new LostIslandsDensityFunction(0L)));
    public static final KeyDispatchDataCodec<DensityFunction> CODEC_CHEESE = KeyDispatchDataCodec.of(MapCodec.unit(DensityFunctions.add(new LostIslandsDensityFunction(0L), BlendedNoise.createUnseeded(0.25D, 0.25D, 80.0D, 160.0D, 4.0D))));
    private static final float ISLAND_THRESHOLD = -0.9F;
    private final SimplexNoise islandNoise;

    public static final ResourceLocation LOST_ISLANDS = new ResourceLocation(LostWorlds.MODID, "lost_islands");
    public static final ResourceLocation LOST_ISLANDS_CHEESE = new ResourceLocation(LostWorlds.MODID, "lost_islands_cheese");

    public LostIslandsDensityFunction(long p_208630_) {
        RandomSource randomsource = new LegacyRandomSource(p_208630_);
        randomsource.consumeCount(17292);
        this.islandNoise = new SimplexNoise(randomsource);
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
                if (k1 * k1 + l1 * l1 > 4096L && noise.getValue((double)k1, (double)l1) < (double)-0.9F) {
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

    public double compute(DensityFunction.FunctionContext context) {
        return ((double)getHeightValue(this.islandNoise, context.blockX() / 8, context.blockZ() / 8) - 8.0D) / 128.0D;
    }

    public double minValue() {
        return -0.84375D;
    }

    public double maxValue() {
        return 0.5625D;
    }

    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
