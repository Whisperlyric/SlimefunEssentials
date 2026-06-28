package me.justahuman.slimefun_essentials.client.payload;

import me.justahuman.slimefun_essentials.SlimefunEssentials;
import me.justahuman.slimefun_essentials.client.PayloadCache;
import me.justahuman.slimefun_essentials.client.SlimefunRegistry;
import me.justahuman.slimefun_essentials.config.ModConfig;
import me.justahuman.slimefun_essentials.utils.DataUtils;
import me.justahuman.slimefun_essentials.utils.Payloads;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class ItemsPayload implements CustomPacketPayload {
    public static final ItemsPayload EMPTY = new ItemsPayload();
    public static final CustomPacketPayload.Type<@NotNull ItemsPayload> TYPE = Payloads.ITEM_CHANNEL;
    public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull ItemsPayload> CODEC =
            Payloads.newSplitCodec(input -> {
                if (!ModConfig.receiveServerPayloads()) {
                    Payloads.LAST_DECODED_BYTES.remove();
                    return EMPTY;
                }
                String addon = input.readUTF();
                int size = input.readInt();
                Map<String, ItemStack> items = new LinkedHashMap<>(size);
                for (int i = 0; i < size; i++) {
                    String id = input.readUTF();
                    try {
                        items.put(id, DataUtils.get(input));
                    } catch (Exception e) {
                        SlimefunEssentials.LOGGER.error("Failed to deserialize slimefun item: {}", id, e);
                    }
                }
                SlimefunRegistry.addItems(addon, items);
                PayloadCache.write(PayloadCache.Type.ITEMS, addon, Payloads.LAST_DECODED_BYTES.get());
                Payloads.LAST_DECODED_BYTES.remove();
                return EMPTY;
            }, EMPTY);

    @Override
    public @NotNull Type<@NotNull ItemsPayload> type() {
        return TYPE;
    }
}
