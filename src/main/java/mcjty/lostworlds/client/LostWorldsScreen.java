package mcjty.lostworlds.client;

import mcjty.lostworlds.LostWorlds;
import mcjty.lostworlds.LostWorldsChunkGenerator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.core.HolderGetter;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.function.Consumer;

public class LostWorldsScreen extends Screen {

    private final CreateWorldScreen parent;
    private final Consumer<NoiseGeneratorSettings> applySettings;
    private NoiseGeneratorSettings generator;
    private final HolderGetter<NoiseGeneratorSettings> noisegetter;

    record SelectedSetting(ResourceKey<NoiseGeneratorSettings> settingsKey, String iconName, String description) {}

    private final static SelectedSetting ISLANDS = new SelectedSetting(LostWorldsChunkGenerator.LOST_ISLANDS, "icon_islands.png", "createWorld.customize.lostworlds.islands.description");
    private final static SelectedSetting CAVES = new SelectedSetting(LostWorldsChunkGenerator.LOST_CAVES, "icon_caves.png", "createWorld.customize.lostworlds.caves.description");
    private SelectedSetting selected = ISLANDS;

    public LostWorldsScreen(CreateWorldScreen worldScreen, HolderGetter<NoiseGeneratorSettings> noisegetter, Consumer<NoiseGeneratorSettings> settingsConsumer, NoiseGeneratorSettings generator) {
        super(Component.translatable("createWorld.customize.lostworlds.title"));
        this.parent = worldScreen;
        this.applySettings = settingsConsumer;
        this.generator = generator;
        this.noisegetter = noisegetter;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.lostworlds.islands"), (button) -> {
            selected = ISLANDS;
        }).bounds(this.width / 2 - 155, this.height - 52, 150, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.lostworlds.caves"), (button) -> {
            selected = CAVES;
        }).bounds(this.width / 2 + 5, this.height - 52, 150, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (p_280791_) -> {
            generator = noisegetter.getOrThrow(selected.settingsKey()).get();
            this.applySettings.accept(this.generator);
            this.minecraft.setScreen(this.parent);
        }).bounds(this.width / 2 - 155, this.height - 28, 150, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (p_280792_) -> {
            this.minecraft.setScreen(this.parent);
        }).bounds(this.width / 2 + 5, this.height - 28, 150, 20).build());
    }

    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
        graphics.drawWordWrap(this.font, Component.translatable(this.selected.description), 20, 40, this.width - 140,16777215);
        graphics.blit(new ResourceLocation(LostWorlds.MODID, "textures/gui/" + selected.iconName), this.width - 120, 40,  100, 100, 0, 0, 128, 128, 128, 128);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

}
