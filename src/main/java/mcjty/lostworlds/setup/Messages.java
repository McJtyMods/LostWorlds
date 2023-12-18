package mcjty.lostworlds.setup;

import mcjty.lostworlds.LostWorlds;
import mcjty.lostworlds.network.PacketWorldInfoToClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class Messages {
    public static SimpleChannel INSTANCE;

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void registerMessages(String name) {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(LostWorlds.MODID, name))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.registerMessage(id(), PacketWorldInfoToClient.class, PacketWorldInfoToClient::toBytes, PacketWorldInfoToClient::new, PacketWorldInfoToClient::handle);
    }
}
