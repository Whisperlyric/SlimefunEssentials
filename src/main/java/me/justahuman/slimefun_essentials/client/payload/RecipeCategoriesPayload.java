package me.justahuman.slimefun_essentials.client.payload;

import me.justahuman.slimefun_essentials.client.RecipeCategory;
import me.justahuman.slimefun_essentials.utils.Payloads;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class RecipeCategoriesPayload implements CustomPacketPayload {
    private static final RecipeCategoriesPayload EMPTY = new RecipeCategoriesPayload();
    public static final CustomPacketPayload.Type<RecipeCategoriesPayload> TYPE = Payloads.RECIPE_CATEGORIES_CHANNEL;
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeCategoriesPayload> CODEC =
            Payloads.newSplitCodec(input -> {
                int size = input.readInt();
                for (int i = 0; i < size; i++) {
                    RecipeCategory.deserialize(input);
                }
                Payloads.checkMetExpected();
                return EMPTY;
            }, EMPTY);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
