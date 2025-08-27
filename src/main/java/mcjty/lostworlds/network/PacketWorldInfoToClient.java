package mcjty.lostworlds.network;

import mcjty.lostworlds.LostWorlds;
import mcjty.lostworlds.client.LostWorldsSpecialEffects;
import mcjty.lostworlds.worldgen.FogColor;
import mcjty.lostworlds.worldgen.LostWorldType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketWorldInfoToClient(LostWorldType worldType, FogColor fogColor) implements CustomPacketPayload {
	public static final Type<PacketWorldInfoToClient> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LostWorlds.MODID, "world_info_to_client"));

	public static final StreamCodec<RegistryFriendlyByteBuf, PacketWorldInfoToClient> STREAM_CODEC = StreamCodec.composite(
		LostWorldType.STREAM_CODEC, PacketWorldInfoToClient::worldType,
		FogColor.STREAM_CODEC, PacketWorldInfoToClient::fogColor,
		PacketWorldInfoToClient::new
	);

    public static void handle(PacketWorldInfoToClient data, IPayloadContext context) {
        context.enqueueWork(() -> {
            LostWorldsSpecialEffects.type = data.worldType;
            LostWorldsSpecialEffects.fogColor = data.fogColor;
        });
    }

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
