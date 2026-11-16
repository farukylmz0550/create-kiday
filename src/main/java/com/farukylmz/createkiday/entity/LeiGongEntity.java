package com.farukylmz.createkiday.entity;

import com.farukylmz.createkiday.registry.ModDamageTypes;
import com.farukylmz.createkiday.registry.ModItems;
import com.farukylmz.createkiday.registry.ModMobEffects;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class LeiGongEntity extends Monster {
    public static final int ACTIVE_ATTACK_TICKS = 40;
    public static final int ATTACK_CYCLE_TICKS = 80;
    public static final int SPECIAL_COOLDOWN_TICKS = 200;
    public static final double ATTACK_RANGE = 12.0;
    public static final double CHAIN_RANGE = 4.0;
    public static final int MAX_CHAIN_TARGETS = 3;
    public static final float MELEE_DAMAGE = 2.0F;
    public static final float PRIMARY_DAMAGE = 6.0F;
    public static final float SECONDARY_DAMAGE = 1.0F;
    public static final int LIGHTNING_STRIKES = 2;
    public static final int LIGHTNING_GAP_TICKS = 20;
    public static final int LIGHTNING_WARNING_TICKS = 5;
    public static final float STUN_CHANCE = 0.05F;
    public static final int STUN_TICKS = 10;

    private static final EntityDataAccessor<Boolean> ELECTRIFYING = SynchedEntityData.defineId(
            LeiGongEntity.class, EntityDataSerializers.BOOLEAN);

    private int attackCycleTick;
    private int specialCooldown;
    private Vec3 aimedStrikePosition;

    public LeiGongEntity(EntityType<? extends LeiGongEntity> type, Level level) {
        super(type, level);
        xpReward = 12;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 120.0)
                .add(Attributes.MOVEMENT_SPEED, 0.184)
                .add(Attributes.FOLLOW_RANGE, 20.0)
                .add(Attributes.ATTACK_DAMAGE, MELEE_DAMAGE);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 20.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ELECTRIFYING, false);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (specialCooldown > 0) specialCooldown--;

        LivingEntity target = getTarget();
        if (target == null || !target.isAlive() || target.distanceToSqr(this) > 20.0 * 20.0) {
            setElectrifying(false);
            attackCycleTick = 0;
            aimedStrikePosition = null;
            return;
        }

        getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (target.distanceToSqr(this) > ATTACK_RANGE * ATTACK_RANGE) {
            getNavigation().moveTo(target, 1.0);
            setElectrifying(false);
            attackCycleTick = 0;
            aimedStrikePosition = null;
            return;
        }

        getNavigation().stop();
        boolean active = attackCycleTick < ACTIVE_ATTACK_TICKS;
        setElectrifying(active);
        if (active && level() instanceof ServerLevel serverLevel) {
            if (attackCycleTick == 0 || attackCycleTick == LIGHTNING_GAP_TICKS) {
                aimedStrikePosition = target.position();
                sendArc(serverLevel, getEyePosition(), aimedStrikePosition.add(0, 0.1, 0));
            }
            if (attackCycleTick == LIGHTNING_WARNING_TICKS
                    || attackCycleTick == LIGHTNING_GAP_TICKS + LIGHTNING_WARNING_TICKS) {
                if (aimedStrikePosition != null) strike(serverLevel, aimedStrikePosition);
            }
        }
        attackCycleTick = (attackCycleTick + 1) % ATTACK_CYCLE_TICKS;
    }

    private void strike(ServerLevel level, Vec3 targetPoint) {
        DamageSource electricity = level.damageSources().source(ModDamageTypes.LEI_GONG_ELECTRICITY, this);
        AABB strikeArea = new AABB(targetPoint.x - 0.8, targetPoint.y, targetPoint.z - 0.8,
                targetPoint.x + 0.8, targetPoint.y + 2.0, targetPoint.z + 0.8);
        List<Player> struckPlayers = level.getEntitiesOfClass(Player.class, strikeArea,
                player -> player.isAlive() && player.distanceToSqr(targetPoint) <= 2.25);
        if (struckPlayers.isEmpty()) {
            sendArc(level, getEyePosition(), targetPoint.add(0, 1.0, 0));
            return;
        }
        for (Player player : struckPlayers) {
            player.invulnerableTime = 0;
            boolean hit = player.hurt(electricity, PRIMARY_DAMAGE);
            sendArc(level, getEyePosition(), player.getBoundingBox().getCenter());
            if (hit && player.isAlive() && random.nextFloat() < STUN_CHANCE) {
                player.addEffect(new MobEffectInstance(ModMobEffects.STUN, STUN_TICKS, 0, false, true, true), this);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0,
                        player.getZ(), 18, 0.35, 0.55, 0.35, 0.08);
            }
        }

        if (specialCooldown > 0) return;
        specialCooldown = SPECIAL_COOLDOWN_TICKS;
        Set<UUID> struck = new HashSet<>();
        struck.add(struckPlayers.getFirst().getUUID());
        LivingEntity previous = struckPlayers.getFirst();
        for (int index = 1; index < MAX_CHAIN_TARGETS; index++) {
            LivingEntity next = nextChainTarget(level, previous, struck);
            if (next == null) break;
            struck.add(next.getUUID());
            next.invulnerableTime = 0;
            next.hurt(electricity, SECONDARY_DAMAGE);
            sendArc(level, previous.getBoundingBox().getCenter(), next.getBoundingBox().getCenter());
            previous = next;
        }
    }

    private LivingEntity nextChainTarget(ServerLevel level, LivingEntity from, Set<UUID> struck) {
        AABB search = from.getBoundingBox().inflate(CHAIN_RANGE);
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, search, living ->
                living.isAlive() && living != this && !(living instanceof LeiGongEntity)
                        && !struck.contains(living.getUUID())
                        && living.distanceToSqr(from) <= CHAIN_RANGE * CHAIN_RANGE);
        return candidates.stream()
                .min(Comparator.comparingDouble(living -> living.distanceToSqr(from)))
                .orElse(null);
    }

    private static void sendArc(ServerLevel level, Vec3 from, Vec3 to) {
        for (int step = 1; step <= 5; step++) {
            Vec3 point = from.lerp(to, step / 6.0);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z,
                    1, 0.03, 0.03, 0.03, 0.0);
        }
    }

    private void setElectrifying(boolean value) {
        if (entityData.get(ELECTRIFYING) != value) entityData.set(ELECTRIFYING, value);
    }

    public boolean isElectrifying() {
        return entityData.get(ELECTRIFYING);
    }

    public int attackCycleTick() {
        return attackCycleTick;
    }

    public int specialCooldown() {
        return specialCooldown;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        if (random.nextFloat() < 0.05F) spawnAtLocation(ModItems.STORM_AMULET.get());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AttackCycle", attackCycleTick);
        tag.putInt("SpecialCooldown", specialCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        attackCycleTick = Math.floorMod(tag.getInt("AttackCycle"), ATTACK_CYCLE_TICKS);
        specialCooldown = Math.max(0, tag.getInt("SpecialCooldown"));
    }
}
