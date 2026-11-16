package com.farukylmz.createkiday.network;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.bracelet.BraceletFlightService;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record LaunchDiskPayload() implements CustomPacketPayload {
    public static final LaunchDiskPayload INSTANCE = new LaunchDiskPayload();
    public static final Type<LaunchDiskPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "launch_disk"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LaunchDiskPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(LaunchDiskPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) BraceletFlightService.launch(player);
    }
}
