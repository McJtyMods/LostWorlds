package mcjty.lostworlds.network;

import mcjty.lostworlds.client.LostWorlsSpecialEffects;
import mcjty.lostworlds.worldgen.LostWorldType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketWorldInfoToClient {

    private final LostWorldType type;

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeShort(type.ordinal());
    }

    public PacketWorldInfoToClient(FriendlyByteBuf buf) {
        type = LostWorldType.values()[buf.readShort()];
    }

    public PacketWorldInfoToClient(LostWorldType type) {
        this.type = type;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            LostWorlsSpecialEffects.type = type;
        });
        ctx.setPacketHandled(true);
    }
}
