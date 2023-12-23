package mcjty.lostworlds.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;

public record LWSettings(LostWorldType type, FogColor fogColor) {
    public static final Codec<LWSettings> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                    StringRepresentable.fromEnum(LostWorldType::values).fieldOf("type").forGetter(LWSettings::type),
                    StringRepresentable.fromEnum(FogColor::values).fieldOf("fog").forGetter(LWSettings::fogColor))
            .apply(instance, instance.stable(LWSettings::new)));

}
