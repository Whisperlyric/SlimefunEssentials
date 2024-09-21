package me.justahuman.slimefun_essentials.client.payloads;

import me.justahuman.slimefun_essentials.utils.Payloads;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record DisabledItemPayload(String id) implements CustomPayload {
    public static final PacketCodec<PacketByteBuf, DisabledItemPayload> CODEC =
            Payloads.newCodec(input -> new DisabledItemPayload(input.readUTF()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return Payloads.ITEM_CHANNEL;
    }
}
