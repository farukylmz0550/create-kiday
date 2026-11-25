package io.github.createkiday.network;

import io.github.createkiday.CreateKidayMod;
import io.github.createkiday.item.LightningStaffItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record LightningStaffPayload() implements CustomPacketPayload {
    public static final LightningStaffPayload INSTANCE = new LightningStaffPayload();
    public static final Type<LightningStaffPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "lightning_staff"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LightningStaffPayload> STREAM_CODEC =
            CustomPacketPayload.codec((payload, buffer) -> { }, buffer -> INSTANCE);

    public static void handle(LightningStaffPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !player.getMainHandItem().is(io.github.createkiday.registry.ModItems.LIGHTNING_STAFF.get())) return;
            var hit = LightningStaffItem.raycastLiving(player, LightningStaffItem.LIGHTNING_RANGE);
            Vec3 target = hit == null
                    ? player.pick((float) LightningStaffItem.LIGHTNING_RANGE, 1.0F, false).getLocation()
                    : hit.getLocation();
            LightningStaffItem.castLightning(player, target);
        });
    }

    @Override
    public Type<LightningStaffPayload> type() {
        return TYPE;
    }
}
