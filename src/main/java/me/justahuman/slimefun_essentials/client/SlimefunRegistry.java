package me.justahuman.slimefun_essentials.client;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.justahuman.slimefun_essentials.SlimefunEssentials;
import me.justahuman.slimefun_essentials.api.DisplayComponentType;
import me.justahuman.slimefun_essentials.api.RecipeDisplay;
import me.justahuman.slimefun_essentials.client.display.BasicDisplay;
import me.justahuman.slimefun_essentials.utils.JsonUtils;
import me.justahuman.slimefun_essentials.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SlimefunRegistry {
    private static final Gson GSON = new Gson().newBuilder().setPrettyPrinting().create();
    private static final Map<String, SlimefunItemStack> SLIMEFUN_ITEMS = new LinkedHashMap<>();
    private static final Set<String> VANILLA_ITEMS = new HashSet<>();
    @Setter @Getter private static int sfTicksPerSecond = 2;

    public static void reset() {
        SLIMEFUN_ITEMS.clear();
        VANILLA_ITEMS.clear();
        sfTicksPerSecond = 2;
    }

    public static int toSfTicks(int ticks) {
        return ticks / (20 / sfTicksPerSecond);
    }

    public static int toTicks(int sfTicks) {
        return sfTicks * (20 / sfTicksPerSecond);
    }

    public static JsonObject jsonObjectFromResource(Resource resource) {
        try {
            final InputStream inputStream = resource.open();
            return GSON.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonObject.class);
        } catch(IOException e) {
            SlimefunEssentials.LOGGER.error("Failed to load resource", e);
            return new JsonObject();
        }
    }

    public static void addItems(String addon, Map<String, ItemStack> items) {
        for (Map.Entry<String, ItemStack> entry : items.entrySet()) {
            SLIMEFUN_ITEMS.put(entry.getKey(), new SlimefunItemStack(addon, entry.getKey(), entry.getValue()));
            VANILLA_ITEMS.add(entry.getValue().getItem().toString());
        }
    }

    /**
     * 从资源包加载所有 Slimefun 数据
     */
    public static void loadResources(ResourceManager manager) {
        loadItems(manager);
        loadLabels(manager);
        loadRecipes(manager);
        loadItemGroups(manager);
        loadComponentTypes(manager);
        loadRecipeDisplays(manager);
        loadItemModels();
    }

    /**
     * 从 PayloadCache 加载所有类型数据到 Registry（覆盖语义）。
     * 用于不接收服务端 Payload 或资源包模式未提供数据时的回退。
     */
    public static void loadFromCache() {
        loadItemsFromCache();
        loadRecipeCategoriesFromCache();
        loadComponentTypesFromCache();
        loadRecipeDisplaysFromCache();
    }

    /**
     * 仅当 Registry 中没有任何 Slimefun 数据时，从缓存加载。
     * 用于资源包模式的补充：资源包未提供数据且缓存存在时立即用缓存显示。
     *
     * @return 是否实际从缓存加载了数据
     */
    public static boolean loadFromCacheIfMissing() {
        if (!SLIMEFUN_ITEMS.isEmpty()) {
            return false;
        }
        boolean anyCache = PayloadCache.hasAny(PayloadCache.Type.ITEMS)
                || PayloadCache.hasAny(PayloadCache.Type.RECIPE_CATEGORIES)
                || PayloadCache.hasAny(PayloadCache.Type.COMPONENT_TYPES)
                || PayloadCache.hasAny(PayloadCache.Type.RECIPE_DISPLAYS);
        if (!anyCache) {
            return false;
        }
        loadFromCache();
        return true;
    }

    private static void loadItemsFromCache() {
        for (PayloadCache.CacheEntry entry : PayloadCache.readAll(PayloadCache.Type.ITEMS)) {
            try {
                ByteArrayDataInput input = ByteStreams.newDataInput(entry.data());
                String addon = input.readUTF();
                int size = input.readInt();
                Map<String, ItemStack> items = new LinkedHashMap<>(size);
                for (int i = 0; i < size; i++) {
                    String id = input.readUTF();
                    try {
                        items.put(id, me.justahuman.slimefun_essentials.utils.DataUtils.get(input));
                    } catch (Exception e) {
                        SlimefunEssentials.LOGGER.error("Failed to deserialize cached slimefun item: {}", id, e);
                    }
                }
                addItems(addon, items);
            } catch (Exception e) {
                SlimefunEssentials.LOGGER.error("Failed to load items cache: {}", entry.key(), e);
            }
        }
        sortItems();
    }

    private static void loadRecipeCategoriesFromCache() {
        for (PayloadCache.CacheEntry entry : PayloadCache.readAll(PayloadCache.Type.RECIPE_CATEGORIES)) {
            try {
                ByteArrayDataInput input = ByteStreams.newDataInput(entry.data());
                String addon = input.readUTF();
                input.readByte(); // subType，加载时无需区分
                int size = input.readInt();
                for (int i = 0; i < size; i++) {
                    RecipeCategory.deserialize(addon, input);
                }
            } catch (Exception e) {
                SlimefunEssentials.LOGGER.error("Failed to load recipe categories cache: {}", entry.key(), e);
            }
        }
    }

    private static void loadComponentTypesFromCache() {
        DisplayComponentType.clear();
        for (PayloadCache.CacheEntry entry : PayloadCache.readAll(PayloadCache.Type.COMPONENT_TYPES)) {
            try {
                ByteArrayDataInput input = ByteStreams.newDataInput(entry.data());
                DisplayComponentType.deserialize(input);
            } catch (Exception e) {
                SlimefunEssentials.LOGGER.error("Failed to load component types cache: {}", entry.key(), e);
            }
        }
    }

    private static void loadRecipeDisplaysFromCache() {
        RecipeDisplay.clear();
        for (PayloadCache.CacheEntry entry : PayloadCache.readAll(PayloadCache.Type.RECIPE_DISPLAYS)) {
            try {
                ByteArrayDataInput input = ByteStreams.newDataInput(entry.data());
                BasicDisplay.deserialize(input);
            } catch (Exception e) {
                SlimefunEssentials.LOGGER.error("Failed to load recipe displays cache: {}", entry.key(), e);
            }
        }
    }

    /**
     * 从 "slimefun/items" 目录加载 Slimefun 物品
     */
    public static void loadItems(ResourceManager manager) {
        for (Map.Entry<Identifier, Resource> entry : manager.listResources("slimefun/items", Utils::filterResources).entrySet()) {
            loadItemsFromResource(entry.getKey(), entry.getValue());
        }
        sortItems();
    }

    /**
     * 从单个资源加载 Slimefun 物品
     */
    public static void loadItemsFromResource(Identifier identifier, Resource resource) {
        final JsonObject items = jsonObjectFromResource(resource);
        if (items == null) {
            return;
        }
        final String addon = identifier.getNamespace();
        for (String id : items.keySet()) {
            final JsonElement itemElement = items.get(id);
            if (!(itemElement instanceof JsonObject itemObject) || !itemObject.has("id")) {
                continue;
            }
            final ItemStack itemStack = JsonUtils.deserializeItem(itemObject);
            if (itemStack.isEmpty()) {
                continue;
            }
            SLIMEFUN_ITEMS.put(id, new SlimefunItemStack(addon, id, itemStack));
            VANILLA_ITEMS.add(itemStack.getItem().toString());
        }
    }

    /**
     * 按 ID 排序 Slimefun 物品
     */
    private static void sortItems() {
        final Map<String, SlimefunItemStack> sorted = new LinkedHashMap<>();
        final List<String> ids = new ArrayList<>(SLIMEFUN_ITEMS.keySet());
        ids.sort(Comparator.naturalOrder());
        for (String id : ids) {
            sorted.put(id, SLIMEFUN_ITEMS.get(id));
        }
        SLIMEFUN_ITEMS.clear();
        SLIMEFUN_ITEMS.putAll(sorted);
    }

    public static void loadItemModels() {
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        loadCustomModels(manager, "item");
        loadCustomModels(manager, "block");
    }

    public static void loadCustomModels(ResourceManager manager, String directory) {
        for (Map.Entry<Identifier, Resource> entry : manager.listResources("models/" + directory, Utils::filterVanillaItems).entrySet()) {
            final Resource resource = entry.getValue();
            if ("minecraft".equals(resource.source().packId()) || "vanilla".equals(resource.source().packId())) {
                continue;
            }

            final JsonObject model = jsonObjectFromResource(resource);
            if (model != null && model.get("overrides") instanceof JsonArray overrides) {
                for (JsonElement element : overrides) {
                    if (element instanceof JsonObject override) {
                        loadCustomModel(override);
                    }
                }
            }
        }
    }

    public static void loadCustomModel(JsonObject override) {
        if (!(override.get("predicate") instanceof JsonObject predicate)
                || !(predicate.get("custom_model_data") instanceof JsonPrimitive modelData)
                || !modelData.isNumber()
                || !(override.get("model") instanceof JsonPrimitive model)
                || !model.isString()) {
            return;
        }

        final int customModelData = modelData.getAsInt();
        final String modelId = model.getAsString();
        final int idStart = modelId.lastIndexOf("/");
        final int idEnd = modelId.lastIndexOf(".");
        final String id = modelId.substring(idStart == -1 ? 0 : idStart + 1,
                idEnd == -1 ? modelId.length() : idEnd).toUpperCase(Locale.ROOT);

        if (SLIMEFUN_ITEMS.containsKey(id)) {
            SLIMEFUN_ITEMS.get(id.toUpperCase()).setCustomModelData(customModelData);
        }
    }

    public static boolean hasItem(String id) {
        return SLIMEFUN_ITEMS.containsKey(id);
    }

    public static ItemStack getItemStack(String id) {
        SlimefunItemStack item = SLIMEFUN_ITEMS.get(id);
        return item != null ? item.itemStack() : ItemStack.EMPTY;
    }

    public static SlimefunItemStack getSlimefunItem(String id) {
        return SLIMEFUN_ITEMS.get(id);
    }

    @NonNull
    public static Map<String, SlimefunItemStack> getSlimefunItems() {
        return Collections.unmodifiableMap(SLIMEFUN_ITEMS);
    }

    @NonNull
    public static Set<String> getVanillaItems() {
        return Collections.unmodifiableSet(VANILLA_ITEMS);
    }

    /**
     * 从 "slimefun/labels" 目录加载配方标签
     */
    public static void loadLabels(ResourceManager manager) {
        SlimefunLabel.clear();
        for (Map.Entry<Identifier, Resource> entry : manager.listResources("slimefun/labels", Utils::filterResources).entrySet()) {
            loadLabelsFromResource(entry.getValue());
        }
    }

    /**
     * 从单个资源加载配方标签
     */
    public static void loadLabelsFromResource(Resource resource) {
        final JsonObject labels = jsonObjectFromResource(resource);
        if (labels == null) {
            return;
        }
        for (String id : labels.keySet()) {
            if ("data_version".equals(id)) {
                continue;
            }
            final JsonElement labelElement = labels.get(id);
            if (labelElement instanceof JsonObject labelObject) {
                SlimefunLabel.deserialize(id, labelObject);
            }
        }
    }

    /**
     * 从 "slimefun/recipes" 目录加载配方类别
     */
    public static void loadRecipes(ResourceManager manager) {
        for (Map.Entry<Identifier, Resource> entry : manager.listResources("slimefun/recipes", Utils::filterResources).entrySet()) {
            loadRecipesFromResource(entry.getKey(), entry.getValue());
        }
        RecipeCategory.finalizeCategories();
    }

    /**
     * 从单个资源加载配方类别
     */
    public static void loadRecipesFromResource(Identifier identifier, Resource resource) {
        final JsonObject recipes = jsonObjectFromResource(resource);
        if (recipes == null) {
            return;
        }
        final String addon = identifier.getNamespace();
        for (String id : recipes.keySet()) {
            final JsonElement categoryElement = recipes.get(id);
            if (categoryElement instanceof JsonObject categoryObject) {
                RecipeCategory.deserialize(addon, id, categoryObject);
            }
        }
    }

    /**
     * 从 "slimefun/item_groups" 目录加载 JEI 物品组
     */
    public static void loadItemGroups(ResourceManager manager) {
        SlimefunItemGroup.clear();
        for (Map.Entry<Identifier, Resource> entry : manager.listResources("slimefun/item_groups", Utils::filterResources).entrySet()) {
            loadItemGroupsFromResource(entry.getKey(), entry.getValue());
        }
        SlimefunItemGroup.addParents();
    }

    /**
     * 从单个资源加载物品组
     */
    public static void loadItemGroupsFromResource(Identifier identifier, Resource resource) {
        final JsonObject itemGroups = jsonObjectFromResource(resource);
        if (itemGroups == null) {
            return;
        }
        final String addon = identifier.getNamespace();
        for (String id : itemGroups.keySet()) {
            if ("data_version".equals(id)) {
                continue;
            }
            final JsonElement groupElement = itemGroups.get(id);
            if (groupElement instanceof JsonObject groupObject) {
                SlimefunItemGroup.deserialize(addon, id, groupObject);
            }
        }
    }

    /**
     * 从 "slimefun/component_types" 目录加载组件类型
     * （能量条、槽位、箭头、填充箭头等渲染组件）
     */
    public static void loadComponentTypes(ResourceManager manager) {
        DisplayComponentType.clear();
        for (Map.Entry<Identifier, Resource> entry : manager.listResources("slimefun/component_types", Utils::filterResources).entrySet()) {
            loadComponentTypesFromResource(entry.getValue());
        }
    }

    public static void loadComponentTypesFromResource(Resource resource) {
        final JsonObject componentType = jsonObjectFromResource(resource);
        if (componentType == null || !componentType.has("id")) {
            return;
        }
        final String id = componentType.get("id").getAsString();
        DisplayComponentType.deserialize(id, componentType);
    }

    /**
     * 从 "slimefun/recipe_displays" 目录加载配方显示布局
     * （grid_3x3、smeltery、reactor 等自定义布局模板）
     */
    public static void loadRecipeDisplays(ResourceManager manager) {
        RecipeDisplay.clear();
        for (Map.Entry<Identifier, Resource> entry : manager.listResources("slimefun/recipe_displays", Utils::filterResources).entrySet()) {
            loadRecipeDisplaysFromResource(entry.getKey(), entry.getValue());
        }
    }

    public static void loadRecipeDisplaysFromResource(Identifier identifier, Resource resource) {
        final JsonObject recipeDisplay = jsonObjectFromResource(resource);
        if (recipeDisplay == null) {
            return;
        }
        final String path = identifier.getPath();
        final String fileName = path.substring(path.lastIndexOf('/') + 1);
        final String id = fileName.endsWith(".json") ? fileName.substring(0, fileName.length() - 5) : fileName;
        BasicDisplay.deserialize(id, recipeDisplay);
    }
}
