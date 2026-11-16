package com.farukylmz.createkiday.network;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.client.KidayChronicleScreen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenChroniclePayload() implements CustomPacketPayload {
    public static final OpenChroniclePayload INSTANCE = new OpenChroniclePayload();
    public static final Type<OpenChroniclePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "open_chronicle"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenChroniclePayload> STREAM_CODEC =
            CustomPacketPayload.codec((payload, buffer) -> { }, buffer -> INSTANCE);

    public static void handle(OpenChroniclePayload payload, IPayloadContext context) {
        context.enqueueWork(KidayChronicleScreen::open);
    }

    @Override
    public Type<OpenChroniclePayload> type() {
        return TYPE;
    }
}
