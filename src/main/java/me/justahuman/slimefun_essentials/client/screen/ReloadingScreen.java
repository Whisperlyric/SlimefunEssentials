package me.justahuman.slimefun_essentials.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

public class ReloadingScreen extends Screen {
    private final BooleanSupplier isReloading;
    private final Runnable finished;

    public ReloadingScreen(BooleanSupplier isReloading, Runnable finished) {
        super(Component.translatable("slimefun_essentials.reloading"));
        this.isReloading = isReloading;
        this.finished = finished;
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (!isReloading.getAsBoolean()) {
            finished.run();
            return;
        }

        super.extractRenderState(graphics, mouseX, mouseY, delta);
        String text = switch ((int) (Util.getMillis() / 300L % 4L)) {
            case 1, 3 -> "o O o";
            case 2 -> "o o O";
            default -> "O o o";
        };
        int width = Math.max(this.font.width(text), this.font.width(title));
        int height = 18;
        int x = this.width / 2 - width / 2;
        int k = x - 12;
        int l = this.height / 2 - height / 2 - 12;
        int m = width + 12 * 2;
        int n = height + 12 * 2;
        int o = this.isFocused() ? -1 : -6250336;
        graphics.fill(k + 1, l, k + m, l + n, -16777216);
        // Draw border manually
        graphics.fill(k, l, k + 1, l + n, o);
        graphics.fill(k + m - 1, l, k + m, l + n, o);
        graphics.fill(k, l, k + m, l + 1, o);
        graphics.fill(k, l + n - 1, k + m, l + n, o);
        graphics.text(this.font, title.getString(), this.width / 2 - this.font.width(title) / 2, l + 12, 0xFFFFFFFF, true);
        graphics.text(this.font, text, this.width / 2 - this.font.width(text) / 2, l + 12 + 9, 0xFF808080, true);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
