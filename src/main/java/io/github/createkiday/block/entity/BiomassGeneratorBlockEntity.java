package io.github.createkiday.block.entity;

import io.github.createkiday.KidayServerConfig;
import io.github.createkiday.advancement.KidayAdvancements;
import io.github.createkiday.registry.ModBlockEntities;
import io.github.createkiday.tag.ModTags;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class BiomassGeneratorBlockEntity extends GeneratingKineticBlockEntity {
    public static final int MIN_RPM = 8;
    public static final int MAX_RPM = 64;
    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override public int getSlotLimit(int slot) { return 32; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return stack.is(ModTags.BIOMASS_FUELS); }
        @Override protected void onContentsChanged(int slot) { setChanged(); sendData(); }
    };
    private final IItemHandler inputOnly = new IItemHandler() {
        @Override public int getSlots() { return inventory.getSlots(); }
        @Override public ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(slot); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return inventory.insertItem(slot, stack, simulate); }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
        @Override public int getSlotLimit(int slot) { return inventory.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return inventory.isItemValid(slot, stack); }
    };
    private int rpm = 32;
    private int fuelTicks;
    private long lastWorldTick = -1;
    public ScrollValueBehaviour targetSpeed;

    public BiomassGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BIOMASS_GENERATOR.get(), pos, state);
    }

    @Override public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        targetSpeed = new ScrollValueBehaviour(Component.translatable("create_kiday.gui.engine_rpm"), this,
                new CenteredSideValueBoxTransform((state, side) -> side == state.getValue(
                        com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING).getOpposite()));
        targetSpeed.between(MIN_RPM, MAX_RPM)
                .withFormatter(value -> Mth.clamp(value, MIN_RPM, MAX_RPM) + " RPM • "
                        + (4096 / Mth.clamp(value, MIN_RPM, MAX_RPM)) + " SU")
                .withCallback(value -> {
                    rpm = Mth.clamp(value, MIN_RPM, MAX_RPM);
                    if (level != null && !level.isClientSide()) {
                        updateGeneratedRotation(); setChanged(); sendData();
                    }
                });
        targetSpeed.value = rpm;
        behaviours.add(targetSpeed);
    }

    @Override public void tick() {
        super.tick();
        if (level == null || level.isClientSide()) return;
        long now = level.getGameTime();
        long elapsed = lastWorldTick < 0 || now <= lastWorldTick ? 0 : now - lastWorldTick;
        lastWorldTick = now;
        if (isOverStressed()) elapsed = 0;
        if (elapsed > 0) advanceFuel(elapsed);
        if (!isOverStressed() && fuelTicks <= 0 && consumeFuel())
            fuelTicks = Math.max(1, KidayServerConfig.BIOMASS_FUEL_TICKS.get());
        if (fuelTicks > 0 && now % 20 == 0 && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            for (var player : serverLevel.getPlayers(candidate -> candidate.distanceToSqr(
                    worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 16 * 16))
                KidayAdvancements.award(player, "biomass_generator", "running");
        }
        if (elapsed > 0 || fuelTicks == 1 || fuelTicks == 0) {
            updateGeneratedRotation(); setChanged(); sendData();
        }
    }

    private void advanceFuel(long elapsed) {
        long remaining = elapsed;
        while (remaining > 0 && fuelTicks > 0) {
            int used = (int) Math.min(remaining, fuelTicks);
            fuelTicks -= used;
            remaining -= used;
        }
        while (remaining > 0 && consumeFuel()) {
            fuelTicks = Math.max(1, KidayServerConfig.BIOMASS_FUEL_TICKS.get());
            int used = (int) Math.min(remaining, fuelTicks);
            fuelTicks -= used;
            remaining -= used;
        }
    }

    private boolean consumeFuel() {
        int slot = inventory.getStackInSlot(0).is(ModTags.BIOMASS_FUELS) ? 0
                : inventory.getStackInSlot(1).is(ModTags.BIOMASS_FUELS) ? 1 : -1;
        if (slot < 0) return false;
        inventory.extractItem(slot, 1, false);
        return true;
    }

    @Override public float getGeneratedSpeed() { return fuelTicks > 0 ? selectedRpm() : 0; }

    @Override public float calculateAddedStressCapacity() {
        int safeRpm = selectedRpm();
        lastCapacityProvided = 4096f / safeRpm;
        return lastCapacityProvided;
    }

    @Override public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, sneaking);
        tooltip.add(Component.translatable("tooltip.create_kiday.biomass_fuel", fuelTicks / 20)
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.create_kiday.engine_output", selectedRpm(), 4096 / selectedRpm())
                .withStyle(ChatFormatting.GRAY));
        return added || true;
    }

    @Override protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("Rpm", rpm);
        tag.putInt("FuelTicks", fuelTicks);
        tag.putLong("LastWorldTick", lastWorldTick);
    }

    @Override protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.contains("Inventory")) inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        rpm = tag.contains("Rpm") ? Mth.clamp(tag.getInt("Rpm"), MIN_RPM, MAX_RPM) : 32;
        fuelTicks = Mth.clamp(tag.getInt("FuelTicks"), 0, Integer.MAX_VALUE);
        lastWorldTick = tag.contains("LastWorldTick") ? tag.getLong("LastWorldTick") : -1;
        if (targetSpeed != null) targetSpeed.setValue(rpm);
    }

    public int selectedRpm() { return Mth.clamp(rpm, MIN_RPM, MAX_RPM); }
    public int fuelTicks() { return fuelTicks; }
    public IItemHandler itemHandler() { return inputOnly; }

    public void clearContents() {
        fuelTicks = 0;
        lastWorldTick = -1;
        inventory.setStackInSlot(0, ItemStack.EMPTY);
        inventory.setStackInSlot(1, ItemStack.EMPTY);
        updateGeneratedRotation(); setChanged();
    }
}
