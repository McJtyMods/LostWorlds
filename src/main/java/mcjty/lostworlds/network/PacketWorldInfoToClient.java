package mcjty.lostworlds.network;

import mcjty.lostworlds.LostWorlds;
import mcjty.lostworlds.worldgen.FogColor;
import mcjty.lostworlds.worldgen.LostWorldType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PacketWorldInfoToClient(LostWorldType worldType, FogColor fogColor, boolean disabled) implements CustomPacketPayload {

    public static final Type<PacketWorldInfoToClient> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(LostWorlds.MODID, "world_info"));
    public static final StreamCodec<FriendlyByteBuf, PacketWorldInfoToClient> STREAM_CODEC =
            StreamCodec.ofMember(PacketWorldInfoToClient::write, PacketWorldInfoToClient::read);

    private static PacketWorldInfoToClient read(FriendlyByteBuf buffer) {
        return new PacketWorldInfoToClient(buffer.readEnum(LostWorldType.class), buffer.readEnum(FogColor.class), buffer.readBoolean());
    }

    private void write(FriendlyByteBuf buffer) {
        buffer.writeEnum(worldType);
        buffer.writeEnum(fogColor);
        buffer.writeBoolean(disabled);
    }

    @Override
    public Type<PacketWorldInfoToClient> type() {
        return TYPE;
    }
}
