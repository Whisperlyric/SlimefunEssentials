package me.justahuman.slimefun_essentials.client.payload;

import me.justahuman.slimefun_essentials.utils.Payloads;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record LoadingStatePayload(int typePackets, int itemPackets, int categoryPackets, int displayPackets) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<LoadingStatePayload> TYPE = Payloads.LOADING_STATE_CHANNEL;
    public static final StreamCodec<RegistryFriendlyByteBuf, LoadingStatePayload> CODEC = Payloads.newCodec(input -> new LoadingStatePayload(
            input.readInt(),
            input.readInt(),
            input.readInt(),
            input.readInt()
    ));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
