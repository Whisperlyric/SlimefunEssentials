package me.justahuman.slimefun_essentials.client.payload;

import me.justahuman.slimefun_essentials.api.DisplayComponentType;
import me.justahuman.slimefun_essentials.client.PayloadCache;
import me.justahuman.slimefun_essentials.config.ModConfig;
import me.justahuman.slimefun_essentials.utils.Payloads;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class ComponentTypePayload implements CustomPacketPayload {
    private static final ComponentTypePayload EMPTY = new ComponentTypePayload();
    public static final CustomPacketPayload.Type<ComponentTypePayload> TYPE = Payloads.COMPONENT_TYPE_CHANNEL;
    public static final StreamCodec<RegistryFriendlyByteBuf, ComponentTypePayload> CODEC =
            Payloads.newSplitCodec(input -> {
                if (!ModConfig.receiveServerPayloads()) {
                    Payloads.LAST_DECODED_BYTES.remove();
                    return EMPTY;
                }
                String id = DisplayComponentType.deserialize(input);
                PayloadCache.write(PayloadCache.Type.COMPONENT_TYPES, id, Payloads.LAST_DECODED_BYTES.get());
                Payloads.LAST_DECODED_BYTES.remove();
                Payloads.checkMetExpected();
                return EMPTY;
            }, EMPTY);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
