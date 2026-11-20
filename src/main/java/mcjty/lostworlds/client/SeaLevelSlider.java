package mcjty.lostworlds.client;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

final class SeaLevelSlider extends AbstractSliderButton {

    private static final int OFF = -64;
    private static final int MAX = 384;

    SeaLevelSlider(int x, int y, int width, int height, @Nullable Integer level) {
        super(x, y, width, height, CommonComponents.EMPTY, level == null ? 0.0 : (double) (level - OFF) / (MAX - OFF));
        updateMessage();
    }

    @Nullable
    Integer seaLevel() {
        int level = (int) Math.round(value * (MAX - OFF)) + OFF;
        return level == OFF ? null : level;
    }

    @Override
    protected void updateMessage() {
        Integer level = seaLevel();
        setMessage(Component.literal("Sea: " + (level == null ? "Off" : level)));
    }

    @Override
    protected void applyValue() {
    }
}
