package com.farukylmz.createkiday.block.entity;

import com.farukylmz.createkiday.KidayBalance;
import com.farukylmz.createkiday.advancement.KidayAdvancements;
import com.farukylmz.createkiday.item.KidayMoldItem;
import com.farukylmz.createkiday.registry.ModBlockEntities;
import com.farukylmz.createkiday.registry.ModFluids;
import com.farukylmz.createkiday.registry.ModItems;
import com.farukylmz.createkiday.registry.ModRecipes;
import com.farukylmz.createkiday.recipe.CastingRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class CastingTableBlockEntity extends BlockEntity {
    private static final int MOLD_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == MOLD_SLOT && stack.getItem() instanceof KidayMoldItem;
        }
    };
    private final IItemHandler outputOnly = new IItemHandler() {
        @Override public int getSlots() { return 1; }
        @Override public ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(OUTPUT_SLOT); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return stack; }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return inventory.extractItem(OUTPUT_SLOT, amount, simulate); }
        @Override public int getSlotLimit(int slot) { return inventory.getSlotLimit(OUTPUT_SLOT); }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return false; }
    };
    private final FluidTank tank = new FluidTank(0, stack -> stack.is(ModFluids.MOLTEN_KIDAY.get())) {
        @Override protected void onContentsChanged() { syncToClient(); }
    };
    private final IFluidHandler patternLimitedFluidInput = new IFluidHandler() {
        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int tankIndex) { return tank.getFluid(); }
        @Override public int getTankCapacity(int tankIndex) { return canReceiveFluid() ? requiredFluidForPattern() : 0; }
        @Override public boolean isFluidValid(int tankIndex, FluidStack stack) { return stack.is(ModFluids.MOLTEN_KIDAY.get()); }
        @Override public int fill(FluidStack resource, FluidAction action) {
            if (!canReceiveFluid() || !resource.is(ModFluids.MOLTEN_KIDAY.get())) return 0;
            updateTankCapacity();
            return tank.fill(resource, action);
        }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) { return FluidStack.EMPTY; }
        @Override public FluidStack drain(int maxDrain, FluidAction action) { return FluidStack.EMPTY; }
    };

    private int progress;
    private int totalTicks;
    private String outputMoldName = "";

    public CastingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CASTING_TABLE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CastingTableBlockEntity table) {
        table.tick();
    }

    private void tick() {
        updateTankCapacity();
        if (progress == 0) tryStart();
        if (progress <= 0) return;
        progress++;
        if (progress >= totalTicks) finishCasting();
        setChanged();
    }

    private void tryStart() {
        ItemStack mold = inventory.getStackInSlot(MOLD_SLOT);
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        if (!(mold.getItem() instanceof KidayMoldItem moldItem) || !output.isEmpty()) return;
        CastingRecipe recipe = recipeFor(mold);
        if (recipe == null) return;
        int required = recipe.amount();
        if (tank.getFluidAmount() < required) return;
        tank.drain(required, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        totalTicks = recipe.processingTime();
        progress = 1;
    }

    private void finishCasting() {
        ItemStack mold = inventory.getStackInSlot(MOLD_SLOT);
        CastingRecipe recipe = recipeFor(mold);
        Item result = recipe == null ? null : recipe.result();
        if (result != null && inventory.getStackInSlot(OUTPUT_SLOT).isEmpty()) {
            outputMoldName = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(mold.getItem()).getPath();
            inventory.setStackInSlot(OUTPUT_SLOT, new ItemStack(result));
            awardCastingAdvancementsNearby();
            int damage = mold.getDamageValue() + 1;
            if (damage >= mold.getMaxDamage()) inventory.setStackInSlot(MOLD_SLOT, ItemStack.EMPTY);
            else mold.setDamageValue(damage);
        }
        progress = 0;
        totalTicks = 0;
    }

    private CastingRecipe recipeFor(ItemStack mold) {
        if (level == null) return null;
        return level.getRecipeManager().getRecipeFor(ModRecipes.CASTING_TYPE.get(), new SingleRecipeInput(mold), level).map(holder -> holder.value()).orElse(null);
    }

    public boolean insertMold(ItemStack held) {
        if (!inventory.getStackInSlot(MOLD_SLOT).isEmpty()) return false;
        inventory.setStackInSlot(MOLD_SLOT, held.copyWithCount(1));
        updateTankCapacity();
        syncToClient();
        return true;
    }

    public boolean takeOutput(Player player) {
        ItemStack mold = inventory.getStackInSlot(MOLD_SLOT);
        ItemStack result = inventory.extractItem(OUTPUT_SLOT, 64, false);
        if (result.isEmpty()) return false;
        if (!player.getInventory().add(result)) player.drop(result, false);
        if (player instanceof ServerPlayer serverPlayer) {
            String moldName = outputMoldName;
            if (moldName.isEmpty() && mold.getItem() instanceof KidayMoldItem)
                moldName = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(mold.getItem()).getPath();
            KidayAdvancements.award(serverPlayer, "first_cast", "cast");
            if (moldName.endsWith("_mold"))
                KidayAdvancements.award(serverPlayer, "all_casts", "cast_" + moldName.substring(0, moldName.length() - 5));
        }
        outputMoldName = "";
        return true;
    }

    private void awardCastingAdvancementsNearby() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        double centerX = worldPosition.getX() + 0.5;
        double centerY = worldPosition.getY() + 0.5;
        double centerZ = worldPosition.getZ() + 0.5;
        serverLevel.getPlayers(player -> player.distanceToSqr(centerX, centerY, centerZ) <= 16 * 16).stream()
                .min(java.util.Comparator.comparingDouble(player -> player.distanceToSqr(centerX, centerY, centerZ)))
                .ifPresent(player -> {
                    KidayAdvancements.award(player, "first_cast", "cast");
                    if (outputMoldName.endsWith("_mold"))
                        KidayAdvancements.award(player, "all_casts", "cast_"
                                + outputMoldName.substring(0, outputMoldName.length() - 5));
                });
    }

    public void dropRetainedItems(Level level, BlockPos pos) {
        for (int slot = 0; slot < 2; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack.copy());
        }
    }

    private boolean canReceiveFluid() {
        return progress == 0 && inventory.getStackInSlot(OUTPUT_SLOT).isEmpty() && requiredFluidForPattern() > 0;
    }

    private void updateTankCapacity() {
        tank.setCapacity(Math.max(0, requiredFluidForPattern()));
    }

    public int requiredFluidForPattern() {
        CastingRecipe recipe = recipeFor(inventory.getStackInSlot(MOLD_SLOT));
        return recipe == null ? 0 : recipe.amount();
    }

    private void syncToClient() {
        setChanged();
        if (level != null && !level.isClientSide()) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public IItemHandler outputHandler() { return outputOnly; }
    public IFluidHandler fluidHandler() { return patternLimitedFluidInput; }
    public FluidTank tank() { return tank; }
    public ItemStack patternStack() { return inventory.getStackInSlot(MOLD_SLOT); }
    public int progress() { return progress; }
    public int totalTicks() { return totalTicks; }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("Progress", progress);
        tag.putInt("TotalTicks", totalTicks);
        tag.putString("OutputMoldName", outputMoldName);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        tank.readFromNBT(registries, tag.getCompound("Tank"));
        progress = tag.getInt("Progress");
        totalTicks = tag.getInt("TotalTicks");
        outputMoldName = tag.getString("OutputMoldName");
        updateTankCapacity();
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
