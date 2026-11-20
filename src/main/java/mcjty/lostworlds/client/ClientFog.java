package mcjty.lostworlds.client;

import mcjty.lostworlds.network.PacketWorldInfoToClient;
import mcjty.lostworlds.setup.Config;
import mcjty.lostworlds.worldgen.FogColor;
import mcjty.lostworlds.worldgen.LostWorldType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public final class ClientFog {

    private static PacketWorldInfoToClient info;

    private ClientFog() {
    }

    public static void update(PacketWorldInfoToClient packet) {
        info = packet;
    }

    @Nullable
    public static LostWorldType worldType(@Nullable ClientLevel level) {
        return info != null && !info.disabled() && level != null
                && Config.getDimensionsWithSpecialFog().contains(level.dimension().identifier())
                ? info.worldType() : null;
    }

    @Nullable
    public static Vec3 color(@Nullable ClientLevel level) {
        LostWorldType type = worldType(level);
        if (type == null) {
            return null;
        }
        if (info.fogColor() != FogColor.NONE) {
            return info.fogColor().getFogColor();
        }
        return type == LostWorldType.CAVES || type == LostWorldType.CAVESPHERES
                ? Vec3.ZERO : null;
    }
}
