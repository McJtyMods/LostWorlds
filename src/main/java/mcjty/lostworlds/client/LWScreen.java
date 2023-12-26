package mcjty.lostworlds.client;

import mcjty.lostworlds.LostWorlds;
import mcjty.lostworlds.worldgen.FogColor;
import mcjty.lostworlds.worldgen.LWChunkGenerator;
import mcjty.lostworlds.worldgen.LWSettings;
import mcjty.lostworlds.worldgen.LostWorldType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.core.HolderGetter;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public class LWScreen extends Screen {

    private final CreateWorldScreen parent;
    private final BiConsumer<NoiseGeneratorSettings, LWSettings> applySettings;
    private NoiseGeneratorSettings generator;
    private final HolderGetter<NoiseGeneratorSettings> noisegetter;

    record SelectedSetting(ResourceKey<NoiseGeneratorSettings> settingsKey, String iconName, String description) {}

    private final static SelectedSetting ISLANDS = new SelectedSetting(LWChunkGenerator.LOST_ISLANDS, "icon_islands.png", "createWorld.customize.lostworlds.islands.description");
    private final static SelectedSetting ISLANDSWATER = new SelectedSetting(LWChunkGenerator.LOST_ISLANDS_WATER, "icon_islandswater.png", "createWorld.customize.lostworlds.islandswater.description");
    private final static SelectedSetting CAVES = new SelectedSetting(LWChunkGenerator.LOST_CAVES, "icon_caves.png", "createWorld.customize.lostworlds.caves.description");
    private final static SelectedSetting VOID = new SelectedSetting(LWChunkGenerator.LOST_VOID, "icon_void.png", "createWorld.customize.lostworlds.void.description");
    private SelectedSetting selected = ISLANDS;

    private Button islandsButton;
    private Button islandswaterButton;
    private Button cavesButton;
    private Button voidButton;

    private CycleButton<FogColor> fogColorButton;

    public LWScreen(CreateWorldScreen worldScreen, HolderGetter<NoiseGeneratorSettings> noisegetter,
                    BiConsumer<NoiseGeneratorSettings, LWSettings> settingsConsumer, NoiseGeneratorSettings generator) {
        super(Component.translatable("createWorld.customize.lostworlds.title"));
        this.parent = worldScreen;
        this.applySettings = settingsConsumer;
        this.generator = generator;
        this.noisegetter = noisegetter;
    }

    @Override
    protected void init() {
        islandsButton = addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.lostworlds.islands"), (button) -> {
            selected = ISLANDS;
        }).bounds(10, this.height - 52, 70, 20).build());
        islandswaterButton = addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.lostworlds.islandswater"), (button) -> {
            selected = ISLANDSWATER;
        }).bounds(90, this.height - 52, 70, 20).build());
        cavesButton = addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.lostworlds.caves"), (button) -> {
            selected = CAVES;
        }).bounds(170, this.height - 52, 70, 20).build());
        voidButton = addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.lostworlds.void"), (button) -> {
            selected = VOID;
        }).bounds(250, this.height - 52, 70, 20).build());

        fogColorButton = addRenderableWidget(CycleButton.builder((FogColor t) -> Component.literal(t.getSerializedName()))
                .withValues(FogColor.values())
                .create(10, 80, 70, 20, Component.literal("Fog")));

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            generator = noisegetter.getOrThrow(selected.settingsKey()).get();
            LWSettings lwSettings = getLwSettings();
            this.applySettings.accept(this.generator, lwSettings);
            this.minecraft.setScreen(this.parent);
        }).bounds(this.width - 130, this.height - 28, 50, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> {
            this.minecraft.setScreen(this.parent);
        }).bounds(this.width - 70, this.height - 28, 50, 20).build());
    }

    @NotNull
    private LWSettings getLwSettings() {
        LostWorldType type;
        if (selected == ISLANDS) {
            type = LostWorldType.ISLANDS;
        } else if (selected == ISLANDSWATER) {
            type = LostWorldType.ISLANDS_WATER;
        } else if (selected == CAVES) {
            type = LostWorldType.CAVES;
        } else {
            type = LostWorldType.VOID;
        }
        return new LWSettings(type, fogColorButton.getValue());
    }

    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        islandsButton.setFGColor(selected == ISLANDS ? 0x0044ff44 : 0x00aaaaaa);
        islandswaterButton.setFGColor(selected == ISLANDSWATER ? 0x0044ff44 : 0x00aaaaaa);
        cavesButton.setFGColor(selected == CAVES ? 0x0044ff44 : 0x00aaaaaa);
        voidButton.setFGColor(selected == VOID ? 0x0044ff44 : 0x00aaaaaa);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
        graphics.drawWordWrap(this.font, Component.translatable(this.selected.description), 20, 40, this.width - 150,16777215);
        graphics.blit(new ResourceLocation(LostWorlds.MODID, "textures/gui/" + selected.iconName), this.width - 120, 40,  100, 100, 0, 0, 128, 128, 128, 128);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

}
