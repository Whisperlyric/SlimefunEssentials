package me.justahuman.slimefun_essentials.client;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import me.justahuman.slimefun_essentials.utils.JsonUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 配方标签类，用于在 JEI/REI 界面显示特殊标签（如白天/夜晚需求、能量图标等）
 */
public class SlimefunLabel {
    private static final Map<String, SlimefunLabel> SLIMEFUN_LABELS = new LinkedHashMap<>();
    
    private final String id;
    private final Map<DrawMode, LabelSettings> settings;
    
    public SlimefunLabel(String id, Map<DrawMode, LabelSettings> settings) {
        this.id = id;
        this.settings = settings;
    }
    
    public static SlimefunLabel of(String id, int u, int v, int width, int height) {
        return new LabelBuilder().id(id).mode(DrawMode.LIGHT, u, v, width, height).build();
    }
    
    public String id() {
        return this.id;
    }
    
    public Identifier identifier() {
        return identifier(DrawMode.LIGHT);
    }
    
    public Identifier identifier(DrawMode drawMode) {
        return this.settings.get(drawMode).identifier;
    }
    
    public int u() {
        return u(DrawMode.LIGHT);
    }
    
    public int u(DrawMode drawMode) {
        return this.settings.get(drawMode).u;
    }
    
    public int v() {
        return v(DrawMode.LIGHT);
    }
    
    public int v(DrawMode drawMode) {
        return this.settings.get(drawMode).v;
    }
    
    public int size() {
        return width(DrawMode.LIGHT);
    }
    
    public int size(boolean y) {
        return y ? height() : width();
    }
    
    public int size(DrawMode drawMode) {
        return width(drawMode);
    }
    
    public int size(DrawMode drawMode, boolean y) {
        return y ? height(drawMode) : width(drawMode);
    }
    
    public int width() {
        return width(DrawMode.LIGHT);
    }
    
    public int width(DrawMode drawMode) {
        return this.settings.get(drawMode).width;
    }
    
    public int height() {
        return height(DrawMode.LIGHT);
    }
    
    public int height(DrawMode drawMode) {
        return this.settings.get(drawMode).height;
    }
    
    public static void deserialize(String id, JsonObject labelObject) {
        final LabelBuilder builder = builder().id(id);
        for (String mode : labelObject.keySet()) {
            if (labelObject.get(mode) instanceof JsonObject settings) {
                builder.mode(DrawMode.valueOf(mode),
                        JsonUtils.get(settings, "u", 0),
                        JsonUtils.get(settings, "v", 0),
                        JsonUtils.get(settings, "width", 13),
                        JsonUtils.get(settings, "height", 13));
            }
        }
        SLIMEFUN_LABELS.put(id, builder.build());
    }
    
    /**
     * Returns an unmodifiable version of {@link SlimefunLabel#SLIMEFUN_LABELS}
     */
    @NonNull
    public static Map<String, SlimefunLabel> getSlimefunLabels() {
        return Collections.unmodifiableMap(SLIMEFUN_LABELS);
    }
    
    public static void clear() {
        SLIMEFUN_LABELS.clear();
    }
    
    public void draw(GuiGraphicsExtractor graphics, int x, int y, DrawMode drawMode) {
        final LabelSettings options = this.settings.get(drawMode);
        graphics.blit(RenderPipelines.GUI_TEXTURED, options.identifier, x, y, options.u, options.v,
                options.width, options.height, options.width, options.height, 256, 256);
    }
    
    public void draw(GuiGraphicsExtractor graphics, int x, int y) {
        draw(graphics, x, y, DrawMode.LIGHT);
    }
    
    public Component text() {
        return Component.translatable("slimefun_essentials.recipes.label." + this.id);
    }
    
    public static LabelBuilder builder() {
        return new LabelBuilder();
    }
    
    public static class LabelBuilder {
        private LabelBuilder() {}
        
        private String id = "";
        private final Map<DrawMode, LabelSettings> settings = new EnumMap<>(DrawMode.class);
        
        public LabelBuilder id(String id) {
            this.id = id;
            return this;
        }
        
        public LabelBuilder mode(DrawMode mode, int u, int v, int width, int height) {
            this.settings.put(mode, new LabelSettings(mode.defaultIdentifier(), u, v, width, height));
            return this;
        }
        
        public SlimefunLabel build() {
            if (id.isBlank()) {
                throw new IllegalArgumentException("Id must be set!");
            }
            if (!settings.containsKey(DrawMode.LIGHT)) {
                throw new IllegalArgumentException("Options must have light mode!");
            }
            final LabelSettings lightMode = settings.get(DrawMode.LIGHT);
            for (DrawMode otherMode : DrawMode.values()) {
                if (!settings.containsKey(otherMode)) {
                    mode(otherMode, lightMode.u, lightMode.v, lightMode.width, lightMode.height);
                }
            }
            return new SlimefunLabel(this.id, this.settings);
        }
    }
    
    @AllArgsConstructor
    public static class LabelSettings {
        private final Identifier identifier;
        private final int u;
        private final int v;
        private final int width;
        private final int height;
    }
}