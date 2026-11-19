package com.farukylmz.createkiday.network;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.item.TagFilterItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SaveTagFilterPayload(InteractionHand hand, String accept, String reject) implements CustomPacketPayload {
    public static final Type<SaveTagFilterPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "save_tag_filter"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SaveTagFilterPayload> STREAM_CODEC = new StreamCodec<>() {
        public SaveTagFilterPayload decode(RegistryFriendlyByteBuf buf) {
            return new SaveTagFilterPayload(buf.readEnum(InteractionHand.class), buf.readUtf(2048), buf.readUtf(2048));
        }
        public void encode(RegistryFriendlyByteBuf buf, SaveTagFilterPayload payload) {
            buf.writeEnum(payload.hand); buf.writeUtf(payload.accept, 2048); buf.writeUtf(payload.reject, 2048);
        }
    };

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SaveTagFilterPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        context.enqueueWork(() -> {
            ItemStack stack = player.getItemInHand(payload.hand);
            if (!(stack.getItem() instanceof TagFilterItem)) return;
            var tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            tag.putString(TagFilterItem.ACCEPT_TAGS, payload.accept);
            tag.putString(TagFilterItem.REJECT_TAGS, payload.reject);
            stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.of(tag));
        });
    }
}
