package mcjty.lostworlds.network;

import mcjty.lostworlds.client.LostWorldsSpecialEffects;
import mcjty.lostworlds.worldgen.FogColor;
import mcjty.lostworlds.worldgen.LostWorldType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketWorldInfoToClient {

    private final LostWorldType type;
    private final FogColor fogColor;

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeShort(type.ordinal());
        buf.writeShort(fogColor.ordinal());
    }

    public PacketWorldInfoToClient(FriendlyByteBuf buf) {
        type = LostWorldType.values()[buf.readShort()];
        fogColor = FogColor.values()[buf.readShort()];
    }

    public PacketWorldInfoToClient(LostWorldType type, FogColor fogColor) {
        this.type = type;
        this.fogColor = fogColor;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            LostWorldsSpecialEffects.type = type;
            LostWorldsSpecialEffects.fogColor = fogColor;
        });
        ctx.setPacketHandled(true);
    }
}
