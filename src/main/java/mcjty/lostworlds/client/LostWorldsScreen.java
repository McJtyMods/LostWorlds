package mcjty.lostworlds.client;

import mcjty.lostworlds.LostWorldsGeneratorSettings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class LostWorldsScreen extends Screen {

    private final CreateWorldScreen parent;
    private final Consumer<LostWorldsGeneratorSettings> applySettings;
    private final LostWorldsGeneratorSettings generator;

    public LostWorldsScreen(CreateWorldScreen worldScreen, Consumer<LostWorldsGeneratorSettings> settingsConsumer, LostWorldsGeneratorSettings generator) {
        super(Component.translatable("createWorld.customize.flat.title"));
        this.parent = worldScreen;
        this.applySettings = settingsConsumer;
        this.generator = generator;
    }

}
