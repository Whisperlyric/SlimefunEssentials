package me.justahuman.slimefun_essentials.client.payloads;

import me.justahuman.slimefun_essentials.utils.Payloads;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record SlimefunBlockPayload(BlockPos pos, String id) implements CustomPayload {
    public static final PacketCodec<PacketByteBuf, SlimefunBlockPayload> CODEC =
            Payloads.newCodec(input -> new SlimefunBlockPayload(new BlockPos(input.readInt(), input.readInt(), input.readInt()), input.readUTF()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return Payloads.BLOCK_CHANNEL;
    }
}