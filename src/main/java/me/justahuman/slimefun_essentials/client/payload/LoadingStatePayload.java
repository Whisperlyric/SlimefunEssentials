package me.justahuman.slimefun_essentials.client.payload;

import me.justahuman.slimefun_essentials.utils.Payloads;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record LoadingStatePayload(int typePackets, int itemPackets, int categoryPackets, int displayPackets) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<@NotNull LoadingStatePayload> TYPE = Payloads.LOADING_STATE_CHANNEL;
    public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull LoadingStatePayload> CODEC = Payloads.newCodec(input -> new LoadingStatePayload(
            input.readInt(),
            input.readInt(),
            input.readInt(),
            input.readInt()
    ));

    @Override
    public @NotNull Type<@NotNull LoadingStatePayload> type() {
        return TYPE;
    }
}
