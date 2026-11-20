package mcjty.lostworlds.client;

import mcjty.lostworlds.LostWorlds;
import mcjty.lostworlds.compat.LostCitiesCompat;
import mcjty.lostworlds.worldgen.FogColor;
import mcjty.lostworlds.worldgen.LWChunkGenerator;
import mcjty.lostworlds.worldgen.LWSettings;
import mcjty.lostworlds.worldgen.LostWorldType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public class LWScreen extends Screen {

    private final CreateWorldScreen parent;
    private final BiConsumer<NoiseGeneratorSettings, LWSettings> applySettings;
    private NoiseGeneratorSettings generator;
    private LWSettings lwSettings;
    private final HolderGetter<NoiseGeneratorSettings> noisegetter;

    record SelectedSetting(LostWorldType type, ResourceKey<NoiseGeneratorSettings> settingsKey, String iconName, String description, String profile) {}

    private final static SelectedSetting NORMAL = new SelectedSetting(LostWorldType.NORMAL, LWChunkGenerator.LOST_NORMAL, "icon_normal.png", "createWorld.customize.lostworlds.normal.description", "biosphere");
    private final static SelectedSetting ATLANTIS = new SelectedSetting(LostWorldType.ATLANTIS, LWChunkGenerator.LOST_ATLANTIS, "icon_atlantis.png", "createWorld.customize.lostworlds.atlantis.description", "atlantis");
    private final static SelectedSetting ISLANDS = new SelectedSetting(LostWorldType.ISLANDS, LWChunkGenerator.LOST_ISLANDS, "icon_islands.png", "createWorld.customize.lostworlds.islands.description", "floating");
    private final static SelectedSetting CAVES = new SelectedSetting(LostWorldType.CAVES, LWChunkGenerator.LOST_CAVES, "icon_caves.png", "createWorld.customize.lostworlds.caves.description", "cavern");
    private final static SelectedSetting SPHERES = new SelectedSetting(LostWorldType.SPHERES, LWChunkGenerator.LOST_SPHERES, "icon_spheres.png", "createWorld.customize.lostworlds.spheres.description", "space");
    private final static SelectedSetting CAVESPHERES = new SelectedSetting(LostWorldType.CAVESPHERES, LWChunkGenerator.LOST_CAVESPHERES, "icon_cavespheres.png", "createWorld.customize.lostworlds.cavespheres.description", "biosphere_caves");
    private SelectedSetting selected = ISLANDS;

    private Button islandsButton;
    private Button normalButton;
    private Button cavesButton;
    private Button spheresButton;
    private Button atlantisButton;
    private Button cavespheresButton;

    private CycleButton<FogColor> fogColorButton;
    private SeaLevelSlider seaLevelSlider;

    public LWScreen(CreateWorldScreen worldScreen, HolderGetter<NoiseGeneratorSettings> noisegetter,
                    BiConsumer<NoiseGeneratorSettings, LWSettings> settingsConsumer,
                    NoiseGeneratorSettings generator,
                    LWSettings lwSettings) {
        super(Component.translatable("createWorld.customize.lostworlds.title"));
        this.parent = worldScreen;
        this.applySettings = settingsConsumer;
        this.generator = generator;
        this.lwSettings = lwSettings;
        this.noisegetter = noisegetter;
    }

    @Override
    protected void init() {
        islandsButton = addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.lostworlds.islands"), (button) -> {
            selected = ISLANDS;
        }).bounds(5, this.height - 56, 65, 20).build());
        cavesButton = addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.lostworlds.caves"), (button) -> {
            selected = CAVES;
        }).bounds(75, this.height - 56, 65, 20).build());
        spheresButton = addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.lostworlds.spheres"), (button) -> {
            selected = SPHERES;
        }).bounds(145, this.height - 56, 65, 20).build());
        normalButton = addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.lostworlds.normal"), (button) -> {
            selected = NORMAL;
        }).bounds(215, this.height - 56, 65, 20).build());
        atlantisButton = addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.lostworlds.atlantis"), (button) -> {
            selected = ATLANTIS;
        }).bounds(285, this.height - 56, 65, 20).build());
        cavespheresButton = addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.lostworlds.cavespheres"), (button) -> {
            selected = CAVESPHERES;
        }).bounds(355, this.height - 56, 65, 20).build());
        selected = switch (lwSettings.type()) {
            case ISLANDS -> ISLANDS;
            case CAVES -> CAVES;
            case SPHERES -> SPHERES;
            case ATLANTIS -> ATLANTIS;
            case CAVESPHERES -> CAVESPHERES;
            default -> NORMAL;
        };

        fogColorButton = addRenderableWidget(CycleButton.builder((FogColor t) -> Component.literal(t.getSerializedName()), lwSettings.fogColor())
                .withValues(FogColor.values())
                .create(10, 80, 70, 20, Component.literal("Fog")));
        fogColorButton.setValue(lwSettings.fogColor());
        seaLevelSlider = addRenderableWidget(new SeaLevelSlider(10, 110, 220, 20, lwSettings.seaLevel()));

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            Holder<NoiseGeneratorSettings> gen = noisegetter.getOrThrow(selected.settingsKey());
            this.generator = gen.value();
            LWSettings lwSettings = createNewLwSettings();
            this.applySettings.accept(this.generator, lwSettings);
            this.minecraft.setScreen(this.parent);
            LostCitiesCompat.setProfile(selected.profile());
        }).bounds(this.width - 130, this.height - 28, 50, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> {
            this.minecraft.setScreen(this.parent);
        }).bounds(this.width - 70, this.height - 28, 50, 20).build());
    }

    @NotNull
    private LWSettings createNewLwSettings() {
        return new LWSettings(selected.type(), fogColorButton.getValue(),
                seaLevelSlider.seaLevel());
    }

    private Component modeLabel(SelectedSetting setting) {
        return Component.translatable("createWorld.customize.lostworlds." + setting.type().getSerializedName())
                .withStyle(selected == setting ? ChatFormatting.GREEN : ChatFormatting.GRAY);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        islandsButton.setMessage(modeLabel(ISLANDS));
        cavesButton.setMessage(modeLabel(CAVES));
        spheresButton.setMessage(modeLabel(SPHERES));
        normalButton.setMessage(modeLabel(NORMAL));
        atlantisButton.setMessage(modeLabel(ATLANTIS));
        cavespheresButton.setMessage(modeLabel(CAVESPHERES));
        seaLevelSlider.active = selected.type.supportsCustomSea();
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFFFF);
        graphics.drawWordWrap(this.font, Component.translatable(this.selected.description), 20, 40, this.width - 150,0xFFFFFFFF);
        graphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(LostWorlds.MODID, "textures/gui/" + selected.iconName), this.width - 120, 40, 0, 0, 100, 100, 128, 128, 128, 128);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

}
