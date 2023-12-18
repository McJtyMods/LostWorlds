package mcjty.lostworlds.network;

import mcjty.lostworlds.client.LostWorlsSpecialEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketWorldInfoToClient {

    private final boolean isFlat;

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(isFlat);
    }

    public PacketWorldInfoToClient(FriendlyByteBuf buf) {
        isFlat = buf.readBoolean();
    }

    public PacketWorldInfoToClient(boolean isFlat) {
        this.isFlat = isFlat;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            System.out.println("PacketWorldInfoToClient.handle: " + isFlat);
            LostWorlsSpecialEffects.isFlat = isFlat;
        });
        ctx.setPacketHandled(true);
    }
}
