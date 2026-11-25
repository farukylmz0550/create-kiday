package io.github.createkiday.block.entity;

import io.github.createkiday.KidayBalance;
import io.github.createkiday.KidayServerConfig;
import io.github.createkiday.advancement.KidayAdvancements;
import io.github.createkiday.registry.ModBlockEntities;
import io.github.createkiday.gameplay.LightningEngineEvents;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;

public final class LightningEngineBlockEntity extends GeneratingKineticBlockEntity {
    public static final Vec3 RPM_CONTROL_BACK_CENTER = new Vec3(0.5, 0.5, 15.5 / 16.0);
    private long chargedTicks;
    private long lastWorldTick = -1;
    private int rpm = 16;
    public ScrollValueBehaviour targetSpeed;

    public LightningEngineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LIGHTNING_ENGINE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel) LightningEngineEvents.registerEngine(serverLevel, worldPosition);
    }

    @Override
    public void remove() {
        if (level instanceof ServerLevel serverLevel) LightningEngineEvents.unregisterEngine(serverLevel, worldPosition);
        super.remove();
    }

    @Override
    public void onChunkUnloaded() {
        if (level instanceof ServerLevel serverLevel) LightningEngineEvents.unregisterEngine(serverLevel, worldPosition);
        super.onChunkUnloaded();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        targetSpeed = new ScrollValueBehaviour(
                Component.translatable("create_kiday.gui.engine_rpm"), this,
                new CenteredSideValueBoxTransform(LightningEngineBlockEntity::isRpmControlSide));
        targetSpeed.between(0, 3)
                .withFormatter(value -> {
                    int safeRpm = KidayBalance.lightningRpm(Mth.clamp(value, 0, 3));
                    return safeRpm + " RPM • " + KidayServerConfig.lightningCapacity(safeRpm) + " SU";
                })
                .withCallback(value -> {
                    rpm = KidayBalance.lightningRpm(Mth.clamp(value, 0, 3));
                    if (level != null && !level.isClientSide()) {
                        updateGeneratedRotation();
                        setChanged();
                        sendData();
                    }
                });
        targetSpeed.value = KidayBalance.lightningRpmIndex(rpm);
        behaviours.add(targetSpeed);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) return;
        long now = level.getGameTime();
        long previousCharge = chargedTicks;
        if (lastWorldTick >= 0 && chargedTicks > 0 && !isOverStressed())
            chargedTicks = KidayBalance.remainingCharge(chargedTicks, lastWorldTick, now);
        lastWorldTick = now;
        // Keep the goggles countdown moving on the client between the regular
        // authoritative server syncs instead of displaying the charge snapshot
        // received when lightning first struck.
        if (level.isClientSide()) return;
        if (chargedTicks > 0 && level.getGameTime() % 20 == 0 && level instanceof ServerLevel serverLevel) {
            for (var player : serverLevel.getPlayers(candidate -> candidate.distanceToSqr(worldPosition.getX() + 0.5,
                    worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 16 * 16))
                KidayAdvancements.award(player, "engine_running", "running");
        }
        if (chargedTicks <= 0) {
            if (previousCharge > 0) {
                updateGeneratedRotation();
                setChanged();
                sendData();
            }
            return;
        }
        if (chargedTicks % 20 == 0) {
            setChanged();
            sendData();
        }
    }

    public void chargeFromLightning() {
        chargedTicks = Math.min(KidayServerConfig.lightningMaxChargeTicks(),
                chargedTicks + KidayServerConfig.lightningChargeTicks());
        if (level != null) lastWorldTick = level.getGameTime();
        updateGeneratedRotation();
        setChanged();
        sendData();
    }

    public void adjustRpm(int delta) {
        int index = Mth.clamp(KidayBalance.lightningRpmIndex(selectedRpm()) + delta, 0, 3);
        rpm = KidayBalance.lightningRpm(index);
        if (targetSpeed != null) targetSpeed.setValue(index);
        updateGeneratedRotation();
        setChanged();
        sendData();
    }

    @Override
    public float getGeneratedSpeed() {
        return chargedTicks > 0 ? selectedRpm() : 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float speed = Math.max(1, Math.abs(getGeneratedSpeed()));
        this.lastCapacityProvided = KidayServerConfig.lightningCapacity(selectedRpm()) / speed;
        return this.lastCapacityProvided;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, sneaking);
        tooltip.add(Component.translatable("tooltip.create_kiday.engine_time", chargedTicks / 20).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.create_kiday.engine_output", selectedRpm(), KidayServerConfig.lightningCapacity(selectedRpm())).withStyle(ChatFormatting.GRAY));
        return added || true;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putLong("ChargedTicks", chargedTicks);
        tag.putLong("LastWorldTick", lastWorldTick);
        tag.putInt("Rpm", rpm);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        chargedTicks = Math.max(0, tag.getLong("ChargedTicks"));
        lastWorldTick = tag.contains("LastWorldTick") ? tag.getLong("LastWorldTick") : -1;
        int savedRpm = tag.getInt("Rpm");
        if (tag.contains("Rpm")) {
            int[] tiers = {16, 32, 64, 128};
            rpm = tiers[0];
            for (int tier : tiers)
                if (Math.abs(tier - savedRpm) < Math.abs(rpm - savedRpm)) rpm = tier;
        } else rpm = 16;
        if (targetSpeed != null) targetSpeed.setValue(KidayBalance.lightningRpmIndex(rpm));
    }

    public int selectedRpm() {
        return targetSpeed == null ? rpm : KidayBalance.lightningRpm(Mth.clamp(targetSpeed.getValue(), 0, 3));
    }

    public long remainingChargeTicks() {
        return chargedTicks;
    }

    public static boolean isRpmControlSide(BlockState state, Direction side) {
        return side == state.getValue(io.github.createkiday.block.LightningEngineBlock.HORIZONTAL_FACING).getOpposite();
    }

}
