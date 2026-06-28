package me.justahuman.slimefun_essentials.client.payload;

import me.justahuman.slimefun_essentials.client.PayloadCache;
import me.justahuman.slimefun_essentials.client.display.BasicDisplay;
import me.justahuman.slimefun_essentials.config.ModConfig;
import me.justahuman.slimefun_essentials.utils.Payloads;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public class RecipeDisplayPayload implements CustomPacketPayload {
    private static final RecipeDisplayPayload EMPTY = new RecipeDisplayPayload();
    public static final CustomPacketPayload.Type<@NotNull RecipeDisplayPayload> TYPE = Payloads.RECIPE_DISPLAY_CHANNEL;
    public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull RecipeDisplayPayload> CODEC =
            Payloads.newSplitCodec(input -> {
                if (!ModConfig.receiveServerPayloads()) {
                    Payloads.LAST_DECODED_BYTES.remove();
                    return EMPTY;
                }
                String id = BasicDisplay.deserialize(input);
                PayloadCache.write(PayloadCache.Type.RECIPE_DISPLAYS, id, Payloads.LAST_DECODED_BYTES.get());
                Payloads.LAST_DECODED_BYTES.remove();
                Payloads.checkMetExpected();
                return EMPTY;
            }, EMPTY);

    @Override
    public @NotNull Type<@NotNull RecipeDisplayPayload> type() {
        return TYPE;
    }
}
