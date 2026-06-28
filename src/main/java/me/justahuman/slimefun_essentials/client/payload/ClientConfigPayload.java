package me.justahuman.slimefun_essentials.client.payload;

import me.justahuman.slimefun_essentials.utils.Payloads;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * 客户端 → 服务端：上报客户端配置，让服务端决定是否发送各 registry channel。
 * <p>
 * 当前字段：
 * <ul>
 *   <li>{@code receiveServerPayloads}：客户端是否愿意接收服务端 Payload</li>
 * </ul>
 */
public record ClientConfigPayload(boolean receiveServerPayloads) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientConfigPayload> TYPE = Payloads.CLIENT_CONFIG_CHANNEL;
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientConfigPayload> CODEC = StreamCodec.of(
            (buf, value) -> buf.writeBoolean(value.receiveServerPayloads),
            buf -> new ClientConfigPayload(buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
