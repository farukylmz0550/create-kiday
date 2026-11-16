package com.farukylmz.createkiday.gameplay;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.KidayBalance;
import com.farukylmz.createkiday.registry.ModItems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.common.Tags;

@EventBusSubscriber(modid = CreateKidayMod.MOD_ID)
public final class EquipmentAbilityEvents {
    private static final Map<UUID, MiningChain> MINING = new HashMap<>();
    private static final Map<UUID, DamageChain> DAMAGE = new HashMap<>();
    private static final Map<UUID, VeinMark> VEIN_MARKS = new HashMap<>();
    private static final List<PendingEcho> ECHOES = new ArrayList<>();
    private static final DustParticleOptions VEIN_PARTICLE = new DustParticleOptions(new Vector3f(234 / 255.0F, 179 / 255.0F, 176 / 255.0F), 0.75F);
    private static boolean applyingEcho;

    private EquipmentAbilityEvents() {
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        ItemStack tool = event.getEntity().getMainHandItem();
        boolean kidayPickaxe = tool.is(ModItems.KIDAY_PICKAXE.get());
        boolean kidayAxe = tool.is(ModItems.KIDAY_AXE.get());
        MiningChain chain = MINING.get(event.getEntity().getUUID());
        if (chain == null || event.getEntity().level().getGameTime() - chain.tick > 60) return;
        if (kidayPickaxe && chain.state.is(event.getState().getBlock())) {
            double boost = KidayBalance.accelerationMultiplier(chain.sameBlockBreaks);
            event.setNewSpeed((float) (event.getNewSpeed() * boost));
        }
        if (kidayAxe && chain.state.is(BlockTags.LOGS)
                && event.getState().is(chain.state.getBlock()) && event.getPosition().filter(pos -> pos.equals(chain.pos.above())).isPresent()) {
            event.setNewSpeed(event.getNewSpeed() * 2.0F);
        }
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof ServerPlayer player)) return;
        long now = event.getLevel().getGameTime();
        MiningChain old = MINING.get(player.getUUID());
        int same = old != null && now - old.tick <= 60 && old.state.is(event.getState().getBlock()) ? Math.min(5, old.sameBlockBreaks + 1) : 1;
        MINING.put(player.getUUID(), new MiningChain(event.getState(), event.getPos(), now, same));

        boolean kidayPickaxe = event.getTool().is(ModItems.KIDAY_PICKAXE.get());
        if (event.getState().is(Tags.Blocks.ORES) && kidayPickaxe && event.getLevel() instanceof ServerLevel level) {
            int radius = 8;
            int limit = 32;
            long duration = 100;
            List<BlockPos> matching = new ArrayList<>(limit);
            BlockPos min = event.getPos().offset(-radius, -radius, -radius);
            BlockPos max = event.getPos().offset(radius, radius, radius);
            for (BlockPos candidate : BlockPos.betweenClosed(min, max)) {
                if (level.getBlockState(candidate).is(event.getState().getBlock())) matching.add(candidate.immutable());
                if (matching.size() >= limit) break;
            }
            if (!matching.isEmpty()) VEIN_MARKS.put(player.getUUID(), new VeinMark(level, matching, now + duration));
        }

        ItemStack tool = event.getTool();
        boolean kidayHoe = tool.is(ModItems.KIDAY_HOE.get());
        if (!(event.getState().getBlock() instanceof CropBlock crop) || !crop.isMaxAge(event.getState()) || !kidayHoe) return;
        float chance = 0.25F;
        if (event.getLevel().random.nextFloat() >= chance || event.getDrops().isEmpty()) return;
        ItemEntity primary = event.getDrops().stream().max((a, b) -> Integer.compare(a.getItem().getCount(), b.getItem().getCount())).orElse(event.getDrops().getFirst());
        ItemStack bonus = primary.getItem().copyWithCount(1);
        event.getDrops().add(new ItemEntity(event.getLevel(), event.getPos().getX() + 0.5, event.getPos().getY() + 0.5, event.getPos().getZ() + 0.5, bonus));
    }

    @SubscribeEvent
    public static void onDamagePre(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.getSource().is(DamageTypes.FALL) && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.KIDAY_LEGGINGS.get())) {
            event.setNewDamage(event.getNewDamage() * 0.75F);
        }
        if (!player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.KIDAY_CHESTPLATE.get())) return;
        long now = player.level().getGameTime();
        ResourceKey<DamageType> type = event.getSource().typeHolder().unwrapKey().orElse(null);
        DamageChain old = DAMAGE.get(player.getUUID());
        int repeats = old != null && old.type == type && now - old.tick <= 100 ? Math.min(3, old.repeats + 1) : 0;
        event.setNewDamage((float) (event.getNewDamage() * (1.0 - KidayBalance.adaptiveChestplateReduction(repeats))));
        DAMAGE.put(player.getUUID(), new DamageChain(type, now, repeats));
    }

    @SubscribeEvent
    public static void onDamagePost(LivingDamageEvent.Post event) {
        if (applyingEcho || event.getNewDamage() <= 0 || !(event.getSource().getEntity() instanceof ServerPlayer attacker)) return;
        if (!attacker.getMainHandItem().is(ModItems.KIDAY_SWORD.get()) || attacker.getAttackStrengthScale(0.0F) < 0.95F) return;
        ECHOES.add(new PendingEcho(event.getEntity().getUUID(), attacker.getUUID(), event.getNewDamage(), attacker.level().getGameTime() + 3, attacker.serverLevel()));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        Iterator<Map.Entry<UUID, VeinMark>> marks = VEIN_MARKS.entrySet().iterator();
        while (marks.hasNext()) {
            Map.Entry<UUID, VeinMark> entry = marks.next();
            VeinMark mark = entry.getValue();
            long now = mark.level.getGameTime();
            ServerPlayer player = mark.level.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null || player.level() != mark.level || now >= mark.expiresAt) {
                marks.remove();
                continue;
            }
            if (now % 10 != 0) continue;
            for (BlockPos pos : mark.positions) {
                if (!mark.level.getBlockState(pos).isAir()) {
                    mark.level.sendParticles(player, VEIN_PARTICLE, true, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0.18, 0.18, 0.18, 0.0);
                }
            }
        }

        Iterator<PendingEcho> iterator = ECHOES.iterator();
        while (iterator.hasNext()) {
            PendingEcho echo = iterator.next();
            if (echo.level.getGameTime() < echo.triggerTick) continue;
            LivingEntity target = echo.level.getEntity(echo.target) instanceof LivingEntity living ? living : null;
            ServerPlayer attacker = echo.level.getServer().getPlayerList().getPlayer(echo.attacker);
            if (target != null && target.isAlive() && attacker != null) {
                applyingEcho = true;
                target.invulnerableTime = 0;
                target.hurt(echo.level.damageSources().playerAttack(attacker), echo.damage);
                applyingEcho = false;
            }
            iterator.remove();
        }
    }

    private record MiningChain(BlockState state, BlockPos pos, long tick, int sameBlockBreaks) {}
    private record DamageChain(ResourceKey<DamageType> type, long tick, int repeats) {}
    private record VeinMark(ServerLevel level, List<BlockPos> positions, long expiresAt) {}
    private record PendingEcho(UUID target, UUID attacker, float damage, long triggerTick, ServerLevel level) {}
}
