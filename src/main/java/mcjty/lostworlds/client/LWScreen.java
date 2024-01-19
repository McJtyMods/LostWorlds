package mcjty.lostworlds.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import mcjty.lostworlds.LostWorlds;
import mcjty.lostworlds.compat.LostCitiesCompat;
import mcjty.lostworlds.setup.ModSetup;
import mcjty.lostworlds.worldgen.FogColor;
import mcjty.lostworlds.worldgen.LWChunkGenerator;
import mcjty.lostworlds.worldgen.LWSettings;
import mcjty.lostworlds.worldgen.LostWorldType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraftforge.client.gui.widget.ForgeSlider;
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
    private final static SelectedSetting CAVES = new SelectedSetting(LostWorldType.CAVES, LWChunkGenerator.LOST_CAVES, "icon_caves.png", "createWorld.customize.lostworlds.caves.description", "caves");
    private final static SelectedSetting SPHERES = new SelectedSetting(LostWorldType.SPHERES, LWChunkGenerator.LOST_SPHERES, "icon_spheres.png", "createWorld.customize.lostworlds.spheres.description", "space");
    private SelectedSetting selected = ISLANDS;

    private Button islandsButton;
    private Button normalButton;
    private Button cavesButton;
    private Button spheresButton;
    private Button atlantisButton;

    private CycleButton<FogColor> fogColorButton;
    private ForgeSlider seaLevelSlider;

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
        }).bounds(10, this.height - 56, 70, 20).build());
        cavesButton = addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.lostworlds.caves"), (button) -> {
            selected = CAVES;
        }).bounds(90, this.height - 56, 70, 20).build());
        spheresButton = addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.lostworlds.spheres"), (button) -> {
            selected = SPHERES;
        }).bounds(170, this.height - 56, 70, 20).build());
        normalButton = addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.lostworlds.normal"), (button) -> {
            selected = NORMAL;
        }).bounds(250, this.height - 56, 70, 20).build());
        atlantisButton = addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.lostworlds.atlantis"), (button) -> {
            selected = ATLANTIS;
        }).bounds(330, this.height - 56, 70, 20).build());
        selected = switch (lwSettings.type()) {
            case ISLANDS -> ISLANDS;
            case CAVES -> CAVES;
            case SPHERES -> SPHERES;
            default -> NORMAL;
        };

        fogColorButton = addRenderableWidget(CycleButton.builder((FogColor t) -> Component.literal(t.getSerializedName()))
                .withValues(FogColor.values())
                .create(10, 80, 70, 20, Component.literal("Fog")));
        fogColorButton.setValue(lwSettings.fogColor());
        seaLevelSlider = addRenderableWidget(new ForgeSlider(10, 110, 150, 20, Component.literal("Sea "), Component.literal(""), -64, 384, 0, 1, 1, true) {
            @Override
            public String getValueString() {
                if (this.getValue() == -64) {
                    return "Off";
                } else
                    return super.getValueString();
                }
        });
        seaLevelSlider.setValue(lwSettings.seaLevel() == null ? -64 : lwSettings.seaLevel());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            Holder<NoiseGeneratorSettings> gen = noisegetter.getOrThrow(selected.settingsKey());
            this.generator = gen.get();
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
                seaLevelSlider.getValue() == -64 ? null : (int) seaLevelSlider.getValue());
    }

    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        islandsButton.setFGColor(selected == ISLANDS ? 0x0044ff44 : 0x00aaaaaa);
        cavesButton.setFGColor(selected == CAVES ? 0x0044ff44 : 0x00aaaaaa);
        spheresButton.setFGColor(selected == SPHERES ? 0x0044ff44 : 0x00aaaaaa);
        normalButton.setFGColor(selected == NORMAL ? 0x0044ff44 : 0x00aaaaaa);
        atlantisButton.setFGColor(selected == ATLANTIS ? 0x0044ff44 : 0x00aaaaaa);
        seaLevelSlider.active = selected.type.supportsCustomSea();
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
        graphics.drawWordWrap(this.font, Component.translatable(this.selected.description), 20, 40, this.width - 150,16777215);
        graphics.blit(new ResourceLocation(LostWorlds.MODID, "textures/gui/" + selected.iconName), this.width - 120, 40,  100, 100, 0, 0, 128, 128, 128, 128);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

}
