package me.justahuman.slimefun_essentials.client;

import net.minecraft.resources.Identifier;

public enum DrawMode {
    LIGHT, DARK, BOOK;

    public Identifier defaultIdentifier() {
        return switch (this) {
            case LIGHT -> Identifier.tryBuild("slimefun_essentials", "textures/gui/widgets.png");
            case DARK -> Identifier.tryBuild("slimefun_essentials", "textures/gui/widgets_dark.png");
            case BOOK -> Identifier.tryBuild("slimefun_essentials", "textures/gui/widgets_book.png");
        };
    }
}