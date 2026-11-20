package mcjty.lostworlds;

import mcjty.lostworlds.setup.Config;
import mcjty.lostworlds.setup.Messages;
import mcjty.lostworlds.setup.Registration;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class LostWorlds implements ModInitializer {

    public static final String MODID = "lostworlds";

    @Override
    public void onInitialize() {
        Config.register();
        Registration.init();
        Messages.register();
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> EventHandlers.sendWorldInfo(handler.player));
        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, from, to) -> EventHandlers.sendWorldInfo(player));
        ServerPlayerEvents.AFTER_RESPAWN.register((previous, player, alive) -> EventHandlers.sendWorldInfo(player));
    }
}
