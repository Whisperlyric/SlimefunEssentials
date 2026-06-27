package me.justahuman.slimefun_essentials.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.DynamicOps;
import me.justahuman.slimefun_essentials.SlimefunEssentials;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class JsonUtils {
    private static final Gson GSON = new Gson().newBuilder().setPrettyPrinting().create();

    public static JsonObject get(JsonObject parent, String key, JsonObject def) {
        return parent.get(key) instanceof JsonObject json ? json : def;
    }
    
    public static JsonArray get(JsonObject parent, String key, JsonArray def, boolean set) {
        final JsonArray result = get(parent, key, def);
        if (set) {
            parent.add(key, result);
        }
        return result;
    }
    
    public static JsonArray get(JsonObject parent, String key, JsonArray def) {
        final JsonElement value = parent.get(key);
        if (value instanceof JsonArray array) {
            return array;
        } else if (value == null) {
            return def;
        }

        final JsonArray array = new JsonArray();
        array.add(value);
        return array;
    }
    
    public static String get(JsonObject parent, String key, String def) {
        return parent.get(key) instanceof JsonPrimitive primitive && primitive.isString() ? primitive.getAsString() : def;
    }
    
    public static Boolean get(JsonObject parent, String key, Boolean def, boolean set) {
        final Boolean result = get(parent, key, def);
        if (set) {
            parent.addProperty(key, result);
        }
        return result;
    }
    
    public static Boolean get(JsonObject parent, String key, Boolean def) {
        return parent.get(key) instanceof JsonPrimitive primitive && primitive.isBoolean() ? primitive.getAsBoolean() : def;
    }
    
    public static Long get(JsonObject parent, String key, Long def) {
        return parent.get(key) instanceof JsonPrimitive primitive && primitive.isNumber() ? primitive.getAsLong() : def;
    }
    
    public static Integer get(JsonObject parent, String key, Integer def) {
        return parent.get(key) instanceof JsonPrimitive primitive && primitive.isNumber() ? primitive.getAsInt() : def;
    }

    public static List<ClientTooltipComponent> getTooltip(JsonObject json) {
        final JsonArray tooltipArray = get(json, "tooltip", new JsonArray());
        if (tooltipArray.isEmpty()) {
            return List.of();
        }

        final List<ClientTooltipComponent> tooltip = new ArrayList<>();
        tooltipArray.forEach(element -> tooltip.add(new ClientTextTooltip(Component.literal(element.getAsString()).getVisualOrderText())));
        return tooltip;
    }

    public static JsonObject toJson(String string) {
        return GSON.fromJson(string, JsonObject.class);
    }

    public static String serializeItem(ItemStack itemStack) {
        final JsonObject json = new JsonObject();
        DataComponentMap components = itemStack.getComponents();
        json.addProperty("item", BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString());
        json.addProperty("amount", itemStack.getCount());
        
        if (components != DataComponentMap.EMPTY) {
            String encoded = DataComponentMap.CODEC.encodeStart(withRegistryAccess(NbtOps.INSTANCE), components).getOrThrow().toString();
            json.addProperty("components", encoded);
        }
        return json.toString();
    }

    public static ItemStack deserializeItem(String string) {
        return deserializeItem(toJson(string));
    }
    
    public static ItemStack deserializeItem(JsonObject json) {
        if (json == null || json.isEmpty() || !json.has("id")) {
            return ItemStack.EMPTY;
        }

        final ItemStack itemStack = new ItemStack(BuiltInRegistries.ITEM.get(Identifier.tryParse(json.get("id").getAsString())).orElseThrow());
        itemStack.setCount(json.has("amount") ? json.get("amount").getAsInt() : 1);

        try {
            if (json.get("components") instanceof JsonPrimitive primitive && primitive.isString() && itemStack.getComponents() instanceof PatchedDataComponentMap patched) {
                DataComponentMap components = DataComponentMap.CODEC.decode(withRegistryAccess(NbtOps.INSTANCE), TagParser.parseCompoundFully(primitive.getAsString())).getOrThrow().getFirst();
                patched.setAll(components);
            }
        } catch (Exception e) {
            SlimefunEssentials.LOGGER.error("Failed to deserialize item components", e);
        }

        return itemStack;
    }

    private static <T> DynamicOps<T> withRegistryAccess(DynamicOps<T> ops) {
        Minecraft instance = Minecraft.getInstance();
        if (instance == null || instance.level == null) {
            return ops;
        }
        return instance.level.registryAccess().createSerializationContext(ops);
    }
}
