package me.justahuman.slimefun_essentials.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.NonNull;
import me.justahuman.slimefun_essentials.utils.JsonUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JEI 物品组类，用于组织 JEI 界面中的 Slimefun 物品分类
 */
public record SlimefunItemGroup(Identifier identifier, ItemStack itemStack, List<String> content, List<String> requirements) {
    private static final Map<String, SlimefunItemGroup> ITEM_GROUPS = new LinkedHashMap<>();
    
    public static void deserialize(String addon, String id, JsonObject groupObject) {
        final Identifier identifier = Identifier.tryBuild(addon, id);
        final ItemStack itemStack = JsonUtils.deserializeItem(JsonUtils.get(groupObject, "item", new JsonObject()));
        final List<String> content = new ArrayList<>();
        final List<String> requirements = new ArrayList<>();
        
        // items 列表
        for (JsonElement element : JsonUtils.get(groupObject, "items", new JsonArray())) {
            if (element instanceof JsonPrimitive primitive && primitive.isString()) {
                content.add(primitive.getAsString());
            }
        }
        
        // nested 子物品组
        for (JsonElement element : JsonUtils.get(groupObject, "nested", new JsonArray())) {
            if (element instanceof JsonPrimitive primitive && primitive.isString()) {
                content.add(primitive.getAsString());
            }
        }
        
        // locked 需求列表
        for (JsonElement element : JsonUtils.get(groupObject, "locked", new JsonArray())) {
            if (element instanceof JsonPrimitive primitive && primitive.isString()) {
                requirements.add(primitive.getAsString());
            }
        }

        if (identifier != null) {
            ITEM_GROUPS.put(identifier.toString(), new SlimefunItemGroup(identifier, itemStack, content, requirements));
        }
    }
    
    @NonNull
    public static Map<String, SlimefunItemGroup> getItemGroups() {
        return ITEM_GROUPS;
    }
    
    public static void clear() {
        ITEM_GROUPS.clear();
    }
    
    /**
     * 添加父物品组关系（子物品组需要解锁父物品组才能访问）
     */
    public static void addParents() {
        for (SlimefunItemGroup itemGroup : ITEM_GROUPS.values()) {
            for (String content : itemGroup.content()) {
                final SlimefunItemGroup child = ITEM_GROUPS.get(content);
                if (child != null) {
                    child.requirements().add("slimefun_essentials:" + itemGroup.identifier().toString().replace(":", "_"));
                }
            }
        }
    }
}