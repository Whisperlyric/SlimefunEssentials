package me.justahuman.slimefun_essentials.client.payloads;

import me.justahuman.slimefun_essentials.utils.Payloads;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record SlimefunAddonPayload(String addon) implements CustomPayload {
    public static final PacketCodec<PacketByteBuf, SlimefunAddonPayload> CODEC =
            Payloads.newCodec(input -> new SlimefunAddonPayload(input.readUTF()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return Payloads.ADDON_CHANNEL;
    }
}
