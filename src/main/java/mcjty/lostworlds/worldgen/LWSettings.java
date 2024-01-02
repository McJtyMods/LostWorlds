package mcjty.lostworlds.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;

import java.util.Optional;

public record LWSettings(LostWorldType type, FogColor fogColor, Integer seaLevel) {
    public static final Codec<LWSettings> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                    StringRepresentable.fromEnum(LostWorldType::values).fieldOf("type").forGetter(LWSettings::type),
                    StringRepresentable.fromEnum(FogColor::values).fieldOf("fog").forGetter(LWSettings::fogColor),
                    Codec.intRange(-64, 384).optionalFieldOf("sea_level").forGetter(s -> Optional.ofNullable(s.seaLevel())))
            .apply(instance, instance.stable((tp, fc, wl) -> new LWSettings(tp, fc, wl.orElse(null)))));

}
