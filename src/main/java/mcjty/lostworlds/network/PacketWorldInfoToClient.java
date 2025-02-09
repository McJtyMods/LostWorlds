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
    private final boolean disabled;

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeShort(type.ordinal());
        buf.writeShort(fogColor.ordinal());
        buf.writeBoolean(disabled);
    }

    public PacketWorldInfoToClient(FriendlyByteBuf buf) {
        type = LostWorldType.values()[buf.readShort()];
        fogColor = FogColor.values()[buf.readShort()];
        disabled = buf.readBoolean();
    }

    public PacketWorldInfoToClient(LostWorldType type, FogColor fogColor, boolean disabled) {
        this.type = type;
        this.fogColor = fogColor;
        this.disabled = disabled;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            LostWorldsSpecialEffects.type = type;
            LostWorldsSpecialEffects.fogColor = fogColor;
            LostWorldsSpecialEffects.disabled = disabled;
        });
        ctx.setPacketHandled(true);
    }
}
