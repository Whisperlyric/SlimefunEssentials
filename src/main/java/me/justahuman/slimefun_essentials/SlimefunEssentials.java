package me.justahuman.slimefun_essentials;

import me.justahuman.slimefun_essentials.api.DisplayComponentType;
import me.justahuman.slimefun_essentials.api.RecipeDisplay;
import me.justahuman.slimefun_essentials.client.RecipeCategory;
import me.justahuman.slimefun_essentials.client.SlimefunRegistry;
import me.justahuman.slimefun_essentials.client.payload.ComponentTypePayload;
import me.justahuman.slimefun_essentials.client.payload.ItemsPayload;
import me.justahuman.slimefun_essentials.client.payload.LoadingStatePayload;
import me.justahuman.slimefun_essentials.client.payload.RecipeCategoriesPayload;
import me.justahuman.slimefun_essentials.client.payload.RecipeDisplayPayload;
import me.justahuman.slimefun_essentials.compat.cloth_config.ConfigScreen;
import me.justahuman.slimefun_essentials.config.ModConfig;
import me.justahuman.slimefun_essentials.utils.Payloads;
import me.justahuman.slimefun_essentials.utils.CompatUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class SlimefunEssentials implements ClientModInitializer {
    public static final String MOD_ID = "slimefun_essentials";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "slimefun"));

    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.clientboundPlay().register(LoadingStatePayload.TYPE, LoadingStatePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ItemsPayload.TYPE, ItemsPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ComponentTypePayload.TYPE, ComponentTypePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(RecipeCategoriesPayload.TYPE, RecipeCategoriesPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(RecipeDisplayPayload.TYPE, RecipeDisplayPayload.CODEC);
        ModConfig.loadConfig();

        if (ModConfig.resourcePackFeatures()) {
            ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(id("reload_listener"), (ResourceManagerReloadListener) manager ->
                SlimefunRegistry.loadResources(manager)
            );
        }

        ClientPlayNetworking.registerGlobalReceiver(LoadingStatePayload.TYPE, (payload, context) -> Payloads.expect(payload));
        ClientPlayNetworking.registerGlobalReceiver(ItemsPayload.TYPE, (payload, context) -> {});
        ClientPlayNetworking.registerGlobalReceiver(ComponentTypePayload.TYPE, (payload, context) -> {});
        ClientPlayNetworking.registerGlobalReceiver(RecipeCategoriesPayload.TYPE, (payload, context) -> {});
        ClientPlayNetworking.registerGlobalReceiver(RecipeDisplayPayload.TYPE, (payload, context) -> {});

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            RecipeCategory.clear();
            DisplayComponentType.clear();
            RecipeDisplay.clear();
            SlimefunRegistry.reset();
            Payloads.reset();
        });

        if (CompatUtils.isClothConfigLoaded()) {
            final KeyMapping configKeyMapping = KeyMappingHelper.registerKeyMapping(new KeyMapping("slimefun_essentials.key_bind.open_config", GLFW.GLFW_KEY_F6, CATEGORY));
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                if (configKeyMapping.consumeClick()) {
                    client.setScreen(ConfigScreen.buildScreen(client.screen));
                }
            });
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path.toLowerCase(Locale.ROOT));
    }
}