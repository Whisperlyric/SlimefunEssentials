package me.justahuman.slimefun_essentials.client.payload;

import me.justahuman.slimefun_essentials.client.PayloadCache;
import me.justahuman.slimefun_essentials.client.RecipeCategory;
import me.justahuman.slimefun_essentials.config.ModConfig;
import me.justahuman.slimefun_essentials.utils.Payloads;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class RecipeCategoriesPayload implements CustomPacketPayload {
    private static final RecipeCategoriesPayload EMPTY = new RecipeCategoriesPayload();
    public static final CustomPacketPayload.Type<RecipeCategoriesPayload> TYPE = Payloads.RECIPE_CATEGORIES_CHANNEL;
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeCategoriesPayload> CODEC =
            Payloads.newSplitCodec(input -> {
                if (!ModConfig.receiveServerPayloads()) {
                    Payloads.LAST_DECODED_BYTES.remove();
                    return EMPTY;
                }
                String addon = input.readUTF();
                int subType = input.readByte() & 0xFF;
                int size = input.readInt();
                for (int i = 0; i < size; i++) {
                    RecipeCategory.deserialize(addon, input);
                }
                String key = addon + "_" + (subType == 0 ? "items" : "types");
                PayloadCache.write(PayloadCache.Type.RECIPE_CATEGORIES, key, Payloads.LAST_DECODED_BYTES.get());
                Payloads.LAST_DECODED_BYTES.remove();
                Payloads.checkMetExpected();
                return EMPTY;
            }, EMPTY);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
