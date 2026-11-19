package com.farukylmz.createkiday.network;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.item.TagFilterData;
import com.farukylmz.createkiday.menu.TagFilterMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SaveTagFilterPayload(int containerId, TagFilterData data, boolean clearSample) implements CustomPacketPayload {
    public static final Type<SaveTagFilterPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "save_tag_filter"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SaveTagFilterPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SaveTagFilterPayload::containerId,
            TagFilterData.STREAM_CODEC, SaveTagFilterPayload::data,
            ByteBufCodecs.BOOL, SaveTagFilterPayload::clearSample,
            SaveTagFilterPayload::new);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SaveTagFilterPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        context.enqueueWork(() -> {
            if (player.containerMenu instanceof TagFilterMenu menu
                    && menu.containerId == payload.containerId
                    && menu.stillValid(player)) {
                if (payload.clearSample) menu.clearContents();
                menu.setRules(payload.data);
            }
        });
    }
}
